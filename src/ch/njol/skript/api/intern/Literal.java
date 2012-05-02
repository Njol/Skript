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
import java.util.regex.Matcher;

import org.bukkit.event.Event;

import ch.njol.skript.Skript;
import ch.njol.skript.api.Converter;

/**
 * Represents a literal, i.e. a static value like a number or a string.
 * 
 * @author Peter Güttinger
 * @see UnparsedLiteral
 */
public class Literal<T> extends Variable<T> {
	
	protected final T[] data;
	protected final Class<T> c;
	
	public Literal(final T[] data, final Class<T> c, final boolean and) {
		this.data = data;
		this.c = c;
		setAnd(and);
	}
	
	@SuppressWarnings("unchecked")
	public Literal(final T data) {
		this.data = (T[]) Array.newInstance(data.getClass(), 1);
		this.data[0] = data;
		c = (Class<T>) data.getClass();
		and = true;
	}
	
	@Override
	public void init(final Variable<?>[] vars, final int matchedPattern, final Matcher matcher) {
		throw new UnsupportedOperationException();
	}
	
	@Override
	protected T[] getAll(final Event e) {
		return data;
	}
	
	public T[] getAll() {
		return data;
	}
	
	@Override
	public Class<T> getReturnType() {
		return c;
	}
	
	@SuppressWarnings("unchecked")
	@Override
	public <R> ConvertedLiteral<? extends R> getConvertedVar(final Class<R> to) {
		if (to.isAssignableFrom(c))
			return new ConvertedLiteral<R>(this, (R[]) this.getAll(), to);
		final Converter<? super T, ? extends R> p = Skript.getConverter(c, to);
		if (p == null)
			return null;
		final R[] parsedData = (R[]) Array.newInstance(to, data.length);
		for (int i = 0; i < data.length; i++)
			parsedData[i] = p.convert(data[i]);
		return new ConvertedLiteral<R>(this, parsedData, to);
	}
	
	@Override
	public String getDebugMessage(final Event e) {
		return "[" + this + "]";
	}
	
	@Override
	public String toString() {
		final StringBuilder b = new StringBuilder();
		for (int i = 0; i < data.length; i++) {
			if (i != 0) {
				if (i == data.length - 1)
					b.append(and ? " and " : " or ");
				else
					b.append(", ");
			}
			b.append(Skript.toString(data[i]));
		}
		return b.toString();
	}
	
}
