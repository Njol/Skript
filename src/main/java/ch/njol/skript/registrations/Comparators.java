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

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import ch.njol.skript.Skript;
import ch.njol.skript.classes.Comparator;
import ch.njol.skript.classes.Comparator.ComparatorInfo;
import ch.njol.skript.classes.Comparator.Relation;
import ch.njol.skript.classes.Converter;
import ch.njol.skript.classes.InverseComparator;
import ch.njol.util.Pair;

/**
 * @author Peter Güttinger
 * 
 */
public class Comparators {
	
	private Comparators() {}
	
	public final static Collection<ComparatorInfo<?, ?>> comparators = new ArrayList<ComparatorInfo<?, ?>>();
	
	/**
	 * Registers a {@link Comparator}.
	 * 
	 * @param t1
	 * @param t2
	 * @param c
	 * @throws IllegalArgumentException if any given class is equal to <code>Object.class</code>
	 */
	public static <T1, T2> void registerComparator(final Class<T1> t1, final Class<T2> t2, final Comparator<T1, T2> c) {
		Skript.checkAcceptRegistrations();
		if (t1 == Object.class || t2 == Object.class)
			throw new IllegalArgumentException("must not add a comparator for Object");
		comparators.add(new ComparatorInfo<T1, T2>(t1, t2, c));
	}
	
	@SuppressWarnings({"rawtypes", "unchecked"})
	public final static Relation compare(final Object o1, final Object o2) {
		assert o1 != null && o2 != null;
		final Comparator<?, ?> c = Comparators.getComparator(o1.getClass(), o2.getClass());
		if (c == null)
			return null;
		return ((Comparator) c).compare(o1, o2);
	}
	
	private final static Map<Pair<Class<?>, Class<?>>, Comparator<?, ?>> comparatorsQuickAccess = new HashMap<Pair<Class<?>, Class<?>>, Comparator<?, ?>>();
	
	@SuppressWarnings("unchecked")
	public final static <F, S> Comparator<? super F, ? super S> getComparator(final Class<F> f, final Class<S> s) {
		final Pair<Class<?>, Class<?>> p = new Pair<Class<?>, Class<?>>(f, s);
		if (comparatorsQuickAccess.containsKey(p))
			return (Comparator<? super F, ? super S>) comparatorsQuickAccess.get(p);
		final Comparator<?, ?> comp = getComparator_i(f, s);
		comparatorsQuickAccess.put(p, comp);
		return (Comparator<? super F, ? super S>) comp;
	}
	
	@SuppressWarnings("unchecked")
	private final static <F, S> Comparator<?, ?> getComparator_i(final Class<F> f, final Class<S> s) {
		
		// perfect match
		for (final ComparatorInfo<?, ?> info : comparators) {
			if (info.c1.isAssignableFrom(f) && info.c2.isAssignableFrom(s) || info.c1.isAssignableFrom(s) && s.isAssignableFrom(info.c2)) {
				return ((info.c1.isAssignableFrom(f) && info.c2.isAssignableFrom(s)) ? info.c : new InverseComparator<F, S>((Comparator<? super S, ? super F>) info.c));
			}
		}
		
		// same class but no comparator
		if (f != Object.class && s != Object.class && (f.isAssignableFrom(s) || s.isAssignableFrom(f))) {
			return (s.isAssignableFrom(f) ? Comparator.equalsComparator : new InverseComparator<F, S>((Comparator<? super S, ? super F>) Comparator.equalsComparator));
		}
		
		final int[] zeroOne = {0, 1};
		Converter<? super F, ?> c1;
		Converter<? super S, ?> c2;
		
		// single conversion
		for (final ComparatorInfo<?, ?> info : comparators) {
			for (final int c : zeroOne) {
				if (info.getType(c).isAssignableFrom(f)) {
					c2 = Converters.getConverter(s, info.getType(1 - c));
					if (c2 != null) {
						return c == 0 ? new ConvertedComparator<F, S>(info.c, c2) : new InverseComparator<F, S>(new ConvertedComparator<S, F>(c2, info.c));
					}
				}
				if (info.getType(c).isAssignableFrom(s)) {
					c1 = Converters.getConverter(f, info.getType(1 - c));
					if (c1 != null) {
						return c == 1 ? new ConvertedComparator<F, S>(c1, info.c) : new InverseComparator<F, S>(new ConvertedComparator<S, F>(info.c, c1));
					}
				}
			}
		}
		
		// double conversion
		for (final ComparatorInfo<?, ?> info : comparators) {
			for (final int c : zeroOne) {
				c1 = Converters.getConverter(f, info.getType(c));
				c2 = Converters.getConverter(s, info.getType(1 - c));
				if (c1 != null && c2 != null) {
					return c == 0 ? new ConvertedComparator<F, S>(c1, info.c, c2) : new InverseComparator<F, S>(new ConvertedComparator<S, F>(c2, info.c, c1));
				}
			}
		}
		
		// different convertible classes and no comparator
		if (f != Object.class) {
			c2 = Converters.getConverter(s, f);
			if (c2 != null) {
				return new ConvertedComparator<F, S>(Comparator.equalsComparator, c2);
			}
		}
		if (s != Object.class) {
			c1 = Converters.getConverter(f, s);
			if (c1 != null) {
				return new ConvertedComparator<F, S>(c1, Comparator.equalsComparator);
			}
		}
		
		return null;
	}
	
	private final static class ConvertedComparator<T1, T2> implements Comparator<T1, T2> {
		private static final long serialVersionUID = -2067438898833733437L;
		
		@SuppressWarnings("rawtypes")
		private final Comparator c;
		@SuppressWarnings("rawtypes")
		private final Converter c1, c2;
		
		public ConvertedComparator(final Converter<? super T1, ?> c1, final Comparator<?, ?> c) {
			this.c1 = c1;
			this.c = c;
			this.c2 = null;
		}
		
		public ConvertedComparator(final Comparator<?, ?> c, final Converter<? super T2, ?> c2) {
			this.c1 = null;
			this.c = c;
			this.c2 = c2;
		}
		
		public ConvertedComparator(final Converter<? super T1, ?> c1, final Comparator<?, ?> c, final Converter<? super T2, ?> c2) {
			this.c1 = c1;
			this.c = c;
			this.c2 = c2;
		}
		
		@Override
		public Relation compare(final T1 o1, final T2 o2) {
			return c.compare(c1 == null ? o1 : c1.convert(o1), c2 == null ? o2 : c2.convert(o2));
		}
		
		@Override
		public boolean supportsOrdering() {
			return c.supportsOrdering();
		}
		
	}
	
}
