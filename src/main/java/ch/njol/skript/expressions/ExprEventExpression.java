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
import ch.njol.skript.Skript.ExpressionType;
import ch.njol.skript.classes.ClassInfo;
import ch.njol.skript.expressions.base.EventValueExpression;
import ch.njol.skript.expressions.base.WrapperExpression;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.skript.log.ErrorQuality;
import ch.njol.skript.registrations.Classes;

/**
 * Provided for convenience: one can write 'event-world' instead of only 'world' to distinguish between the event-world and the loop-world.
 * 
 * @author Peter Güttinger
 */
public class ExprEventExpression extends WrapperExpression<Object> {
	private static final long serialVersionUID = 3943483158827368442L;
	
	static {
		Skript.registerExpression(ExprEventExpression.class, Object.class, ExpressionType.PROPERTY, "[the] event-<.+>");// property so that it is parsed after most other expressions
	}
	
	@Override
	public boolean init(final Expression<?>[] vars, final int matchedPattern, final int isDelayed, final ParseResult parser) {
		ClassInfo<?> ci = Classes.getClassInfoFromUserInput(parser.regexes.get(0).group());
		if (ci == null) {
			final Class<?> c = Classes.getClassByName(parser.regexes.get(0).group());
			if (c == null) {
				Skript.error("'" + parser.regexes.get(0) + "' is not a valid type", ErrorQuality.SEMANTIC_ERROR);
				return false;
			}
			ci = Classes.getExactClassInfo(c);
		}
		final EventValueExpression<?> e = new EventValueExpression<Object>(ci.getC());
		setExpr(e);
		return e.init();
	}
	
	@Override
	public String toString(final Event e, final boolean debug) {
		return getExpr().toString(e, debug);
	}
	
}
