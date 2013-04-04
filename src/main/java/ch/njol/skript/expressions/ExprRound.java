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

package ch.njol.skript.expressions;

import org.bukkit.event.Event;

import ch.njol.skript.Skript;
import ch.njol.skript.classes.Converter;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Examples;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import ch.njol.skript.expressions.base.PropertyExpression;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.ExpressionType;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.util.Kleenean;

/**
 * @author Peter Güttinger
 */
@SuppressWarnings("serial")
@Name("Rounding")
@Description("Rounds numbers normally, up (ceiling) or down (floor) respectively")
@Examples({"set {var} to rounded health of player",
		"set line 1 of the block to round(1.5 * player's level)",
		"set {_x} to floor({_y}) - ceil({_x})",
		"add rounded down argument to the player's health"})
@Since("2.0")
public class ExprRound extends PropertyExpression<Number, Integer> {
	static {
		Skript.registerExpression(ExprRound.class, Integer.class, ExpressionType.PROPERTY,
				"floor\\(%number%\\)", "round[ed] down %number%",
				"round\\(%number%\\)", "round[ed] %number%",
				"ceil[ing]\\(%number%\\)", "round[ed] up %number%");
	}
	
	int action;
	
	@SuppressWarnings("unchecked")
	@Override
	public boolean init(final Expression<?>[] exprs, final int matchedPattern, final Kleenean isDelayed, final ParseResult parseResult) {
		setExpr((Expression<? extends Number>) exprs[0]);
		action = matchedPattern / 2 - 1;
		return true;
	}
	
	@Override
	protected Integer[] get(final Event e, final Number[] source) {
		return get(source, new Converter<Number, Integer>() {
			@Override
			public Integer convert(final Number n) {
				if (n instanceof Integer)
					return (Integer) n;
				return (int) (action == -1 ? Math.floor(n.doubleValue()) : action == 0 ? Math.round(n.doubleValue()) : Math.ceil(n.doubleValue()));
			}
		});
	}
	
	@Override
	public Class<? extends Integer> getReturnType() {
		return Integer.class;
	}
	
	@Override
	public String toString(final Event e, final boolean debug) {
		return (action == -1 ? "floor" : action == 0 ? "round" : "ceil") + "(" + getExpr().toString(e, debug) + ")";
	}
	
}
