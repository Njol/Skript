/*
 *   This file is part of Skript.
 *
 *  Skript is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  Skript is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with Skript.  If not, see <http://www.gnu.org/licenses/>.
 * 
 * 
 * Copyright 2011, 2012 Peter Güttinger
 * 
 */

package ch.njol.skript.lang;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.regex.MatchResult;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import ch.njol.skript.Skript;
import ch.njol.skript.api.SkriptEvent;
import ch.njol.skript.api.SkriptEvent.SkriptEventInfo;
import ch.njol.skript.api.exception.InitException;
import ch.njol.skript.api.exception.ParseException;
import ch.njol.skript.api.intern.SkriptAPIException;
import ch.njol.skript.util.Utils;
import ch.njol.util.Pair;
import ch.njol.util.StringUtils;

/**
 * @author Peter Güttinger
 * 
 */
public class ExprParser {
	
	private final String expr;
	
	final boolean parseStatic;
	
	private String bestError = null;
	private int bestErrorQuality = 0;
	
	private ExprParser(final String expr) {
		this.expr = expr;
		parseStatic = false;
	}
	
	private ExprParser(final String expr, final boolean parseStatic) {
		this.expr = expr;
		this.parseStatic = parseStatic;
	}
	
	public final static String wildcard = "[^\"]*?(?:\"[^\"]*?\"[^\"]*?)*?";
	public final static String stringMatcher = "\"[^\"]*?(?:\"\"[^\"]*)*?\"";
	
	public final static class ParseResult {
		public final Variable<?>[] vars;
		public final List<MatchResult> regexes = new ArrayList<MatchResult>();
		public final String expr;
		
		public ParseResult(final String expr, final String pattern) {
			this.expr = expr;
			vars = new Variable<?>[StringUtils.count(pattern, '%') / 2];
		}
	}
	
	private final static class MalformedPatternException extends RuntimeException {
		
		private static final long serialVersionUID = -2479399963189481643L;
		
		public MalformedPatternException(final String pattern, final String message) {
			super("\"" + pattern + "\": " + message);
		}
		
	}
	
	public static final Expression parse(final String expr, final Iterator<? extends ExpressionInfo<?>> source, final boolean parseLiteral) {
		final Expression e = parse(expr, source);
		if (e != null)
			return e;
		if (parseLiteral) {
			return parseUnparsedLiteral(expr);
		}
		//error already set by parse()
		return null;
	}
	
	public static final Expression parse(final String expr, final Iterator<? extends ExpressionInfo<?>> source) {
		return new ExprParser(expr).parse(source);
	}
	
	private final Expression parse(final Iterator<? extends ExpressionInfo<?>> source) {
		if (Skript.getCurrentErrorSession() == null)
			throw new SkriptAPIException("must start an ErrorSession before trying to parse anything!");
		while (source.hasNext()) {
			final ExpressionInfo<?> info = source.next();
			for (int i = 0; i < info.patterns.length; i++) {
				try {
					final ParseResult res = parse_i(info.patterns[i], 0, 0);
					if (res != null) {
						int x = -1;
						for (int j = 0; (x = next(info.patterns[i], '%', x + 1)) != -1; j++) {
							final int x2 = next(info.patterns[i], '%', x + 1);
							if (res.vars[j] == null) {
								String name = info.patterns[i].substring(x + 1, x2);
								if (!name.startsWith("-")) {
									final Pair<String, Boolean> p = Utils.getPlural(name);
									name = p.first;
									final Class<? extends SimpleVariable<?>> c = Skript.getDefaultVariable(name);
									if (c == null)
										throw new SkriptAPIException("The class '" + name + "' does not provide a default variable. Either allow null (%-" + name + "%) or make it mandatory");
									final SimpleVariable<?> var = c.newInstance();
									if (!p.second && !var.isSingle()) {
										throw new SkriptAPIException("The default variable of '" + name + "' is not a single-element variable. Change your pattern to allow multiple elements or make the variable mandatory");
									}
									res.vars[j] = var;
								}
							}
							x = x2;
						}
						Skript.getCurrentErrorSession().clearErrors();
						final Expression e = info.c.newInstance();
						e.init(res.vars, i, res);
						return e;
					}
				} catch (final ParseException e) {
					if (e.getError() != null)
						Skript.error(e.getError());
					if (bestErrorQuality < 100) {
						bestError = e.getError();
						bestErrorQuality = 100;
					}
					return null;
				} catch (final InstantiationException e) {
					SkriptAPIException.instantiationException("the " + Skript.getExpressionName(info.c), info.c, e);
				} catch (final IllegalAccessException e) {
					SkriptAPIException.inaccessibleConstructor(info.c, e);
				} catch (final InitException e) {
					Skript.getCurrentErrorSession().clearErrors();
					continue;
				}
			}
		}
		if (bestError != null) {
			Skript.error(bestError);
		}
		return null;
	}
	
	private final Variable<?> parseVar(final Class<?> returnType, final String expr, final boolean literalOnly) {
		if (!literalOnly) {
			final ExprParser parser = new ExprParser(expr);
			final Variable<?> v = (Variable<?>) parser.parse(Skript.getVariables().iterator());
			if (v != null) {
				final Variable<?> w = v.getConvertedVariable(returnType);
				if (w == null && bestErrorQuality < 90) {
					bestError = v.toString() + " " + (v.isSingle() ? "is" : "are") + " not " + Utils.a(Skript.getExactClassName(returnType));
					bestErrorQuality = 90;
				}
				return w;
			} else {
				if (parser.bestErrorQuality > bestErrorQuality) {
					bestError = parser.bestError;
					bestErrorQuality = parser.bestErrorQuality;
				}
			}
		}
		final UnparsedLiteral l = parseUnparsedLiteral(expr);
		if (l == null || returnType == Object.class)
			return l;
		Skript.getCurrentErrorSession().clearErrors();
		final Literal<?> p = l.getConvertedVar(returnType);
		if (p == null && bestErrorQuality < 90) {
			bestError = Skript.getCurrentErrorSession().getLastError();
			bestErrorQuality = 90;
		}
		return p;
	}
	
	public static Pair<SkriptEventInfo<?>, SkriptEvent> parseEvent(final String event) {
		return new ExprParser(event, true).parseEvent();
	}
	
	private Pair<SkriptEventInfo<?>, SkriptEvent> parseEvent() {
		if (Skript.getCurrentErrorSession() == null)
			throw new SkriptAPIException("must start an ErrorSession before trying to parse anything!");
		for (final SkriptEventInfo<?> info : Skript.getEvents()) {
			for (int i = 0; i < info.patterns.length; i++) {
				try {
					final ParseResult res = parse_i(info.patterns[i], 0, 0);
					if (res != null) {
						Skript.getCurrentErrorSession().clearErrors();
						final SkriptEvent e = info.c.newInstance();
						e.init(Arrays.copyOf(res.vars, res.vars.length, Literal[].class), i, res);
						return new Pair<SkriptEventInfo<?>, SkriptEvent>(info, e);
					}
				} catch (final ParseException e) {
					if (e.getError() != null)
						Skript.error(e.getError());
					return null;
				} catch (final InstantiationException e) {
					SkriptAPIException.instantiationException("the event", info.c, e);
				} catch (final IllegalAccessException e) {
					SkriptAPIException.inaccessibleConstructor(info.c, e);
				}
			}
		}
		if (bestError != null) {
			Skript.error(bestError);
		}
		return null;
	}
	
	private static int next(final String s, final char c, final char x, final int start) {
		int n = 0;
		for (int i = start; i < s.length(); i++) {
			if (s.charAt(i) == '\\') {
				i++;
				continue;
			} else if (s.charAt(i) == c) {
				if (n == 0)
					return i;
				n--;
			} else if (s.charAt(i) == x) {
				n++;
			}
		}
		throw new MalformedPatternException(s, "missing closing bracket '" + x + "'");
	}
	
	private static int next(final String s, final char c, final int from) {
		for (int i = from; i < s.length(); i++) {
			if (s.charAt(i) == '\\') {
				i++;
			} else if (s.charAt(i) == c) {
				return i;
			}
		}
		return -1;
	}
	
	private static int nextQuote(final String s, final int from) {
		for (int i = from; i < s.length(); i++) {
			if (s.charAt(i) == '"') {
				if (i == s.length() - 1 || s.charAt(i + 1) != '"')
					return i;
				i++;
			}
		}
		return -1;
	}
	
	private static boolean hasOnly(final String s, final String what, final int start, final int end) {
		for (int i = start; i < end; i++) {
			if (what.indexOf(s.charAt(i)) == -1)
				return false;
		}
		return true;
	}
	
	private final ParseResult parse_i(final String pattern, int i, int j) throws ParseException {
		if (expr.isEmpty())
			throw new RuntimeException(pattern);
		ParseResult res;
		int end, i2;
		while (j < pattern.length()) {
			switch (pattern.charAt(j)) {
				case '[':
					res = parse_i(pattern, i, j + 1);
					if (res != null) {
						return res;
					}
					end = next(pattern, ']', '[', j + 1);
					if ((hasOnly(pattern, "[(", 0, j) || pattern.charAt(j - 1) == ' ')
							&& end < pattern.length() - 1 && pattern.charAt(end + 1) == ' ') {
						end++;
					}
					j = end + 1;
				break;
				case '(':
					end = next(pattern, ')', '(', j + 1);
					final String[] gs = pattern.substring(j + 1, end).split("\\|");
					int j2 = j + 1;
					for (int k = 0; k < gs.length; k++) {
						res = parse_i(pattern, i, j2);
						if (res != null) {
							return res;
						}
						j2 += gs[k].length() + 1;
					}
					return null;
				case '%':
					if (i == expr.length())
						return null;
					end = next(pattern, '%', j + 1);
					if (end == -1)
						throw new MalformedPatternException(pattern, "odd number of '%'");
					String name = pattern.substring(j + 1, end);
					if (name.startsWith("-"))
						name = name.substring(1);
					final Pair<String, Boolean> p = Utils.getPlural(name);
					name = p.first;
					final Class<?> returnType = Skript.getClass(name);
					if (end == pattern.length() - 1) {
						i2 = expr.length();
					} else if (expr.charAt(i) == '"') {
						i2 = nextQuote(expr, i + 1) + 1;
						if (i2 == 0)
							return null;
					} else {
						i2 = i + 1;
					}
					for (; i2 <= expr.length(); i2++) {
						if (i2 < expr.length() && expr.charAt(i2) == '"') {
							i2 = nextQuote(expr, i2 + 1) + 1;
							if (i2 == 0)
								return null;
						}
						res = parse_i(pattern, i2, end + 1);
						if (res != null) {
							final Variable<?> var = parseVar(returnType, expr.substring(i, i2), parseStatic);
							if (var != null) {
								if (!p.second && !var.isSingle()) {
									throw new ParseException("this expression can only accept a single " + Skript.getExactClassName(returnType) + ", but multiple are given.");
								}
								res.vars[StringUtils.count(pattern, '%', 0, j - 1) / 2] = var;
								return res;
							} else if (bestErrorQuality < 80) {
								bestError = "'" + expr.substring(i, i2) + "' is not " + Utils.a(Skript.getExactClassName(returnType));
								bestErrorQuality = 80;
							}
						}
					}
					return null;
				case '<':
					end = pattern.indexOf('>', j + 1);// not next()
					if (end == -1)
						throw new MalformedPatternException(pattern, "missing closing regex bracket '>'");
					for (i2 = i + 1; i2 <= expr.length(); i2++) {
						res = parse_i(pattern, i2, end + 1);
						if (res != null) {
							final Matcher m = Pattern.compile(pattern.substring(j + 1, end)).matcher(expr.substring(i, i2));
							if (m.matches()) {
								res.regexes.add(0, m.toMatchResult());
								return res;
							} else {
								// TODO maybe set error here?
							}
						}
					}
					return null;
				case ')':
				case ']':
					j++;
				break;
				case '|':
					j = next(pattern, ')', '(', j + 1) + 1;
				break;
				case ' ':
					if (i == expr.length()) {
						j++;
						break;
					} else if (expr.charAt(i) != ' ') {
						return null;
					}
					i++;
					j++;
				break;
				case '\\':
					j++;
					if (j == pattern.length())
						throw new MalformedPatternException(pattern, "must not end with a backslash");
					//$FALL-THROUGH$
				default:
					if (i == expr.length() || Character.toLowerCase(pattern.charAt(j)) != Character.toLowerCase(expr.charAt(i)))
						return null;
					i++;
					j++;
			}
		}
		if (i == expr.length() && j == pattern.length())
			return new ParseResult(expr, pattern);
		return null;
	}
	
	private final static UnparsedLiteral parseUnparsedLiteral(final String s) {
		final ArrayList<String> parts = new ArrayList<String>();
		final Pattern p = Pattern.compile("^(" + wildcard + ")(,\\s*|,?\\s+and\\s+|,?\\s+or\\s+)");
		final Matcher m = p.matcher(s);
		int prevEnd = 0;
		boolean and = true;
		boolean isAndSet = false;
		while (m.find()) {
			if (!m.group(2).matches(",\\s*")) {
				if (isAndSet) {
					Skript.warning("list has multiple 'and' or 'or', will default to 'and': " + s);
					and = true;
				} else {
					and = m.group(2).contains("and");
					isAndSet = true;
				}
			}
			parts.add(m.group(1).trim());
			prevEnd = m.end();
			m.region(m.end(), s.length());
		}
		if (!isAndSet && !parts.isEmpty()) {
			Skript.warning("list is missing 'and' or 'or', will default to 'and': " + s);
		}
		parts.add(s.substring(prevEnd).trim());
		return new UnparsedLiteral(parts.toArray(new String[0]), and);
	}
	
}
