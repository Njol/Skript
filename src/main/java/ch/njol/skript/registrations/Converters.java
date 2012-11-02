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

package ch.njol.skript.registrations;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import ch.njol.skript.Skript;
import ch.njol.skript.classes.ChainedConverter;
import ch.njol.skript.classes.Converter;
import ch.njol.skript.classes.Converter.ConverterInfo;
import ch.njol.skript.classes.Converter.ConverterOptions;
import ch.njol.skript.classes.Converter.ConverterUtils;
import ch.njol.skript.classes.SerializableConverter;
import ch.njol.util.Pair;

/**
 * @author Peter Güttinger
 * 
 */
public abstract class Converters {
	
	private Converters() {}
	
	private static List<ConverterInfo<?, ?>> converters = new ArrayList<ConverterInfo<?, ?>>(50);
	
	public static List<ConverterInfo<?, ?>> getConverters() {
		return Collections.unmodifiableList(converters);
	}
	
	/**
	 * Registers a converter.
	 * 
	 * @param from
	 * @param to
	 * @param converter
	 */
	public static <F, T> void registerConverter(final Class<F> from, final Class<T> to, final SerializableConverter<F, T> converter) {
		Converters.registerConverter(from, to, converter, 0);
	}
	
	public static <F, T> void registerConverter(final Class<F> from, final Class<T> to, final SerializableConverter<F, T> converter, final int options) {
		Skript.checkAcceptRegistrations();
		converters.add(new ConverterInfo<F, T>(from, to, converter, options));
	}
	
	// TODO how to manage overriding of converters? - shouldn't actually matter
	public static void createMissingConverters() {
		for (int i = 0; i < converters.size(); i++) {
			final ConverterInfo<?, ?> info = converters.get(i);
			for (int j = 0; j < converters.size(); j++) {// not from j = i+1 since new converters get added during the loops
				final ConverterInfo<?, ?> info2 = converters.get(j);
				if ((info.options & ConverterOptions.NO_RIGHT_CHAINING) == 0 && (info2.options & ConverterOptions.NO_LEFT_CHAINING) == 0
						&& info2.from.isAssignableFrom(info.to) && !Converters.converterExists(info.from, info2.to)) {
					converters.add(Converters.createChainedConverter(info, info2));
				} else if ((info.options & ConverterOptions.NO_LEFT_CHAINING) == 0 && (info2.options & ConverterOptions.NO_RIGHT_CHAINING) == 0
						&& info.from.isAssignableFrom(info2.to) && !Converters.converterExists(info2.from, info.to)) {
					converters.add(Converters.createChainedConverter(info2, info));
				}
			}
		}
	}
	
	@SuppressWarnings("unchecked")
	private static <F, M, T> ConverterInfo<F, T> createChainedConverter(final ConverterInfo<?, ?> first, final ConverterInfo<?, ?> second) {
		return new ConverterInfo<F, T>((Class<F>) first.from, (Class<T>) second.to, new ChainedConverter<F, M, T>((SerializableConverter<F, M>) first.converter, (SerializableConverter<M, T>) second.converter), first.options | second.options);
	}
	
	/**
	 * Converts the given value to the desired type. If you want to convert multiple values of the same type you should use {@link #getConverter(Class, Class)} to get a
	 * converter to convert the values.
	 * 
	 * @param o
	 * @param to
	 * @return The converted value or null if no converter exists or the converter returned null for the given value.
	 */
	@SuppressWarnings("unchecked")
	public static <F, T> T convert(final F o, final Class<T> to) {
		assert to != null;
		if (o == null)
			return null;
		if (to.isInstance(o))
			return (T) o;
		final Converter<? super F, ? extends T> conv = getConverter((Class<F>) o.getClass(), to);
		if (conv == null)
			return null;
		return conv.convert(o);
	}
	
	/**
	 * Converts an object into one of the given types.
	 * <p>
	 * This method does not convert the object if it is already an instance of any of the given classes.
	 * 
	 * @param o
	 * @param to
	 * @return
	 */
	@SuppressWarnings("unchecked")
	public final static <F, T> T convert(final F o, final Class<? extends T>[] to) {
		assert to != null;
		if (o == null)
			return null;
		for (final Class<? extends T> t : to)
			if (t.isInstance(o))
				return (T) o;
		for (final Class<? extends T> t : to) {
			final Converter<? super F, ? extends T> conv = getConverter((Class<F>) o.getClass(), t);
			if (conv != null)
				return conv.convert(o);
		}
		return null;
	}
	
	/**
	 * Converts all entries in the given array to the desired type, using {@link convert} to convert every single value. If you want to convert an array of values
	 * of a known type, consider using {@link #convert(Object[], Class, Converter)} for much better performance.
	 * 
	 * @param o
	 * @param to
	 * @return A T[] array without null elements
	 */
	@SuppressWarnings("unchecked")
	public static <T> T[] convertArray(final Object[] o, final Class<T> to) {
		assert to != null;
		if (o == null)
			return null;
		if (to.isAssignableFrom(o.getClass().getComponentType()))
			return (T[]) o;
		final List<T> l = new ArrayList<T>(o.length);
		for (final Object e : o) {
			final T c = convert(e, to);
			if (c != null)
				l.add(c);
		}
		return l.toArray((T[]) Array.newInstance(to, l.size()));
	}
	
	/**
	 * Converts multiple objects into any of the given classes.
	 * 
	 * @param o
	 * @param to
	 * @param superType The component type of the returned array
	 * @return
	 */
	@SuppressWarnings("unchecked")
	public static <T> T[] convertArray(final Object[] o, final Class<? extends T>[] to, final Class<T> superType) {
		assert to != null;
		if (o == null)
			return null;
		for (final Class<? extends T> t : to)
			if (t.isAssignableFrom(o.getClass().getComponentType()))
				return (T[]) o;
		final List<T> l = new ArrayList<T>(o.length);
		for (final Object e : o) {
			final T c = convert(e, to);
			if (c != null)
				l.add(c);
		}
		return l.toArray((T[]) Array.newInstance(superType, l.size()));
	}
	
	/**
	 * Tests whether a converter between the given classes exists.
	 * 
	 * @param from
	 * @param to
	 * @return
	 */
	public final static boolean converterExists(final Class<?> from, final Class<?> to) {
		assert from != null;
		assert to != null;
		if (to.isAssignableFrom(from) || from.isAssignableFrom(to))
			return true;
		for (final ConverterInfo<?, ?> conv : converters) {
			if ((conv.from.isAssignableFrom(from) || from.isAssignableFrom(conv.from)) && (conv.to.isAssignableFrom(to) || to.isAssignableFrom(conv.to)))
				return true;
		}
		return false;
	}
	
	private final static Map<Pair<Class<?>, Class<?>>, SerializableConverter<?, ?>> convertersQuickAccess = new HashMap<Pair<Class<?>, Class<?>>, SerializableConverter<?, ?>>();
	
	/**
	 * Gets a converter
	 * 
	 * @param from
	 * @param to
	 * @return the converter or null if none exist
	 */
	public final static <F, T> SerializableConverter<? super F, ? extends T> getConverter(final Class<F> from, final Class<T> to) {
		assert from != null && to != null;
		final Pair<Class<?>, Class<?>> p = new Pair<Class<?>, Class<?>>(from, to);
		if (Converters.convertersQuickAccess.containsKey(p)) // can contain null to denote nonexistence of a converter
			return (SerializableConverter<? super F, ? extends T>) Converters.convertersQuickAccess.get(p);
		final SerializableConverter<? super F, ? extends T> c = Converters.getConverter_i(from, to);
		Converters.convertersQuickAccess.put(p, c);
		return c;
	}
	
	@SuppressWarnings("unchecked")
	private final static <F, T> SerializableConverter<? super F, ? extends T> getConverter_i(final Class<F> from, final Class<T> to) {
		for (final ConverterInfo<?, ?> conv : converters) {
			if (conv.from.isAssignableFrom(from) && to.isAssignableFrom(conv.to))
				return (SerializableConverter<? super F, ? extends T>) conv.converter;
		}
		for (final ConverterInfo<?, ?> conv : converters) {
			if (conv.from.isAssignableFrom(from) && conv.to.isAssignableFrom(to)) {
				return (SerializableConverter<? super F, ? extends T>) ConverterUtils.createInstanceofConverter(conv.converter, to);
			} else if (from.isAssignableFrom(conv.from) && to.isAssignableFrom(conv.to)) {
				return (SerializableConverter<? super F, ? extends T>) ConverterUtils.createInstanceofConverter(conv);
			}
		}
		for (final ConverterInfo<?, ?> conv : converters) {
			if (from.isAssignableFrom(conv.from) && conv.to.isAssignableFrom(to)) {
				return (SerializableConverter<? super F, ? extends T>) ConverterUtils.createDoubleInstanceofConverter(conv, to);
			}
		}
		return null;
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
		assert from != null;
		assert to != null;
		assert conv != null;
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
