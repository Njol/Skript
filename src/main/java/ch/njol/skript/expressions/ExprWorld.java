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

import org.bukkit.World;
import org.bukkit.event.Event;

import ch.njol.skript.Skript;
import ch.njol.skript.Skript.ExpressionType;
import ch.njol.skript.expressions.base.PropertyExpression;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.SkriptParser.ParseResult;

/**
 * @author Peter Güttinger
 * 
 */
public class ExprWorld extends PropertyExpression<World, World> {
	
	static {
		Skript.registerExpression(ExprWorld.class, World.class, ExpressionType.PROPERTY, "[the] world [of %world%]", "%world%'[s] world");
	}
	
	@SuppressWarnings("unchecked")
	@Override
	public boolean init(final Expression<?>[] exprs, final int matchedPattern, final boolean isDelayed, final ParseResult parser) {
		setExpr((Expression<? extends World>) exprs[0]);
		return true;
	}
	
	@Override
	public String toString(final Event e, final boolean debug) {
		return "the world" + (getExpr().isDefault() ? "" : " of " + getExpr().toString(e, debug));
	}
	
	@Override
	public Class<World> getReturnType() {
		return World.class;
	}
	
	@Override
	protected World[] get(final Event e, final World[] source) {
		return source;
	}
	
}
