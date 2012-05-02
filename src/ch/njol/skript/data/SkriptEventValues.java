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

package ch.njol.skript.data;

import org.bukkit.World;
import org.bukkit.command.CommandSender;

import ch.njol.skript.Skript;
import ch.njol.skript.api.Getter;
import ch.njol.skript.command.CommandEvent;
import ch.njol.skript.util.ScheduledEvent;

/**
 * @author Peter Güttinger
 * 
 */
public class SkriptEventValues {
	
	public SkriptEventValues() {}
	
	static {
		
		Skript.addEventValue(CommandEvent.class, CommandSender.class, new Getter<CommandSender, CommandEvent>() {
			@Override
			public CommandSender get(final CommandEvent e) {
				return e.getSender();
			}
		});
		
		Skript.addEventValue(ScheduledEvent.class, World.class, new Getter<World, ScheduledEvent>() {
			@Override
			public World get(final ScheduledEvent e) {
				return e.getWorld();
			}
		});
		
	}
	
}
