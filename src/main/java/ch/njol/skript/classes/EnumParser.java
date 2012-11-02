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

package ch.njol.skript.classes;

import ch.njol.skript.lang.ParseContext;

/**
 * @author Peter Güttinger
 */
public class EnumParser<E extends Enum<E>> extends Parser<E> {
	
	private final Class<E> c;
	private final String codeType;
	
	public EnumParser(final Class<E> c) {
		assert c != null;
		this.c = c;
		codeType = c.getSimpleName().toLowerCase();
	}
	
	public EnumParser(final Class<E> c, final String codeType) {
		assert c != null;
		this.c = c;
		this.codeType = codeType;
	}
	
	@Override
	public E parse(final String s, final ParseContext context) {
		try {
			return Enum.valueOf(c, s);
		} catch (final IllegalArgumentException e) {
			return null;
		}
	}
	
	@Override
	public String toString(final E o) {
		return o.toString().toLowerCase().replace('_', ' ');
	}
	
	@Override
	public String toVariableNameString(final E o) {
		return codeType == null ? o.toString().toLowerCase().replace('_', ' ') : codeType + ":" + o.toString().toLowerCase().replace('_', ' ');
	}
	
	@Override
	public String getVariableNamePattern() {
		return "\\S+";
	}
	
}
