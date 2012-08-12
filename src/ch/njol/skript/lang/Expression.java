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

import java.util.Iterator;

import org.bukkit.event.Event;

import ch.njol.skript.Skript;
import ch.njol.skript.SkriptAPIException;
import ch.njol.skript.classes.Changer.ChangeMode;
import ch.njol.skript.classes.Converter;
import ch.njol.skript.lang.util.SimpleExpression;
import ch.njol.util.Checker;

/**
 * Represents an expression. Expressions are used within conditions, effects and other expressions.
 * 
 * @author Peter Güttinger
 * @see Skript#registerExpression(Class, Class, String...)
 * @see SimpleExpression
 * @see SyntaxElement
 */
public interface Expression<T> extends SyntaxElement, Debuggable {
	
	/**
	 * Get the single value of this expression.<br/>
	 * Do not use this in conditions, use {@link #check(Event, Checker, Condition)} instead.<br/>
	 * This method may only return null if it always returns null for the given event.
	 * 
	 * @param e
	 * @return The value or null if this expression doesn't have any value for the event
	 * @throws SkriptAPIException (optional) if this was called on a non-single expression
	 */
	public T getSingle(final Event e);
	
	/**
	 * Get all the values of this expression. The returned array is empty if this expression doesn't have any values for the given event.<br/>
	 * Do not use this in conditions, use {@link #check(Event, Checker, Condition)} instead.<br/>
	 * The returned array must not contain any null valuse.
	 * 
	 * @param e
	 * @return An array of values of this expression. Does not contain nulls.
	 */
	public T[] getArray(final Event e);
	
	/**
	 * Gets all possible return values of this expression, i.e. it returns the same as {@link #getArray(Event)} if {@link #getAnd()} is true, otherwise all possible values for
	 * {@link #getSingle(Event)}.
	 * 
	 * @param e
	 * @return An array of all possible values of this expression for the given event. Does not contain nulls.
	 */
	public T[] getAll(final Event e);
	
	/**
	 * Gets a/the single value of this expression converted with the given converter.
	 * 
	 * @param e
	 * @param converter
	 * @return The converted value or null if the unconverted value was null or the converter returned null for the value.
	 */
	public <V> V getSingle(final Event e, final Converter<? super T, ? extends V> converter);
	
	/**
	 * Gets all values of this expression converted to the desired class using the given converter.
	 * 
	 * @param e
	 * @param to
	 * @param converter
	 * @return An array which hold the converted values. Does not contain nulls.
	 */
	public <V> V[] getArray(final Event e, final Class<V> to, final Converter<? super T, ? extends V> converter);
	
	/**
	 * 
	 * @return true if this expression will ever only return one value at most, false if it can return multiple values
	 */
	public abstract boolean isSingle();
	
	/**
	 * Checks this expression against the given checker. This is the normal version of this method and the one which must be used for simple checks,
	 * or as the outmost check of nested checks.
	 * 
	 * @param e
	 * @param c
	 * @param cond The condition that is checking this expression. This is required as the check needs the condition's negated state.
	 * @return
	 */
	public boolean check(final Event e, final Checker<? super T> c, final Condition cond);
	
	/**
	 * Checks this expression against the given checker. This method must only be used <b>in</b> nested expression checks, use {@link #check(Event, Checker, Condition)} otherwise!
	 * 
	 * @param e
	 * @param c
	 * @return
	 */
	public boolean check(final Event e, final Checker<? super T> c);
	
	/**
	 * Tries to convert this expression to the given type. This method can print an error prior to returning null to specify the cause.
	 * 
	 * @param to the desired return type of the returned expression
	 * @return Expression with the desired return type or null if the expression can't be converted to the given type. Returns the expression itself if it already returns the
	 *         desired type.
	 * @see Converter
	 */
	public <R> Expression<? extends R> getConvertedExpression(final Class<R> to);
	
	/**
	 * Gets the return type of this expression.
	 * 
	 * @return The type retured by {@link #getSingle(Event)} and {@link #getArray(Event)}
	 */
	public abstract Class<? extends T> getReturnType();
	
	/**
	 * Changes the expression value by the given amount. This will only be called on supported modes and with the desired <code>delta</code> type as returned by
	 * {@link #acceptChange(ChangeMode)}<br/>
	 * 
	 * @param e
	 * @param delta the amount to vary this expression by or null for {@link ChangeMode#CLEAR}
	 * @param mode
	 * 
	 * @throws UnsupportedOperationException (optional) if this method was called on an unsupported ChangeMode.
	 */
	public void change(final Event e, final Object delta, final ChangeMode mode) throws UnsupportedOperationException;
	
	/**
	 * Tests whether this expression supports the given mode, and if yes what type it expects the <code>delta</code> to be.
	 * 
	 * @param mode
	 * @return the type that {@link #change(Event, Expression, ChangeMode)} accepts as it's <code>delta</code> parameter's type param,
	 *         or null if the given mode is not supported. For {@link ChangeMode#CLEAR} this can return any non-null class instance to mark clear as supported.
	 */
	public Class<?> acceptChange(final ChangeMode mode);
	
	/**
	 * Returns true if this expression returns all possible values, false if it only returns one.<br>
	 * This method heavily influences {@link #check(Event, Checker)} and {@link #check(Event, Checker, Condition)} and thus breaks conditions that use this expression if it returns
	 * a wrong value.
	 * 
	 * @return
	 */
	public boolean getAnd();
	
	/**
	 * Sets the time of this expression, i.e. whether the returned value represents this expression before or after the event.
	 * 
	 * @param time -1 for past, 0 for default and 1 for future respectively
	 * @return whether this expression has distinct states, e.g. a player never changes but a block can. This should also be sensitive for the event but doesn't have to be.
	 */
	public boolean setTime(int time);
	
	/**
	 * 
	 * @return
	 * @see #setTime(int)
	 */
	public int getTime();
	
	/**
	 * Returns whether this value represents the default value of it's type for the event, i.e. it can be replaced with a call to event.getXyz() if one knows the event & value.
	 * 
	 * @return
	 */
	public boolean isDefault();
	
	/**
	 * 
	 * @return Whether this expression can be looped
	 */
	public boolean canLoop();
	
	/**
	 * 
	 * @param e the event
	 * @return An iterator to iterate over all values of this expression which may be empty and/or null.
	 */
	public Iterator<T> iterator(Event e);
	
	/**
	 * Checks whether the given 'loop-...' expression should match this loop, e.g. loop-blovk matches any loops that loop through blocks while loop-argument only matches an
	 * argument loop.<br>
	 * You should usually just return false as e.g. loop-block will automatically match the expression if it's returnType is Block or a subtype of it.
	 * 
	 * @param s the entered string
	 * @return whether this loop matches the given string
	 */
	public boolean isLoopOf(String s);
	
	/**
	 * Returns the original expression that was parsed, i.e. without any conversions done.
	 * 
	 * @return
	 */
	public Expression<?> getSource();
	
}
