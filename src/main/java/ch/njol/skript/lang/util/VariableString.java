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
 * Copyright 2011-2013 Peter Güttinger
 * 
 */

package ch.njol.skript.lang.util;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.regex.Pattern;

import org.bukkit.event.Event;

import ch.njol.skript.ScriptLoader;
import ch.njol.skript.Skript;
import ch.njol.skript.SkriptConfig;
import ch.njol.skript.classes.Changer.ChangeMode;
import ch.njol.skript.classes.ClassInfo;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.ExpressionList;
import ch.njol.skript.lang.ParseContext;
import ch.njol.skript.lang.SkriptParser;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.skript.log.RetainingLogHandler;
import ch.njol.skript.log.SkriptLogger;
import ch.njol.skript.registrations.Classes;
import ch.njol.skript.util.StringMode;
import ch.njol.skript.util.Utils;
import ch.njol.util.Checker;
import ch.njol.util.Kleenean;
import ch.njol.util.StringUtils;
import ch.njol.util.iterator.SingleItemIterator;

/**
 * represents a string that may contain expressions.
 * 
 * @author Peter Güttinger
 */
@SuppressWarnings("serial")
public class VariableString implements Expression<String> {
	
	private final String name;
	
	private final Object[] string;
	private final boolean isSimple;
	private final String simple;
	private StringMode mode;
	
	private VariableString(final String s, final StringMode mode) {
		name = s;
		string = null;
		isSimple = true;
		simple = s;
		this.mode = mode;
	}
	
	private VariableString(final String name, final Object[] string, final StringMode mode) {
		this.name = name;
		isSimple = false;
		simple = null;
		this.string = string;
		this.mode = mode;
	}
	
	/**
	 * Prints errors
	 * 
	 * @param s unquoted string
	 * @return
	 */
	public static VariableString newInstance(final String s) {
		return newInstance(s, StringMode.MESSAGE);
	}
	
	public final static Map<String, Pattern> variableNames = new HashMap<String, Pattern>();
	
	/**
	 * Tests whether a string is correctly quoted, i.e. only has doubled double quotes in it.
	 * 
	 * @param s The string
	 * @param withQuotes Whether s must be surrounded by double quotes or not
	 * @return
	 */
	public final static boolean isQuotedCorrectly(final String s, final boolean withQuotes) {
		if (withQuotes && (!s.startsWith("\"") || !s.endsWith("\"")))
			return false;
		boolean quote = false;
		for (int i = withQuotes ? 1 : 0; i < (withQuotes ? s.length() - 1 : s.length()); i++) {
			if (s.charAt(i) != '"') {
				if (quote)
					return false;
			} else {
				quote = !quote;
			}
		}
		return !quote;
	}
	
	/**
	 * Prints errors
	 * 
	 * @param s unquoted string
	 * @param mode
	 * @return
	 */
	public static VariableString newInstance(String s, final StringMode mode) {
		if (!isQuotedCorrectly(s, false))
			return null;
		s = Utils.replaceChatStyles(s.replace("\"\"", "\""));
		final ArrayList<Object> string = new ArrayList<Object>();
		int c = s.indexOf('%');
		if (c != -1) {
			string.add(s.substring(0, c));
			while (c != s.length()) {
				int c2 = s.indexOf('%', c + 1);
				int a = c, b;
				while (c2 != -1 && (b = s.indexOf('{', a + 1)) != -1 && b < c2) {
					a = nextVariableBracket(s, b + 1);
					if (a == -1) {
						Skript.error("Missing closing bracket '}' to end variable");
						return null;
					}
					c2 = s.indexOf('%', a + 1);
				}
				if (c2 == -1) {
					Skript.error("The percent sign is used for expressions (e.g. %player%). To insert a '%' type it twice: %%.");
					return null;
				}
				if (c + 1 == c2) {
					string.add("%");
				} else {
					final RetainingLogHandler log = SkriptLogger.startRetainingLog();
					try {
						@SuppressWarnings("unchecked")
						final Expression<?> expr = SkriptParser.parseExpression(s.substring(c + 1, c2), SkriptParser.PARSE_EXPRESSIONS, ParseContext.DEFAULT, Object.class);
						if (expr == null) {
							log.printErrors("Can't understand this expression: " + s.substring(c + 1, c2));
							return null;
						} else {
							string.add(expr);
						}
					} finally {
						log.stop();
					}
					log.printLog();
				}
				c = s.indexOf('%', c2 + 1);
				if (c == -1)
					c = s.length();
				string.add(s.substring(c2 + 1, c));
			}
		} else {
			string.add(s);
		}
		
		checkVariableConflicts(s, mode, string);
		
		if (c == -1)
			return new VariableString(s, mode);
		return new VariableString(s, string.toArray(), mode);
	}
	
	private static void checkVariableConflicts(final String name, final StringMode mode, final Iterable<Object> string) {
		if (mode == StringMode.VARIABLE_NAME && !variableNames.containsKey(name)) {
			if (name.startsWith("%")) // inside the if to only print this message once per variable
				Skript.warning("Starting a variable's name with an expression is discouraged ({" + name + "}). You could prefix it with the script's name: {" + StringUtils.substring(ScriptLoader.currentScript.getFileName(), 0, -3) + "." + name + "}");
			
			final Pattern pattern;
			if (string != null) {
				final StringBuilder p = new StringBuilder();
				stringLoop: for (final Object o : string) {
					if (o instanceof Expression) {
						for (final ClassInfo<?> ci : Classes.getClassInfos()) {
							if (ci.getParser() != null && ci.getC().isAssignableFrom(((Expression<?>) o).getReturnType())) {
								p.append("(?!%)" + ci.getParser().getVariableNamePattern() + "(?<!%)");
								continue stringLoop;
							}
						}
						p.append("[^%*](.*[^%])?"); // [^*] to not report {var::%index%}/{var::*} as conflict
					} else {
						p.append(Pattern.quote(o.toString()));
					}
				}
				pattern = Pattern.compile(p.toString());
			} else {
				pattern = Pattern.compile(Pattern.quote(name));
			}
			if (!SkriptConfig.disableVariableConflictWarnings.value()) {
				for (final Entry<String, Pattern> e : variableNames.entrySet()) {
					if (e.getValue().matcher(name).matches() || pattern.matcher(e.getKey()).matches()) {
						Skript.warning("Possible name conflict of variables {" + name + "} and {" + e.getKey() + "} (there might be more conflicts).");
						break;
					}
				}
			}
			variableNames.put(name, pattern);
		}
	}
	
	private void readObject(final ObjectInputStream in) throws ClassNotFoundException, IOException {
		in.defaultReadObject();
		checkVariableConflicts(name, mode, string == null ? null : Arrays.asList(string));
	}
	
	/**
	 * Copied from {@link SkriptParser#nextBracket(String, char, char, int)}, but removed escaping & returns -1 on error.
	 * 
	 * @param s
	 * @param closingBracket
	 * @param openingBracket
	 * @param start
	 * @return
	 */
	public static int nextVariableBracket(final String s, final int start) {
		int n = 0;
		for (int i = start; i < s.length(); i++) {
			if (s.charAt(i) == '}') {
				if (n == 0)
					return i;
				n--;
			} else if (s.charAt(i) == '{') {
				n++;
			}
		}
		return -1;
	}
	
	public static VariableString[] makeStrings(final String[] args) {
		final VariableString[] strings = new VariableString[args.length];
		int j = 0;
		for (int i = 0; i < args.length; i++) {
			final VariableString vs = newInstance(args[i]);
			if (vs != null)
				strings[j++] = vs;
		}
		if (j != args.length)
			return Arrays.copyOf(strings, j);
		return strings;
	}
	
	/**
	 * @param args Quoted strings - This is not checked!
	 * @return
	 */
	public static VariableString[] makeStringsFromQuoted(final List<String> args) {
		final VariableString[] strings = new VariableString[args.size()];
		for (int i = 0; i < args.size(); i++) {
			assert args.get(i).startsWith("\"") && args.get(i).endsWith("\"");
			final VariableString vs = newInstance(args.get(i).substring(1, args.get(i).length() - 1));
			if (vs == null)
				return null;
			strings[i] = vs;
		}
		return strings;
	}
	
	/**
	 * Parses all expressions in the string and returns it.
	 * 
	 * @param e Event to pass to the expressions.
	 * @return The input string with all expressions replaced.
	 */
	public String toString(final Event e) {
		if (isSimple)
			return simple;
		final StringBuilder b = new StringBuilder();
		for (int i = 0; i < string.length; i++) {
			final Object o = string[i];
			if (o instanceof Expression) {
				b.append(Classes.toString(((Expression<?>) o).getArray(e), ((Expression<?>) o).getAnd(), mode, mode == StringMode.MESSAGE &&
						(Math.abs(StringUtils.numberBefore(b, b.length() - 1)) != 1 || i + 1 < string.length && string[i + 1] instanceof String && StringUtils.startsWithIgnoreCase((String) string[i + 1], "s"))));
			} else {
				if (mode == StringMode.MESSAGE && i != 0 && string[i - 1] instanceof Expression && StringUtils.startsWithIgnoreCase((String) o, "s"))
					b.append(((String) o).substring(1));
				else
					b.append(o);
			}
		}
		return b.toString();
	}
	
	@Override
	public String toString() {
		return toString(null, false);
	}
	
	/**
	 * Use {@link #toString(Event)} to get the actual string
	 * 
	 * @param e
	 * @param debug
	 * @return
	 */
	@Override
	public String toString(final Event e, final boolean debug) {
		if (isSimple)
			return '"' + simple + '"';
		final StringBuilder b = new StringBuilder("\"");
		for (final Object o : string) {
			if (o instanceof Expression) {
				b.append("%" + ((Expression<?>) o).toString(e, debug) + "%");
			} else {
				b.append(o);
			}
		}
		b.append('"');
		return b.toString();
	}
	
	public String getDefaultVariableName() {
		if (isSimple)
			return simple;
		final StringBuilder b = new StringBuilder();
		for (final Object o : string) {
			if (o instanceof Expression) {
				b.append("<" + Classes.getSuperClassInfo(((Expression<?>) o).getReturnType()).getCodeName() + ">");
			} else {
				b.append(o);
			}
		}
		return b.toString();
	}
	
	public boolean isSimple() {
		return isSimple;
	}
	
	public StringMode getMode() {
		return mode;
	}
	
	public void setMode(final StringMode mode) {
		this.mode = mode;
	}
	
	@Override
	public boolean init(final Expression<?>[] exprs, final int matchedPattern, final Kleenean isDelayed, final ParseResult parseResult) {
		throw new UnsupportedOperationException();
	}
	
	@Override
	public String getSingle(final Event e) {
		return toString(e);
	}
	
	@Override
	public String[] getArray(final Event e) {
		return new String[] {toString(e)};
	}
	
	@Override
	public String[] getAll(final Event e) {
		return new String[] {toString(e)};
	}
	
	@Override
	public boolean isSingle() {
		return true;
	}
	
	@Override
	public boolean check(final Event e, final Checker<? super String> c, final boolean negated) {
		return SimpleExpression.check(getAll(e), c, negated, false);
	}
	
	@Override
	public boolean check(final Event e, final Checker<? super String> c) {
		return SimpleExpression.check(getAll(e), c, false, false);
	}
	
	@SuppressWarnings("unchecked")
	@Override
	public <R> Expression<? extends R> getConvertedExpression(final Class<R> to) {
		if (to.isAssignableFrom(String.class))
			return (Expression<? extends R>) this;
		return null;
	}
	
	@Override
	public Class<? extends String> getReturnType() {
		return String.class;
	}
	
	@Override
	public Class<?>[] acceptChange(final ChangeMode mode) {
		return null;
	}
	
	@Override
	public void change(final Event e, final Object delta, final ChangeMode mode) throws UnsupportedOperationException {
		throw new UnsupportedOperationException();
	}
	
	@Override
	public boolean getAnd() {
		return false;
	}
	
	@Override
	public boolean setTime(final int time) {
		return false;
	}
	
	@Override
	public int getTime() {
		return 0;
	}
	
	@Override
	public boolean isDefault() {
		return false;
	}
	
	@Override
	public Iterator<? extends String> iterator(final Event e) {
		return new SingleItemIterator<String>(toString(e));
	}
	
	@Override
	public boolean isLoopOf(final String s) {
		return false;
	}
	
	@Override
	public Expression<?> getSource() {
		return this;
	}
	
	public final static void setStringMode(final Expression<?> e, final StringMode mode) {
		if (e instanceof ExpressionList) {
			for (final Expression<?> ex : ((ExpressionList<?>) e).getExpressions()) {
				if (ex instanceof VariableString)
					((VariableString) ex).setMode(mode);
			}
		} else if (e instanceof VariableString) {
			((VariableString) e).setMode(mode);
		}
	}
	
	@Override
	public Expression<String> simplify() {
		return this;
	}
	
	/* TODO allow special characters?
	private static String allowedChars = null;
	private static Field allowedCharacters = null;
	
	static {
		if (Skript.isRunningCraftBukkit()) {
			try {
				allowedCharacters = SharedConstants.class.getDeclaredField("allowedCharacters");
				allowedCharacters.setAccessible(true);
				Field modifiersField = Field.class.getDeclaredField("modifiers");
				modifiersField.setAccessible(true);
				modifiersField.setInt(allowedCharacters, allowedCharacters.getModifiers() & ~Modifier.FINAL);
				allowedChars = (String) allowedCharacters.get(null);
			} catch (Throwable e) {
				allowedChars = null;
				allowedCharacters = null;
			}
		}
	}
	*/
}
