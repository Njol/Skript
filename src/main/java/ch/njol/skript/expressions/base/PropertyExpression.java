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

import ch.njol.skript.Skript;
import ch.njol.skript.Skript.ExpressionType;
import ch.njol.skript.classes.Converter;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.skript.lang.util.SimpleExpression;
import ch.njol.skript.registrations.Converters;

/**
 * Represents an expression which represents a property of another one. Remember to set the expression with {@link #setExpr(Expression)} in
 * {@link #init(Expression[], int, int, ParseResult)}.
 * 
 * @author Peter Güttinger
 */
public abstract class PropertyExpression<F, T> extends SimpleExpression<T> {
	private static final long serialVersionUID = 8485116870998540931L;
	
	/**
	 * 
	 * @param c
	 * @param type
	 * @param property The name of the property
	 * @param fromType Should be plural but doesn't have to be
	 */
	public static <T> void register(final Class<? extends Expression<T>> c, final Class<T> type, final String property, final String fromType) {
		Skript.registerExpression(c, type, ExpressionType.PROPERTY, "[the] " + property + " of %" + fromType + "%", "%" + fromType + "%'[s] " + property);
	}
	
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
	 * @see Converters#convert(Object[], Class, Converter)
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
		return Converters.convertUnsafe(source, getReturnType(), converter);
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
