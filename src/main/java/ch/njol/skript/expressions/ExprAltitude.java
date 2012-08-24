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
 * 
 */
public class ExprAltitude extends PropertyExpression<Location, Double> {
	
	static {
		Skript.registerExpression(ExprAltitude.class, Double.class, ExpressionType.PROPERTY, "[the] altitude[s] of %locations%", "%locations%'[s] altitudes");
	}
	
	@SuppressWarnings("unchecked")
	@Override
	public boolean init(final Expression<?>[] exprs, final int matchedPattern, final int isDelayed, final ParseResult parseResult) {
		setExpr((Expression<? extends Location>) exprs[0]);
		return true;
	}
	
	@Override
	protected Double[] get(final Event e, final Location[] source) {
		return get(source, new Converter<Location, Double>() {
			@Override
			public Double convert(final Location l) {
				return l.getY();
			}
		});
	}
	
	@Override
	public Class<? extends Double> getReturnType() {
		return Double.class;
	}
	
	@Override
	public String toString(final Event e, final boolean debug) {
		return "the altitude of " + getExpr().toString(e, debug);
	}
	
}
