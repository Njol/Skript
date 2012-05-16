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

import ch.njol.skript.Skript;
import ch.njol.skript.lang.Literal;
import ch.njol.skript.lang.SimpleLiteral;

/**
 * @author Peter Güttinger
 * @see SimpleLiteral
 */
public class ConvertedLiteral<T> extends ConvertedVariable<T> implements Literal<T> {
	
	private final T[] data;
	
	public ConvertedLiteral(final SimpleLiteral<?> source, final T[] data, final Class<T> to) {
		super(source, to);
		this.data = data;
	}
	
	@Override
	protected T[] getAll(final Event e) {
		return data;
	}
	
	@Override
	public String getDebugMessage(final Event e) {
		return Skript.toString(data);
	}
	
	@Override
	public String toString() {
		return Skript.toString(data);
	}
	
	@Override
	public T[] getArray() {
		return getArray(null);
	}
	
	@Override
	public T getSingle() {
		return getSingle(null);
	}
}
