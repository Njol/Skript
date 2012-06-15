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

import org.bukkit.event.Event;

import ch.njol.skript.Skript;
import ch.njol.skript.api.Changer.ChangeMode;
import ch.njol.skript.api.Condition;
import ch.njol.skript.api.Converter;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.SimpleExpression;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.util.Checker;

/**
 * Represents a expression converted to another type. This, and not Expression, is the required return type of {@link SimpleExpression#getConvertedExpr(Class)} because this class<br/>
 * <ol>
 * <li>automatically lets the source expression handle everything apart from the get() methods</li>
 * <li>will never convert itself to another type, but rather request a new converted expression from the source expression.</li>
 * </ol>
 * 
 * @author Peter Güttinger
 * 
 */
public class ConvertedExpression<F, T> implements Expression<T> {
	
	protected Expression<? extends F> source;
	protected Class<T> to;
	private final Converter<? super F, ? extends T> conv;
	
	public ConvertedExpression(final Expression<? extends F> source, final Class<T> to, final Converter<? super F, ? extends T> conv) {
		this.source = source;
		this.to = to;
		this.conv = conv;
	}
	
	public static <F, T> ConvertedExpression<F, T> newInstance(final Expression<F> v, final Class<T> to) {
		if (v == null || to == null || to.isAssignableFrom(v.getReturnType()))
			return null;
		return newInstance(v, v.getReturnType(), to);
	}
	
	@SuppressWarnings("unchecked")
	private static <F, T> ConvertedExpression<F, T> newInstance(final Expression<F> v, final Class<? extends F> from, final Class<T> to) {
		if (from.isAssignableFrom(to)) {
			return new ConvertedExpression<F, T>(v, to, new Converter<F, T>() {
				@Override
				public T convert(final F f) {
					if (to.isInstance(f))
						return (T) f;
					return null;
				}
			});
		}
		// casting <? super ? extends F> to <? super F> is wrong, but since the converter is only used for values returned by the expression
		// (which are instances of "<? extends F>") this won't result in any ClassCastExceptions.
		final Converter<? super F, ? extends T> c = (Converter<? super F, ? extends T>) Skript.getConverter(from, to);
		if (c == null)
			return null;
		return new ConvertedExpression<F, T>(v, to, c);
	}
	
	@Override
	public final boolean init(final Expression<?>[] vars, final int matchedPattern, final ParseResult matcher) {
		throw new UnsupportedOperationException();
	}
	
	@Override
	public String getDebugMessage(final Event e) {
		if (e == null)
			return "{" + source.getDebugMessage(e) + "}->" + to.getName();
		return source.getDebugMessage(e);
	}
	
	@Override
	public String toString() {
		return source.toString();
	}
	
	@Override
	public Class<T> getReturnType() {
		return to;
	}
	
	@Override
	public boolean isSingle() {
		return source.isSingle();
	}
	
	@SuppressWarnings("unchecked")
	@Override
	public <R> Expression<? extends R> getConvertedExpression(final Class<R> to) {
		if (to.isAssignableFrom(this.to))
			return (Expression<? extends R>) this;
		return source.getConvertedExpression(to);
	}
	
	@Override
	public void setAnd(final boolean and) {
		source.setAnd(and);
	}
	
	@Override
	public Class<?> acceptChange(final ChangeMode mode) {
		return source.acceptChange(mode);
	}
	
	@Override
	public void change(final Event e, final Object delta, final ChangeMode mode) {
		source.change(e, delta, mode);
	}
	
	@Override
	public T getSingle(final Event e) {
		final F f = source.getSingle(e);
		if (f == null)
			return null;
		return conv.convert(f);
	}
	
	@Override
	public T[] getArray(final Event e) {
		return source.getArray(e, to, conv);
	}
	
	@Override
	public <V> V getSingle(final Event e, final Converter<? super T, ? extends V> converter) {
		final T t = getSingle(e);
		if (t == null)
			return null;
		return converter.convert(t);
	}
	
	@Override
	public <V> V[] getArray(final Event e, final Class<V> to, final Converter<? super T, ? extends V> converter) {
		return source.getArray(e, to, new ChainedConverter<F, T, V>(conv, converter));
	}
	
	@Override
	public boolean check(final Event e, final Checker<? super T> c, final Condition cond) {
		return source.check(e, new Checker<F>() {
			@Override
			public boolean check(final F f) {
				final T t = conv.convert(f);
				if (t == null)
					return false;
				return c.check(t);
			}
		}, cond);
	}
	
	@Override
	public boolean check(final Event e, final Checker<? super T> c) {
		return source.check(e, new Checker<F>() {
			@Override
			public boolean check(final F f) {
				final T t = conv.convert(f);
				if (t == null)
					return false;
				return c.check(t);
			}
		});
	}
	
	@Override
	public boolean getAnd() {
		return source.getAnd();
	}
	
	@Override
	public boolean setTime(final int time) {
		return source.setTime(time);
	}
	
	@Override
	public int getTime() {
		return source.getTime();
	}
	
	@Override
	public boolean isDefault() {
		return source.isDefault();
	}
}
