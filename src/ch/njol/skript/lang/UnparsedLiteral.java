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

import java.lang.reflect.Array;

import org.bukkit.event.Event;

import ch.njol.skript.Skript;
import ch.njol.skript.SkriptLogger;
import ch.njol.skript.SkriptLogger.SubLog;
import ch.njol.skript.api.Converter;
import ch.njol.skript.api.intern.ConvertedLiteral;
import ch.njol.skript.api.intern.SkriptAPIException;
import ch.njol.skript.api.intern.VariableStringLiteral;
import ch.njol.skript.util.Utils;

/**
 * A literal which has yet to be parsed. This is returned if %object(s)% is used within patterns and no variable matches.
 * 
 * @author Peter Güttinger
 * @see SimpleLiteral
 */
public class UnparsedLiteral extends SimpleLiteral<Object> {
	
	/**
	 * 
	 * @param data trim()med Strings
	 * @param and
	 */
	public UnparsedLiteral(final String[] data, final boolean and) {
		super(data, Object.class, and);
	}
	
	@Override
	protected Object[] getAll(final Event e) {
		throw new SkriptAPIException("UnparsedLiterals must be converted before use");
	}
	
	public String[] getData() {
		return (String[]) data;
	}
	
	@SuppressWarnings("unchecked")
	@Override
	public <R> ConvertedLiteral<Object, ? extends R> getConvertedVar(final Class<R> to) {
		if (to == String.class) {
			final VariableStringLiteral vsl = VariableStringLiteral.newInstance(this);
			if (vsl == null)
				return null;
			return (ConvertedLiteral<Object, ? extends R>) vsl;
		} else if (to == Object.class) {
			throw new SkriptAPIException("can't parse as Object");
		}
		final Converter<String, ? extends R> p = Skript.getParser(to);
		if (p == null)
			return null;
		final R[] parsedData = (R[]) Array.newInstance(to, data.length);
		final SubLog log = SkriptLogger.startSubLog();
		for (int i = 0; i < data.length; i++) {
			if ((parsedData[i] = p.convert((String) data[i])) == null) {
				SkriptLogger.stopSubLog(log);
				log.printErrors("'" + data[i] + "' is not " + Utils.a(Skript.getExactClassName(to)));
				return null;
			}
		}
		SkriptLogger.stopSubLog(log);
		return new ConvertedLiteral<Object, R>(this, parsedData, to);
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
