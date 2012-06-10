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

import org.bukkit.event.Event;
import org.bukkit.event.block.BlockDispenseEvent;
import org.bukkit.event.entity.ItemSpawnEvent;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerEggThrowEvent;
import org.bukkit.inventory.ItemStack;

import ch.njol.skript.Skript;
import ch.njol.skript.api.SkriptEvent;
import ch.njol.skript.lang.ExprParser.ParseResult;
import ch.njol.skript.lang.Literal;
import ch.njol.skript.util.ItemType;
import ch.njol.util.Checker;

/**
 * @author Peter Güttinger
 * 
 */
public class EvtItem extends SkriptEvent {
	
	static {
		Skript.registerEvent(EvtItem.class, BlockDispenseEvent.class, "dispense [[of] %itemtypes%]");
		Skript.registerEvent(EvtItem.class, ItemSpawnEvent.class, "item spawn [[of] %itemtypes%]");
		Skript.registerEvent(EvtItem.class, PlayerDropItemEvent.class, "drop [[of] %itemtypes%]");
		Skript.registerEvent(EvtItem.class, PlayerEggThrowEvent.class, "throw [[of] %itemtypes%]");
	}
	
	private Literal<ItemType> types;
	
	@SuppressWarnings("unchecked")
	@Override
	public boolean init(final Literal<?>[] args, final int matchedPattern, final ParseResult parser) {
		types = (Literal<ItemType>) args[0];
		return true;
	}
	
	@Override
	public boolean check(final Event e) {
		if (types == null)
			return true;
		final ItemStack is = Skript.getEventValue(e, ItemStack.class, 0);
		return types.check(e, new Checker<ItemType>() {
			@Override
			public boolean check(final ItemType t) {
				return t.isOfType(is);
			}
		});
	}
	
	@Override
	public String getDebugMessage(final Event e) {
		return "dispense/spawn/drop/throw" + (types == null ? "" : " of " + types);
	}
	
}
