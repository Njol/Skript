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

package ch.njol.skript.classes.data;

import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.BlockState;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;

import ch.njol.skript.classes.Changer;
import ch.njol.skript.util.ItemType;
import ch.njol.skript.util.Time;
import ch.njol.skript.util.Timespan;

/**
 * @author Peter Güttinger
 * 
 */
public class DefaultChangers {
	
	public DefaultChangers() {}
	
	/**
	 * Although this is a Changer&lt;World, ?&gt;, it should not be used for worlds.
	 */
	public final static Changer<World, Object> timeChanger = new Changer<World, Object>() {
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
		public void change(final World[] worlds, final Object delta, final ChangeMode mode) {
			int mod = 1;
			switch (mode) {
				case SET:
					final Time time = (Time) delta;
					for (final World w : worlds) {
						w.setTime(time.getTicks());
					}
				break;
				case REMOVE:
					mod = -1;
					//$FALL-THROUGH$
				case ADD:
					final Timespan ts = (Timespan) delta;
					for (final World w : worlds) {
						w.setTime(w.getTime() + mod * ts.getTicks());
					}
				break;
			}
		}
	};
	
	public final static Changer<Inventory, ItemType[]> inventoryChanger = new Changer<Inventory, ItemType[]>() {
		@Override
		public Class<ItemType[]> acceptChange(final ChangeMode mode) {
			return ItemType[].class;
		}
		
		@SuppressWarnings({"deprecation"})
		@Override
		public void change(final Inventory[] invis, final ItemType[] delta, final ChangeMode mode) {
			for (final Inventory invi : invis) {
				switch (mode) {
					case CLEAR:
					case SET:
						invi.clear();
						if (invi instanceof PlayerInventory) {
							((PlayerInventory) invi).setArmorContents(new ItemStack[4]);
						}
						if (mode == ChangeMode.CLEAR)
							break;
						//$FALL-THROUGH$
					case ADD:
						for (final ItemType type : delta) {
							type.addTo(invi);
						}
					break;
					case REMOVE:
						for (final ItemType type : delta) {
							type.removeFrom(invi);
						}
					break;
				}
				if (invi instanceof PlayerInventory) {
					((Player) invi.getHolder()).updateInventory();
				}
			}
		}
	};
	
	public final static Changer<Block, Object> blockChanger = new Changer<Block, Object>() {
		@Override
		public Class<?> acceptChange(final ChangeMode mode) {
			if (mode == ChangeMode.SET)
				return ItemType.class;
			return ItemType[].class;
		}
		
		@Override
		public void change(final Block[] blocks, final Object delta, final ChangeMode mode) {
			for (final Block block : blocks) {
				switch (mode) {
					case SET:
						((ItemType) delta).setBlock(block, true);
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
							for (final ItemType type : (ItemType[]) delta) {
								type.addTo(((InventoryHolder) state).getInventory());
							}
						} else {
							for (final ItemType type : (ItemType[]) delta) {
								type.removeFrom(((InventoryHolder) state).getInventory());
							}
						}
						state.update();
					break;
				}
			}
		}
	};
	
}
