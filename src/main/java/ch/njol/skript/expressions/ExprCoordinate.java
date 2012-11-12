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
import ch.njol.skript.Skript.ExpressionType;
import ch.njol.skript.classes.Converter;
import ch.njol.skript.expressions.base.PropertyExpression;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.SkriptParser.ParseResult;

/**
 * @author Peter Güttinger
 */
public class ExprCoordinate extends PropertyExpression<Location, Double> {
	private static final long serialVersionUID = -193327160570257540L;
	
	static {
		Skript.registerExpression(ExprCoordinate.class, Double.class, ExpressionType.PROPERTY,
				"[the] <[xyz]>(-| )(coord[inate]|pos[ition]|loc[ation])[s] of %locations%",
				"%locations%'[s] <[xyz]>(-| )(coord[inate]|pos[ition]|loc[ation])[s]");
	}
	
	private final static char[] axes = {'x', 'y', 'z'};
	
	private int axis;
	
	@SuppressWarnings("unchecked")
	@Override
	public boolean init(final Expression<?>[] exprs, final int matchedPattern, final int isDelayed, final ParseResult parseResult) {
		setExpr((Expression<? extends Location>) exprs[0]);
		axis = parseResult.regexes.get(0).group().charAt(0) - 'x';
		return true;
	}
	
	@Override
	protected Double[] get(final Event e, final Location[] source) {
		return get(source, new Converter<Location, Double>() {
			@Override
			public Double convert(final Location l) {
				return axis == 0 ? l.getX() : axis == 1 ? l.getY() : l.getZ();
			}
		});
	}
	
	@Override
	public Class<? extends Double> getReturnType() {
		return Double.class;
	}
	
	@Override
	public String toString(final Event e, final boolean debug) {
		return "the " + axes[axis] + "-coordinate of " + getExpr().toString(e, debug);
	}
	
}
