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

package ch.njol.skript.classes.data;

import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.BlockState;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.inventory.CraftingInventory;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.potion.PotionEffectType;

import ch.njol.skript.aliases.ItemType;
import ch.njol.skript.classes.SerializableChanger;
import ch.njol.skript.entity.XpOrbData;
import ch.njol.skript.util.Experience;
import ch.njol.skript.util.Time;
import ch.njol.skript.util.Timespan;
import ch.njol.skript.util.Utils;

/**
 * @author Peter Güttinger
 */
public class DefaultChangers {
	
	public DefaultChangers() {}
	
	/**
	 * Although this is a Changer&lt;World, ?&gt;, it should not be used for worlds.
	 */
	@SuppressWarnings("serial")
	public final static SerializableChanger<World, Object> timeChanger = new SerializableChanger<World, Object>() {
		@SuppressWarnings("unchecked")
		@Override
		public Class<?>[] acceptChange(final ChangeMode mode) {
			switch (mode) {
				case ADD:
				case REMOVE:
					return Utils.array(Timespan.class);
				case SET:
					return Utils.array(Time.class);
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
			}
		}
	};
	
	@SuppressWarnings("serial")
	public final static SerializableChanger<Entity, Object[]> entityChanger = new SerializableChanger<Entity, Object[]>() {
		@SuppressWarnings("unchecked")
		@Override
		public Class<? extends Object[]>[] acceptChange(final ChangeMode mode) {
			switch (mode) {
				case ADD:
					return Utils.array(ItemType[].class, Experience[].class);
				case DELETE:
					return Utils.array();
				case REMOVE:
					return Utils.array(ItemType[].class, PotionEffectType[].class);
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
						if (mode == ChangeMode.DELETE)
							e.remove();
						continue;
					}
					if (mode == ChangeMode.DELETE)
						continue;
					if (delta instanceof Experience[]) {
						int xp = 0;
						for (final Experience x : (Experience[]) delta)
							xp += x.getXP();
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
	
	@SuppressWarnings("serial")
	public final static SerializableChanger<Entity, Object> nonLivingEntityChanger = new SerializableChanger<Entity, Object>() {
		@SuppressWarnings("unchecked")
		@Override
		public Class<Object>[] acceptChange(final ChangeMode mode) {
			if (mode == ChangeMode.DELETE)
				return Utils.array();
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
	
	@SuppressWarnings("serial")
	public final static SerializableChanger<Player, Object> playerChanger = new SerializableChanger<Player, Object>() {
		@SuppressWarnings("unchecked")
		@Override
		public Class<? extends Object>[] acceptChange(final ChangeMode mode) {
			if (mode == ChangeMode.SET || mode == ChangeMode.DELETE)
				return null;
			if (mode == ChangeMode.ADD)
				return Utils.array(ItemType[].class, Inventory.class, XpOrbData.class);
			else
				return Utils.array(ItemType[].class, Inventory.class);
		}
		
		@SuppressWarnings("deprecation")
		@Override
		public void change(final Player[] players, final Object delta, final ChangeMode mode) {
			for (final Player p : players) {
				final PlayerInventory invi = p.getInventory();
				if (delta instanceof Inventory) {
					if (mode == ChangeMode.ADD)
						invi.addItem(((Inventory) delta).getContents());
					else
						invi.removeItem(((Inventory) delta).getContents());
				} else if (delta instanceof XpOrbData) {
					final int xp = ((XpOrbData) delta).getExperience();
					assert mode == ChangeMode.ADD;
					p.giveExp(xp);
				} else {
					for (final ItemType type : (ItemType[]) delta) {
						if (mode == ChangeMode.ADD)
							type.addTo(invi);
						else
							type.removeFrom(invi);
					}
				}
				p.updateInventory();
			}
		}
	};
	
	@SuppressWarnings("serial")
	public final static SerializableChanger<Inventory, Object> inventoryChanger = new SerializableChanger<Inventory, Object>() {
		@SuppressWarnings("unchecked")
		@Override
		public Class<? extends Object>[] acceptChange(final ChangeMode mode) {
			return Utils.array(ItemType[].class, Inventory.class);
		}
		
		@SuppressWarnings("deprecation")
		@Override
		public void change(final Inventory[] invis, final Object delta, final ChangeMode mode) {
			for (final Inventory invi : invis) {
				switch (mode) {
					case DELETE:
						invi.clear();
						if (invi instanceof PlayerInventory) {
							((PlayerInventory) invi).setArmorContents(new ItemStack[4]);
							if (((PlayerInventory) invi).getHolder() instanceof Player) {
								final Player p = (Player) ((PlayerInventory) invi).getHolder();
								if (invi.equals(p.getOpenInventory().getBottomInventory()))
									p.getOpenInventory().setCursor(null);
								if (p.getOpenInventory().getTopInventory() instanceof CraftingInventory)
									p.getOpenInventory().getTopInventory().clear();
							}
						}
						break;
					case SET:
						invi.clear();
						//$FALL-THROUGH$
					case ADD:
						if (delta instanceof Inventory) {
							invi.addItem(((Inventory) delta).getContents());
						} else {
							for (final ItemType type : (ItemType[]) delta) {
								type.addTo(invi);
							}
						}
						break;
					case REMOVE:
						if (delta instanceof Inventory) {
							invi.removeItem(((Inventory) delta).getContents());
						} else {
							for (final ItemType type : (ItemType[]) delta) {
								type.removeFrom(invi);
							}
						}
						break;
				}
				if (invi instanceof PlayerInventory) {
					((Player) invi.getHolder()).updateInventory();
				}
			}
		}
	};
	
	@SuppressWarnings("serial")
	public final static SerializableChanger<Block, Object> blockChanger = new SerializableChanger<Block, Object>() {
		@SuppressWarnings("unchecked")
		@Override
		public Class<?>[] acceptChange(final ChangeMode mode) {
			if (mode == ChangeMode.SET)
				return Utils.array(ItemType.class);
			return Utils.array(ItemType[].class, Inventory.class);
		}
		
		@Override
		public void change(final Block[] blocks, final Object delta, final ChangeMode mode) {
			for (final Block block : blocks) {
				switch (mode) {
					case SET:
						((ItemType) delta).getBlock().setBlock(block, true);
						break;
					case DELETE:
						block.setTypeId(0, true);
						break;
					case ADD:
					case REMOVE:
						final BlockState state = block.getState();
						if (!(state instanceof InventoryHolder))
							break;
						final Inventory invi = ((InventoryHolder) state).getInventory();
						if (mode == ChangeMode.ADD) {
							if (delta instanceof Inventory) {
								invi.addItem(((Inventory) delta).getContents());
							} else {
								for (final ItemType type : (ItemType[]) delta) {
									type.addTo(invi);
								}
							}
						} else {
							if (delta instanceof Inventory) {
								invi.removeItem(((Inventory) delta).getContents());
							} else {
								for (final ItemType type : (ItemType[]) delta) {
									type.removeFrom(invi);
								}
							}
						}
						state.update();
						break;
				}
			}
		}
	};
	
}
