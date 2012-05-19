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

import java.lang.reflect.Array;
import java.util.Arrays;

import org.bukkit.event.Event;

import ch.njol.skript.Skript;
import ch.njol.skript.api.Changer.ChangeMode;
import ch.njol.skript.api.Condition;
import ch.njol.skript.api.Converter;
import ch.njol.skript.api.intern.ConvertedVariable;
import ch.njol.skript.api.intern.SimpleConvertedVariable;
import ch.njol.skript.api.intern.SkriptAPIException;
import ch.njol.util.Checker;

/**
 * Represents a variable. Variables are used within conditions, effects and other variables.
 * 
 * @author Peter Güttinger
 * @see Skript#addVariable(Class, Class, String...)
 * @see Expression
 */
public abstract class SimpleVariable<T> implements Variable<T> {
	
	private boolean and = true;
	
	protected SimpleVariable() {}
	
	@Override
	public final T getSingle(final Event e) {
		final T[] all = getArray(e);
		if (all.length == 0)
			return null;
		if (all.length > 1)
			throw new SkriptAPIException("Call to getSingle() on a non-single variable");
		return all[0];
	}
	
	@Override
	@SuppressWarnings("unchecked")
	public final T[] getArray(final Event e) {
		final T[] all = getAll(e);
		if (all == null)
			return (T[]) Array.newInstance(getReturnType(), 0);
		if (all.length == 0)
			return all;
		
		int numNonNull = 0;
		for (final T t : all)
			if (t != null)
				numNonNull++;
		
		if (!and) {
			if (all.length == 1 && all[0] != null)
				return all;
			int rand = Skript.random.nextInt(numNonNull);
			final T[] one = (T[]) Array.newInstance(getReturnType(), 1);
			for (final T t : all) {
				if (t != null) {
					if (rand == 0) {
						one[0] = t;
						return one;
					}
					rand--;
				}
			}
			throw new RuntimeException();// shouldn't happen
		}
		
		if (numNonNull == all.length)
			return all;
		final T[] r = (T[]) Array.newInstance(getReturnType(), numNonNull);
		int i = 0;
		for (final T t : all)
			if (t != null)
				r[i++] = t;
		return r;
	}
	
	@Override
	public final <V> V getSingle(final Event e, final Converter<? super T, ? extends V> converter) {
		return converter.convert(getSingle(e));
	}
	
	@Override
	@SuppressWarnings("unchecked")
	public final <V> V[] getArray(final Event e, final Class<V> to, final Converter<? super T, ? extends V> converter) {
		final T[] ts = getArray(e);
		final V[] vs = (V[]) Array.newInstance(to, ts.length);
		int j = 0;
		for (int i = 0; i < vs.length; i++) {
			final V v = converter.convert(ts[i]);
			if (v == null)
				continue;
			vs[j++] = v;
		}
		if (j != vs.length - 1)
			return Arrays.copyOf(vs, j + 1);
		return vs;
	}
	
	/**
	 * This is the internal method to get a variable's values.
	 * To get the variable's value from the outside use {@link #getSingle(Event, boolean)}.
	 * 
	 * @param e
	 * @return
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
	@Override
	public final boolean check(final Event e, final Checker<? super T> c, final Condition cond) {
		return check(e, c, cond.isNegated());
	}
	
	@Override
	public final boolean check(final Event e, final Checker<? super T> c) {
		return check(e, c, false);
	}
	
	private final boolean check(final Event e, final Checker<? super T> c, final boolean invert) throws ClassCastException {
		boolean hasElement = false;
		boolean hasNonNullElement = false;
		for (final T t : getAll(e)) {
			hasElement = true;
			if (t != null)
				hasNonNullElement = true;
			final boolean b = (t == null ? false : c.check(t));
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
		if (!hasElement)
			return false;
		if (!hasNonNullElement)
			return invert;
		return and;
	}
	
	/**
	 * Converts this Variable to another type. Unless the variable is special, the default implementation is sufficient.<br/>
	 * This method is guaranteed to never being called with a supertype of the return type of this variable, or the return type itself.
	 * 
	 * @param to the desired return type of the returned variable
	 * @return variable with the desired return type or null if it can't be converted to the given type
	 * @see SimpleConvertedVariable#newInstance(SimpleVariable, Class)
	 * @see Converter
	 * @see SimpleVariable#getConvertedVariable(Class)
	 */
	protected <R> ConvertedVariable<? extends R> getConvertedVar(final Class<R> to) {
		if (to.isAssignableFrom(getReturnType())) {
			throw new SkriptAPIException("invalid call to Variable.getConvertedVar (current type: " + getReturnType().getName() + ", requested type: " + to.getName() + ")");
		}
		return SimpleConvertedVariable.newInstance(this, to);
	}
	
	/**
	 * Tries to convert this variable to the given type.
	 * 
	 * @param to the desired return type of the returned variable
	 * @return Variable with the desired return type or null if the variable can't be converted to the given type. Returns the variable itself if it already returns the desired
	 *         type.
	 * @see Converter
	 */
	@Override
	@SuppressWarnings("unchecked")
	public final <R> SimpleVariable<? extends R> getConvertedVariable(final Class<R> to) {
		if (to.isAssignableFrom(getReturnType()))
			return (SimpleVariable<? extends R>) this;
		return this.getConvertedVar(to);
	}
	
	@Override
	public boolean getAnd() {
		return and;
	}
	
	/**
	 * this is set automatically and should not be changed.
	 * 
	 * @param and
	 */
	@Override
	public void setAnd(final boolean and) {
		this.and = and;
	}
	
	@Override
	public void change(final Event e, final Variable<?> delta, final ChangeMode mode) throws UnsupportedOperationException {
		throw new UnsupportedOperationException();
	}
	
	@Override
	public Class<?> acceptChange(final ChangeMode mode) {
		return null;
	}
	
}
