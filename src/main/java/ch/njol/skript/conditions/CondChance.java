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
import ch.njol.util.Kleenean;

/**
 * @author Peter Güttinger
 */
public class CondChance extends Condition {
	
	private static final long serialVersionUID = -4186689549153575171L;
	
	static {
		Skript.registerCondition(CondChance.class, "chance of %number%(1¦\\%|)");
	}
	
	private Expression<Double> chance;
	boolean percent;
	
	@SuppressWarnings("unchecked")
	@Override
	public boolean init(final Expression<?>[] exprs, final int matchedPattern, final Kleenean isDelayed, final ParseResult parser) {
		chance = (Expression<Double>) exprs[0];
		percent = parser.mark == 1;
		return true;
	}
	
	@Override
	public boolean check(final Event e) {
		final Number n = chance.getSingle(e);
		if (n == null)
			return false;
		return Math.random() < (percent ? n.doubleValue()/100 : n.doubleValue());
	}
	
	@Override
	public String toString(final Event e, final boolean debug) {
		return "chance of " + chance.toString(e, debug) + (percent ? "%" : "");
	}
	
}
