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
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Entity;
import org.bukkit.event.Event;
import org.bukkit.util.Vector;

import ch.njol.skript.Skript;
import ch.njol.skript.Skript.ExpressionType;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.skript.lang.util.SimpleExpression;
import ch.njol.skript.util.Direction;

/**
 * @author Peter Güttinger
 * 
 */
public class ExprDirection extends SimpleExpression<Direction> {
	private static final long serialVersionUID = -2703003572226455590L;
	
	static {
		Skript.registerExpression(ExprDirection.class, Direction.class, ExpressionType.COMBINED,
				"[%-number% [(block|meter)[s]]] [to the]] (" +
						"2¦north[(-| |)(6¦east|7¦west)[(ward(s|ly|)|er(n|ly|))]] [of]" +
						"|3¦south[(-| |)(8¦east|9¦west)[(ward(s|ly|)|er(n|ly|))]] [of]" +
						"|(4¦east|5¦west)[(ward(s|ly|)|er(n|ly|))]] [of]" +
						"|0¦above|0¦over|(0¦up|1¦down)[ward(s|ly|)]|1¦below|1¦under[neath]|1¦beneath" +
						")",
				"[%-number% [(block|meter)[s]]] in [the] (0¦direction|1¦horizontal direction|2¦facing|3¦horizontal facing) of %entity/block% (of|from|)",
				"[%-number% [(block|meter)[s]]] (0¦in[ ]front [of]|0¦forward[s]|2¦behind|2¦backwards|to the (1¦right|-1¦left) [of])");
	}
	
	private final static BlockFace[] byMark = new BlockFace[] {BlockFace.UP, BlockFace.DOWN, BlockFace.NORTH, BlockFace.SOUTH, BlockFace.EAST, BlockFace.WEST,
			BlockFace.NORTH_EAST, BlockFace.NORTH_WEST, BlockFace.SOUTH_EAST, BlockFace.SOUTH_WEST};
	
	private Expression<Number> amount;
	
	private Vector direction;
	
	private Expression<?> relativeTo;
	boolean horizontal;
	boolean facing;
	
	private double yaw;
	
	@SuppressWarnings("unchecked")
	@Override
	public boolean init(final Expression<?>[] exprs, final int matchedPattern, final int isDelayed, final ParseResult parseResult) {
		amount = (Expression<Number>) exprs[0];
		switch (matchedPattern) {
			case 0:
				direction = new Vector(byMark[parseResult.mark].getModX(), byMark[parseResult.mark].getModY(), byMark[parseResult.mark].getModZ());
				break;
			case 1:
				relativeTo = exprs[1];
				horizontal = parseResult.mark % 2 != 0;
				facing = parseResult.mark >= 2;
				break;
			case 2:
				yaw = Math.PI / 2 * parseResult.mark;
		}
		return true;
	}
	
	@Override
	protected Direction[] get(final Event e) {
		final Number n = amount == null ? 1 : amount.getSingle(e);
		if (n == null)
			return null;
		if (direction != null) {
			return new Direction[] {new Direction(direction.clone().multiply(n.doubleValue()))};
		} else if (relativeTo != null) {
			final Object o = relativeTo.getSingle(e);
			if (o == null)
				return null;
			if (o instanceof Block) {
				final BlockFace f = Direction.getFacing((Block) o);
				if (f == BlockFace.SELF || horizontal && (f == BlockFace.UP || f == BlockFace.DOWN))
					return new Direction[] {Direction.ZERO};
				return new Direction[] {new Direction(f)};
			} else {
				final Location l = ((Entity) o).getLocation();
				if (!horizontal && !facing)
					return new Direction[] {new Direction(l.getDirection())};
				final double yaw = Direction.yawToRadians(l.getYaw());
				if (horizontal && !facing) {
					return new Direction[] {new Direction(Math.cos(yaw), 0, Math.sin(yaw))};
				}
				final double pitch = Direction.pitchToRadians(l.getPitch());
				assert yaw >= -Math.PI && yaw <= Math.PI;
				assert pitch > -Math.PI / 2 && pitch < Math.PI / 2;
				if (!horizontal && pitch > Math.PI / 4)
					return new Direction[] {new Direction(0, 1, 0)};
				if (!horizontal && pitch < -Math.PI / 4)
					return new Direction[] {new Direction(0, -1, 0)};
				if (yaw > -Math.PI / 4 && yaw < Math.PI / 4)
					return new Direction[] {new Direction(1, 0, 0)};
				if (yaw >= Math.PI / 4 && yaw < 3 * Math.PI / 4)
					return new Direction[] {new Direction(0, 0, 1)};
				if (yaw <= -Math.PI / 4 && yaw > -3 * Math.PI / 4)
					return new Direction[] {new Direction(0, 0, -1)};
				assert yaw >= 3 * Math.PI / 4 && yaw <= -3 * Math.PI / 4;
				return new Direction[] {new Direction(-1, 0, 0)};
			}
		} else {
			return new Direction[] {new Direction(0, yaw, n.doubleValue())};
		}
	}
	
	@Override
	public boolean isSingle() {
		return true;
	}
	
	@Override
	public Class<? extends Direction> getReturnType() {
		return Direction.class;
	}
	
	@Override
	public boolean getAnd() {
		return false;
	}
	
	@Override
	public String toString(final Event e, final boolean debug) {
		return (amount == null ? "" : amount.toString(e, debug) + " ") + (direction != null ?
				Direction.toString(direction) : relativeTo != null ?
						" in " + (horizontal ? "horizontal " : "") + (facing ? "facing" : "direction") + " of " + relativeTo.toString(e, debug) :
						Direction.toString(0, yaw, 1));
	}
	
}
