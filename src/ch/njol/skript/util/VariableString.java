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

package ch.njol.skript.util;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.bukkit.event.Event;

import ch.njol.skript.Skript;
import ch.njol.skript.api.Debuggable;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.SkriptParser;
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
	private final boolean isCodeString;
	
	private VariableString(final String s, final boolean code) {
		string = null;
		isSimple = true;
		simple = s;
		isCodeString = code;
	}
	
	private VariableString(final ArrayList<Object> string, final boolean code) {
		isSimple = false;
		simple = null;
		this.string = string;
		isCodeString = code;
	}
	
	/**
	 * 
	 * @param s unquoted string
	 * @return
	 */
	public static VariableString newInstance(final String s) {
		return newInstance(s, false);
	}
	
	public static VariableString newInstance(final String s, final boolean code) {
		if (code && (s.contains("<") || s.contains(">"))) {
			Skript.error("A variable's name must not contain <angle brackets>");
			return null;
		}
		final ArrayList<Object> string = new ArrayList<Object>();
		int c = s.indexOf('%');
		if (c == -1) {
			return new VariableString(s, code);
		}
		string.add(s.substring(0, c));
		while (c != s.length()) {
			int c2 = s.indexOf('%', c + 1);
			int a = c, b;
			while (c2 != -1 && (b = s.indexOf('{', a + 1)) != -1 && b < c2) {
				final int b2 = s.indexOf('}', b + 1);
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
		return new VariableString(string, code);
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
			if (o instanceof Expression<?>) {
				boolean plural = false;
				if (!isCodeString && i + 1 < string.size()) {
					if (string.get(i + 1) instanceof String) {
						if (((String) string.get(i + 1)).startsWith("s "))
							plural = true;
					}
				}
				if (!isCodeString && (plural || Math.abs(StringUtils.numberBefore(b, b.length() - 1)) != 1))
					b.append(Utils.toPlural(Skript.toString(((Expression<?>) o).getArray(e), ((Expression<?>) o).getAnd())));
				else
					b.append(Skript.toString(((Expression<?>) o).getArray(e), ((Expression<?>) o).getAnd(), isCodeString));
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
	
	public boolean isSimple() {
		return isSimple;
	}
	
	public boolean isCodeString() {
		return isCodeString;
	}
	
}
