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

package ch.njol.skript.api;

import ch.njol.skript.Skript;

/**
 * A parser used to parse data from a string. <br/>
 * 
 * @author Peter Güttinger
 * 
 * @param <T> the type of this parser
 * @see Skript#addClass(String, Class, Class, Parser, String...)
 */
public abstract class Parser<T> implements Converter<String, T> {
	
	/**
	 * Parses the input. <b>This function must not throw and/or print exceptions/syntax errors</b> but return null instead.
	 * 
	 * @param s The String to parse. This string is already trim()med.
	 * @return The parsed input or null if the input is invalid for this parser.
	 */
	public abstract T parse(String s);
	
	@Override
	public final T convert(final String s) {
		return parse(s);
	}
	
	/**
	 * Returns a string representation of the given object to be used in messages.
	 * 
	 * @param o The object. This will never be <code>null</code>.
	 * @return The String representation of the object.
	 */
	public abstract String toString(T o);
}
