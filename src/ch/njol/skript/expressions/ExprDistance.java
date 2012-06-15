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

import org.bukkit.Location;
import org.bukkit.event.Event;

import ch.njol.skript.Skript;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.SimpleExpression;
import ch.njol.skript.lang.SkriptParser.ParseResult;

/**
 * @author Peter Güttinger
 * 
 */
public class ExprDistance extends SimpleExpression<Double> {
	
	static {
		Skript.registerExpression(ExprDistance.class, Double.class, "distance between %location% and %location%");
	}
	
	private Expression<Location> loc1, loc2;
	
	@SuppressWarnings("unchecked")
	@Override
	public boolean init(final Expression<?>[] vars, final int matchedPattern, final ParseResult parseResult) {
		loc1 = (Expression<Location>) vars[0];
		loc2 = (Expression<Location>) vars[1];
		return true;
	}
	
	@Override
	protected Double[] getAll(final Event e) {
		final Location l1 = loc1.getSingle(e), l2 = loc2.getSingle(e);
		if (l1 == null || l2 == null || l1.getWorld() != l2.getWorld())
			return null;
		return new Double[] {l1.distance(l2)};
	}
	
	@Override
	public String toString() {
		return "distance between " + loc1 + " and " + loc2;
	}
	
	@Override
	public String getDebugMessage(final Event e) {
		return "distance between " + loc1.getDebugMessage(e) + " and " + loc2.getDebugMessage(e);
	}
	
	@Override
	public boolean isSingle() {
		return true;
	}
	
	@Override
	public Class<? extends Double> getReturnType() {
		return Double.class;
	}
	
}
