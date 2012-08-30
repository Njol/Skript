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
import ch.njol.skript.lang.UnparsedLiteral;

/**
 * @author Peter Güttinger
 */
public class CondIsSet extends Condition {
	
	static {
		Skript.registerCondition(CondIsSet.class,
				"%objects% (exists|is set)",
				"%objects% (doesn't exist|does not exist|isn't set|is not set)");
	}
	
	private Expression<?> expr;
	
	@Override
	public boolean init(final Expression<?>[] exprs, final int matchedPattern, final int isDelayed, final ParseResult parseResult) {
		expr = exprs[0];
		if (expr instanceof UnparsedLiteral)
			return false;
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
