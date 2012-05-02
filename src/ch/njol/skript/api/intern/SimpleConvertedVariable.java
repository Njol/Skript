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
import ch.njol.skript.api.Converter;

/**
 * Represents a converted variable. This is a wrapper to convert the output of a variable to the desired type. It is used by the default implementation of
 * {@link Variable#getConvertedVar(Class)}.
 * 
 * @author Peter Güttinger
 * @see Converter
 * @see Skript#addConverter(Class, Class, Converter)
 */
public class SimpleConvertedVariable<F, T> extends ConvertedVariable<T> {
	
	private final Variable<? extends F> var;
	private final Converter<? super F, ? extends T> converter;
	
	private SimpleConvertedVariable(final Variable<? extends F> var, final Converter<? super F, ? extends T> converter, final Class<T> to) {
		super(var, to);
		this.var = var;
		this.converter = converter;
	}
	
	public static <F, T> ConvertedVariable<T> newInstance(final Variable<F> v, final Class<T> to) {
		if (v == null || to == null || to.isAssignableFrom(v.getReturnType()))
			return null;
		return newInstance(v, v.getReturnType(), to);
	}
	
	@SuppressWarnings("unchecked")
	private static <F, T> SimpleConvertedVariable<F, T> newInstance(final Variable<F> v, final Class<? extends F> from, final Class<T> to) {
		final Converter<? super F, ? extends T> c = (Converter<? super F, ? extends T>) Skript.getConverter(from, to);
		if (c == null)
			return null;
		return new SimpleConvertedVariable<F, T>(v, c, to);
	}
	
	@Override
	protected T[] getAll(final Event e) {
		return get(e, var, converter, true);
	}
	
}
