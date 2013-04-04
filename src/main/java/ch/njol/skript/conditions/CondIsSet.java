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
 * Copyright 2011-2013 Peter Güttinger
 * 
 */

package ch.njol.skript.conditions;

import org.bukkit.event.Event;

import ch.njol.skript.Skript;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Examples;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import ch.njol.skript.lang.Condition;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.Literal;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.util.Kleenean;

/**
 * @author Peter Güttinger
 */
@SuppressWarnings("serial")
@Name("Exists/Is Set")
@Description("Checks whether a given expression or variable is set.")
@Examples({"{teamscript.%player%.preferred team} is not set",
		"on damage:",
		"	projectile exists",
		"	broadcast \"%attacker% used a projectile to attack %victim%!\""})
@Since("1.2")
public class CondIsSet extends Condition {
	
	static {
		Skript.registerCondition(CondIsSet.class,
				"%objects% (exists|is set)",
				"%objects% (doesn't exist|does not exist|isn't set|is not set)");
	}
	
	private Expression<?> expr;
	
	@Override
	public boolean init(final Expression<?>[] exprs, final int matchedPattern, final Kleenean isDelayed, final ParseResult parseResult) {
		expr = exprs[0];
		if (expr instanceof Literal<?>) {
//			Skript.error("Can't understand this expression: " + expr, ErrorQuality.NOT_AN_EXPRESSION);
			return false;
		}
		setNegated(matchedPattern == 1);
		return true;
	}
	
	@Override
	public String toString(final Event e, final boolean debug) {
		return expr.toString(e, debug) + (isNegated() ? " isn't" : " is") + " set";
	}
	
	@Override
	public boolean check(final Event e) {
		return isNegated() ^ (expr.getArray(e).length > 0);
	}
	
}
