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

import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.SimpleExpression;
import ch.njol.skript.lang.SkriptParser.ParseResult;

/**
 * Represents an expression which represents a property of another one. Remember to set the expression with {@link #setExpr(Expression)} in
 * {@link #init(Expression[], int, ParseResult)}.
 * 
 * @author Peter Güttinger
 * 
 */
public abstract class PropertyExpression<T> extends SimpleExpression<T> {
	
	private Expression<?> expr;
	
	protected void setExpr(final Expression<?> expr) {
		this.expr = expr;
	}
	
	public Expression<?> getExpr() {
		return expr;
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
