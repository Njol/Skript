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

package ch.njol.skript.conditions;

import org.bukkit.event.Event;

import ch.njol.skript.Skript;
import ch.njol.skript.lang.Condition;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.SkriptParser.ParseResult;

/**
 * @author Peter Güttinger
 * 
 */
public class CondChance extends Condition {
	
	static {
		Skript.registerCondition(CondChance.class, "chance of %double%\\%");
	}
	
	private Expression<Double> chance;
	
	@SuppressWarnings("unchecked")
	@Override
	public boolean init(final Expression<?>[] exprs, final int matchedPattern, final boolean isDelayed, final ParseResult parser) {
		chance = (Expression<Double>) exprs[0];
		return true;
	}
	
	@Override
	public boolean check(final Event e) {
		final Double c = chance.getSingle(e);
		if (c == null)
			return false;
		return Math.random() * 100 < c;
	}
	
	@Override
	public String toString(final Event e, final boolean debug) {
		return "chance of " + chance.toString(e, debug) + "%";
	}
	
}
