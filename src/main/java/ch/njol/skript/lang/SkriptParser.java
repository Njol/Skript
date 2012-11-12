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
import java.util.Deque;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.regex.MatchResult;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import ch.njol.skript.ScriptLoader;
import ch.njol.skript.Skript;
import ch.njol.skript.SkriptAPIException;
import ch.njol.skript.classes.ClassInfo;
import ch.njol.skript.classes.Converter;
import ch.njol.skript.command.Argument;
import ch.njol.skript.command.ScriptCommand;
import ch.njol.skript.command.ScriptCommandEvent;
import ch.njol.skript.lang.SkriptEvent.SkriptEventInfo;
import ch.njol.skript.lang.util.VariableString;
import ch.njol.skript.log.ErrorQuality;
import ch.njol.skript.log.ParseLog;
import ch.njol.skript.log.SimpleLog;
import ch.njol.skript.log.SkriptLogger;
import ch.njol.skript.registrations.Classes;
import ch.njol.skript.util.Utils;
import ch.njol.util.Pair;
import ch.njol.util.StringUtils;

/**
 * Used for parsing my custom patterns.<br>
 * <br>
 * Note: All parse methods print one error at most xor any amount of warnings and lower level log messages. If the given string doesn't match any pattern then nothing is printed.
 * 
 * @author Peter Güttinger
 */
public class SkriptParser {
	
	private final String expr;
	
	private final boolean parseStatic;
	
	public final ParseContext context;
	
	private SkriptParser(final String expr) {
		this(expr, false);
	}
	
	private SkriptParser(final String expr, final boolean parseStatic) {
		this(expr, parseStatic, ParseContext.DEFAULT);
	}
	
	private SkriptParser(final String expr, final boolean parseStatic, final ParseContext context) {
		assert expr != null;
		this.expr = expr;
		this.parseStatic = parseStatic;
		this.context = context;
	}
	
	public final static String wildcard = "[^\"]*?(?:\"[^\"]*?\"[^\"]*?)*?";
	public final static String stringMatcher = "\"[^\"]*?(?:\"\"[^\"]*)*?\"";
	
	public final static class ParseResult {
		public final Expression<?>[] exprs;
		public final List<MatchResult> regexes = new ArrayList<MatchResult>();
		public final String expr;
		public int mark = -1;
		
		public ParseResult(final SkriptParser parser, final String pattern) {
			expr = parser.expr;
			exprs = new Expression<?>[countUnescaped(pattern, '%') / 2];
		}
	}
	
	private final static class MalformedPatternException extends RuntimeException {
		
		private static final long serialVersionUID = -2479399963189481643L;
		
		public MalformedPatternException(final String pattern, final String message) {
			super(message + " [pattern: " + pattern + "]");
		}
		
	}
	
	/**
	 * Prints errors.
	 * 
	 * @param expr
	 * @param c
	 * @param context
	 * @return
	 */
	@SuppressWarnings("unchecked")
	public static final <T> Literal<? extends T> parseLiteral(String expr, final Class<T> c, final ParseContext context) {
		expr = expr.trim();
		if (expr.isEmpty())
			return null;
		return new UnparsedLiteral(expr).getConvertedExpression(context, c);
	}
	
	/**
	 * Parses a string as one of the given syntax elements
	 * 
	 * <p>
	 * Can print an error
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
		final ParseLog log = SkriptLogger.startParseLog();
		final T e = new SkriptParser(expr).parse(source);
		log.stop();
		if (e != null) {
			log.printLog();
			return e;
		}
		log.printError(defaultError);
		return null;
	}
	
	public static final <T extends SyntaxElement> T parseStatic(String expr, final Iterator<? extends SyntaxElementInfo<? extends T>> source, final String defaultError) {
		expr = expr.trim();
		if (expr.isEmpty()) {
			Skript.error(defaultError);
			return null;
		}
		final ParseLog log = SkriptLogger.startParseLog();
		final T e = new SkriptParser(expr, true).parse(source);
		log.stop();
		if (e != null) {
			log.printLog();
			return e;
		}
		log.printError(defaultError);
		return null;
	}
	
	private final <T extends SyntaxElement> T parse(final Iterator<? extends SyntaxElementInfo<? extends T>> source) {
		final ParseLog log = SkriptLogger.startParseLog();
		while (source.hasNext()) {
			final SyntaxElementInfo<? extends T> info = source.next();
			patternsLoop: for (int i = 0; i < info.patterns.length; i++) {
				log.clear();
				try {
					final ParseResult res = parse_i(info.patterns[i], 0, 0);
					if (res != null) {
						int x = -1;
						for (int j = 0; (x = nextUnescaped(info.patterns[i], '%', x + 1)) != -1; j++) {
							final int x2 = nextUnescaped(info.patterns[i], '%', x + 1);
							if (res.exprs[j] == null) {
								final String name = info.patterns[i].substring(x + 1, x2);
								if (!name.startsWith("-")) {
									final ExprInfo vi = getExprInfo(name);
									final DefaultExpression<?> expr = vi.classes[0].getDefaultExpression();
									if (expr == null)
										throw new SkriptAPIException("The class '" + vi.classes[0].getName() + "' does not provide a default expression. Either allow null (with %-" + vi.classes[0].getCodeName() + "%) or make it mandatory [pattern: " + info.patterns[i] + "]");
									if (!vi.isPlural[0] && !expr.isSingle())
										throw new SkriptAPIException("The default expression of '" + vi.classes[0].getName() + "' is not a single-element expression. Change your pattern to allow multiple elements or make the expression mandatory [pattern: " + info.patterns[i] + "]");
									if (vi.time != 0 && !expr.setTime(vi.time))
										throw new SkriptAPIException("The default expression of '" + vi.classes[0].getName() + "' does not have distinct time states. [pattern: " + info.patterns[i] + "]");
									if (!expr.init())
										continue patternsLoop;
									res.exprs[j] = expr;
								}
							}
							x = x2;
						}
						final T t = info.c.newInstance();
						if (t.init(res.exprs, i, ScriptLoader.hasDelayBefore, res)) {
							log.printLog();
							return t;
						}
					}
				} catch (final InstantiationException e) {
					assert false;
				} catch (final IllegalAccessException e) {
					assert false;
				}
			}
		}
		log.printError(null);
		return null;
	}
	
	private final static Pattern varPattern = Pattern.compile("^(?i)((the )?var(iable)? )?\\{([^{}]|%\\{|\\}%)+\\}$");
	
	/**
	 * Prints errors
	 * 
	 * @param expr
	 * @param returnType
	 * @return
	 */
	private final static <T> Variable<T> parseVariable(final String expr, final Class<? extends T>[] returnTypes) {
		if (varPattern.matcher(expr).matches())
			return Variable.newInstance(expr.substring(expr.indexOf('{') + 1, expr.lastIndexOf('}')), returnTypes);
		return null;
	}
	
	public final static Pattern listSplitPattern = Pattern.compile("\\s*,?\\s+(and|n?or)\\s+|\\s*,\\s*", Pattern.CASE_INSENSITIVE);
	
	@SuppressWarnings("unchecked")
	public final static <T> Expression<? extends T> parseExpression(final String s, final boolean parseStatic, final ParseContext context, final Class<? extends T>... types) {
		assert s != null && context != null && types != null && types.length > 0;
		assert types.length == 1 || !Utils.contains(types, Object.class);
		if (types.length == 1 && types[0] == Object.class) {
			if (parseStatic)
				return (Expression<? extends T>) new UnparsedLiteral(s);
			else
				return (Expression<? extends T>) new SkriptParser(s, parseStatic, context).parseObjectExpression();
		}
		return new SkriptParser(s, parseStatic, context).parseExpression(types);
	}
	
	// TODO: use the general method below?
	private final <T> Expression<? extends T> parseExpression(final Class<? extends T>[] types) {
		assert !Utils.contains(types, Object.class);
		String lastExpr = expr;
		final ParseLog log = SkriptLogger.startParseLog();
		
		final Expression<? extends T> r = parseSingleExpr(expr, true, types);
		if (r != null) {
			log.printLog();
			return r;
		}
		log.clear();
		
		final Deque<Expression<? extends T>> ts = new LinkedList<Expression<? extends T>>();
		final Matcher m = listSplitPattern.matcher(expr);
		int end = expr.length();
		int expectedEnd = -1;
		boolean and = true;
		boolean isAndSet = false;
		boolean last = false;
		while (m.find() || (last = !last)) {
			if (expectedEnd == -1)
				expectedEnd = last ? expr.length() : m.start();
			final Expression<? extends T> t = parseSingleExpr(lastExpr = expr.substring(last ? 0 : m.end(), end), true, types);
			if (t != null) {
				if (!last && !m.group().matches("\\s*,\\s*")) {
					if (isAndSet) {
						if (and != m.group().toLowerCase().contains("and")) {
							Skript.warning("List has multiple 'and' or 'or', will default to 'and'");
							and = true;
						}
					} else {
						and = m.group().toLowerCase().contains("and");
						isAndSet = true;
					}
				}
				ts.addFirst(t);
				if (last)
					break;
				end = m.start();
				m.region(0, end);
			} else {
				log.clear();
			}
		}
		if (end != expectedEnd || ts.isEmpty()) {
			log.printError("'" + lastExpr + "' is " + notOfType(types));
			return null;
		}
		log.printLog();
		if (ts.size() == 1)
			return ts.getFirst();
		if (!isAndSet)
			Skript.warning("List is missing 'and' or 'or', defaulting to 'and'");
		if (parseStatic) {
			return new LiteralList<T>(ts.toArray(new Literal[ts.size()]), and);
		} else {
			return new ExpressionList<T>(ts.toArray(new Expression[ts.size()]), and);
		}
	}
	
	@SuppressWarnings("unchecked")
	private final Expression<?> parseObjectExpression() {
		assert !parseStatic;
		final ParseLog log = SkriptLogger.startParseLog();
		
		final Expression<?> r = parseSingleExpr(expr, false, Object.class);
		if (r != null) {
			log.printLog();
			return r;
		}
		log.clear();
		
		final List<Expression<?>> ts = new ArrayList<Expression<?>>();
		final Matcher m = listSplitPattern.matcher(expr);
		int startOfLiteral = 0;
		int start = 0, lastEnd = 0;
		boolean and = true;
		boolean isAndSet = false;
		boolean last = false;
		while (m.find() || (last = !last)) {
			final Expression<?> t = parseSingleExpr(expr.substring(start, last ? expr.length() : m.start()), false, Object.class);
			if (t != null) {
				if (!last && !m.group().matches("\\s*,\\s*")) {
					if (isAndSet) {
						if (and != m.group().toLowerCase().contains("and")) {
							Skript.warning("List has multiple 'and' or 'or', will default to 'and'");
							and = true;
						}
					} else {
						and = m.group().toLowerCase().contains("and");
						isAndSet = true;
					}
				}
				if (startOfLiteral != start)
					ts.add(new UnparsedLiteral(expr.substring(startOfLiteral, lastEnd)));
				startOfLiteral = last ? expr.length() : m.end();
				ts.add(t);
				log.clear();
			} else { // TODO ^/v clears log in both cases?
				log.clear();
			}
			if (last)
				break;
			start = m.end();
			lastEnd = m.end();
		}
		log.stop();
		if (startOfLiteral == 0)
			return new UnparsedLiteral(expr);
		if (startOfLiteral != expr.length())
			ts.add(new UnparsedLiteral(expr.substring(startOfLiteral)));
		log.printLog();
		if (ts.size() == 1)
			return ts.get(0);
		if (!isAndSet)
			Skript.warning("List is missing 'and' or 'or', defaulting to 'and'");
		return new ExpressionList<Object>(ts.toArray(new Expression[ts.size()]), and);
	}
	
	@SuppressWarnings({"unchecked", "rawtypes"})
	private final <T> Expression<? extends T> parseSingleExpr(final String s, final boolean parseLiteral, final Class<? extends T>... types) {
		assert types.length > 0;
		assert types.length == 1 || !Utils.contains(types, Object.class);
		assert !(parseStatic && types[0] == Object.class);
		assert !(parseStatic && !parseLiteral);
		if (s.isEmpty())
			return null;
		final ParseLog log = SkriptLogger.startParseLog();
		final Variable<? extends T> var = parseVariable(expr, types);
		if (var != null) {
			log.stop();
			if (parseStatic)
				return null;
			log.printLog();
			return var;
		} else if (log.hasError()) {
			log.printError(null);
			return null;
		}
		log.clear();
		if (!parseStatic) {
			final Expression<? extends T> e;
			if (s.startsWith("\"") && s.endsWith("\"") && (types[0] == Object.class || Utils.contains(types, String.class))) {
				e = (Expression<? extends T>) VariableString.newInstance(s);
			} else {
				e = (Expression<? extends T>) parse(s, (Iterator) Skript.getExpressions(types), null);
			}
			if (e != null) {
				log.printLog();
				return e;
			}
			log.clear();
		}
		if (!parseLiteral) {
			log.stop();
			return null;
		}
		final UnparsedLiteral ul = new UnparsedLiteral(s);
		final Literal<? extends T> l = types[0] == Object.class ? (Literal<? extends T>) ul : ul.getConvertedExpression(context, types);
		if (l == null)
			log.printError(null);
		else
			log.printLog();
		return l;
	}
	
	public final static <T> Expression<? extends T> parseExpression(final String s, final Converter<String, ? extends Expression<? extends T>> parser, final String defaultError) {
		return new SkriptParser(s, false).parseExpression(parser, defaultError);
	}
	
	private final <T> Expression<? extends T> parseExpression(final Converter<String, ? extends Expression<? extends T>> parser, final String defaultError) {
		final ParseLog log = SkriptLogger.startParseLog();
		
		final Expression<? extends T> r = parser.convert(expr);
		if (r != null) {
			log.printLog();
			return r;
		}
		log.clear();
		
		final Deque<Expression<? extends T>> ts = new LinkedList<Expression<? extends T>>();
		final Matcher m = listSplitPattern.matcher(expr);
		int end = expr.length();
		int expectedEnd = -1;
		boolean and = true;
		boolean isAndSet = false;
		boolean last = false;
		boolean isLiteralList = true;
		while (m.find() || (last = !last)) {
			if (expectedEnd == -1)
				expectedEnd = last ? expr.length() : m.start();
			final Expression<? extends T> t = parser.convert(expr.substring(last ? 0 : m.end(), end));
			if (t != null) {
				isLiteralList &= t instanceof Literal;
				if (!last && !m.group().matches("\\s*,\\s*")) {
					if (isAndSet) {
						if (and != m.group().toLowerCase().contains("and")) {
							Skript.warning("List has multiple 'and' or 'or', will default to 'and'");
							and = true;
						}
					} else {
						and = m.group().toLowerCase().contains("and");
						isAndSet = true;
					}
				}
				ts.addFirst(t);
				if (last)
					break;
				end = m.start();
				m.region(0, end);
			} else {
				log.clear();
			}
		}
		if (end != expectedEnd || ts.isEmpty()) {
			log.printError(defaultError);
			return null;
		}
		log.printLog();
		if (ts.size() == 1)
			return ts.getFirst();
		if (!isAndSet)
			Skript.warning("List is missing 'and' or 'or', defaulting to 'and'");
		if (isLiteralList) {
			return new LiteralList<T>(ts.toArray(new Literal[ts.size()]), and);
		} else {
			return new ExpressionList<T>(ts.toArray(new Expression[ts.size()]), and);
		}
	}
	
	/**
	 * Prints errors
	 * 
	 * @param args
	 * @param command
	 * @param event
	 * @return
	 */
	public static boolean parseArguments(final String args, final ScriptCommand command, final ScriptCommandEvent event) {
		
		final SkriptParser parser = new SkriptParser(args, true, ParseContext.COMMAND);
		final ParseResult res = parser.parse_i(command.getPattern(), 0, 0);
		if (res == null)
			return false;
		
		final List<Argument<?>> as = command.getArguments();
		assert as.size() == res.exprs.length;
		
		for (int i = 0; i < res.exprs.length; i++) {
			if (res.exprs[i] == null)
				as.get(i).setToDefault(event);
			else
				as.get(i).set(event, res.exprs[i].getArray(event));
		}
		return true;
	}
	
	public static Pair<SkriptEventInfo<?>, SkriptEvent> parseEvent(final String event, final String defaultError) {
		final SimpleLog log = SkriptLogger.startSubLog();
		final Pair<SkriptEventInfo<?>, SkriptEvent> e = new SkriptParser(event, true, ParseContext.EVENT).parseEvent();
		log.stop();
		if (e != null) {
			log.printLog();
			return e;
		}
		log.printErrors(defaultError);
		return null;
	}
	
	private Pair<SkriptEventInfo<?>, SkriptEvent> parseEvent() {
		assert context == ParseContext.EVENT;
		assert parseStatic;
		final ParseLog log = SkriptLogger.startParseLog();
		for (final SkriptEventInfo<?> info : Skript.getEvents()) {
			for (int i = 0; i < info.patterns.length; i++) {
				log.clear();
				try {
					final ParseResult res = parse_i(info.patterns[i], 0, 0);
					if (res != null) {
						final SkriptEvent e = info.c.newInstance();
						e.init(Arrays.copyOf(res.exprs, res.exprs.length, Literal[].class), i, res);
						log.printLog();
						return new Pair<SkriptEventInfo<?>, SkriptEvent>(info, e);
					}
				} catch (final InstantiationException e) {
					assert false;
				} catch (final IllegalAccessException e) {
					assert false;
				}
			}
		}
		log.printLog();
		return null;
	}
	
	/**
	 * Finds the closing bracket of the group at <tt>start</tt> (i.e. <tt>start</tt> has to be <i>in</i> a group).
	 * 
	 * @param pattern
	 * @param closingBracket The bracket to look for, e.g. ')'
	 * @param openingBracket A bracket that opens another group, e.g. '('
	 * @param start This must not be the index of the opening bracket!
	 * @return
	 * @throws MalformedPatternException
	 */
	private static int nextBracket(final String pattern, final char closingBracket, final char openingBracket, final int start) throws MalformedPatternException {
		int n = 0;
		for (int i = start; i < pattern.length(); i++) {
			if (pattern.charAt(i) == '\\') {
				i++;
				continue;
			} else if (pattern.charAt(i) == closingBracket) {
				if (n == 0)
					return i;
				n--;
			} else if (pattern.charAt(i) == openingBracket) {
				n++;
			}
		}
		throw new MalformedPatternException(pattern, "Missing closing bracket '" + closingBracket + "'");
	}
	
	/**
	 * Gets the next occurrence of a character in a string that is not escaped with a preceding backslash.
	 * 
	 * @param pattern
	 * @param c The character to search for
	 * @param from The index to start searching from
	 * @return
	 */
	private static int nextUnescaped(final String pattern, final char c, final int from) {
		for (int i = from; i < pattern.length(); i++) {
			if (pattern.charAt(i) == '\\') {
				i++;
			} else if (pattern.charAt(i) == c) {
				return i;
			}
		}
		return -1;
	}
	
	private static int countUnescaped(final String pattern, final char c) {
		int r = 0;
		for (int i = 0; i < pattern.length(); i++) {
			if (pattern.charAt(i) == '\\') {
				i++;
			} else if (pattern.charAt(i) == c) {
				r++;
			}
		}
		return r;
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
	
	/**
	 * 
	 * @param cs
	 * @return "not x" or "neither x, y nor z"
	 */
	public final static String notOfType(final Class<?>... cs) {
		if (cs.length == 1) {
			return "not " + Utils.a(Classes.getSuperClassInfo(cs[0]).getName());
		} else {
			final StringBuilder b = new StringBuilder("neither ");
			for (int k = 0; k < cs.length; k++) {
				if (k != 0) {
					if (k != cs.length - 1)
						b.append(", ");
					else
						b.append(" nor ");
				}
				b.append(Utils.a(Classes.getSuperClassInfo(cs[k]).getName()));
			}
			return b.toString();
		}
	}
	
	public final static String notOfType(final ClassInfo<?>... cs) {
		if (cs.length == 1) {
			return "not " + Utils.a(cs[0].getName());
		} else {
			final StringBuilder b = new StringBuilder("neither ");
			for (int k = 0; k < cs.length; k++) {
				if (k != 0) {
					if (k != cs.length - 1)
						b.append(", ");
					else
						b.append(" nor ");
				}
				b.append(Utils.a(cs[k].getName()));
			}
			return b.toString();
		}
	}
	
	/**
	 * Prints errors
	 * 
	 * @param pattern
	 * @param i
	 * @param j
	 * @return
	 */
	private final ParseResult parse_i(final String pattern, int i, int j) {
		ParseResult res;
		int end, i2;
		
		while (j < pattern.length()) {
			switch (pattern.charAt(j)) {
				case '[':
					final ParseLog log0 = SkriptLogger.startParseLog();
					res = parse_i(pattern, i, j + 1);
					if (res != null) {
						log0.printLog();
						return res;
					}
					log0.clear();
					j = nextBracket(pattern, ']', '[', j + 1) + 1;
					res = parse_i(pattern, i, j);
					if (res == null)
						log0.printError(null);
					else
						log0.printLog();
					return res;
				case '(':
					final ParseLog log = SkriptLogger.startParseLog();
					final int start = j;
					for (; j < pattern.length(); j++) {
						log.clear();
						if (j == start || pattern.charAt(j) == '|') {
							int mark = -1;
							if (j != pattern.length() - 1 && (Character.isDigit(pattern.charAt(j + 1)) || pattern.charAt(j + 1) == '-')) {
								final int j2 = pattern.indexOf('¦', j + 2);
								if (j2 != -1) {
									try {
										mark = Integer.parseInt(pattern.substring(j + 1, j2));
										j = j2 + 1;
									} catch (final NumberFormatException e) {}
								}
							}
							res = parse_i(pattern, i, j + 1);
							if (res != null) {
								log.printLog();
								if (mark != -1)
									res.mark = mark;
								return res;
							}
						} else if (pattern.charAt(j) == '(') {
							j = nextBracket(pattern, ')', '(', j + 1);
						} else if (pattern.charAt(j) == ')') {
							break;
						} else if (j == pattern.length() - 1) {
							throw new MalformedPatternException(pattern, "Missing closing bracket ')'");
						}
					}
					log.printError(null);
					return null;
				case '%':
					if (i == expr.length())
						return null;
					end = pattern.indexOf('%', j + 1);
					if (end == -1)
						throw new MalformedPatternException(pattern, "odd number of '%'");
					final String name = pattern.substring(j + 1, end);
					final ExprInfo vi = getExprInfo(name);
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
					final ParseLog log1 = SkriptLogger.startParseLog();
					for (; i2 <= expr.length(); i2++) {
						log1.clear();
						if (i2 < expr.length()) {
							if (expr.charAt(i2) == '"') {
								i2 = nextQuote(expr, i2 + 1) + 1;
								if (i2 == 0) {
									log1.stop();
									return null;
								}
							} else if (expr.charAt(i2) == '{') {
								i2 = expr.indexOf('}', i2 + 1) + 1;
								if (i2 == 0) {
									log1.stop();
									return null;
								}
							}
						}
						res = parse_i(pattern, i2, end + 1);
						if (res != null) {
							final ParseLog log2 = SkriptLogger.startParseLog();
							for (int k = 0; k < vi.classes.length; k++) {
								log2.clear();
								@SuppressWarnings("unchecked")
								final Expression<?> e = parseExpression(expr.substring(i, i2), parseStatic, context, vi.classes[k].getC());
								if (e != null) {
									log2.stop();
									log1.stop();
									if (!vi.isPlural[k] && !((Expression<?>) e).isSingle()) {
										if (context == ParseContext.COMMAND) {
											Skript.error("This command can only accept a single " + vi.classes[k].getName() + ".", ErrorQuality.SEMANTIC_ERROR);
											return null;
										} else {
											Skript.error("This expression can only accept a single " + vi.classes[k].getName() + ", but multiple are given.", ErrorQuality.SEMANTIC_ERROR);
											return null;
										}
									}
									if (vi.time != 0) {
										if (e instanceof Literal<?>)
											return null;
										if (ScriptLoader.hasDelayBefore == 1) {
											Skript.error("Cannot use time states after the event has already passed", ErrorQuality.SEMANTIC_ERROR);
											return null;
										}
										if (!((Expression<?>) e).setTime(vi.time)) {
											Skript.error(e + " does not have a " + (vi.time == -1 ? "past" : "future") + " state", ErrorQuality.SEMANTIC_ERROR);
											return null;
										}
									}
									log2.printLog();
									log1.printLog();
									res.exprs[StringUtils.count(pattern, '%', 0, j - 1) / 2] = e;
									return res;
								}
							}
							log2.stop();
							log1.stop();
							if (log2.hasError()) {
								log2.printError(null);
								return null;
							}
							Skript.error("'" + expr.substring(i, i2) + "' is " + notOfType(vi.classes), ErrorQuality.NOT_AN_EXPRESSION);
							return null;
						}
					}
					log1.printError(null);
					return null;
				case '<':
					end = pattern.indexOf('>', j + 1);// not next()
					if (end == -1)
						throw new MalformedPatternException(pattern, "missing closing regex bracket '>'");
					final ParseLog log2 = SkriptLogger.startParseLog();
					for (i2 = i + 1; i2 <= expr.length(); i2++) {
						log2.clear();
						res = parse_i(pattern, i2, end + 1);
						if (res != null) {
							final Matcher m = Pattern.compile(pattern.substring(j + 1, end)).matcher(expr.substring(i, i2));
							if (m.matches()) {
								res.regexes.add(0, m.toMatchResult());
								log2.printLog();
								return res;
							}
						}
					}
					log2.printError(null);
					return null;
				case ']':
				case ')':
					j++;
					continue;
				case '|':
					j = nextBracket(pattern, ')', '(', j + 1) + 1;
					break;
				case ' ':
					if (i == 0 || i == expr.length() || (i > 0 && expr.charAt(i - 1) == ' ')) {
						j++;
						continue;
					} else if (expr.charAt(i) != ' ') {
						return null;
					}
					i++;
					j++;
					continue;
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
			return new ParseResult(this, pattern);
		return null;
	}
	
	private final static class ExprInfo {
		ClassInfo<?>[] classes;
		boolean isOptional;
		boolean[] isPlural;
		int time = 0;
	}
	
	private static ExprInfo getExprInfo(String s) throws MalformedPatternException {
		final ExprInfo r = new ExprInfo();
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
			r.classes[i] = Classes.getClassInfo(p.first);
			r.isPlural[i] = p.second;
		}
		return r;
	}
	
}
