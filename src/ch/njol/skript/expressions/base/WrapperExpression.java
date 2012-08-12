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
import ch.njol.skript.classes.Changer.ChangeMode;
import ch.njol.skript.classes.Converter;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.skript.lang.util.ConvertedExpression;
import ch.njol.skript.lang.util.SimpleExpression;
import ch.njol.util.Validate;
import ch.njol.util.iterator.NonNullIterator;

/**
 * Represents an expression which is a wrapper of another one. Remember to set the wrapped expression in the constructor ({@link #WrapperExpression(SimpleExpression)})
 * or with {@link #setExpr(Expression)} in {@link #init(Expression[], int, boolean, ParseResult)}.<br/>
 * If you override {@link #get(Event)} you must override {@link #iterator(Event)} as well.
 * 
 * @author Peter Güttinger
 */
public abstract class WrapperExpression<T> extends SimpleExpression<T> {
	
	private Expression<? extends T> expr;
	
	protected WrapperExpression() {}
	
	public WrapperExpression(final SimpleExpression<? extends T> expr) {
		Validate.notNull(expr);
		setExpr(expr);
	}
	
	protected void setExpr(final Expression<? extends T> expr) {
		this.expr = expr;
	}
	
	public Expression<?> getExpr() {
		return expr;
	}
	
	@Override
	protected <R> ConvertedExpression<T, ? extends R> getConvertedExpr(final Class<R> to) {
		final Converter<? super T, ? extends R> conv = (Converter<? super T, ? extends R>) Skript.getConverter(getReturnType(), to);
		if (conv == null)
			return null;
		return new ConvertedExpression<T, R>(expr, to, conv) {
			@Override
			public String toString(final Event e, final boolean debug) {
				if (debug && e == null)
					return "(" + WrapperExpression.this.toString(e, debug) + ")->" + to.getName();
				return WrapperExpression.this.toString(e, debug);
			}
		};
	}
	
	@Override
	protected T[] get(final Event e) {
		return expr.getArray(e);
	}
	
	@Override
	public NonNullIterator<T> iterator(final Event e) {
		return (NonNullIterator<T>) expr.iterator(e);
	}
	
	@Override
	public boolean isSingle() {
		return expr.isSingle();
	}
	
	@Override
	public boolean getAnd() {
		return expr.getAnd();
	}
	
	@Override
	public Class<? extends T> getReturnType() {
		return expr.getReturnType();
	}
	
	@Override
	public Class<?> acceptChange(final ChangeMode mode) {
		return expr.acceptChange(mode);
	}
	
	@Override
	public void change(final Event e, final Object delta, final ChangeMode mode) {
		expr.change(e, delta, mode);
	}
	
	@Override
	public boolean setTime(final int time) {
		return expr.setTime(time);
	}
	
	@Override
	public int getTime() {
		return expr.getTime();
	}
	
	@Override
	public boolean isDefault() {
		return expr.isDefault();
	}
	
}
