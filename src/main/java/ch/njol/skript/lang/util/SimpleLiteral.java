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

import org.bukkit.event.Event;

import ch.njol.skript.Skript;
import ch.njol.skript.classes.Changer.ChangeMode;
import ch.njol.skript.classes.Converter;
import ch.njol.skript.classes.Converter.ConverterUtils;
import ch.njol.skript.lang.Condition;
import ch.njol.skript.lang.DefaultExpression;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.Literal;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.skript.lang.UnparsedLiteral;
import ch.njol.skript.util.StringMode;
import ch.njol.skript.util.Utils;
import ch.njol.util.Checker;
import ch.njol.util.Validate;
import ch.njol.util.iterator.NonNullIterator;

/**
 * Represents a literal, i.e. a static value like a number or a string.
 * 
 * @author Peter Güttinger
 * @see UnparsedLiteral
 */
public class SimpleLiteral<T> implements Literal<T>, DefaultExpression<T> {
	
	protected final T[] data;
	protected final Class<T> c;
	
	private final boolean isDefault;
	private final boolean and;
	
	private UnparsedLiteral source = null;
	
	public SimpleLiteral(final T[] data, final Class<T> c, final boolean and) {
		Validate.notNullOrEmpty(data, "data");
		Validate.notNull(c, "c");
		this.data = data;
		this.c = c;
		this.and = data.length == 1 || and;
		this.isDefault = false;
	}
	
	public SimpleLiteral(final T data, final boolean isDefault) {
		Validate.notNull(data, "data");
		this.data = (T[]) Array.newInstance(data.getClass(), 1);
		this.data[0] = data;
		c = (Class<T>) data.getClass();
		and = true;
		this.isDefault = isDefault;
	}
	
	public SimpleLiteral(final T[] data, final Class<T> to, final boolean and, final UnparsedLiteral source) {
		this(data, to, and);
		this.source = source;
	}
	
	@Override
	public boolean init(final Expression<?>[] vars, final int matchedPattern, final int isDelayed, final ParseResult parseResult) {
		throw new UnsupportedOperationException();
	}
	
	@Override
	public boolean init() {
		return true;
	}
	
	@Override
	public T[] getArray() {
		return data;
	}
	
	@Override
	public T[] getArray(final Event e) {
		return data;
	}
	
	@Override
	public T[] getAll() {
		return data;
	}
	
	@Override
	public T[] getAll(final Event e) {
		return data;
	}
	
	@Override
	public T getSingle() {
		return Utils.getRandom(data);
	}
	
	@Override
	public T getSingle(final Event e) {
		return getSingle();
	}
	
	@Override
	public Class<T> getReturnType() {
		return c;
	}
	
	@SuppressWarnings("unchecked")
	@Override
	public <R> ConvertedLiteral<T, ? extends R> getConvertedExpression(final Class<R> to) {
		if (to.isAssignableFrom(c))
			return new ConvertedLiteral<T, R>(this, (R[]) data, to);
		final Converter<? super T, ? extends R> p = Skript.getConverter(c, to);
		if (p == null)
			return null;
		final R[] parsedData = ConverterUtils.convert(data, to, p);
		return new ConvertedLiteral<T, R>(this, parsedData, to);
	}
	
	@Override
	public String toString(final Event e, final boolean debug) {
		if (debug)
			return "[" + Skript.toString(data, getAnd(), StringMode.DEBUG, false) + "]";
		return Skript.toString(data, getAnd());
	}
	
	@Override
	public String toString() {
		return toString(null, false);
	}
	
	@Override
	public boolean isSingle() {
		return !getAnd() || data.length == 1;
	}
	
	@Override
	public boolean isDefault() {
		return isDefault;
	}
	
	@Override
	public boolean check(final Event e, final Checker<? super T> c, final Condition cond) {
		return SimpleExpression.check(data, c, cond.isNegated(), getAnd());
	}
	
	@Override
	public boolean check(final Event e, final Checker<? super T> c) {
		return SimpleExpression.check(data, c, false, getAnd());
	}
	
	@Override
	public Class<?> acceptChange(final ChangeMode mode) {
		return null;
	}
	
	@Override
	public void change(final Event e, final Object delta, final ChangeMode mode) throws UnsupportedOperationException {
		throw new UnsupportedOperationException();
	}
	
	@Override
	public boolean getAnd() {
		return and;
	}
	
	@Override
	public boolean setTime(final int time) {
		return false;
	}
	
	@Override
	public int getTime() {
		return 0;
	}
	
	@Override
	public NonNullIterator<T> iterator(final Event e) {
		return new NonNullIterator<T>() {
			private int i = 0;
			
			@Override
			protected T getNext() {
				if (i == data.length)
					return null;
				return data[i++];
			}
		};
	}
	
	@Override
	public boolean isLoopOf(final String s) {
		return false;
	}
	
	@Override
	public boolean canLoop() {
		return !isSingle();
	}
	
	@Override
	public Expression<?> getSource() {
		return source == null ? this : source;
	}
	
}
