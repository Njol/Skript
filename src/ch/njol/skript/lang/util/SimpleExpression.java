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

package ch.njol.skript.lang.util;

import java.lang.reflect.Array;
import java.util.Iterator;

import org.bukkit.event.Event;

import ch.njol.skript.ScriptLoader;
import ch.njol.skript.Skript;
import ch.njol.skript.SkriptAPIException;
import ch.njol.skript.classes.Changer.ChangeMode;
import ch.njol.skript.classes.Converter;
import ch.njol.skript.lang.Condition;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.util.Utils;
import ch.njol.util.Checker;
import ch.njol.util.iterator.ArrayIterator;

/**
 * An implementation of the {@link Expression} interface. You should usually extend this class to make a new expression.
 * 
 * @see Skript#registerExpression(Class, Class, String...)
 * 
 * @author Peter Güttinger
 */
public abstract class SimpleExpression<T> implements Expression<T> {
	
	private int time = 0;
	
	protected SimpleExpression() {}
	
	@Override
	public final T getSingle(final Event e) {
		final T[] all = getArray(e);
		if (all.length == 0)
			return null;
		if (all.length > 1)
			throw new SkriptAPIException("Call to getSingle() on a non-single expression");
		return all[0];
	}
	
	@Override
	public T[] getAll(final Event e) {
		final T[] all = get(e);
		if (all == null)
			return (T[]) Array.newInstance(getReturnType(), 0);
		if (all.length == 0)
			return all;
		int numNonNull = 0;
		for (final T t : all)
			if (t != null)
				numNonNull++;
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
	public final T[] getArray(final Event e) {
		final T[] all = get(e);
		if (all == null)
			return (T[]) Array.newInstance(getReturnType(), 0);
		if (all.length == 0)
			return all;
		
		int numNonNull = 0;
		for (final T t : all)
			if (t != null)
				numNonNull++;
		
		if (!getAnd()) {
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
			assert false;
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
	
	/**
	 * This is the internal method to get an expression's values.<br>
	 * To get the expression's value from the outside use {@link #getSingle(Event)} or {@link #getArray(Event)}.
	 * 
	 * @param e The event
	 * @return An array of values for this event. May not contain nulls.
	 */
	protected abstract T[] get(Event e);
	
	@Override
	public final boolean check(final Event e, final Checker<? super T> c, final Condition cond) {
		return check(e, c, cond.isNegated());
	}
	
	@Override
	public final boolean check(final Event e, final Checker<? super T> c) {
		return check(e, c, false);
	}
	
	private final boolean check(final Event e, final Checker<? super T> c, final boolean invert) throws ClassCastException {
		return check(get(e), c, invert, getAnd());
	}
	
	public final static <T> boolean check(final T[] all, final Checker<? super T> c, final boolean invert, final boolean and) {
		if (all == null)
			return false;
		boolean hasElement = false;
		for (final T t : all) {
			if (t == null)
				continue;
			hasElement = true;
			final boolean b = invert ^ c.check(t);
			if (and && !b)
				return false;
			if (!and && b)
				return true;
		}
		if (!hasElement)
			return false;
		return and;
	}
	
	/**
	 * Converts this expression to another type. Unless the expression is special, the default implementation is sufficient.<br/>
	 * This method is guaranteed to never being called with a supertype of the return type of this expression, or the return type itself.
	 * 
	 * @param to The desired return type of the returned expression
	 * @return Expression with the desired return type or null if it can't be converted to the given type
	 * @see Expression#getConvertedExpression(Class)
	 * @see ConvertedExpression#newInstance(Expression, Class)
	 * @see Converter
	 */
	protected <R> ConvertedExpression<T, ? extends R> getConvertedExpr(final Class<R> to) {
		if (to.isAssignableFrom(getReturnType())) {
			throw new SkriptAPIException("invalid call to SimpleExpression.getConvertedExpr (current type: " + getReturnType().getName() + ", requested type: " + to.getName() + ")");
		}
		return ConvertedExpression.newInstance(this, to);
	}
	
	@Override
	@SuppressWarnings("unchecked")
	public final <R> Expression<? extends R> getConvertedExpression(final Class<R> to) {
		if (to.isAssignableFrom(getReturnType()))
			return (Expression<? extends R>) this;
		return this.getConvertedExpr(to);
	}
	
	@Override
	public Class<?> acceptChange(final ChangeMode mode) {
		return null;
	}
	
	@Override
	public void change(final Event e, final Object delta, final ChangeMode mode) throws UnsupportedOperationException {
		throw new UnsupportedOperationException();
	}
	
	/**
	 * {@inheritDoc} <br>
	 * <br>
	 * This implementation sets the time but returns false.
	 * 
	 * @see #setTime(int, Class, Expression...)
	 * @see #setTime(int, Expression, Class...)
	 */
	@Override
	public boolean setTime(final int time) {
		if (ScriptLoader.hasDelayBefore && time != 0) {
			Skript.error("Can't use time states after the event has already passed");
			return false;
		}
		this.time = time;
		return false;
	}
	
	protected final boolean setTime(final int time, final Class<? extends Event> applicableEvent, final Expression<?>... mustbeDefaultVars) {
		if (ScriptLoader.hasDelayBefore && time != 0) {
			Skript.error("Can't use time states after the event has already passed");
			return false;
		}
		if (!Utils.contains(ScriptLoader.currentEvents, applicableEvent))
			return false;
		for (final Expression<?> var : mustbeDefaultVars) {
			if (!var.isDefault()) {
				return false;
			}
		}
		this.time = time;
		return true;
	}
	
	protected final boolean setTime(final int time, final Expression<?> mustbeDefaultVar, final Class<? extends Event>... applicableEvents) {
		if (ScriptLoader.hasDelayBefore && time != 0) {
			Skript.error("Can't use time states after the event has already passed");
			return false;
		}
		if (mustbeDefaultVar.isDefault()) {
			for (final Class<? extends Event> e : applicableEvents) {
				if (Utils.contains(ScriptLoader.currentEvents, e)) {
					this.time = time;
					return true;
				}
			}
		}
		return false;
	}
	
	@Override
	public int getTime() {
		return time;
	}
	
	@Override
	public boolean isDefault() {
		return false;
	}
	
	@Override
	public boolean canLoop() {
		return false;
	}
	
	@Override
	public boolean isLoopOf(final String s) {
		return false;
	}
	
	@Override
	public Iterator<T> iterator(final Event e) {
		return new ArrayIterator<T>(getArray(e));
	}
	
	@Override
	public String toString() {
		return toString(null, false);
	}
	
	@Override
	public Expression<?> getSource() {
		return this;
	}
	
}
