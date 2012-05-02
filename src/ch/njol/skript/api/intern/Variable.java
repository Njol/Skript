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

package ch.njol.skript.api.intern;

import java.lang.reflect.Array;
import java.util.Iterator;
import java.util.NoSuchElementException;

import org.bukkit.event.Event;

import ch.njol.skript.Skript;
import ch.njol.skript.api.Changer.ChangeMode;
import ch.njol.skript.api.Condition;
import ch.njol.skript.api.Converter;
import ch.njol.skript.api.Debuggable;
import ch.njol.skript.api.Registerable;
import ch.njol.skript.util.Utils;
import ch.njol.util.Checker;
import ch.njol.util.iterator.ArrayIterator;

/**
 * Represents a variable. Variables are used within conditions, effects and other variables.
 * 
 * @author Peter Güttinger
 * @see Skript#addVariable(Class, Class, String...)
 * @see Expression
 */
public abstract class Variable<T> implements Expression, Debuggable, Registerable {
	
	/**
	 * 
	 */
	protected boolean and = true;
	
	protected Variable() {}
	
	/**
	 * You rarely need this, use {@link #get(Event, boolean)} if you don't explicitly need an array.
	 * 
	 * @param e
	 * @return
	 */
	@SuppressWarnings("unchecked")
	public final T[] get(final Event e) {
		final T[] all = getAll(e);
		if (all == null)
			return (T[]) Array.newInstance(this.getReturnType(), 1);
		if (and || all.length <= 1)
			return all;
		final T r = Utils.getRandom(all);
		final T[] one = (T[]) Array.newInstance(this.getReturnType(), 1);
		one[0] = r;
		return one;
	}
	
	/**
	 * Gets the values from another variable.
	 * 
	 * @param e
	 * @param v
	 * @param converter Converter/getter to get the values.
	 * @param includeNull does not affect the output, but the input to the converter. If true, null elements won't be converted but left blank (=null) in the returned array.
	 * @return
	 */
	@SuppressWarnings("unchecked")
	protected final <V> T[] get(final Event e, final Variable<V> v, final Converter<? super V, ? extends T> converter, final boolean includeNull) {
		final V[] vs = v.get(e);
		final T[] ts = (T[]) Array.newInstance(getReturnType(), vs.length);
		for (int i = 0; i < vs.length; i++) {
			if (vs[i] != null || includeNull)
				ts[i] = converter.convert(vs[i]);
		}
		return ts;
	}
	
	/**
	 * Returns an Iterable to be able to loop through the values of this variable. You should use {@link #check(Event, Checker, Condition)} within conditions, not this function.
	 * 
	 * @param e
	 * @param includeNull whether the returned Iterator should include null elements or skip them. Setting this to true might result in no values being looped at all.
	 * @return An Iterable to loop through all values of the event.
	 */
	public final Iterable<T> get(final Event e, final boolean includeNull) {
		return new Iterable<T>() {
			
			private final T[] ts = get(e);
			
			@Override
			public Iterator<T> iterator() {
				if (includeNull)
					return new ArrayIterator<T>(ts);
				
				return new Iterator<T>() {
					
					private int i = 0;
					
					@Override
					public boolean hasNext() {
						while (i < ts.length && ts[i] == null)
							i++;
						return i < ts.length;
					}
					
					@Override
					public T next() {
						if (i >= ts.length)
							throw new NoSuchElementException();
						return ts[i++];
					}
					
					@Override
					public void remove() {
						throw new UnsupportedOperationException();
					}
					
				};
			}
		};
	}
	
	/**
	 * Gets the first value of this variable. Useful if you don't expect multiple values, e.g. setting a block to multiple ids makes no sense.
	 * 
	 * @param e
	 * @return the first value
	 */
	public final T getFirst(final Event e) {
		final T[] all = getAll(e);
		if (all == null || all.length < 1)
			return null;
		return all[0];
	}
	
	/**
	 * This is the internal method to get a variable's values.
	 * To get the variable's value from the outside use {@link #get(Event, boolean)}.
	 * 
	 * @param e
	 * @return An array holding all values. Must not be null.
	 */
	protected abstract T[] getAll(Event e);
	
	/**
	 * Checks through the values to find whether this variable matches the checker.
	 * 
	 * @param e
	 * @param c
	 * @param cond
	 * @param includeNull
	 * @return
	 */
	public final boolean check(final Event e, final Checker<? super T> c, final Condition cond, final boolean includeNull) {
		return check(e, c, cond.isNegated(), includeNull);
	}
	
	public final boolean check(final Event e, final Checker<? super T> c, final boolean includeNull) {
		return check(e, c, false, includeNull);
	}
	
	private final boolean check(final Event e, final Checker<? super T> c, final boolean invert, final boolean includeNull) {
		for (final T o : getAll(e)) {
			final boolean b = includeNull ? c.check(o) : (o == null ? false : c.check(o));
			if (invert) {
				if (and && b)
					return false;
				if (!and && !b)
					return true;
			} else {
				if (and && !b)
					return false;
				if (!and && b)
					return true;
			}
		}
		return and;
	}
	
	/**
	 * Converts this Variable to another type. Unless the variable is special, the default implementation is sufficient.<br/>
	 * This method is guaranteed to never being called with a supertype of the return type of this variable, or the return type itself.
	 * 
	 * @param to the desired return type of the returned variable
	 * @return variable with the desired return type or null if it can't be converted to the given type
	 * @see SimpleConvertedVariable#newInstance(Variable, Class)
	 * @see Converter
	 * @see Variable#getConvertedVariable(Class)
	 */
	protected <R> ConvertedVariable<? extends R> getConvertedVar(final Class<R> to) {
		if (to.isAssignableFrom(getReturnType())) {
			throw new SkriptAPIException("invalid call to Variable.getConvertedVar (return type=" + getReturnType().getName() + ", parameter=" + to.getName() + ")");
		}
		return SimpleConvertedVariable.newInstance(this, to);
	}
	
	/**
	 * Tries to convert this variable to the given type.
	 * 
	 * @param to the desired return type of the returned variable
	 * @return Variable with the desired return type or null if the variable can't be converted to the given type. Returns the variable itself if it already returns the desired
	 *         type.
	 * @see SimpleConvertedVariable#newInstance(Variable, Class)
	 * @see Converter
	 * @see Variable#getConvertedVar(Class)
	 */
	@SuppressWarnings("unchecked")
	public final <R> Variable<? extends R> getConvertedVariable(final Class<R> to) {
		if (to.isAssignableFrom(this.getReturnType()))
			return (Variable<? extends R>) this;
		return this.getConvertedVar(to);
	}
	
	public static final <T> Variable<? extends T> parse(final String s, final Class<T> returnType) {
		return parse(s, returnType, Skript.getVariables().listIterator());
	}
	
	public static final Variable<?> parseNoLiteral(final String s, final Iterator<? extends VariableInfo<?>> source) {
		return (Variable<?>) Expressions.parse(s, source);
	}
	
	@SuppressWarnings("unchecked")
	public static final <T> Variable<? extends T> parse(final String s, final Class<T> returnType, final Iterator<? extends VariableInfo<?>> source) {
		
		final Variable<?> v = parseNoLiteral(s, source);
		if (v != null) {
			if (returnType.isAssignableFrom(v.getReturnType()))
				return (Variable<? extends T>) v;
			final Variable<? extends T> w = v.getConvertedVariable(returnType);
			if (w != null)
				return w;
			Skript.setErrorCause(v + " is not " + Utils.addUndefinedArticle(returnType.getSimpleName()), false);
			return null;
		}
		
		// TODO variable array (e.g. "set block above and block below to air")
		
		if (returnType == Object.class)
			return (Variable<? extends T>) Expressions.parseUnparsedLiteral(s);
		final Variable<? extends T> t = Expressions.parseLiteral(s, returnType);
		if (t != null)
			return t;
		
		Skript.setErrorCause("'" + s + "' is not " + Utils.addUndefinedArticle(returnType.getSimpleName()), false);
		return null;
	}
	
	/**
	 * this is set automatically and should not be changed.
	 * 
	 * @param and
	 */
	public void setAnd(final boolean and) {
		this.and = and;
	}
	
	/**
	 * Returns this variable's string representation destined for the end user.
	 */
	@Override
	public abstract String toString();
	
	/**
	 * Gets the return type of this variable.
	 * 
	 * @return a Class instance <code>c</code> which must fulfill <code>c.{@link Class#isInstance(Object) isInstance}(g)</code> for every <code>g</code> returned by
	 *         {@link #getAll(Event)}.
	 *         This is never checked and will generate ClassCastExceptions at runtime if invalid.
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
	public void change(final Event e, final Variable<?> delta, final ChangeMode mode) throws UnsupportedOperationException {
		throw new UnsupportedOperationException();
	}
	
	/**
	 * tests whether this variable supports the given mode, and if yes what type it expects the <code>delta</code> to be.<br/>
	 * The default implementation returns null, i.e. it rejects any change attempts to this variable.
	 * 
	 * @param mode
	 * @return the type that {@link #change(Event, Variable, ChangeMode)} accepts as it's <code>delta</code> parameter,
	 *         or null if the given mode is not supported. For {@link ChangeMode#CLEAR} this can return any non-null class instance to mark clear as supported.
	 */
	public Class<?> acceptChange(final ChangeMode mode) {
		return null;
	}
	
}
