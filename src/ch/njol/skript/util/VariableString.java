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

import org.bukkit.event.Event;

import ch.njol.skript.Skript;
import ch.njol.skript.api.Debuggable;
import ch.njol.skript.api.intern.UnparsedLiteral;
import ch.njol.skript.api.intern.Variable;

/**
 * 
 * represents a string that may contain variables.
 * 
 * @author Peter Güttinger
 * 
 */
public class VariableString implements Debuggable {
	private final ArrayList<Object> string = new ArrayList<Object>();
	private Event last = null;
	private String lastString = null;
	private final boolean isSimple;
	
	public VariableString(final String s) {
		if (!s.contains("%")) {
			lastString = s;
			isSimple = true;
			return;
		}
		int c = s.indexOf('%');
		string.add(s.substring(0, c));
		while (c != s.length()) {
			final int c2 = s.indexOf('%', c + 1);
			if (c2 == -1) {
				Skript.error("The percent sign is used for variables (e.g. %player%). To insert a %, type it twice: %%. (found in \"" + s + "\")");
				isSimple = true;
				return;
			}
			int p = s.indexOf('(', c + 1);
			String[] params = new String[0];
			if (p != -1 && p < c2) {
				params = s.substring(p + 1, s.indexOf(')', p + 1)).split(", ?");
			}
			if (c + 1 == c2) {
				string.add("%");
			} else {
				if (params.length == 0)
					p = c2;
				final Variable<?> var = Variable.parse(s.substring(c + 1, p), Object.class);
				if (var == null || var instanceof UnparsedLiteral) {
					Skript.printErrorAndCause("can't understand the variable %" + s.substring(c + 1, c2) + "%");
					isSimple = true;
					return;
				} else {
					string.add(var);
				}
			}
			c = s.indexOf('%', c2 + 1);
			if (c == -1)
				c = s.length();
			string.add(s.substring(c2 + 1, c));
		}
		isSimple = false;
	}

	public static VariableString[] makeStrings(final String[] args) {
		final VariableString[] strings = new VariableString[args.length];
		for (int i = 0; i < args.length; i++) {
			strings[i] = new VariableString(args[i]);
		}
		return strings;
	}

	public static VariableString[] makeStringsFromQuoted(final String[] args) {
		final VariableString[] strings = new VariableString[args.length];
		for (int i = 0; i < args.length; i++) {
			strings[i] = new VariableString(args[i].substring(1, args[i].length() - 1));
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
			if (o instanceof Variable) {
				boolean first = true;
				for (final Object x : ((Variable<?>) o).get(e, false)) {
					if (!first)
						b.append(", ");
					else
						first = false;
					b.append(Skript.toString(x));
				}
			} else if (o instanceof String) {
				b.append((String) o);
			}
		}
		last = e;
		return lastString = b.toString();
	}
	
	@Override
	public String getDebugMessage(final Event e) {
		if (isSimple)
			return '"' + lastString + '"';
		if (e != null)
			return '"' + get(e) + '"';
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
