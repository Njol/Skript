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

import org.bukkit.event.Event;

import ch.njol.skript.Skript;
import ch.njol.skript.expressions.base.WrapperExpression;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.SkriptParser;
import ch.njol.skript.lang.SkriptParser.ParseResult;

/**
 * Provided for convenience: one can write 'event-world' instead of only 'world' to distinguish between the event-world and the loop-world.
 * 
 * @author Peter Güttinger
 */
public class ExprEventExpression extends WrapperExpression<Object> {
	
	static {
		Skript.registerExpression(ExprEventExpression.class, Object.class, "[the] event-<.+>");
	}
	
	@Override
	public boolean init(final Expression<?>[] vars, final int matchedPattern, final ParseResult parser) {
		expr = (Expression<?>) SkriptParser.parse(parser.regexes.get(0).group(), Skript.getExpressions().iterator(), false, "'" + parser.expr + "' is not an event expression");
		return expr != null;
	}
	
	@Override
	public String getDebugMessage(final Event e) {
		return expr.getDebugMessage(e);
	}
	
}
