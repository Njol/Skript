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
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.skript.lang.util.SimpleExpression;
import ch.njol.skript.util.Patterns;

/**
 * @author Peter Güttinger
 * 
 */
public class ExprArithmetic extends SimpleExpression<Number> {
	
	private static enum Operator {
		PLUS('+') {
			@Override
			public Number calculate(final Number n1, final Number n2, final boolean integer) {
				if (integer)
					return Integer.valueOf(n1.intValue() + n2.intValue());
				return Double.valueOf(n1.doubleValue() + n2.doubleValue());
			}
		},
		MINUS('-') {
			@Override
			public Number calculate(final Number n1, final Number n2, final boolean integer) {
				if (integer)
					return Integer.valueOf(n1.intValue() - n2.intValue());
				return Double.valueOf(n1.doubleValue() - n2.doubleValue());
			}
		},
		MULT('*') {
			@Override
			public Number calculate(final Number n1, final Number n2, final boolean integer) {
				if (integer)
					return Integer.valueOf(n1.intValue() * n2.intValue());
				return Double.valueOf(n1.doubleValue() * n2.doubleValue());
			}
		},
		DIV('/') {
			@Override
			public Number calculate(final Number n1, final Number n2, final boolean integer) {
				if (integer)
					return Integer.valueOf(n1.intValue() / n2.intValue());
				return Double.valueOf(n1.doubleValue() / n2.doubleValue());
			}
		},
		EXP('^') {
			@Override
			public Number calculate(final Number n1, final Number n2, final boolean integer) {
				if (integer)
					return Integer.valueOf((int) Math.pow(n1.intValue(), n2.intValue()));
				return Double.valueOf(Math.pow(n1.doubleValue(), n2.doubleValue()));
			}
		};
		
		public final char sign;
		
		private Operator(final char sign) {
			this.sign = sign;
		}
		
		public abstract Number calculate(Number n1, Number n2, boolean integer);
		
		@Override
		public String toString() {
			return "" + sign;
		}
	}
	
	private final static Patterns<Operator> patterns = new Patterns<Operator>(new Object[][] {
			
			{"%number%[ ]+[ ]%number%", Operator.PLUS},
			{"%number%[ ]-[ ]%number%", Operator.MINUS},
			
			{"%number%[ ]*[ ]%number%", Operator.MULT},
			{"%number%[ ]/[ ]%number%", Operator.DIV},
			
			{"%number%[ ]^[ ]%number%", Operator.EXP},
			
			{"\\(%number%[ ]+[ ]%number%\\)", Operator.PLUS},
			{"\\(%number%[ ]-[ ]%number%\\)", Operator.MINUS},
			
			{"\\(%number%[ ]*[ ]%number%\\)", Operator.MULT},
			{"\\(%number%[ ]/[ ]%number%\\)", Operator.DIV},
			
			{"\\(%number%[ ]^[ ]%number%\\)", Operator.EXP},
	
	});
	
	static {
		Skript.registerExpression(ExprArithmetic.class, Number.class, ExpressionType.COMBINED, patterns.getPatterns());
	}
	
	private Expression<? extends Number> first, second;
	private Operator op;
	
	private Class<? extends Number> returnType;
	private boolean integer;
	private Number[] one;
	
	@SuppressWarnings("unchecked")
	@Override
	public boolean init(final Expression<?>[] exprs, final int matchedPattern, final int isDelayed, final ParseResult parseResult) {
		first = (Expression<? extends Number>) exprs[0];
		second = (Expression<? extends Number>) exprs[1];
		op = patterns.getInfo(matchedPattern);
		if (op == Operator.DIV || op == Operator.EXP) {
			returnType = Double.class;
		} else {
			final Class<?> f = first.getReturnType(), s = second.getReturnType();
			final Class<?>[] integers = {Long.class, Integer.class, Short.class, Byte.class};
			boolean firstIsInt = false, secondIsInt = false;
			for (final Class<?> i : integers) {
				firstIsInt |= i.isAssignableFrom(f);
				secondIsInt |= i.isAssignableFrom(s);
			}
			if (firstIsInt && secondIsInt)
				returnType = Integer.class;
			else
				returnType = Double.class;
		}
		integer = returnType == Integer.class;
		one = (Number[]) Array.newInstance(returnType, 1);
		return true;
	}
	
	@Override
	protected Number[] get(final Event e) {
		Number n1 = first.getSingle(e), n2 = second.getSingle(e);
		if (n1 == null)
			n1 = Integer.valueOf(0);
		if (n2 == null)
			n2 = Integer.valueOf(0);
		one[0] = op.calculate(n1, n2, integer);
		return one;
	}
	
	@Override
	public Class<? extends Number> getReturnType() {
		return returnType;
	}
	
	@Override
	public boolean isSingle() {
		return true;
	}
	
	@Override
	public boolean getAnd() {
		return true;
	}
	
	@Override
	public String toString(final Event e, final boolean debug) {
		return "(" + first.toString(e, debug) + " " + op + " " + second.toString(e, debug) + ")";
	}
	
}
