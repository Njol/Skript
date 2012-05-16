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

import org.bukkit.event.Event;

import ch.njol.skript.api.Changer.ChangeMode;
import ch.njol.skript.api.Condition;
import ch.njol.skript.api.Converter;
import ch.njol.skript.api.Debuggable;
import ch.njol.util.Checker;

/**
 * @author Peter Güttinger
 * 
 */
public interface Variable<T> extends Expression, Debuggable {
	
	/**
	 * Get a/the single value of this variable
	 * 
	 * @param e
	 * @return the value or null if this variable doesn't have any value for the event
	 */
	public T getSingle(final Event e);
	
	/**
	 * Get all the values of this variable. The returned array is empty if this variable doesn't have any values for the given event.
	 * 
	 * @param e
	 * @return an array of values of this variable which must not contain nulls.
	 */
	public T[] getArray(final Event e);
	
	/**
	 * Gets a/the sinle value of this variable converted with the given converter.
	 * 
	 * @param e
	 * @param converter
	 * @return the converted value or null if the unconverted value was nul or the converter returned null.
	 */
	public <V> V getSingle(final Event e, final Converter<? super T, ? extends V> converter);
	
	/**
	 * Gets all values of this variable converted to the desired class using the given converter.
	 * 
	 * @param e
	 * @param to
	 * @param converter
	 * @return an array which hold the converted values and which must not contain nulls.
	 */
	public <V> V[] getArray(final Event e, final Class<V> to, final Converter<? super T, ? extends V> converter);
	
	/**
	 * 
	 * @return true if this variable will ever only return one value at most, false if it can return multiple values
	 */
	public abstract boolean isSingle();
	
	/**
	 * Checks this variable against the given checker.
	 * 
	 * @param e
	 * @param c
	 * @param cond
	 * @return
	 */
	public boolean check(final Event e, final Checker<? super T> c, final Condition cond);
	
	/**
	 * Checks this variable against the given checker.
	 * 
	 * @param e
	 * @param c
	 * @return
	 */
	public boolean check(final Event e, final Checker<? super T> c);
	
	public <R> Variable<? extends R> getConvertedVariable(final Class<R> to);
	
	/**
	 * this is set automatically and should not be changed.
	 * 
	 * @param and
	 */
	public void setAnd(final boolean and);
	
	/**
	 * Returns this variable's string representation destined for the end user.
	 */
	@Override
	public abstract String toString();
	
	/**
	 * Gets the return type of this variable.
	 * 
	 * @return The component type of the array returned by {@link #getArray(Event)}
	 */
	public abstract Class<? extends T> getReturnType();
	
	/**
	 * Changes the variable's value by the given amount. This will only be called on supported modes and with the desired <code>delta</code> type as returned by
	 * {@link #acceptChange(ChangeMode)}<br/>
	 * The default implementation of this method throws an exception at runtime.
	 * 
	 * @param e
	 * @param delta the amount to vary this variable by
	 * @param mode
	 * 
	 * @throws UnsupportedOperationException if this method was called on an unsupported ChangeMode.
	 */
	public void change(final Event e, final Variable<?> delta, final ChangeMode mode) throws UnsupportedOperationException;
	
	/**
	 * tests whether this variable supports the given mode, and if yes what type it expects the <code>delta</code> to be.<br/>
	 * The default implementation returns null, i.e. it rejects any change attempts to this variable.
	 * 
	 * @param mode
	 * @return the type that {@link #change(Event, Variable, ChangeMode)} accepts as it's <code>delta</code> parameter,
	 *         or null if the given mode is not supported. For {@link ChangeMode#CLEAR} this can return any non-null class instance to mark clear as supported.
	 */
	public Class<?> acceptChange(final ChangeMode mode);
	
}
