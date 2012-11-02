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
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.potion.PotionEffectType;

import ch.njol.skript.Skript;
import ch.njol.skript.classes.SerializableChanger;
import ch.njol.skript.entity.XpOrbData;
import ch.njol.skript.util.ItemType;
import ch.njol.skript.util.Time;
import ch.njol.skript.util.Timespan;

/**
 * @author Peter Güttinger
 */
public class DefaultChangers {
	
	public DefaultChangers() {}
	
	/**
	 * Although this is a Changer&lt;World, ?&gt;, it should not be used for worlds.
	 */
	public final static SerializableChanger<World, Object> timeChanger = new SerializableChanger<World, Object>() {
		private static final long serialVersionUID = -7723176266948346432L;
		
		@SuppressWarnings("unchecked")
		@Override
		public Class<?>[] acceptChange(final ChangeMode mode) {
			switch (mode) {
				case ADD:
				case REMOVE:
					return Skript.array(Timespan.class);
				case SET:
					return Skript.array(Time.class);
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
	
	public final static SerializableChanger<Entity, Object[]> entityChanger = new SerializableChanger<Entity, Object[]>() {
		private static final long serialVersionUID = 4191773847489489771L;
		
		@SuppressWarnings("unchecked")
		@Override
		public Class<? extends Object[]>[] acceptChange(final ChangeMode mode) {
			switch (mode) {
				case ADD:
					return Skript.array(ItemType[].class, XpOrbData[].class);
				case CLEAR:
					return Skript.array();
				case REMOVE:
					return Skript.array(ItemType[].class, PotionEffectType[].class);
				case SET:
					return null;
			}
			assert false;
			return null;
		}
		
		@SuppressWarnings("deprecation")
		@Override
		public void change(final Entity[] entities, final Object[] delta, final ChangeMode mode) {
			if (delta instanceof PotionEffectType[]) {
				for (final Entity e : entities) {
					if (!(e instanceof LivingEntity))
						continue;
					for (final PotionEffectType t : (PotionEffectType[]) delta)
						((LivingEntity) e).removePotionEffect(t);
				}
			} else {
				for (final Entity e : entities) {
					if (!(e instanceof Player)) {
						if (mode == ChangeMode.CLEAR)
							e.remove();
						continue;
					}
					if (mode == ChangeMode.CLEAR)
						continue;
					if (delta instanceof XpOrbData[]) {
						int xp = 0;
						for (final XpOrbData x : (XpOrbData[]) delta)
							xp += x.getExperience();
						((Player) e).giveExp(xp);
					} else {
						final PlayerInventory invi = ((Player) e).getInventory();
						for (final ItemType type : (ItemType[]) delta) {
							if (mode == ChangeMode.ADD)
								type.addTo(invi);
							else
								type.removeFrom(invi);
						}
						((Player) e).updateInventory();
					}
				}
			}
		}
	};
	
	public final static SerializableChanger<Entity, Object> nonLivingEntityChanger = new SerializableChanger<Entity, Object>() {
		private static final long serialVersionUID = 2080340413775243075L;
		
		@SuppressWarnings("unchecked")
		@Override
		public Class<Object>[] acceptChange(final ChangeMode mode) {
			if (mode == ChangeMode.CLEAR)
				return Skript.array();
			return null;
		}
		
		@Override
		public void change(final Entity[] entities, final Object delta, final ChangeMode mode) {
			for (final Entity e : entities) {
				if (e instanceof Player)
					continue;
				e.remove();
			}
		}
	};
	
	public final static SerializableChanger<Player, ItemType[]> playerChanger = new SerializableChanger<Player, ItemType[]>() {
		private static final long serialVersionUID = 9048165091425550382L;
		
		@SuppressWarnings("unchecked")
		@Override
		public Class<? extends ItemType[]>[] acceptChange(final ChangeMode mode) {
			if (mode == ChangeMode.SET || mode == ChangeMode.CLEAR)
				return null;
			return Skript.array(ItemType[].class);
		}
		
		@SuppressWarnings("deprecation")
		@Override
		public void change(final Player[] players, final ItemType[] delta, final ChangeMode mode) {
			for (final Player p : players) {
				final PlayerInventory invi = p.getInventory();
				for (final ItemType type : delta) {
					if (mode == ChangeMode.ADD)
						type.addTo(invi);
					else
						type.removeFrom(invi);
				}
				p.updateInventory();
			}
		}
	};
	
	public final static SerializableChanger<Inventory, ItemType[]> inventoryChanger = new SerializableChanger<Inventory, ItemType[]>() {
		private static final long serialVersionUID = -8150546084341399001L;
		
		@SuppressWarnings("unchecked")
		@Override
		public Class<ItemType[]>[] acceptChange(final ChangeMode mode) {
			return Skript.array(ItemType[].class);
		}
		
		@SuppressWarnings("deprecation")
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
	
	public final static SerializableChanger<Block, Object> blockChanger = new SerializableChanger<Block, Object>() {
		private static final long serialVersionUID = -490468386085652881L;
		
		@SuppressWarnings("unchecked")
		@Override
		public Class<?>[] acceptChange(final ChangeMode mode) {
			if (mode == ChangeMode.SET)
				return Skript.array(ItemType.class);
			return Skript.array(ItemType[].class);
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
