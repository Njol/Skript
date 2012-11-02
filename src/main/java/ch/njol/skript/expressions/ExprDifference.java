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

package ch.njol.skript.expressions;

import java.lang.reflect.Array;

import org.bukkit.event.Event;

import ch.njol.skript.Skript;
import ch.njol.skript.Skript.ExpressionType;
import ch.njol.skript.classes.Arithmetic;
import ch.njol.skript.classes.ClassInfo;
import ch.njol.skript.conditions.CondIs;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.Literal;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.skript.lang.Variable;
import ch.njol.skript.lang.util.SimpleExpression;
import ch.njol.skript.log.ErrorQuality;
import ch.njol.skript.registrations.Classes;

/**
 * @author Peter Güttinger
 */
public class ExprDifference extends SimpleExpression<Object> {
	private static final long serialVersionUID = -394336424028012141L;
	
	static {
		Skript.registerExpression(ExprDifference.class, Object.class, ExpressionType.COMBINED, "difference (between|of) %object% and %object%");
	}
	
	private Expression<?> first, second;
	
	private Arithmetic<?, ?> math;
	private Class<?> relativeType;
	
	@Override
	public boolean init(final Expression<?>[] exprs, final int matchedPattern, final int isDelayed, final ParseResult parseResult) {
		first = exprs[0];
		second = exprs[1];
		final ClassInfo<?> ci;
		if (first instanceof Variable && second instanceof Variable) {
			ci = Classes.getExactClassInfo(Double.class);
			first = first.getConvertedExpression(Double.class);
			second = second.getConvertedExpression(Double.class);
		} else if (first instanceof Literal<?> && second instanceof Literal<?>) {
			first = first.getConvertedExpression(Object.class);
			second = second.getConvertedExpression(Object.class);
			if (first == null || second == null)
				return false;
			ci = Classes.getSuperClassInfo(second.getReturnType().isAssignableFrom(first.getReturnType()) ? second.getReturnType() : first.getReturnType());
		} else {
			if (first instanceof Literal<?>) {
				first = first.getConvertedExpression(second.getReturnType());
				if (first == null)
					return false;
			} else if (second instanceof Literal<?>) {
				second = second.getConvertedExpression(first.getReturnType());
				if (second == null)
					return false;
			}
			if (first instanceof Variable) {
				first = first.getConvertedExpression(second.getReturnType());
			} else if (second instanceof Variable) {
				second = second.getConvertedExpression(first.getReturnType());
			}
			ci = Classes.getSuperClassInfo(second.getReturnType().isAssignableFrom(first.getReturnType()) ? second.getReturnType() : first.getReturnType());
		}
		if (ci.getMath() == null) {
			Skript.error("Can't get the difference of " + CondIs.f(first) + " and " + CondIs.f(second), ErrorQuality.SEMANTIC_ERROR);
			return false;
		}
		math = ci.getMath();
		relativeType = ci.getMathRelativeType();
		return true;
	}
	
	@Override
	protected Object[] get(final Event e) {
		final Object f = first.getSingle(e), s = second.getSingle(e);
		if (s == null || f == null)
			return null;
		final Object[] one = (Object[]) Array.newInstance(relativeType, 1);
		one[0] = diff(math, f, s);
		return one;
	}
	
	@SuppressWarnings("unchecked")
	private static <A> Object diff(final Arithmetic<A, ?> math, final Object f, final Object s) {
		return math.difference((A) f, (A) s);
	}
	
	@Override
	public Class<? extends Object> getReturnType() {
		return relativeType;
	}
	
	@Override
	public String toString(final Event e, final boolean debug) {
		return "difference between " + first.toString(e, debug) + " and " + second.toString(e, debug);
	}
	
	@Override
	public boolean isSingle() {
		return true;
	}
	
	@Override
	public boolean getAnd() {
		return false;
	}
	
}
