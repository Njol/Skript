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
import org.bukkit.block.Block;
import org.bukkit.event.Event;

import ch.njol.skript.Skript;
import ch.njol.skript.Skript.ExpressionType;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.skript.lang.util.SimpleExpression;
import ch.njol.skript.util.Direction;

/**
 * @author Peter Güttinger
 */
public class ExprLocation extends SimpleExpression<Location> {
	private static final long serialVersionUID = -2703003572226455590L;
	
	static {
		Skript.registerExpression(ExprLocation.class, Location.class, ExpressionType.PATTERN_MATCHES_EVERYTHING,
				"%direction% %block/location%");
	}
	
	private Expression<Direction> direction;
	private Expression<Block> block;
	private Expression<Location> location;
	
	@SuppressWarnings("unchecked")
	@Override
	public boolean init(final Expression<?>[] exprs, final int matchedPattern, final int isDelayed, final ParseResult parseResult) {
		direction = (Expression<Direction>) exprs[0];
		if (Block.class.isAssignableFrom(exprs[1].getReturnType()))
			block = (Expression<Block>) exprs[1];
		else
			location = (Expression<Location>) exprs[1];
		return true;
	}
	
	@Override
	protected Location[] get(final Event e) {
		final Direction d = direction.getSingle(e);
		if (d == null)
			return null;
		if (block != null) {
			final Block b = block.getSingle(e);
			if (b == null)
				return null;
			return new Location[] {d.getRelative(b)};
		} else {
			final Location l = location.getSingle(e);
			if (l == null)
				return null;
			return new Location[] {d.getRelative(l)};
		}
	}
	
	@Override
	public boolean isSingle() {
		return true;
	}
	
	@Override
	public Class<? extends Location> getReturnType() {
		return Location.class;
	}
	
	@Override
	public boolean getAnd() {
		return false;
	}
	
	@Override
	public String toString(final Event e, final boolean debug) {
		return direction.toString(e, debug) + " " + location.toString(e, debug);
	}
	
}
