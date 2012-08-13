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

import java.lang.reflect.Array;
import java.util.Arrays;

import ch.njol.skript.Skript;
import ch.njol.util.Validate;

/**
 * used to convert data from one type to another.
 * 
 * @param <F> the accepted type of objects to convert <u>f</u>rom
 * @param <T> the type to convert <u>t</u>o
 * 
 * @author Peter Güttinger
 * 
 * @see Skript#registerConverter(Class, Class, Converter)
 */
public interface Converter<F, T> {
	
	public final static class ConverterOptions {
		public final static int NO_LEFT_CHAINING = 1;
		public final static int NO_RIGHT_CHAINING = 2;
		public final static int NO_CHAINING = 3;
	}
	
	/**
	 * holds information about a converter
	 * 
	 * @author Peter Güttinger
	 * 
	 * @param <F> same as in {@link Converter}
	 * @param <T> dito
	 */
	public static final class ConverterInfo<F, T> {
		
		public final Class<F> from;
		public final Class<T> to;
		public final Converter<F, T> converter;
		public final int options;
		
		public ConverterInfo(final Class<F> from, final Class<T> to, final Converter<F, T> converter, final int options) {
			this.from = from;
			this.to = to;
			this.converter = converter;
			this.options = options;
		}
		
	}
	
	/**
	 * converts an object from the given to the desired type.
	 * Please note that the given object may be null, thus make sure that you test for null first.
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
			if (o == null)
				return null;
			if (info.from.isInstance(o))
				return info.converter.convert((F) o);
			return null;
		}
		
		@SuppressWarnings("unchecked")
		public final static <F, T> T convertUnsafe(final F from, final Converter<?, T> conv) {
			if (from == null)
				return null;
			return ((Converter<F, T>) conv).convert(from);
		}
		
		public final static <F, T> Converter<?, T> createInstanceofConverter(final ConverterInfo<F, T> conv) {
			return createInstanceofConverter(conv.from, conv.converter);
		}
		
		public final static <F, T> Converter<?, T> createInstanceofConverter(final Class<F> from, final Converter<F, T> conv) {
			return new Converter<Object, T>() {
				@SuppressWarnings("unchecked")
				@Override
				public T convert(final Object o) {
					if (!from.isInstance(o))
						return null;
					return conv.convert((F) o);
				}
			};
		}
		
		public final static <F, T> Converter<F, T> createInstanceofConverter(final Converter<F, ?> conv, final Class<T> to) {
			return new Converter<F, T>() {
				@SuppressWarnings("unchecked")
				@Override
				public T convert(final F f) {
					final Object o = conv.convert(f);
					if (to.isInstance(o))
						return (T) o;
					return null;
				}
			};
		}
		
		public final static <F, T> Converter<?, T> createDoubleInstanceofConverter(final ConverterInfo<F, ?> conv, final Class<T> to) {
			return createDoubleInstanceofConverter(conv.from, conv.converter, to);
		}
		
		public final static <F, T> Converter<?, T> createDoubleInstanceofConverter(final Class<F> from, final Converter<F, ?> conv, final Class<T> to) {
			return new Converter<Object, T>() {
				@SuppressWarnings("unchecked")
				@Override
				public T convert(final Object o) {
					if (!from.isInstance(o))
						return null;
					final Object o2 = conv.convert((F) o);
					if (to.isInstance(o2))
						return (T) o2;
					return null;
				}
			};
		}
		
		/**
		 * 
		 * @param from
		 * @param to
		 * @param conv
		 * @return
		 * @throws ArrayStoreException if the given class is not a superclass of all objects returned by the converter
		 */
		@SuppressWarnings("unchecked")
		public final static <F, T> T[] convertUnsafe(final F[] from, final Class<?> to, final Converter<? super F, ? extends T> conv) {
			return convert(from, (Class<T>) to, conv);
		}
		
		public final static <F, T> T[] convert(final F[] from, final Class<T> to, final Converter<? super F, ? extends T> conv) {
			Validate.notNull(from, to, conv);
			final T[] ts = (T[]) Array.newInstance(to, from.length);
			int j = 0;
			for (int i = 0; i < from.length; i++) {
				final T t = from[i] == null ? null : conv.convert(from[i]);
				if (t != null)
					ts[j++] = t;
			}
			if (j != ts.length)
				return Arrays.copyOf(ts, j);
			return ts;
		}
		
	}
	
}
