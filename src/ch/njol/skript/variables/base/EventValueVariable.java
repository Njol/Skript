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

package ch.njol.skript.variables.base;

import java.lang.reflect.Array;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import org.bukkit.event.Event;

import ch.njol.skript.Skript;
import ch.njol.skript.TriggerFileLoader;
import ch.njol.skript.api.Changer;
import ch.njol.skript.api.Changer.ChangeMode;
import ch.njol.skript.api.DefaultVariable;
import ch.njol.skript.api.Getter;
import ch.njol.skript.api.exception.ParseException;
import ch.njol.skript.lang.ExprParser.ParseResult;
import ch.njol.skript.lang.SimpleVariable;
import ch.njol.skript.lang.Variable;

/**
 * A useful class for creating default variables. It simply returns the event value of the given type.<br/>
 * This class can be used as default variable with <code>new EventValueVariable&lt;T&gt;(T.class)</code> or extended to make it manually placeable in expressions with:
 * 
 * <pre>
 * class MyVariable extends EventValueVariable&lt;T&gt; {
 * public MyVariable() {
 * 	super(T.class);
 * }
 * </pre>
 * 
 * @author Peter Güttinger
 * @see Skript#registerClass(ch.njol.skript.api.ClassInfo)
 */
public class EventValueVariable<T> extends SimpleVariable<T> implements DefaultVariable<T> {
	
	private final Class<T> c;
	private final T[] one;
	private final Changer<T> changer;
	private final Map<Class<? extends Event>, Getter<? extends T, ?>> getters = new HashMap<Class<? extends Event>, Getter<? extends T, ?>>();
	
	public EventValueVariable(final Class<T> c) {
		this(c, null);
	}
	
	@SuppressWarnings("unchecked")
	public EventValueVariable(final Class<T> c, final Changer<T> changer) {
		this.c = c;
		one = (T[]) Array.newInstance(c, 1);
		this.changer = changer;
	}
	
	@Override
	protected final T[] getAll(final Event e) {
		if ((one[0] = getValue(e)) == null)
			return null;
		return one;
	}
	
	@SuppressWarnings("unchecked")
	private <E extends Event> T getValue(final E e) {
		final Getter<? extends T, ? super E> g = (Getter<? extends T, ? super E>) getters.get(e.getClass());
		if (g != null)
			return g.get(e);
		
		for (final Entry<Class<? extends Event>, Getter<? extends T, ?>> p : getters.entrySet()) {
			if (p.getKey().isAssignableFrom(e.getClass()))
				return ((Getter<? extends T, ? super E>) p.getValue()).get(e);
		}
		
		return null;
	}
	
	@Override
	public final void init(final Variable<?>[] vars, final int matchedPattern, final ParseResult parser) throws ParseException {
		init();
	}
	
	@Override
	public void init() throws ParseException {
		boolean hasValue = false;
		for (final Class<? extends Event> e : TriggerFileLoader.currentEvents) {
			if (getters.containsKey(e)) {
				hasValue = true;
				continue;
			}
			final Getter<? extends T, ?> getter = Skript.getEventValueGetter(e, c);
			if (getter != null) {
				getters.put(e, getter);
				hasValue = true;
			}
		}
		if (!hasValue)
			throw new ParseException("There's no " + Skript.getExactClassName(c) + " in this event");
	}
	
	@Override
	public Class<T> getReturnType() {
		return c;
	}
	
	@Override
	public boolean isSingle() {
		return true;
	}
	
	@Override
	public String getDebugMessage(final Event e) {
		if (e == null)
			return "event-" + c.getName();
		return Skript.getDebugMessage(getValue(e));
	}
	
	@Override
	public Class<?> acceptChange(final ChangeMode mode) {
		if (changer == null)
			return null;
		return changer.acceptChange(mode);
	}
	
	@Override
	public void change(final Event e, final Variable<?> delta, final ChangeMode mode) {
		if (changer == null)
			throw new UnsupportedOperationException();
		changer.change(e, this, delta, mode);
	}
	
	@Override
	public String toString() {
		return "event-" + Skript.getExactClassName(c);
	}
	
}
