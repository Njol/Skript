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
 * Copyright 2011-2013 Peter Güttinger
 * 
 */

package ch.njol.skript.registrations;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import org.bukkit.event.Event;

import ch.njol.skript.Skript;
import ch.njol.skript.classes.Converter;
import ch.njol.skript.classes.SerializableGetter;
import ch.njol.skript.expressions.base.EventValueExpression;
import ch.njol.skript.util.Getter;

/**
 * @author Peter Güttinger
 */
public class EventValues {
	
	private EventValues() {}
	
	@SuppressWarnings("serial")
	private static final class EventValueInfo<E extends Event, T> implements Serializable {
		
		public final Class<E> event;
		public final Class<T> c;
		public final SerializableGetter<T, E> getter;
		public final Class<? extends E>[] exculdes;
		public final String excludeErrorMessage;
		
		public EventValueInfo(final Class<E> event, final Class<T> c, final SerializableGetter<T, E> getter, final String excludeErrorMessage, final Class<? extends E>[] exculdes) {
			assert event != null;
			assert c != null;
			assert getter != null;
			this.event = event;
			this.c = c;
			this.getter = getter;
			this.exculdes = exculdes;
			this.excludeErrorMessage = excludeErrorMessage;
		}
	}
	
	private static final List<EventValueInfo<?, ?>> defaultEventValues = new ArrayList<EventValueInfo<?, ?>>(30);
	private static final List<EventValueInfo<?, ?>> futureEventValues = new ArrayList<EventValueInfo<?, ?>>();
	private static final List<EventValueInfo<?, ?>> pastEventValues = new ArrayList<EventValueInfo<?, ?>>();
	
	private static final List<EventValueInfo<?, ?>> getEventValuesList(final int time) {
		if (time == -1)
			return pastEventValues;
		if (time == 0)
			return defaultEventValues;
		if (time == 1)
			return futureEventValues;
		throw new IllegalArgumentException("time must be -1, 0, or 1");
	}
	
	/**
	 * Registers an event value.
	 * 
	 * @param e the event type
	 * @param c the type of the default value
	 * @param g the getter to get the value
	 * @param time -1 if this is the value before the event, 1 if after, and 0 if it's the default or this value doesn't have distinct states.
	 *            <b>Always register a default state!</b> You can leave out one of the other states instead, e.g. only register a default and a past state. The future state will
	 *            default to the default state in this case.
	 */
	public static <T, E extends Event> void registerEventValue(final Class<E> e, final Class<T> c, final SerializableGetter<T, E> g, final int time) {
		EventValues.registerEventValue(e, c, g, time, null, (Class<? extends E>[]) null);
	}
	
	/**
	 * Same as {@link registerEventValue}
	 * 
	 * @param e
	 * @param c
	 * @param g
	 * @param time
	 * @param excludes Subclasses of the event for which this event value should not be registered for
	 */
	public static <T, E extends Event> void registerEventValue(final Class<E> e, final Class<T> c, final SerializableGetter<T, E> g, final int time, final String excludeErrorMessage, final Class<? extends E>... excludes) {
		Skript.checkAcceptRegistrations();
		final List<EventValueInfo<?, ?>> eventValues = getEventValuesList(time);
		for (int i = 0; i < eventValues.size(); i++) {
			final EventValueInfo<?, ?> info = eventValues.get(i);
			if ((info.event.isAssignableFrom(e) && info.event != e) || (info.event == e && info.c.isAssignableFrom(c))) {
				eventValues.add(i, new EventValueInfo<E, T>(e, c, g, excludeErrorMessage, excludes));
				return;
			}
		}
		eventValues.add(new EventValueInfo<E, T>(e, c, g, excludeErrorMessage, excludes));
	}
	
	/**
	 * Gets a specific value from an event. Returns null if the event doesn't have such a value (conversions are done to try and get the desired value).<br>
	 * It is recommended to use {@link EventValues#getEventValueGetter(Class, Class)} or {@link EventValueExpression#EventValueExpression(Class)} instead of invoking this method
	 * repeatedly.
	 * 
	 * @param e
	 * @param c
	 * @param time
	 * @return
	 * @see registerEventValue
	 */
	public static <T, E extends Event> T getEventValue(final E e, final Class<T> c, final int time) {
		final Getter<? extends T, ? super E> g = EventValues.getEventValueGetter((Class<E>) e.getClass(), c, time);
		if (g == null)
			return null;
		return g.get(e);
	}
	
	/**
	 * Returns a getter to get a value from an event.<br>
	 * Can print an error if the event value is blocked for the given event.
	 * 
	 * @param e
	 * @param c
	 * @param time
	 * @return
	 * @see registerEventValue
	 * @see EventValueExpression#EventValueExpression(Class)
	 */
	public static final <T, E extends Event> SerializableGetter<? extends T, ? super E> getEventValueGetter(final Class<E> e, final Class<T> c, final int time) {
		return EventValues.getEventValueGetter(e, c, time, true);
	}
	
	@SuppressWarnings({"unchecked", "serial"})
	private static final <T, E extends Event> SerializableGetter<? extends T, ? super E> getEventValueGetter(final Class<E> e, final Class<T> c, final int time, final boolean allowDefault) {
		final List<EventValueInfo<?, ?>> eventValues = getEventValuesList(time);
		for (final EventValueInfo<?, ?> ev : eventValues) {
			if (ev.event.isAssignableFrom(e) && c.isAssignableFrom(ev.c)) {
				if (!EventValues.checkExcludes(ev, e, true))
					return null;
				return (SerializableGetter<? extends T, ? super E>) ev.getter;
			}
		}
		for (final EventValueInfo<?, ?> ev : eventValues) {
			if (ev.event.isAssignableFrom(e) && ev.c.isAssignableFrom(c)) {
				if (!EventValues.checkExcludes(ev, e, true))
					return null;
				return new SerializableGetter<T, E>() {
					@Override
					public T get(final E e) {
						final Object o = ((Getter<? super T, ? super E>) ev.getter).get(e);
						if (c.isInstance(o))
							return (T) o;
						return null;
					}
				};
			}
		}
		for (final EventValueInfo<?, ?> ev : eventValues) {
			if (ev.event.isAssignableFrom(e)) {
				if (!EventValues.checkExcludes(ev, e, true))
					return null;
				final SerializableGetter<? extends T, ? super E> g = (SerializableGetter<? extends T, ? super E>) EventValues.getConvertedGetter(ev, c);
				if (g != null)
					return g;
			}
		}
		if (allowDefault && time != 0)
			return getEventValueGetter(e, c, 0);
		return null;
	}
	
	private final static boolean checkExcludes(final EventValueInfo<?, ?> ev, final Class<? extends Event> e, final boolean printError) {
		if (ev.exculdes == null)
			return true;
		for (final Class<? extends Event> ex : ev.exculdes) {
			if (ex.isAssignableFrom(e)) {
				if (printError)
					Skript.error(ev.excludeErrorMessage);
				return false;
			}
		}
		return true;
	}
	
	@SuppressWarnings("serial")
	private final static <E extends Event, F, T> SerializableGetter<? extends T, ? super E> getConvertedGetter(final EventValueInfo<E, F> i, final Class<T> to) {
		final Converter<? super F, ? extends T> c = Converters.getConverter(i.c, to);
		if (c == null)
			return null;
		return new SerializableGetter<T, E>() {
			@Override
			public T get(final E e) {
				final F f = i.getter.get(e);
				if (f == null)
					return null;
				return c.convert(f);
			}
		};
	}
	
	public static final boolean doesEventValueHaveTimeStates(final Class<? extends Event> e, final Class<?> c) {
		return getEventValueGetter(e, c, -1, false) != null || getEventValueGetter(e, c, 1, false) != null;
	}
	
}
