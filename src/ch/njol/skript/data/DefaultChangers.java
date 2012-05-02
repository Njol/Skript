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

import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.BlockState;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;

import ch.njol.skript.api.Changer;
import ch.njol.skript.api.intern.Variable;
import ch.njol.skript.util.ItemType;
import ch.njol.skript.util.Slot;
import ch.njol.skript.util.Time;
import ch.njol.skript.util.Timespan;

/**
 * @author Peter Güttinger
 * 
 */
public class DefaultChangers {
	
	public DefaultChangers() {}
	
	public final static Changer<Block> blockChanger = new Changer<Block>() {
		
		@Override
		public Class<?> acceptChange(final ChangeMode mode) {
			return ItemType.class;
		}
		
		@SuppressWarnings("unchecked")
		@Override
		public void change(final Event e, final Variable<Block> blocks, final Variable<?> delta, final ch.njol.skript.api.Changer.ChangeMode mode) {
			for (final Block block : blocks.get(e, false)) {
				switch (mode) {
					case SET:
						((ItemType) delta.getFirst(e)).setBlock(block, true);
					break;
					case CLEAR:
						block.setTypeId(0, true);
					break;
					case ADD:
					case REMOVE:
						final BlockState state = block.getState();
						if (!(state instanceof InventoryHolder))
							break;
						if (mode == ChangeMode.ADD) {
							for (final ItemType type : (Iterable<ItemType>) delta.get(e, false)) {
								type.addTo(((InventoryHolder) state).getInventory());
							}
						} else {
							for (final ItemType type : (Iterable<ItemType>) delta.get(e, false)) {
								type.removeFrom(((InventoryHolder) state).getInventory());
							}
						}
						state.update();
					break;
				}
			}
		}
		
	};
	
	public final static Changer<Inventory> inventoryChanger = new Changer<Inventory>() {
		
		@Override
		public Class<?> acceptChange(final ch.njol.skript.api.Changer.ChangeMode mode) {
			return ItemType.class;
		}
		
		@SuppressWarnings({"deprecation", "unchecked"})
		@Override
		public void change(final Event e, final Variable<Inventory> invis, final Variable<?> delta, final ch.njol.skript.api.Changer.ChangeMode mode) {
			for (final Inventory invi : invis.get(e, false)) {
				switch (mode) {
					case SET:
						invi.clear();
						//$FALL-THROUGH$
					case ADD:
						for (final ItemType type : (Iterable<ItemType>) delta.get(e, false)) {
							if (type == null)
								continue;
							type.addTo(invi);
						}
					break;
					case REMOVE:
						for (final ItemType type : (Iterable<ItemType>) delta.get(e, false)) {
							if (type == null)
								continue;
							type.removeFrom(invi);
						}
					break;
					case CLEAR:
						invi.clear();
						if (invi instanceof PlayerInventory) {
							((PlayerInventory) invi).setArmorContents(new ItemStack[4]);
						}
				}
				if (invi instanceof PlayerInventory) {
					final Player p = Bukkit.getPlayerExact(((PlayerInventory) invi).getName());
					if (p != null)
						p.updateInventory();
				}
			}
		}
		
	};
	
	public final static Changer<Slot> slotChanger = new Changer<Slot>() {
		
		@Override
		public Class<?> acceptChange(final ch.njol.skript.api.Changer.ChangeMode mode) {
			return ItemType.class;
		}
		
		@SuppressWarnings("deprecation")
		@Override
		public void change(final Event e, final Variable<Slot> slots, final Variable<?> delta, final ch.njol.skript.api.Changer.ChangeMode mode) {
			final ItemType type = (ItemType) delta.getFirst(e);
			if (type == null && mode != ChangeMode.CLEAR)
				return;
			for (final Slot slot : slots.get(e, false)) {
				switch (mode) {
					case SET:
						slot.getInventory().setItem(slot.getIndex(), type.getItem().getRandom());
					break;
					case ADD:
						slot.getInventory().setItem(slot.getIndex(), type.getItem().addTo(slot.getItem()));
					break;
					case REMOVE:
						slot.getInventory().setItem(slot.getIndex(), type.removeFrom(slot.getItem()));
					break;
					case CLEAR:
						slot.getInventory().setItem(slot.getIndex(), null);
				}
				if (slot.getInventory() instanceof PlayerInventory) {
					final Player p = Bukkit.getPlayer(((PlayerInventory) slot.getInventory()).getName());
					if (p != null)
						p.updateInventory();
				}
			}
		}
		
	};
	
	/**
	 * Although this is a Changer&lt;World&gt;, it should not be used for worlds.
	 */
	public final static Changer<World> timeChanger = new Changer<World>() {
		@Override
		public Class<?> acceptChange(final ChangeMode mode) {
			switch (mode) {
				case ADD:
				case REMOVE:
					return Timespan.class;
				case SET:
					return Time.class;
				default:
					return null;
			}
		}
		
		@SuppressWarnings("incomplete-switch")
		@Override
		public void change(final Event e, final Variable<World> worlds, final Variable<?> delta, final ChangeMode mode) {
			int x = 1;
			switch (mode) {
				case SET:
					final Time time = (Time) delta.getFirst(e);
					for (final World w : worlds.get(e, false)) {
						w.setTime(time.getTicks());
					}
				break;
				case REMOVE:
					x = -1;
					//$FALL-THROUGH$
				case ADD:
					final Timespan ts = (Timespan) delta.getFirst(e);
					for (final World w : worlds.get(e, false)) {
						w.setTime((long) (w.getTime() + x * ts.getTicks()));
					}
				break;
			}
		}
	};
}
