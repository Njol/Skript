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
import ch.njol.skript.api.exception.InitException;
import ch.njol.skript.api.exception.ParseException;
import ch.njol.skript.lang.ExprParser.ParseResult;
import ch.njol.skript.lang.Variable;
import ch.njol.skript.util.Utils;

/**
 * @author Peter Güttinger
 * 
 */
public class EffTeleport extends Effect {
	
	static {
		Skript.addEffect(EffTeleport.class, "teleport %entities% to %location%");
	}
	
	private Variable<Entity> entities;
	private Variable<Location> location;
	
	@SuppressWarnings("unchecked")
	@Override
	public void init(final Variable<?>[] vars, final int matchedPattern, final ParseResult parser) throws InitException, ParseException {
		entities = (Variable<Entity>) vars[0];
		location = (Variable<Location>) vars[1];
	}
	
	@Override
	public String getDebugMessage(final Event e) {
		return "teleport " + entities.getDebugMessage(e) + " to " + location.getDebugMessage(e);
	}
	
	@Override
	protected void execute(final Event e) {
		final Location to = location.getSingle(e);
		final Block on = to.getBlock().getRelative(BlockFace.DOWN);
		if (0.4 < to.getX() - to.getBlockX() && to.getX() - to.getBlockX() < 0.6 && 0.4 < to.getZ() - to.getBlockZ() && to.getZ() - to.getBlockZ() < 0.6 && on.getType() != Material.AIR)
			to.setY(on.getY() + Utils.getBlockHeight(on.getType()));
		for (final Entity entity : entities.getArray(e)) {
			final Location loc = to.clone();
			loc.setPitch(entity.getLocation().getPitch());
			loc.setYaw(entity.getLocation().getYaw());
			entity.teleport(loc);
		}
	}
	
}
