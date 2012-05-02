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

import java.lang.reflect.Array;

import org.bukkit.event.Event;

import ch.njol.skript.Skript;
import ch.njol.skript.api.Converter;
import ch.njol.skript.util.Utils;

/**
 * A literal whioch has yet to be parsed. This is returned if %object% is used within patterns and no variable matches.
 * 
 * @author Peter Güttinger
 * @see Literal
 */
public class UnparsedLiteral extends Literal<Object> {
	
	/**
	 * 
	 * @param data trim()med Strings
	 * @param and
	 */
	public UnparsedLiteral(final String[] data, final boolean and) {
		super(data, Object.class, and);
	}
	
	@SuppressWarnings("unchecked")
	@Override
	public <R> ConvertedLiteral<? extends R> getConvertedVar(final Class<R> to) {
		if (to == String.class) {
			VariableStringLiteral vsl = VariableStringLiteral.newInstance(this);
			if (vsl != null)
				return (ConvertedLiteral<? extends R>) vsl;
		}
		if (to == Object.class)
			throw new SkriptAPIException("can't parse as Object");
		final Converter<String, ? extends R> p = Skript.getParser(to);
		if (p == null)
			return null;
		final R[] parsedData = (R[]) Array.newInstance(to, data.length);
		for (int i = 0; i < data.length; i++) {
			if ((parsedData[i] = p.convert((String) data[i])) == null) {
				Skript.setErrorCause("'" + data[i] + "' is not a(n) " + to.getSimpleName(), false);
				return null;
			}
		}
		return new ConvertedLiteral<R>(this, parsedData, to);
	}
	
	@Override
	public String getDebugMessage(final Event e) {
		return "<" + Utils.join(data) + ">";
	}
	
	@Override
	public String toString() {
		return Utils.join(data);
	}
}
