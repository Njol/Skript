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

import org.bukkit.event.Event;

import ch.njol.skript.Skript;
import ch.njol.skript.api.Debuggable;
import ch.njol.skript.lang.ExprParser;
import ch.njol.skript.lang.Variable;

/**
 * 
 * represents a string that may contain variables.
 * 
 * @author Peter Güttinger
 * 
 */
public class VariableString implements Debuggable {
	private final ArrayList<Object> string;
	private Event last = null;
	private String lastString = null;
	private final boolean isSimple;
	
	private VariableString(final String s) {
		string = null;
		isSimple = true;
		lastString = s;
	}
	
	private VariableString(final ArrayList<Object> string) {
		isSimple = false;
		this.string = string;
	}
	
	public static VariableString newInstance(final String s) {
		final ArrayList<Object> string = new ArrayList<Object>();
		if (!s.contains("%")) {
			return new VariableString(s);
		}
		int c = s.indexOf('%');
		string.add(s.substring(0, c));
		while (c != s.length()) {
			final int c2 = s.indexOf('%', c + 1);
			if (c2 == -1) {
				Skript.error("The percent sign is used for variables (e.g. %player%). To insert a %, type it twice: %%.");
				return null;
			}
			if (c + 1 == c2) {
				string.add("%");
			} else {
				final Variable<?> var = (Variable<?>) ExprParser.parse(s.substring(c + 1, c2), Skript.getVariables().iterator(), false, "can't understand the variable %" + s.substring(c + 1, c2) + "%");
				if (var == null) {
					return null;
				} else {
					string.add(var);
				}
			}
			c = s.indexOf('%', c2 + 1);
			if (c == -1)
				c = s.length();
			string.add(s.substring(c2 + 1, c));
		}
		return new VariableString(string);
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
	public static VariableString[] makeStringsFromQuoted(final String[] args) {
		final VariableString[] strings = new VariableString[args.length];
		for (int i = 0; i < args.length; i++) {
			if (Skript.debug() && (!args[i].startsWith("\"") || !args[i].endsWith("\"")))
				Skript.warning("Call to VariableString.makeStringsFromQuoted with unquoted string: " + args[i]);
			final VariableString vs = newInstance(args[i].substring(1, args[i].length() - 1));
			if (vs == null)
				return null;
			strings[i] = vs;
		}
		return strings;
	}
	
	/**
	 * Parses all variables in the string and returns it. The returned string is cached as long as this method is always called with the same event argument.
	 * 
	 * @param e Event to pass to the variables.
	 * @return The input string with all variables replaced.
	 */
	public String get(final Event e) {
		if (isSimple || last == e)
			return lastString;
		final StringBuilder b = new StringBuilder();
		for (final Object o : string) {
			if (o instanceof Variable<?>) {
				if (((Variable<?>) o).isSingle())
					b.append(Skript.toString(((Variable<?>) o).getSingle(e)));
				else
					b.append(Skript.toString(((Variable<?>) o).getArray(e), ((Variable<?>) o).getAnd()));
			} else {
				b.append(o);
			}
		}
		last = e;
		return lastString = b.toString();
	}
	
	@Override
	public String getDebugMessage(final Event e) {
		if (isSimple)
			return '"' + lastString + '"';
		final StringBuilder b = new StringBuilder("\"");
		for (final Object o : string) {
			if (o instanceof Variable) {
				b.append("%" + ((Variable<?>) o).getDebugMessage(e) + "%");
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
	
}
