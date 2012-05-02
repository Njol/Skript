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

import java.util.regex.Matcher;

import org.bukkit.event.Event;
import org.bukkit.event.block.BlockDispenseEvent;
import org.bukkit.event.entity.ItemSpawnEvent;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerEggThrowEvent;
import org.bukkit.inventory.ItemStack;

import ch.njol.skript.Skript;
import ch.njol.skript.api.SkriptEvent;
import ch.njol.skript.util.ItemType;

/**
 * @author Peter Güttinger
 * 
 */
public class EvtItem extends SkriptEvent {
	
	static {
		Skript.addEvent(EvtItem.class, BlockDispenseEvent.class, "dispense(( of)? %itemtype%)?");
		Skript.addEvent(EvtItem.class, ItemSpawnEvent.class, "item spawn(( of)? %itemtype%)?");
		Skript.addEvent(EvtItem.class, PlayerDropItemEvent.class, "drop(( of)? %itemtype%)?");
		Skript.addEvent(EvtItem.class, PlayerEggThrowEvent.class, "throw(( of)? %itemtype%)?");
	}
	
	private ItemType[] types;
	
	@Override
	public void init(final Object[][] args, final int matchedPattern, final Matcher matcher) {
		types = (ItemType[]) args[0];
	}
	
	@Override
	public boolean check(final Event e) {
		if (types == null)
			return true;
		final ItemStack is = Skript.getEventValue(e, ItemStack.class);
		for (final ItemType type : types) {
			if (type.isOfType(is))
				return true;
		}
		return false;
	}
	
	@Override
	public String getDebugMessage(final Event e) {
		return "dispense/spawn/drop/throw of " + Skript.toString(types);
	}
	
}
