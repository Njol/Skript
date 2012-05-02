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
 * used to convert data from one type to another.
 * 
 * @author Peter Güttinger
 * 
 * @param <F> the accepted type of objects to convert
 * @param <T> the type to convert to
 * @see Skript#addConverter(Class, Class, Converter)
 */
public interface Converter<F, T> {
	
	/**
	 * holds information about a converter
	 * 
	 * @author Peter Güttinger
	 * 
	 * @param <F> same as {@link Converter}
	 * @param <T> dito
	 */
	public static final class ConverterInfo<F, T> {
		
		public Class<F> from;
		public Class<T> to;
		public Converter<F, T> converter;
		
		public ConverterInfo(final Class<F> from, final Class<T> to, final Converter<F, T> converter) {
			this.from = from;
			this.to = to;
			this.converter = converter;
		}
		
	}
	
	/**
	 * converts an object from the given to the desired type.
	 * Please note that the given object may be null, so prevent NullPointerExceptions!
	 * 
	 * @param f The object to convert which can be null.
	 * @return the converted object
	 */
	public T convert(F f);
	
	public static final class ConverterUtils {
		
		/**
		 * Converts a value using a ConverterInfo. Does not throw an error if the given object is of the wrong type, but returns null instead.
		 * 
		 * @param info
		 * @param o
		 * @return info.converter.convert(o) or null if o is not of the correct type.
		 */
		@SuppressWarnings("unchecked")
		public final static <T, F> T convert(final ConverterInfo<F, T> info, final Object o) {
			if (info.from.isInstance(o))
				return info.converter.convert((F) o);
			return null;
		}
		
	}
	
}
