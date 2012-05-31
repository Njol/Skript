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

import ch.njol.skript.Skript;
import ch.njol.skript.api.Changer.ChangeMode;
import ch.njol.skript.api.Condition;
import ch.njol.skript.api.Converter;
import ch.njol.skript.api.Debuggable;
import ch.njol.skript.api.intern.SkriptAPIException;
import ch.njol.util.Checker;

/**
 * Represents a variable. Variables are used within conditions, effects and other variables.
 * 
 * @author Peter Güttinger
 * @see Skript#registerVariable(Class, Class, String...)
 * @see Expression
 */
public interface Variable<T> extends Expression, Debuggable {
	
	/**
	 * Get the single value of this variable.<br/>
	 * Do not use this in conditions, use {@link #check(Event, Checker, Condition)} instead.
	 * 
	 * @param e
	 * @return The value or null if this variable doesn't have any value for the event
	 * @throws SkriptAPIException (optional) if this was called on a non-single variable
	 */
	public T getSingle(final Event e);
	
	/**
	 * Get all the values of this variable. The returned array is empty if this variable doesn't have any values for the given event.<br/>
	 * Do not use this in conditions, use {@link #check(Event, Checker, Condition)} instead.
	 * 
	 * @param e
	 * @return An array of values of this variable. Does not contain nulls.
	 */
	public T[] getArray(final Event e);
	
	/**
	 * Gets a/the single value of this variable converted with the given converter.
	 * 
	 * @param e
	 * @param converter
	 * @return The converted value or null if the unconverted value was null or the converter returned null for the value.
	 */
	public <V> V getSingle(final Event e, final Converter<? super T, ? extends V> converter);
	
	/**
	 * Gets all values of this variable converted to the desired class using the given converter.
	 * 
	 * @param e
	 * @param to
	 * @param converter
	 * @return An array which hold the converted values. Does not contain nulls.
	 */
	public <V> V[] getArray(final Event e, final Class<V> to, final Converter<? super T, ? extends V> converter);
	
	/**
	 * 
	 * @return true if this variable will ever only return one value at most, false if it can return multiple values
	 */
	public abstract boolean isSingle();
	
	/**
	 * Checks this variable against the given checker. This is the normal version of this method and the one which must be used for simple checks,
	 * or as the outmost check of a nested check.
	 * 
	 * @param e
	 * @param c
	 * @param cond The condition that is checking this variable. This is required as the check needs the condition's negated state.
	 * @return
	 */
	public boolean check(final Event e, final Checker<? super T> c, final Condition cond);
	
	/**
	 * Checks this variable against the given checker. This method must only be used <b>in</b> nested variable checks, use {@link #check(Event, Checker, Condition)} otherwise!
	 * 
	 * @param e
	 * @param c
	 * @return
	 */
	public boolean check(final Event e, final Checker<? super T> c);
	
	/**
	 * Tries to convert this variable to the given type.
	 * 
	 * @param to the desired return type of the returned variable
	 * @return Variable with the desired return type or null if the variable can't be converted to the given type. Returns the variable itself if it already returns the desired
	 *         type.
	 * @see Converter
	 */
	public <R> Variable<? extends R> getConvertedVariable(final Class<R> to);
	
	/**
	 * this is set automatically and must not be changed.
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
	 * 
	 * @param e
	 * @param delta the amount to vary this variable by or null for {@link ChangeMode#CLEAR}
	 * @param mode
	 * 
	 * @throws UnsupportedOperationException (optional) if this method was called on an unsupported ChangeMode.
	 */
	public void change(final Event e, final Variable<?> delta, final ChangeMode mode) throws UnsupportedOperationException;
	
	/**
	 * Tests whether this variable supports the given mode, and if yes what type it expects the <code>delta</code> to be.
	 * 
	 * @param mode
	 * @return the type that {@link #change(Event, Variable, ChangeMode)} accepts as it's <code>delta</code> parameter's type param,
	 *         or null if the given mode is not supported. For {@link ChangeMode#CLEAR} this can return any non-null class instance to mark clear as supported.
	 */
	public Class<?> acceptChange(final ChangeMode mode);
	
	/**
	 * 
	 * @return True if this variable returns all possible values, false if it only returns one.
	 */
	public boolean getAnd();
	
}
