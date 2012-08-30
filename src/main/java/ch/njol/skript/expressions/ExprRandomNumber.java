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

import java.util.Random;

import org.bukkit.event.Event;

import ch.njol.skript.Skript;
import ch.njol.skript.Skript.ExpressionType;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.skript.lang.util.SimpleExpression;

/**
 * @author Peter Güttinger
 */
public class ExprRandomNumber extends SimpleExpression<Number> {
	
	static {
		Skript.registerExpression(ExprRandomNumber.class, Number.class, ExpressionType.NORMAL, "[a] random number between %double% and %double%", "[a] random integer between %double% and %double%");
	}
	
	private Expression<Double> lower, upper;
	
	private final Random rand = new Random();
	
	private boolean integer;
	
	@SuppressWarnings("unchecked")
	@Override
	public boolean init(final Expression<?>[] vars, final int matchedPattern, final int isDelayed, final ParseResult parser) {
		lower = (Expression<Double>) vars[0];
		upper = (Expression<Double>) vars[1];
		integer = matchedPattern == 1;
		return true;
	}
	
	@Override
	protected Number[] get(final Event e) {
		final Double l = lower.getSingle(e);
		final Double u = upper.getSingle(e);
		
		if (u == null || l == null)
			return null;
		
		if (integer) {
			return new Integer[] {(int) (Math.ceil(l) + rand.nextInt((int) (Math.floor(u) - Math.ceil(l) + 1)))};
		} else {
			return new Double[] {l + rand.nextDouble() * (u - l)};
		}
	}
	
	@Override
	public Class<? extends Number> getReturnType() {
		return integer ? Integer.class : Double.class;
	}
	
	@Override
	public String toString(final Event e, final boolean debug) {
		return "a random number between " + lower + " and " + upper;
	}
	
	@Override
	public boolean isSingle() {
		return true;
	}
	
	@Override
	public boolean getAnd() {
		return true;
	}
	
}
