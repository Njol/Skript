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
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.NoSuchElementException;

import org.bukkit.event.Event;

import ch.njol.skript.classes.Changer.ChangeMode;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.skript.lang.util.SimpleExpression;
import ch.njol.skript.util.Utils;
import ch.njol.util.Checker;

/**
 * Used for lists of expressions, i.e. expr1, expr2, ..., and/or exprN
 * 
 * @author Peter Güttinger
 */
public class ExpressionList<T> implements Expression<T> {
	private static final long serialVersionUID = -9121024064780836522L;
	
	protected final Expression<? extends T>[] expressions;
	protected final boolean and;
	private final boolean single;
	private final Class<T> returnType;
	private ExpressionList<?> source;
	
	public ExpressionList(final Expression<? extends T>[] expressions, final boolean and) {
		this(expressions, and, null);
	}
	
	protected ExpressionList(final Expression<? extends T>[] expressions, final boolean and, final ExpressionList<?> source) {
		assert expressions != null && expressions.length > 1;
		this.expressions = expressions;
		this.and = and;
		final Class<?>[] cs = new Class[expressions.length];
		for (int i = 0; i < cs.length; i++)
			cs[i] = expressions[i].getReturnType();
		final Class<T> returnType = (Class<T>) Utils.getSuperType(cs);
		this.returnType = returnType;
		if (and) {
			single = false;
		} else {
			boolean single = true;
			for (final Expression<?> e : expressions) {
				if (!e.isSingle()) {
					single = false;
					break;
				}
			}
			this.single = single;
		}
		this.source = source;
	}
	
	@Override
	public boolean init(final Expression<?>[] exprs, final int matchedPattern, final int isDelayed, final ParseResult parseResult) {
		throw new UnsupportedOperationException();
	}
	
	@Override
	public T getSingle(final Event e) {
		if (!single)
			throw new UnsupportedOperationException();
		for (final int i : Utils.permutation(expressions.length)) {
			final T t = expressions[i].getSingle(e);
			if (t != null)
				return t;
		}
		return null;
	}
	
	@Override
	public T[] getArray(final Event e) {
		if (and) {
			final ArrayList<T> r = new ArrayList<T>();
			for (final Expression<? extends T> expr : expressions)
				r.addAll(Arrays.asList(expr.getArray(e)));
			return r.toArray((T[]) Array.newInstance(returnType, r.size()));
		}
		for (final int i : Utils.permutation(expressions.length)) {
			final T[] t = expressions[i].getArray(e);
			if (t.length > 0)
				return t;
		}
		return null;
	}
	
	@Override
	public T[] getAll(final Event e) {
		final ArrayList<T> r = new ArrayList<T>();
		for (final Expression<? extends T> expr : expressions)
			r.addAll(Arrays.asList(expr.getAll(e)));
		return r.toArray((T[]) Array.newInstance(returnType, r.size()));
	}
	
	@Override
	public Iterator<? extends T> iterator(final Event e) {
		if (!and) {
			for (final int i : Utils.permutation(expressions.length)) {
				final Iterator<? extends T> t = expressions[i].iterator(e);
				if (t.hasNext())
					return t;
			}
			return null;
		}
		return new Iterator<T>() {
			private int i = 0;
			private Iterator<? extends T> current = expressions[0].iterator(e);
			
			@Override
			public boolean hasNext() {
				while (i + 1 < expressions.length && !current.hasNext())
					current = expressions[++i].iterator(e);
				return current.hasNext();
			}
			
			@Override
			public T next() {
				if (!hasNext())
					throw new NoSuchElementException();
				return current.next();
			}
			
			@Override
			public void remove() {
				throw new UnsupportedOperationException();
			}
		};
	}
	
	@Override
	public boolean isSingle() {
		return single;
	}
	
	@Override
	public boolean check(final Event e, final Checker<? super T> c, final Condition cond) {
		return SimpleExpression.check(getAll(e), c, cond.isNegated(), and);
	}
	
	@Override
	public boolean check(final Event e, final Checker<? super T> c) {
		return SimpleExpression.check(getAll(e), c, false, and);
	}
	
	@Override
	public <R> Expression<? extends R> getConvertedExpression(final Class<R> to) {
		@SuppressWarnings("unchecked")
		final Expression<? extends R>[] exprs = new Expression[expressions.length];
		for (int i = 0; i < exprs.length; i++)
			if ((exprs[i] = expressions[i].getConvertedExpression(to)) == null)
				return null;
		return new ExpressionList<R>(exprs, and, this);
	}
	
	@Override
	public Class<T> getReturnType() {
		return returnType;
	}
	
	@Override
	public boolean getAnd() {
		return and;
	}
	
	@Override
	public Class<?>[] acceptChange(final ChangeMode mode) {
		return null; // TODO change?
	}
	
	@Override
	public void change(final Event e, final Object delta, final ChangeMode mode) throws UnsupportedOperationException {
		throw new UnsupportedOperationException();
	}
	
	@Override
	public boolean setTime(final int time) {
		return false; // TODO change?
	}
	
	@Override
	public int getTime() {
		return 0;
	}
	
	@Override
	public boolean isDefault() {
		return false;
	}
	
	@Override
	public boolean isLoopOf(final String s) {
		for (final Expression<?> e : expressions)
			if (e.isLoopOf(s))
				return true;
		return false;
	}
	
	@Override
	public Expression<?> getSource() {
		return source == null ? this : source;
	}
	
	@Override
	public String toString(final Event e, final boolean debug) {
		final StringBuilder b = new StringBuilder();
		for (int i = 0; i < expressions.length; i++) {
			if (i != 0) {
				if (i == expressions.length - 1)
					b.append(and ? " and " : " or ");
				else
					b.append(", ");
			}
			b.append(expressions[i].toString(e, debug));
		}
		return b.toString();
	}
	
	@Override
	public String toString() {
		return toString(null, false);
	}
	
	public Expression<? extends T>[] getExpressions() {
		return expressions;
	}
}
