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

package ch.njol.skript.events.bukkit;

import org.bukkit.World;
import org.bukkit.event.HandlerList;

import ch.njol.skript.registrations.EventValues;
import ch.njol.skript.util.Getter;

/**
 * @author Peter Güttinger
 */
public class ScheduledWorldEvent extends ScheduledEvent {
	static {
		EventValues.registerEventValue(ScheduledWorldEvent.class, World.class, new Getter<World, ScheduledWorldEvent>() {
			@Override
			public World get(final ScheduledWorldEvent e) {
				return e.getWorld();
			}
		}, 0);
	}
	
	private final World world;
	
	public ScheduledWorldEvent(final World world) {
		this.world = world;
	}
	
	public final World getWorld() {
		return world;
	}
	
	// Bukkit stuff
	private final static HandlerList handlers = new HandlerList();
	
	@Override
	public HandlerList getHandlers() {
		return handlers;
	}
	
	public static HandlerList getHandlerList() {
		return handlers;
	}
}
