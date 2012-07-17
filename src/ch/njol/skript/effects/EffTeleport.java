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

package ch.njol.skript.effects;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Entity;
import org.bukkit.event.Event;

import ch.njol.skript.Skript;
import ch.njol.skript.api.Effect;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.skript.util.Utils;

/**
 * @author Peter Güttinger
 * 
 */
public class EffTeleport extends Effect {
	
	static {
		Skript.registerEffect(EffTeleport.class, "teleport %entities% to %location%");
	}
	
	private Expression<Entity> entities;
	private Expression<Location> location;
	
	@SuppressWarnings("unchecked")
	@Override
	public boolean init(final Expression<?>[] vars, final int matchedPattern, final ParseResult parser) {
		entities = (Expression<Entity>) vars[0];
		location = (Expression<Location>) vars[1];
		return true;
	}
	
	@Override
	public String toString(final Event e, final boolean debug) {
		return "teleport " + entities.toString(e, debug) + " to " + location.toString(e, debug);
	}
	
	@Override
	protected void execute(final Event e) {
		final Location to = location.getSingle(e);
		final Block on = to.getBlock().getRelative(BlockFace.DOWN);
		if (Math.abs(to.getX() - to.getBlockX() - 0.5) < Skript.EPSILON && Math.abs(to.getZ() - to.getBlockZ() - 0.5) < Skript.EPSILON && on.getType() != Material.AIR)
			to.setY(on.getY() + Utils.getBlockHeight(on.getType()));
		for (final Entity entity : entities.getArray(e)) {
			if (to.getYaw() == 0 && to.getPitch() == 0) {
				final Location loc = to.clone();
				loc.setPitch(entity.getLocation().getPitch());
				loc.setYaw(entity.getLocation().getYaw());
				loc.getChunk().load();
				entity.teleport(loc);
			} else {
				to.getChunk().load();
				entity.teleport(to);
			}
		}
	}
	
}
