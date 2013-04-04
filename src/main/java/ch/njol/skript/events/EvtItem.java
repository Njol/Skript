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
 * Copyright 2011-2013 Peter Güttinger
 * 
 */

package ch.njol.skript.events;

import org.bukkit.event.Event;
import org.bukkit.event.block.BlockDispenseEvent;
import org.bukkit.event.entity.ItemSpawnEvent;
import org.bukkit.event.inventory.CraftItemEvent;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerPickupItemEvent;
import org.bukkit.inventory.ItemStack;

import ch.njol.skript.Skript;
import ch.njol.skript.aliases.ItemType;
import ch.njol.skript.lang.Literal;
import ch.njol.skript.lang.SkriptEvent;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.util.Checker;

/**
 * @author Peter Güttinger
 */
@SuppressWarnings("serial")
public class EvtItem extends SkriptEvent {
	
	static {
		Skript.registerEvent("Dispense", EvtItem.class, BlockDispenseEvent.class, "dispense [[of] %itemtypes%]")
				.description("Called when a dispenser dispenses an item.")
				.examples("")
				.since("");
		Skript.registerEvent("Item Spawn", EvtItem.class, ItemSpawnEvent.class, "item spawn [[of] %itemtypes%]")
				.description("Called whenever an item stack is spawned in a world, e.g. as drop of a block or mob, a player throwing items out of his inventory, or a dispenser dispensing an item (not shooting it).")
				.examples("")
				.since("");
		Skript.registerEvent("Drop", EvtItem.class, PlayerDropItemEvent.class, "drop [[of] %itemtypes%]")
				.description("Called when a player drops an item.")
				.examples("")
				.since("");
		Skript.registerEvent("Craft", EvtItem.class, CraftItemEvent.class, "craft [[of] %itemtypes%]")
				.description("Called when a player crafts an item.")
				.examples("")
				.since("");
		Skript.registerEvent("Pick Up", EvtItem.class, PlayerPickupItemEvent.class, "(pick[ ]up|picking up) [[of] %itemtypes%]")
				.description("Called when a player picks up an item. The item is still on the ground when this event is called.")
				.examples("")
				.since("");
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
		final ItemStack is;
		if (e instanceof BlockDispenseEvent) {
			is = ((BlockDispenseEvent) e).getItem();
		} else if (e instanceof ItemSpawnEvent) {
			is = ((ItemSpawnEvent) e).getEntity().getItemStack();
		} else if (e instanceof PlayerDropItemEvent) {
			is = ((PlayerDropItemEvent) e).getItemDrop().getItemStack();
		} else if (e instanceof CraftItemEvent) {
			is = ((CraftItemEvent) e).getRecipe().getResult();
		} else if (e instanceof PlayerPickupItemEvent) {
			is = ((PlayerPickupItemEvent) e).getItem().getItemStack();
		} else {
			assert false;
			return false;
		}
		return types.check(e, new Checker<ItemType>() {
			@Override
			public boolean check(final ItemType t) {
				return t.isOfType(is);
			}
		});
	}
	
	@Override
	public String toString(final Event e, final boolean debug) {
		return "dispense/spawn/drop/craft/pickup" + (types == null ? "" : " of " + types);
	}
	
}
