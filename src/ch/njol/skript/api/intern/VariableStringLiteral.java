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

package ch.njol.skript.api.intern;

import org.bukkit.event.Event;

import ch.njol.skript.lang.UnparsedLiteral;
import ch.njol.skript.util.Utils;
import ch.njol.skript.util.VariableString;

/**
 * @author Peter Güttinger
 * 
 */
public class VariableStringLiteral extends ConvertedLiteral<String> {
	
	private final VariableString[] strings;
	private final String[] temp;
	
	public VariableStringLiteral(final UnparsedLiteral source) {
		super(source, null, String.class);
		strings = VariableString.makeStringsFromQuoted(source.getData());
		temp = new String[strings.length];
	}
	
	public static VariableStringLiteral newInstance(final UnparsedLiteral source) {
		for (final String s : source.getData()) {
			if (!s.startsWith("\"") && !s.endsWith("\""))
				return null;
		}
		return new VariableStringLiteral(source);
	}
	
	@Override
	public String getDebugMessage(final Event e) {
		return "[" + Utils.join(strings, e, getAnd()) + "]";
	}
	
	@Override
	protected String[] getAll(final Event e) {
		for (int i = 0; i < strings.length; i++) {
			temp[i] = strings[i].get(e);
		}
		return temp;
	}
	
	@Override
	public String toString() {
		return "[" + Utils.join(strings, null, getAnd()) + "]";
	}
	
	@Override
	public Class<String> getReturnType() {
		return String.class;
	}
	
}
