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

import java.util.Iterator;

import org.bukkit.event.Event;

import ch.njol.skript.classes.Changer.ChangeMode;
import ch.njol.skript.classes.Converter;
import ch.njol.skript.classes.SerializableConverter;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.skript.registrations.Converters;
import ch.njol.util.Checker;
import ch.njol.util.Kleenean;

/**
 * Represents a expression converted to another type. This, and not Expression, is the required return type of {@link SimpleExpression#getConvertedExpr(Class)} because this class<br/>
 * <ol>
 * <li>automatically lets the source expression handle everything apart from the get() methods</li>
 * <li>will never convert itself to another type, but rather request a new converted expression from the source expression.</li>
 * </ol>
 * 
 * @author Peter Güttinger
 */
public class ConvertedExpression<F, T> implements Expression<T> {
	private static final long serialVersionUID = -8169038485835998271L;
	
	protected Expression<? extends F> source;
	protected Class<T> to;
	private final Converter<? super F, ? extends T> conv;
	
	public ConvertedExpression(final Expression<? extends F> source, final Class<T> to, final SerializableConverter<? super F, ? extends T> conv) {
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
			return new ConvertedExpression<F, T>(v, to, new SerializableConverter<F, T>() {
				private static final long serialVersionUID = 7203493497281269183L;
				
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
		final SerializableConverter<? super F, ? extends T> c = (SerializableConverter<? super F, ? extends T>) Converters.getConverter(from, to);
		if (c == null)
			return null;
		return new ConvertedExpression<F, T>(v, to, c);
	}
	
	@Override
	public final boolean init(final Expression<?>[] vars, final int matchedPattern, final Kleenean isDelayed, final ParseResult matcher) {
		throw new UnsupportedOperationException();
	}
	
	@Override
	public String toString(final Event e, final boolean debug) {
		if (debug && e == null)
			return "(" + source.toString(e, debug) + ")->" + to.getName();
		return source.toString(e, debug);
	}
	
	@Override
	public String toString() {
		return toString(null, false);
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
	public Class<?>[] acceptChange(final ChangeMode mode) {
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
		return Converters.convert(source.getArray(e), to, conv);
	}
	
	@Override
	public T[] getAll(final Event e) {
		return Converters.convert(source.getAll(e), to, conv);
	}
	
	@Override
	public boolean check(final Event e, final Checker<? super T> c, final boolean negated) {
		return source.check(e, new Checker<F>() {
			@Override
			public boolean check(final F f) {
				final T t = conv.convert(f);
				if (t == null)
					return false;
				return c.check(t);
			}
		}, negated);
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
	
	@Override
	public boolean isLoopOf(final String s) {
		return false;// A loop does not convert the variable to loop
	}
	
	@Override
	public Iterator<T> iterator(final Event e) {
		final Iterator<? extends F> iter = source.iterator(e);
		return new Iterator<T>() {
			
			@Override
			public boolean hasNext() {
				return iter.hasNext();
			}
			
			@Override
			public T next() {
				return conv.convert(iter.next());
			}
			
			@Override
			public void remove() {
				throw new UnsupportedOperationException();
			}
		};
	}
	
	@Override
	public Expression<?> getSource() {
		return source;
	}

	@Override
	public Expression<? extends T> simplify() {
		return source.simplify().getConvertedExpression(to);
	}
	
}
