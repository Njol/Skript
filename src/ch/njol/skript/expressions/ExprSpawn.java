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
import org.bukkit.World;
import org.bukkit.event.Event;

import ch.njol.skript.Skript;
import ch.njol.skript.Skript.ExpressionType;
import ch.njol.skript.classes.Changer.ChangeMode;
import ch.njol.skript.classes.Converter;
import ch.njol.skript.expressions.base.PropertyExpression;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.SkriptParser.ParseResult;

/**
 * @author Peter Güttinger
 * 
 */
public class ExprSpawn extends PropertyExpression<World, Location> {
	
	static {
		Skript.registerExpression(ExprSpawn.class, Location.class, ExpressionType.PROPERTY, "[the] spawn[s] [(point|location)[s]] [of %worlds%]", "%worlds%'[s] spawn[s] [(point|location)[s]]");
	}
	
	@SuppressWarnings("unchecked")
	@Override
	public boolean init(final Expression<?>[] exprs, final int matchedPattern, final boolean isDelayed, final ParseResult parseResult) {
		setExpr((Expression<? extends World>) exprs[0]);
		return true;
	}
	
	@Override
	protected Location[] get(final Event e, final World[] source) {
		return get(source, new Converter<World, Location>() {
			@Override
			public Location convert(final World w) {
				return w.getSpawnLocation();
			}
		});
	}
	
	@Override
	public Class<? extends Location> getReturnType() {
		return Location.class;
	}
	
	@Override
	public String toString(final Event e, final boolean debug) {
		return "spawn of " + getExpr().toString(e, debug);
	}
	
	@Override
	public Class<?> acceptChange(final ChangeMode mode) {
		if (mode == ChangeMode.SET)
			return Location.class;
		return null;
	}
	
	@Override
	public void change(final Event e, final Object delta, final ChangeMode mode) throws UnsupportedOperationException {
		final Location l = (Location) delta;
		final int x = l.getBlockX(), y = l.getBlockY(), z = l.getBlockZ();
		for (final World w : getExpr().getArray(e)) {
			w.setSpawnLocation(x, y, z);
		}
	}
}
