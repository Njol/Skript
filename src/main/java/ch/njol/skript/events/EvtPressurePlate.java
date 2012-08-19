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

package ch.njol.skript.events;

import org.bukkit.Material;
import org.bukkit.event.Event;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;

import ch.njol.skript.Skript;
import ch.njol.skript.lang.Literal;
import ch.njol.skript.lang.SkriptEvent;
import ch.njol.skript.lang.SkriptParser.ParseResult;

/**
 * @author Peter Güttinger
 * 
 */
public class EvtPressurePlate extends SkriptEvent {
	
	static {
		Skript.registerEvent(EvtPressurePlate.class, PlayerInteractEvent.class,
				"[step[ping] on] [a] [pressure] plate",
				"(trip|[step[ping] on] [a] tripwire)");
	}
	
	private boolean tripwire;
	
	@Override
	public boolean init(final Literal<?>[] args, final int matchedPattern, final ParseResult parser) {
		tripwire = matchedPattern == 1;
		return true;
	}
	
	@Override
	public boolean check(final Event e) {
		return ((PlayerInteractEvent) e).getAction() == Action.PHYSICAL &&
				(tripwire ? ((PlayerInteractEvent) e).getClickedBlock().getType() == Material.TRIPWIRE : (((PlayerInteractEvent) e).getClickedBlock().getType() == Material.WOOD_PLATE || ((PlayerInteractEvent) e).getClickedBlock().getType() == Material.STONE_PLATE));
	}
	
	@Override
	public String toString(final Event e, final boolean debug) {
		return tripwire ? "trip" : "stepping on a pressure plate";
	}
	
}
