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

package ch.njol.skript.lang.util;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.regex.Pattern;

import org.bukkit.event.Event;

import ch.njol.skript.ScriptLoader;
import ch.njol.skript.Skript;
import ch.njol.skript.lang.Debuggable;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.SkriptParser;
import ch.njol.skript.util.StringMode;
import ch.njol.util.StringUtils;

/**
 * 
 * represents a string that may contain expressions.
 * 
 * @author Peter Güttinger
 * 
 */
public class VariableString implements Debuggable {
	private final ArrayList<Object> string;
	private final boolean isSimple;
	private final String simple;
	private StringMode mode;
	
	private VariableString(final String s, final StringMode mode) {
		string = null;
		isSimple = true;
		simple = s;
		this.mode = mode;
	}
	
	private VariableString(final ArrayList<Object> string, final StringMode mode) {
		isSimple = false;
		simple = null;
		this.string = string;
		this.mode = mode;
	}
	
	/**
	 * 
	 * @param s unquoted string
	 * @return
	 */
	public static VariableString newInstance(final String s) {
		return newInstance(s, StringMode.MESSAGE);
	}
	
	public final static Map<String, Pattern> variableNames = new HashMap<String, Pattern>();
	
	public static VariableString newInstance(final String s, final StringMode mode) {
//		if (mode == StringMode.VARIABLE_NAME && (s.contains("<") || s.contains(">"))) {
//			Skript.error("A variable's name must not contain <angle brackets>");
//			return null;
//		}
		final ArrayList<Object> string = new ArrayList<Object>();
		int c = s.indexOf('%');
		if (c != -1) {
			string.add(s.substring(0, c));
			while (c != s.length()) {
				int c2 = s.indexOf('%', c + 1);
				int a = c, b;
				while (c2 != -1 && (b = s.indexOf('{', a + 1)) != -1 && b < c2) {
					final int b2 = nextBracket(s, '}', '{', b + 1);
					if (b2 == -1) {
						Skript.error("Missing closing bracket '}' to end variable");
						return null;
					}
					c2 = s.indexOf('%', b2 + 1);
					a = b2;
				}
				if (c2 == -1) {
					Skript.error("The percent sign is used for expressions (e.g. %player%). To insert a %, type it twice: %%.");
					return null;
				}
				if (c + 1 == c2) {
					string.add("%");
				} else {
					final Expression<?> expr = (Expression<?>) SkriptParser.parse(s.substring(c + 1, c2), Skript.getExpressions().iterator(), false, true, "can't understand the expression %" + s.substring(c + 1, c2) + "%");
					if (expr == null) {
						return null;
					} else {
						string.add(expr);
					}
				}
				c = s.indexOf('%', c2 + 1);
				if (c == -1)
					c = s.length();
				string.add(s.substring(c2 + 1, c));
			}
		} else {
			string.add(s);
		}
		
		if (mode == StringMode.VARIABLE_NAME && !variableNames.containsKey(s)) {
			if (s.startsWith("%") && !Skript.disableVariableConflictWarnings) // inside the if to only print this message once per variable
				Skript.warning("Starting a variable's name with an expression is discouraged ({" + s + "}). You could prefix it with the script's name: {" + StringUtils.substring(ScriptLoader.currentScript.getFileName(), 0, -3) + "." + s + "}");
			
			final StringBuilder p = new StringBuilder();
			for (final Object o : string) {
				if (o instanceof Expression)
					p.append("[^%](.*[^%])?");
				else
					p.append(Pattern.quote(o.toString()));
			}
			final Pattern pattern = Pattern.compile(p.toString());
			for (final Entry<String, Pattern> e : variableNames.entrySet()) {
				if (e.getValue().matcher(s).matches() || pattern.matcher(e.getKey()).matches()) {
					Skript.warning("Possible name conflict of variables {" + s + "} and {" + e.getKey() + "} (there might be more conflicts).");
					break;
				}
			}
			variableNames.put(s, pattern);
		}
		
		if (c == -1)
			return new VariableString(s, mode);
		return new VariableString(string, mode);
	}
	
	/**
	 * Copied from {@link SkriptParser#nextBracket(String, char, char, int)}
	 * 
	 * @param s
	 * @param closingBracket
	 * @param openingBracket
	 * @param start
	 * @return
	 */
	private static int nextBracket(final String s, final char closingBracket, final char openingBracket, final int start) {
		int n = 0;
		for (int i = start; i < s.length(); i++) {
			if (s.charAt(i) == closingBracket) {
				if (n == 0)
					return i;
				n--;
			} else if (s.charAt(i) == openingBracket) {
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
	 * 
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
	 * Parses all expressions in the string and returns it. The returned string is cached as long as this method is always called with the same event argument.
	 * 
	 * @param e Event to pass to the expressions.
	 * @return The input string with all expressions replaced.
	 */
	public String toString(final Event e) {
		if (isSimple)
			return simple;
		final StringBuilder b = new StringBuilder();
		for (int i = 0; i < string.size(); i++) {
			final Object o = string.get(i);
			if (o instanceof Expression) {
				boolean plural = false;
				if (mode == StringMode.MESSAGE && i + 1 < string.size()) {
					if (string.get(i + 1) instanceof String) {
						if (((String) string.get(i + 1)).startsWith("s "))
							plural = true;
					}
				}
				if (mode == StringMode.MESSAGE && (plural || Math.abs(StringUtils.numberBefore(b, b.length() - 1)) != 1))
					b.append(Skript.toString(((Expression<?>) o).getArray(e), ((Expression<?>) o).getAnd(), mode, true));
				else
					b.append(Skript.toString(((Expression<?>) o).getArray(e), ((Expression<?>) o).getAnd(), mode, false));
			} else {
				b.append(o);
			}
		}
		return b.toString();
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
				b.append("<" + Skript.getSuperClassInfo(((Expression<?>) o).getReturnType()).getCodeName() + ">");
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
}
