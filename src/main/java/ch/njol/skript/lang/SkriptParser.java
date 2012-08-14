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
import java.util.logging.Level;
import java.util.regex.MatchResult;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import ch.njol.skript.ScriptLoader;
import ch.njol.skript.Skript;
import ch.njol.skript.SkriptAPIException;
import ch.njol.skript.SkriptLogger;
import ch.njol.skript.SkriptLogger.SubLog;
import ch.njol.skript.classes.ClassInfo;
import ch.njol.skript.command.Argument;
import ch.njol.skript.command.SkriptCommand;
import ch.njol.skript.command.SkriptCommandEvent;
import ch.njol.skript.lang.SkriptEvent.SkriptEventInfo;
import ch.njol.skript.util.StringMode;
import ch.njol.skript.util.Utils;
import ch.njol.skript.util.VariableString;
import ch.njol.util.Pair;
import ch.njol.util.StringUtils;
import ch.njol.util.Validate;

/**
 * Used for parsing my custom patterns.<br>
 * <br>
 * Note: All parse methods print one error at most xor any amount of warnings and lower level log messages. If the given string doesn't match any pattern nothing is printed.
 * 
 * @author Peter Güttinger
 * 
 */
public class SkriptParser {
	
	private final String expr;
	
	private final boolean parseStatic;
	
	private String bestError = null;
	private int bestErrorQuality = 0;
	
	private static enum ErrorQuality {
		NONE, NOT_AN_EXPRESSION, EXPRESSION_OF_WRONG_TYPE, SEMANTIC_ERROR;
		int quality() {
			return ordinal();
		}
	}
	
	private final void setBestError(final ErrorQuality quality, final String error, final boolean appendCurrentNodeSuffix) {
		if (bestErrorQuality < quality.quality()) {
			bestError = appendCurrentNodeSuffix ? error + SkriptLogger.getCurrentNodeSuffix() : error;
			bestErrorQuality = quality.quality();
		}
	}
	
	private final void setBestError(final SkriptParser other) {
		if (bestErrorQuality < other.bestErrorQuality) {
			bestError = other.bestError;
			bestErrorQuality = other.bestErrorQuality;
		}
	}
	
	public final ParseContext context;
	
	private SkriptParser(final String expr) {
		this(expr, false);
	}
	
	private SkriptParser(final String expr, final boolean parseStatic) {
		this(expr, parseStatic, ParseContext.DEFAULT);
	}
	
	private SkriptParser(final String expr, final boolean parseStatic, final ParseContext context) {
		Validate.notNull(expr, "expr");
		this.expr = expr;
		this.parseStatic = parseStatic;
		this.context = context;
	}
	
	public final static String wildcard = "[^\"]*?(?:\"[^\"]*?\"[^\"]*?)*?";
	public final static String stringMatcher = "\"[^\"]*?(?:\"\"[^\"]*)*?\"";
	
	public final static class ParseResult {
		public final Expression<?>[] vars;
		public final List<MatchResult> regexes = new ArrayList<MatchResult>();
		public final String expr;
		int matchedChars = 0;
		
		public ParseResult(final String expr, final String pattern, final int matchedChars) {
			this.expr = expr;
			vars = new Expression<?>[StringUtils.count(pattern, '%') / 2];
			this.matchedChars = matchedChars;
		}
	}
	
	private final static class MalformedPatternException extends RuntimeException {
		
		private static final long serialVersionUID = -2479399963189481643L;
		
		public MalformedPatternException(final String pattern, final String message) {
			super(message + " [pattern: " + pattern + "]");
		}
		
	}
	
	public static final <T> Literal<? extends T> parseLiteral(String expr, final Class<T> c, final ParseContext context) {
		expr = expr.trim();
		if (expr.isEmpty())
			return null;
		return new UnparsedLiteral(expr).getConvertedExpression(c, context);
	}
	
	/**
	 * Parses a string as one of the given expressions or as a literal
	 * 
	 * @param expr
	 * @param source
	 * @param parseLiteral
	 * @param defaultError
	 * @return
	 */
	public static final SyntaxElement parse(String expr, final Iterator<? extends SyntaxElementInfo<?>> source, final boolean parseLiteral, final boolean parseVariable, final String defaultError) {
		expr = expr.trim();
		if (expr.isEmpty()) {
			Skript.error(defaultError);
			return null;
		}
		final Variable<?> var = parseVariable(expr, Object.class);
		if (var != null) {
			if (parseVariable)
				return var;
			Skript.error(defaultError);
			return null;
		}
		final SubLog log = SkriptLogger.startSubLog();
		@SuppressWarnings({"unchecked", "rawtypes"})
		final SyntaxElement e = parse(expr, (Iterator) source, null);
		SkriptLogger.stopSubLog(log);
		if (e != null) {
			log.printLog();
			return e;
		}
		if (parseLiteral) {
			return new UnparsedLiteral(expr);
		}
		log.printErrors(defaultError);
		return null;
	}
	
	/**
	 * Parses a string as one of the given expressions
	 * 
	 * @param expr
	 * @param source
	 * @param defaultError
	 * @return
	 */
	public static final <T extends SyntaxElement> T parse(String expr, final Iterator<? extends SyntaxElementInfo<T>> source, final String defaultError) {
		expr = expr.trim();
		if (expr.isEmpty()) {
			Skript.error(defaultError);
			return null;
		}
		final SubLog log = SkriptLogger.startSubLog();
		final T e = new SkriptParser(expr).parse(source);
		SkriptLogger.stopSubLog(log);
		if (e != null) {
			log.printLog();
			return e;
		}
		log.printErrors(defaultError);
		return null;
	}
	
	public static final <T extends SyntaxElement> T parseStatic(String expr, final Iterator<? extends SyntaxElementInfo<? extends T>> source, final String defaultError) {
		expr = expr.trim();
		if (expr.isEmpty()) {
			Skript.error(defaultError);
			return null;
		}
		final SubLog log = SkriptLogger.startSubLog();
		final T e = new SkriptParser(expr, true).parse(source);
		SkriptLogger.stopSubLog(log);
		if (e != null) {
			log.printLog();
			return e;
		}
		log.printErrors(defaultError);
		return null;
	}
	
	private final <T extends SyntaxElement> T parse(final Iterator<? extends SyntaxElementInfo<? extends T>> source) {
		while (source.hasNext()) {
			final SyntaxElementInfo<? extends T> info = source.next();
			for (int i = 0; i < info.patterns.length; i++) {
				try {
					final ParseResult res = parse_i(info.patterns[i], 0, 0);
					if (res != null) {
						int x = -1;
						for (int j = 0; (x = next(info.patterns[i], '%', x + 1)) != -1; j++) {
							final int x2 = next(info.patterns[i], '%', x + 1);
							if (res.vars[j] == null) {
								final String name = info.patterns[i].substring(x + 1, x2);
								if (!name.startsWith("-")) {
									final VarInfo vi = getVarInfo(name);
									final DefaultExpression<?> var = vi.classes[0].getDefaultExpression();
									if (var == null)
										throw new SkriptAPIException("The class '" + vi.classes[0].getName() + "' does not provide a default expression. Either allow null (with %-" + vi.classes[0].getCodeName() + "%) or make it mandatory [pattern: " + info.patterns[i] + "]");
									if (!vi.isPlural[0] && !var.isSingle())
										throw new SkriptAPIException("The default expression of '" + vi.classes[0].getName() + "' is not a single-element expression. Change your pattern to allow multiple elements or make the expression mandatory [pattern: " + info.patterns[i] + "]");
									if (vi.time != 0 && !var.setTime(vi.time))
										throw new SkriptAPIException("The default expression of '" + vi.classes[0].getName() + "' does not have distinct time states. Either allow null (with %-" + vi.classes[0].getCodeName() + "%) or make it mandatory [pattern: " + info.patterns[i] + "]");
									var.init();
									res.vars[j] = var;
								}
							}
							x = x2;
						}
						final T e = info.c.newInstance();
						final SubLog log = SkriptLogger.startSubLog();
						if (!e.init(res.vars, i, ScriptLoader.hasDelayBefore, res)) {
							SkriptLogger.stopSubLog(log);
							if (!log.hasErrors())
								continue;
							setBestError(ErrorQuality.SEMANTIC_ERROR, log.getLastError(), false);
//							Skript.error(bestError);
//							return null;
							continue;
						}
						SkriptLogger.stopSubLog(log);
						log.printLog();
						return e;
					}
//					if (bestErrorQuality == ErrorQuality.SEMANTIC_ERROR.quality()) {
//						Skript.error(bestError);
//						return null;
//					}
				} catch (final InstantiationException e) {
					SkriptAPIException.instantiationException("the " + Skript.getSyntaxElementName(info.c), info.c, e);
				} catch (final IllegalAccessException e) {
					SkriptAPIException.inaccessibleConstructor(info.c, e);
				}
			}
		}
		if (bestError != null)
			SkriptLogger.logDirect(Level.SEVERE, bestError);
		return null;
	}
	
	private final static Pattern varPattern = Pattern.compile("^(?i)((the )?var(iable)? )?\\{([^{}]|%\\{|\\}%)+\\}$");
	
	private final static <T> Variable<T> parseVariable(final String expr, final Class<T> returnType) {
		if (varPattern.matcher(expr).matches()) {
			
			final VariableString vs = VariableString.newInstance(expr.substring(expr.indexOf('{') + 1, expr.lastIndexOf('}')), StringMode.VARIABLE_NAME);
			if (vs == null)
				return null;
			return new Variable<T>(vs, returnType);
		}
		return null;
	}
	
	/**
	 * Does not print errors
	 * 
	 * @param returnType
	 * @param expr
	 * @param literalOnly
	 * @return
	 */
	@SuppressWarnings("unchecked")
	private final <T> Expression<? extends T> parseExpr(final Class<T> returnType, final String expr, final boolean literalOnly) {
		if (expr.isEmpty())
			return null;
		if (!literalOnly) {
			final Variable<T> var = parseVariable(expr, returnType);
			if (var != null)
				return var;
			final SubLog log = SkriptLogger.startSubLog();
			final SkriptParser parser = new SkriptParser(expr);
			final Expression<?> v = parser.parse(Skript.getExpressions().iterator());
			SkriptLogger.stopSubLog(log);
			if (v != null) {
				final Expression<? extends T> w = v.getConvertedExpression(returnType);
				if (w == null)
					setBestError(ErrorQuality.EXPRESSION_OF_WRONG_TYPE, v.toString() + " " + (v.isSingle() ? "is" : "are") + " not " + Utils.a(Skript.getExactClassName(returnType)), true);
				return w;
			} else {
				setBestError(parser);
			}
		}
		final UnparsedLiteral l = new UnparsedLiteral(expr);
		if (returnType == Object.class)
			return (Expression<? extends T>) l;
		final SubLog log = SkriptLogger.startSubLog();
		final Literal<? extends T> p = l.getConvertedExpression(returnType, context);
		SkriptLogger.stopSubLog(log);
		if (p == null)
			setBestError(ErrorQuality.NOT_AN_EXPRESSION, log.getLastError() == null ? "'" + expr + "' is not " + Utils.a(Skript.getExactClassName(returnType)) : log.getLastError(), log.getLastError() == null);
		return p;
	}
	
	public static Pair<SkriptEventInfo<?>, SkriptEvent> parseEvent(final String event, final String defaultError) {
		final SubLog log = SkriptLogger.startSubLog();
		final Pair<SkriptEventInfo<?>, SkriptEvent> e = new SkriptParser(event, true, ParseContext.EVENT).parseEvent();
		SkriptLogger.stopSubLog(log);
		if (e != null) {
			log.printLog();
			return e;
		}
		log.printErrors(defaultError);
		return null;
	}
	
	/**
	 * Prints errors
	 * 
	 * @param args
	 * @param command
	 * @param event
	 * @return
	 */
	public static boolean parseArguments(final String args, final SkriptCommand command, final SkriptCommandEvent event) {
		
		final SkriptParser parser = new SkriptParser(args, true, ParseContext.COMMAND);
		final ParseResult res = parser.parse_i(command.getPattern(), 0, 0);
		if (res == null) {
			if (parser.bestError != null)
				SkriptLogger.logDirect(Level.SEVERE, parser.bestError);
			return false;
		}
		
		final List<Argument<?>> as = command.getArguments();
		assert as.size() == res.vars.length;
		
		for (int i = 0; i < res.vars.length; i++) {
			if (res.vars[i] == null)
				as.get(i).setToDefault(event);
			else
				as.get(i).set(res.vars[i].getArray(null));
		}
		return true;
	}
	
	private Pair<SkriptEventInfo<?>, SkriptEvent> parseEvent() {
		for (final SkriptEventInfo<?> info : Skript.getEvents()) {
			for (int i = 0; i < info.patterns.length; i++) {
				try {
					final ParseResult res = parse_i(info.patterns[i], 0, 0);
					if (res != null) {
						final SkriptEvent e = info.c.newInstance();
						e.init(Arrays.copyOf(res.vars, res.vars.length, Literal[].class), i, res);
						return new Pair<SkriptEventInfo<?>, SkriptEvent>(info, e);
					}
//					if (bestErrorQuality == ErrorQuality.SEMANTIC_ERROR.quality()) {
//						Skript.error(bestError);
//						return null;
//					}
				} catch (final InstantiationException e) {
					SkriptAPIException.instantiationException("the event", info.c, e);
				} catch (final IllegalAccessException e) {
					SkriptAPIException.inaccessibleConstructor(info.c, e);
				}
			}
		}
		if (bestError != null)
			SkriptLogger.logDirect(Level.SEVERE, bestError);
		return null;
	}
	
	private static int nextBracket(final String s, final char closingBracket, final char openingBracket, final int start) {
		int n = 0;
		for (int i = start; i < s.length(); i++) {
			if (s.charAt(i) == '\\') {
				i++;
				continue;
			} else if (s.charAt(i) == closingBracket) {
				if (n == 0)
					return i;
				n--;
			} else if (s.charAt(i) == openingBracket) {
				n++;
			}
		}
		throw new MalformedPatternException(s, "missing closing bracket '" + closingBracket + "'");
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
	
	private static boolean hasOnly(final String s, final String chars, final int start, final int end) {
		for (int i = start; i < end; i++) {
			if (chars.indexOf(s.charAt(i)) == -1)
				return false;
		}
		return true;
	}
	
	/**
	 * Does not print errors, but sets this parser's {@link #bestError}.
	 * 
	 * @param pattern
	 * @param i
	 * @param j
	 * @return
	 */
	private final ParseResult parse_i(final String pattern, int i, int j) {
		ParseResult res;
		int matchedChars = 0;
		int end, i2;
		
		while (j < pattern.length()) {
			switch (pattern.charAt(j)) {
				case '[':
					res = parse_i(pattern, i, j + 1);
					if (res != null) {
						return res;
					}
					end = nextBracket(pattern, ']', '[', j + 1);
					if ((hasOnly(pattern, "[(", 0, j) || pattern.charAt(j - 1) == ' ')
							&& end < pattern.length() - 1 && pattern.charAt(end + 1) == ' ') {
						end++;
					}
					j = end + 1;
				break;
				case '(':
					end = nextBracket(pattern, ')', '(', j + 1);
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
					final String name = pattern.substring(j + 1, end);
					final VarInfo vi = getVarInfo(name);
					if (end == pattern.length() - 1) {
						i2 = expr.length();
					} else if (expr.charAt(i) == '"') {
						i2 = nextQuote(expr, i + 1) + 1;
						if (i2 == 0)
							return null;
					} else if (expr.charAt(i) == '{') {
						i2 = expr.indexOf('}', i + 1) + 1;
						if (i2 == 0)
							return null;
					} else {
						i2 = i + 1;
					}
					for (; i2 <= expr.length(); i2++) {
						if (i2 < expr.length()) {
							if (expr.charAt(i2) == '"') {
								i2 = nextQuote(expr, i2 + 1) + 1;
								if (i2 == 0)
									return null;
							} else if (expr.charAt(i2) == '{') {
								i2 = expr.indexOf('}', i2 + 1) + 1;
								if (i2 == 0)
									return null;
							}
						}
						res = parse_i(pattern, i2, end + 1);
						if (res != null) {
							for (int k = 0; k < vi.classes.length; k++) {
								final Expression<?> var = parseExpr(vi.classes[k].getC(), expr.substring(i, i2), parseStatic);
								if (var != null) {
									if (!vi.isPlural[k] && !(var instanceof UnparsedLiteral) && !var.isSingle()) {
										if (context == ParseContext.COMMAND)
											setBestError(ErrorQuality.SEMANTIC_ERROR, "this command can only accept a single " + vi.classes[k].getName() + "!", false);
										else
											setBestError(ErrorQuality.SEMANTIC_ERROR, "this expression can only accept a single " + vi.classes[k].getName() + ", but multiple are given.", true);
										return null;
									}
									if (vi.time != 0) {
										if (var instanceof UnparsedLiteral)
											return null;
										if (ScriptLoader.hasDelayBefore) {
											setBestError(ErrorQuality.SEMANTIC_ERROR, "Cannot use time states after the event has already passed", true);
											return null;
										}
										if (!var.setTime(vi.time)) {
											setBestError(ErrorQuality.SEMANTIC_ERROR, var + " does not have a " + (vi.time == -1 ? "past" : "future") + " state", true);
											return null;
										}
									}
									res.vars[StringUtils.count(pattern, '%', 0, j - 1) / 2] = var;
									return res;
								}
							}
							if (res.matchedChars + matchedChars >= 5) {
								final String types;
								if (vi.classes.length == 1) {
									types = Utils.a(vi.classes[0].getName());
								} else {
									final StringBuilder b = new StringBuilder("neither ");
									for (int k = 0; k < vi.classes.length; k++) {
										if (k != 0) {
											if (k != vi.classes.length - 1)
												b.append(", ");
											else
												b.append(" nor ");
										}
										b.append(Utils.a(vi.classes[k].getName()));
									}
									types = b.toString();
								}
								setBestError(ErrorQuality.NOT_AN_EXPRESSION, "'" + expr.substring(i, i2) + "' is not " + types, true);
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
							}
						}
					}
					return null;
				case ')':
				case ']':
					j++;
				break;
				case '|':
					j = nextBracket(pattern, ')', '(', j + 1) + 1;
				break;
				case ' ':
					if (i == expr.length() || (i > 0 && expr.charAt(i - 1) == ' ')) {
						j++;
						break;
					} else if (expr.charAt(i) != ' ') {
						return null;
					}
					matchedChars++;
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
					matchedChars++;
					i++;
					j++;
			}
		}
		if (i == expr.length() && j == pattern.length())
			return new ParseResult(expr, pattern, matchedChars);
		return null;
	}
	
	private final static class VarInfo {
		ClassInfo<?>[] classes;
		boolean isOptional;
		boolean[] isPlural;
		int time = 0;
	}
	
	private static VarInfo getVarInfo(String s) throws MalformedPatternException {
		final VarInfo r = new VarInfo();
		r.isOptional = s.startsWith("-");
		if (r.isOptional)
			s = s.substring(1);
		final int a = s.indexOf("@");
		if (a != -1) {
			r.time = Integer.parseInt(s.substring(a + 1));
			s = s.substring(0, a);
		}
		final String[] classes = s.split("/");
		r.classes = new ClassInfo<?>[classes.length];
		r.isPlural = new boolean[classes.length];
		for (int i = 0; i < classes.length; i++) {
			final Pair<String, Boolean> p = Utils.getPlural(classes[i]);
			r.classes[i] = Skript.getClassInfo(p.first);
			r.isPlural[i] = p.second;
			if (r.classes[i] == null)
				throw new MalformedPatternException(s, "invalid class '" + p.first + "'");
		}
		return r;
	}
	
}
