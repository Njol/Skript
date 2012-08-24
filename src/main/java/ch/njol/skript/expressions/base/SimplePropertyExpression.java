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

/**
 * @author Peter Güttinger
 * 
 */
public abstract class SimplePropertyExpression<F, T> extends PropertyExpression<F, T> {
	
	/**
	 * 
	 * @param c
	 * @param type
	 * @param property
	 * @param fromType Should be plural but doesn't have to be
	 */
	protected static <T> void register(final Class<? extends SimplePropertyExpression<?, T>> c, final Class<T> type, final String property, final String fromType) {
		Skript.registerExpression(c, type, ExpressionType.PROPERTY, "[the] " + property + " of %" + fromType + "%", "%" + fromType + "%'[s] " + property);
	}
	
	private final Class<T> returnType;
	private final String propertyName;
	private final Converter<? super F, ? extends T> converter;
	
	public SimplePropertyExpression(final Class<T> returnType, final String propertyName, final Converter<? super F, ? extends T> converter) {
		this.returnType = returnType;
		this.propertyName = propertyName;
		this.converter = converter;
	}
	
	@SuppressWarnings("unchecked")
	@Override
	public boolean init(final Expression<?>[] exprs, final int matchedPattern, final int isDelayed, final ParseResult parseResult) {
		setExpr((Expression<? extends F>) exprs[0]);
		return true;
	}
	
	@Override
	public Class<T> getReturnType() {
		return returnType;
	}
	
	@Override
	protected T[] get(final Event e, final F[] source) {
		return super.get(source, converter);
	}
	
	@Override
	public String toString(final Event e, final boolean debug) {
		return "the " + propertyName + " of " + getExpr().toString(e, debug);
	}
}
