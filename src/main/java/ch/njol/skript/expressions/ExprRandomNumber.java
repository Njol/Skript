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
	private static final long serialVersionUID = -9204174109547211339L;
	
	static {
		Skript.registerExpression(ExprRandomNumber.class, Number.class, ExpressionType.NORMAL,
				"[a] random number (from|between) %number% (to|and) %number%",
				"[a] random integer (from|between) %number% (to|and) %number%");
	}
	
	private Expression<Number> lower, upper;
	
	private final Random rand = new Random();
	
	private boolean integer;
	
	@SuppressWarnings("unchecked")
	@Override
	public boolean init(final Expression<?>[] vars, final int matchedPattern, final int isDelayed, final ParseResult parser) {
		lower = (Expression<Number>) vars[0];
		upper = (Expression<Number>) vars[1];
		integer = matchedPattern == 1;
		return true;
	}
	
	@Override
	protected Number[] get(final Event e) {
		final Number l = lower.getSingle(e);
		final Number u = upper.getSingle(e);
		
		if (u == null || l == null)
			return null;
		
		if (integer) {
			return new Integer[] {(int) (Math.ceil(l.doubleValue()) + rand.nextInt((int) (Math.floor(u.doubleValue()) - Math.ceil(l.doubleValue()) + 1)))};
		} else {
			return new Double[] {l.doubleValue() + rand.nextDouble() * (u.doubleValue() - l.doubleValue())};
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
