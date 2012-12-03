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

import ch.njol.skript.lang.util.SimpleLiteral;

/**
 * @author Peter Güttinger
 */
public class LiteralList<T> extends ExpressionList<T> implements Literal<T> {
	private static final long serialVersionUID = -5054176694578441222L;
	
	public LiteralList(final Literal<? extends T>[] literals, final boolean and) {
		super(literals, and);
	}
	
	public LiteralList(final Literal<? extends T>[] literals, final boolean and, final LiteralList<?> source) {
		super(literals, and, source);
	}
	
	@Override
	public T[] getArray() {
		return getArray(null);
	}
	
	@Override
	public T getSingle() {
		return getSingle(null);
	}
	
	@Override
	public T[] getAll() {
		return getAll(null);
	}
	
	@Override
	public <R> Literal<? extends R> getConvertedExpression(final Class<R> to) {
		@SuppressWarnings("unchecked")
		final Literal<? extends R>[] exprs = new Literal[expressions.length];
		for (int i = 0; i < exprs.length; i++)
			if ((exprs[i] = (Literal<? extends R>) expressions[i].getConvertedExpression(to)) == null)
				return null;
		return new LiteralList<R>(exprs, and, this);
	}
	
	@Override
	public Literal<? extends T>[] getExpressions() {
		return (Literal<? extends T>[]) super.getExpressions();
	}

	@Override
	public Expression<T> simplify() {
		boolean isSimpleList = true;
		for (int i = 0; i < expressions.length; i++)
			isSimpleList &= expressions[i].isSingle();
		if (isSimpleList) {
			T[] values = (T[]) Array.newInstance(getReturnType(), expressions.length);
			for (int i = 0; i < values.length; i++)
				values[i] = ((Literal<? extends T>) expressions[i]).getSingle();
			return new SimpleLiteral<T>(values, getReturnType(), and);
		}
		return this;
	}
	
}
