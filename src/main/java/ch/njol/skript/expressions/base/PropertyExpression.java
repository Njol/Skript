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

package ch.njol.skript.expressions.base;

import org.bukkit.event.Event;

import ch.njol.skript.classes.Converter;
import ch.njol.skript.classes.Converter.ConverterUtils;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.skript.lang.util.SimpleExpression;

/**
 * Represents an expression which represents a property of another one. Remember to set the expression with {@link #setExpr(Expression)} in
 * {@link #init(Expression[], int, int, ParseResult)}.
 * 
 * @author Peter Güttinger
 */
public abstract class PropertyExpression<F, T> extends SimpleExpression<T> {
	
	private Expression<? extends F> expr;
	
	protected void setExpr(final Expression<? extends F> expr) {
		this.expr = expr;
	}
	
	public Expression<? extends F> getExpr() {
		return expr;
	}
	
	@Override
	protected final T[] get(final Event e) {
		return get(e, expr.getArray(e));
	}
	
	@Override
	public final T[] getAll(final Event e) {
		return get(e, expr.getAll(e));
	}
	
	/**
	 * 
	 * @param e
	 * @param source
	 * @return
	 * @see ConverterUtils#convert(Object[], Class, Converter)
	 */
	protected abstract T[] get(Event e, F[] source);
	
	/**
	 * 
	 * @param source
	 * @param converter must return instances of {@link #getReturnType()}
	 * @return
	 * @throws ArrayStoreException if the converter returned invalid values
	 */
	protected T[] get(final F[] source, final Converter<? super F, ? extends T> converter) {
		return ConverterUtils.convertUnsafe(source, getReturnType(), converter);
	}
	
	@Override
	public final boolean isSingle() {
		return expr.isSingle();
	}
	
	@Override
	public final boolean getAnd() {
		return expr.getAnd();
	}
	
}
