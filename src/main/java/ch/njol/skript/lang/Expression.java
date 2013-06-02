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

package ch.njol.skript.lang;

import java.util.Iterator;

import org.bukkit.event.Event;

import ch.njol.skript.ScriptLoader;
import ch.njol.skript.Skript;
import ch.njol.skript.classes.Changer;
import ch.njol.skript.classes.Changer.ChangeMode;
import ch.njol.skript.classes.Converter;
import ch.njol.skript.lang.util.SimpleExpression;
import ch.njol.skript.log.ErrorQuality;
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
	 * Get the single value of this expression.
	 * <p>
	 * This method may only return null if it always returns null for the given event, i.e. it is equivalent to getting a random element out of {@link #getAll(Event)} or null iff
	 * that array is empty.
	 * <p>
	 * Do not use this in conditions, use {@link #check(Event, Checker, boolean)} instead.
	 * 
	 * @param e The event
	 * @return The value or null if this expression doesn't have any value for the event
	 * @throws UnsupportedOperationException (optional) if this was called on a non-single expression
	 */
	public T getSingle(final Event e);
	
	/**
	 * Get all the values of this expression. The returned array is empty if this expression doesn't have any values for the given event.
	 * <p>
	 * The returned array must not contain any null values.
	 * <p>
	 * Do not use this in conditions, use {@link #check(Event, Checker, boolean)} instead.
	 * 
	 * @param e The event
	 * @return An array of values of this expression which must neither be null nor contain nulls.
	 */
	public T[] getArray(final Event e);
	
	/**
	 * Gets all possible return values of this expression, i.e. it returns the same as {@link #getArray(Event)} if {@link #getAnd()} is true, otherwise all possible values for
	 * {@link #getSingle(Event)}.
	 * 
	 * @param e The event
	 * @return An array of all possible values of this expression for the given event which must neither be null nor contain nulls.
	 */
	public T[] getAll(final Event e);
	
	/**
	 * @return true if this expression will ever only return one value at most, false if it can return multiple values.
	 */
	public abstract boolean isSingle();
	
	/**
	 * Checks this expression against the given checker. This is the normal version of this method and the one which must be used for simple checks,
	 * or as the innermost check of nested checks.
	 * 
	 * @param e The event
	 * @param c A checker
	 * @param negated The cheking condition's negated state. This is used to invert the output of the checker if set to true (i.e. <tt>negated ^ checker.check(...)</tt>)
	 * @return Whether this expression matches or doesn't match the given checker depending on the condition's negated state.
	 * @see SimpleExpression#check(Object[], Checker, boolean, boolean)
	 */
	public boolean check(final Event e, final Checker<? super T> c, final boolean negated);
	
	/**
	 * Checks this expression against the given checker. This method must only be used around other checks, use {@link #check(Event, Checker, boolean)} for a simple ckeck or the
	 * innermost check of a nested check.
	 * 
	 * @param e The event
	 * @param c A checker
	 * @return Whether this expression matches the given checker
	 * @see SimpleExpression#check(Object[], Checker, boolean, boolean)
	 */
	public boolean check(final Event e, final Checker<? super T> c);
	
	/**
	 * Tries to convert this expression to the given type. This method can print an error prior to returning null to specify the cause.
	 * <p>
	 * Please note that expressions whose {@link #getReturnType() returnType} is not Object will not be parsed at all for a certain class if there's no converter from the
	 * expression's returnType to the desired class. Thus this method should only be overridden if this expression's returnType is Object.
	 * 
	 * @param to The desired return type of the returned expression //TODO allow multiple types?
	 * @return Expression with the desired return type or null if the expression can't be converted to the given type. Returns the expression itself if it already returns the
	 *         desired type.
	 * @see Converter
	 */
	public <R> Expression<? extends R> getConvertedExpression(final Class<R> to);
	
	/**
	 * Gets the return type of this expression.
	 * 
	 * @return The type returned by {@link #getSingle(Event)} and {@link #getArray(Event)}
	 */
	public abstract Class<? extends T> getReturnType();
	
	/**
	 * Returns true if this expression returns all possible values, false if it only returns one.
	 * <p>
	 * This method significantly influences {@link #check(Event, Checker)} and {@link #check(Event, Checker, boolean)} and thus breaks conditions that use this expression if it
	 * returns a wrong value.
	 * <p>
	 * This method can return anything if this is a {@link #isSingle() single} expression.
	 * 
	 * @return
	 */
	public boolean getAnd();
	
	/**
	 * Sets the time of this expression, i.e. whether the returned value represents this expression before or after the event.
	 * <p>
	 * This method will <b>not</b> be called if this expression is <i>guaranteed</i> to be used after a delay (an error will be printed immediately), but <b>will</b> be called if
	 * it only <i>can be</i> after a delay (e.g. if the preceding delay is in an if or a loop) as well as if there's no delay involved.
	 * <p>
	 * If this method returns false the expression will be discarded and an error message is printed. Custom error messages must be of {@link ErrorQuality#SEMANTIC_ERROR} to be
	 * printed.
	 * 
	 * @param time -1 for past or 1 for future. 0 is never passed to this method as it represents the default state.
	 * @return Whether this expression has distinct time states, e.g. a player never changes but a block can. This should be sensitive for the event (using
	 *         {@link ScriptLoader#isCurrentEvent(Class)}).
	 * @see SimpleExpression#setTime(int, Class, Expression...)
	 * @see SimpleExpression#setTime(int, Expression, Class...)
	 * @see ScriptLoader#isCurrentEvent(Class...)
	 */
	public boolean setTime(int time);
	
	/**
	 * @return The value passed to {@link #setTime(int)} or 0 if it was never changed.
	 * @see #setTime(int)
	 */
	public int getTime();
	
	/**
	 * Returns whether this value represents the default value of it's type for the event, i.e. it can be replaced with a call to event.getXyz() if one knows the event & value
	 * type.
	 * <p>
	 * This method might be removed in the future as it's better to check whether value == e.getXyz() for every value an expression returns.
	 * 
	 * @return
	 */
	public boolean isDefault();
	
	/**
	 * Returns the same as {@link #getArray(Event)} but as an iterator. This method should be overriden by expressions intended to be looped to increase performance.
	 * 
	 * @param e The event
	 * @return An iterator to iterate over all values of this expression which may be empty and/or null, but must not return null elements.
	 */
	public Iterator<? extends T> iterator(Event e);
	
	/**
	 * Checks whether the given 'loop-...' expression should match this loop, e.g. loop-block matches any loops that loop through blocks and loop-argument matches an
	 * argument loop.
	 * <p>
	 * You should usually just return false as e.g. loop-block will automatically match the expression if it's returnType is Block or a subtype of it.
	 * 
	 * @param s The entered string
	 * @return Whether this loop matches the given string
	 */
	public boolean isLoopOf(String s);
	
	/**
	 * Returns the original expression that was parsed, i.e. without any conversions done.
	 * <p>
	 * This method is undefined for simplified expressions.
	 * 
	 * @return The unconverted source expression of this expression or this expression itself if it was never converted.
	 */
	public Expression<?> getSource();
	
	/**
	 * Simplifies the expression, e.g. if it only contains literals the expression may be simplified to a literal, and wrapped expressions are unwrapped.
	 * <p>
	 * After this method was used the toString methods are likely not useful anymore.
	 * <p>
	 * This method is not yet used but will be used to improve efficiency in the future.
	 * 
	 * @return A reference to a simpler version of this expression. Can change this expression directly and return itself if applicable, i.e. no references to the expression before
	 *         this method call should be kept!
	 */
	public Expression<? extends T> simplify();
	
	/**
	 * Tests whether this expression supports the given mode, and if yes what type it expects the <code>delta</code> to be.
	 * <p>
	 * Please note that if a changer is registered for this expression's {@link #getReturnType() returnType} this method does not have to be overridden.
	 * <p>
	 * Unlike {@link Changer#acceptChange(ChangeMode)} this method may print errors.
	 * 
	 * @param mode
	 * @return An array of types that {@link #change(Event, Object, ChangeMode)} accepts as it's <code>delta</code> parameter (which can be arrays),
	 *         or null if the given mode is not supported. For {@link ChangeMode#DELETE} this can return any array to mark clear as supported, even an empty one.
	 */
	public Class<?>[] acceptChange(ChangeMode mode);
	
	/**
	 * Changes the expression's value by the given amount. This will only be called on supported modes and with the desired <code>delta</code> type as returned by
	 * {@link #acceptChange(ChangeMode)}
	 * 
	 * @param e
	 * @param delta The amount to vary this expression by or null for {@link ChangeMode#DELETE}. Can also be null if {@link #acceptChange(ChangeMode)} didn't return an array class,
	 *            otherwise it cannot be null but an empty array.
	 * @param mode
	 * @throws UnsupportedOperationException (optional) - If this method was called on an unsupported ChangeMode.
	 * @throws ClassCastException (automatic) - If the delta parameter is of a wrong type.
	 */
	public void change(Event e, final Object delta, final ChangeMode mode);
	
}
