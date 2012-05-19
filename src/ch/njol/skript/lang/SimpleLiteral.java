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
import ch.njol.skript.api.Converter;
import ch.njol.skript.api.intern.ConvertedLiteral;
import ch.njol.skript.lang.ExprParser.ParseResult;

/**
 * Represents a literal, i.e. a static value like a number or a string.
 * 
 * @author Peter Güttinger
 * @see UnparsedLiteral
 */
public class SimpleLiteral<T> extends SimpleVariable<T> implements ch.njol.skript.lang.Literal<T> {
	
	protected final T[] data;
	protected final Class<T> c;
	
	public SimpleLiteral(final T[] data, final Class<T> c, final boolean and) {
		this.data = data;
		this.c = c;
		setAnd(and);
	}
	
	@SuppressWarnings("unchecked")
	public SimpleLiteral(final T data) {
		this.data = (T[]) Array.newInstance(data.getClass(), 1);
		this.data[0] = data;
		c = (Class<T>) data.getClass();
		setAnd(true);
	}
	
	@Override
	public void init(final Variable<?>[] vars, final int matchedPattern, final ParseResult parseResult) {
		throw new UnsupportedOperationException();
	}
	
	@Override
	protected T[] getAll(final Event e) {
		return data;
	}
	
	@Override
	public T[] getArray() {
		return getArray(null);
	}
	
	@Override
	public T getSingle() {
		return getSingle(null);
	}
	
	@Override
	public Class<T> getReturnType() {
		return c;
	}
	
	@SuppressWarnings("unchecked")
	@Override
	public <R> ConvertedLiteral<? extends R> getConvertedVar(final Class<R> to) {
		if (to.isAssignableFrom(c))
			return new ConvertedLiteral<R>(this, (R[]) this.getAll(null), to);
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
		return Skript.toString(data, getAnd());
	}
	
	@Override
	public boolean isSingle() {
		return !getAnd() || data.length <= 1;
	}
	
}
