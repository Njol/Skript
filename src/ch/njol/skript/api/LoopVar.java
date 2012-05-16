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

import java.lang.reflect.Array;
import java.util.Iterator;

import org.bukkit.event.Event;

import ch.njol.skript.Skript;
import ch.njol.skript.api.Changer.ChangeMode;
import ch.njol.skript.lang.SimpleVariable;

/**
 * A variable that can be looped.<br/>
 * This class extends Variable, thus has the {@link SimpleVariable#change(Event, SimpleVariable, ChangeMode)} method which has an event argument.
 * You usually don't need it for the variable itself, what you need is {@link #current()}.
 * 
 * @author Peter Güttinger
 * 
 */
public abstract class LoopVar<T> extends SimpleVariable<T> {
	
	/**
	 * holds information about a loop variable
	 * 
	 * @author Peter Güttinger
	 * 
	 */
	public static class LoopInfo<E extends LoopVar<T>, T> extends VariableInfo<E, T> {
		
		public LoopInfo(final Class<E> c, final Class<T> returnType, final String[] patterns) {
			super(patterns, returnType, c);
		}
		
	}
	
	private Iterator<? extends T> iter;
	private T current;
	
	/**
	 * 
	 * 
	 * @param e the event
	 * @return an iterator to iterate over all values of the event
	 */
	protected abstract Iterator<? extends T> iterator(final Event e);
	
	public final void startLoop(final Event e) {
		iter = iterator(e);
	}
	
	public final boolean hasNext() {
		if (iter == null)
			return false;
		return iter.hasNext();
	}
	
	public final T next() {
		return current = iter.next();
	}
	
	@Override
	public final boolean isSingle() {
		return true;
	}
	
	@SuppressWarnings("unchecked")
	@Override
	protected final T[] getAll(final Event e) {
		final T[] t = (T[]) Array.newInstance(getReturnType(), 1);
		t[0] = current;
		return t;
	}
	
	/**
	 * This is the function you should use in set(), add(), etc., simply ignore the events given to those functions.
	 * 
	 * @return the current value of the loop
	 */
	public final T current() {
		return current;
	}
	
	public abstract boolean isLoopOf(String s);
	
	public abstract String getLoopDebugMessage(Event e);
	
	@Override
	public final String getDebugMessage(final Event e) {
		if (e == null)
			return "loop-" + Skript.getExactClassName(getReturnType());
		return Skript.toString(current);
	}
	
}
