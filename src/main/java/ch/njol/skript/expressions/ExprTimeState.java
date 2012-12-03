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
import ch.njol.skript.lang.ExpressionType;
import ch.njol.skript.lang.Literal;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.skript.log.ErrorQuality;
import ch.njol.util.Kleenean;

/**
 * @author Peter Güttinger
 */
public class ExprTimeState extends WrapperExpression<Object> {
	private static final long serialVersionUID = 7845622836544154737L;
	
	static {
		Skript.registerExpression(ExprTimeState.class, Object.class, ExpressionType.PROPERTY,
				"(former|past) [state] [of] %object%", "%object% before [the event]",
				"(future|to-be) [state] [of] %object%", "%object%(-to-be| after[(wards| the event)])");
	}
	
	@Override
	public boolean init(final Expression<?>[] vars, final int matchedPattern, final Kleenean isDelayed, final ParseResult parseResult) {
		final Expression<?> expr = vars[0];
		if (expr instanceof Literal<?>)
			return false;
		if (isDelayed == Kleenean.TRUE) {
			Skript.error("Cannot use time states after the event has already passed", ErrorQuality.SEMANTIC_ERROR);
			return false;
		}
		if (!expr.setTime(matchedPattern >= 2 ? 1 : -1)) {
			Skript.error(expr + " does not have a " + (matchedPattern >= 2 ? "future" : "past") + " state", ErrorQuality.SEMANTIC_ERROR);
			return false;
		}
		setExpr(expr);
		return true;
	}
	
	@Override
	public String toString(final Event e, final boolean debug) {
		return "the " + (getTime() == -1 ? "past" : "future") + " state of " + getExpr().toString(e, debug);
	}
	
}
