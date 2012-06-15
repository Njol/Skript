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
import ch.njol.skript.api.Changer.ChangeMode;
import ch.njol.skript.api.Converter;
import ch.njol.skript.api.intern.ConvertedExpression;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.SimpleExpression;
import ch.njol.util.Validate;

/**
 * Represents an expression which is a wrapper of another one. To use, set the protected field {@link #expr} to the expression you want to wrap.<br/>
 * If you don't override {@link #getConvertedExpr(Class)} you should not override any other methods of this class.
 * 
 * @author Peter Güttinger
 */
public abstract class WrapperExpression<T> extends SimpleExpression<T> {
	
	/** the wrapped expression */
	protected Expression<? extends T> expr;
	
	protected WrapperExpression() {}
	
	public WrapperExpression(final SimpleExpression<? extends T> var) {
		Validate.notNull(var);
		this.expr = var;
	}
	
	@SuppressWarnings("unchecked")
	@Override
	protected <R> ConvertedExpression<T, ? extends R> getConvertedExpr(final Class<R> to) {
		final Converter<? super T, ? extends R> conv = (Converter<? super T, ? extends R>) Skript.getConverter(getReturnType(), to);
		if (conv == null)
			return null;
		return new ConvertedExpression<T, R>(expr, to, conv) {
			@Override
			public String getDebugMessage(final Event e) {
				return "{" + WrapperExpression.this.getDebugMessage(e) + "}->" + to.getName();
			}
		};
	}
	
	@Override
	protected T[] getAll(final Event e) {
		return expr.getArray(e);
	}
	
	@Override
	public boolean isSingle() {
		return expr.isSingle();
	}
	
	@Override
	public void setAnd(final boolean and) {
		super.setAnd(and);
		expr.setAnd(and);
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
	public String toString() {
		return expr.toString();
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
