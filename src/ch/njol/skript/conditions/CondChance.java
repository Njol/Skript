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
import ch.njol.skript.api.Condition;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.SkriptParser.ParseResult;

/**
 * @author Peter Güttinger
 * 
 */
public class CondChance extends Condition {
	
	static {
		Skript.registerCondition(CondChance.class, "chance of <\\d+(.\\d+)?>\\%");
	}
	
	private double chance;
	
	@Override
	public boolean init(final Expression<?>[] vars, final int matchedPattern, final ParseResult parser) {
		chance = Double.parseDouble(parser.regexes.get(0).group()) / 100;
		return true;
	}
	
	@Override
	public boolean run(final Event e) {
		return Math.random() < chance;
	}
	
	@Override
	public String getDebugMessage(final Event e) {
		return "chance of " + chance * 100 + "%";
	}
	
}
