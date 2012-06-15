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

package ch.njol.skript.classes;

import java.util.List;
import java.util.Locale;

import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.OfflinePlayer;
import org.bukkit.TreeType;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.BlockState;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;

import ch.njol.skript.Aliases;
import ch.njol.skript.Serializer;
import ch.njol.skript.Skript;
import ch.njol.skript.api.Changer;
import ch.njol.skript.api.Parser;
import ch.njol.skript.expressions.base.EventValueExpression;
import ch.njol.skript.lang.SimpleLiteral;
import ch.njol.skript.util.EntityType;
import ch.njol.skript.util.ItemType;
import ch.njol.skript.util.Utils;
import ch.njol.util.StringUtils;

/**
 * @author Peter Güttinger
 * 
 */
public class BukkitClasses {
	
	public BukkitClasses() {}
	
	static {
		Skript.registerClass(new ClassInfo<TreeType>(TreeType.class, "treetype").user("tree type", "tree ?type", "tree").defaultExpression(new SimpleLiteral<TreeType>(TreeType.TREE, true)).parser(new Parser<TreeType>() {
			
			@Override
			public TreeType parse(String s) {
				s = s.toLowerCase(Locale.ENGLISH);
				if (s.endsWith(" tree"))
					s = s.substring(0, s.length() - " tree".length());
				
				if (s.equals("regular"))
					return TreeType.TREE;
				if (s.equals("normal"))
					return TreeType.TREE;
				
				if (s.equals("big"))
					return TreeType.BIG_TREE;
				
				if (s.equals("fir"))
					return TreeType.REDWOOD;
				
				if (s.equals("tall fir"))
					return TreeType.TALL_REDWOOD;
				if (s.equals("big fir"))
					return TreeType.TALL_REDWOOD;
				if (s.equals("big redwood"))
					return TreeType.TALL_REDWOOD;
				
				try {
					return TreeType.valueOf(s.toUpperCase(Locale.ENGLISH).replace(' ', '_'));
				} catch (final IllegalArgumentException e) {
					return null;
				}
			}
			
			@Override
			public String toString(final TreeType o) {
				return o.toString().toLowerCase().replace('_', ' ');
			}
			
		}));
		
		Skript.registerClass(new ClassInfo<Entity>(Entity.class, "entity").defaultExpression(new EventValueExpression<Entity>(Entity.class)).parser(new Parser<Entity>() {
			@Override
			public Entity parse(final String s) {
				return null;
			}
			
			@Override
			public String toString(final Entity e) {
				return EntityType.toString(e);
			}
		}));
		
		Skript.registerClass(new ClassInfo<LivingEntity>(LivingEntity.class, "livingentity").defaultExpression(new EventValueExpression<LivingEntity>(LivingEntity.class)));
		
		Skript.registerClass(new ClassInfo<Block>(Block.class, "block").defaultExpression(new EventValueExpression<Block>(Block.class)).parser(new Parser<Block>() {
			@Override
			public Block parse(final String s) {
				return null;
			}
			
			@Override
			public String toString(final Block b) {
				return ItemType.toString(new ItemStack(b.getTypeId(), 1, b.getState().getRawData()));
			}
			
			@Override
			public String getDebugMessage(final Block b) {
				return toString(b) + " block (" + b.getWorld().getName() + "|" + b.getX() + "/" + b.getY() + "/" + b.getZ() + ")";
			}
		}).changer(new Changer<Block, Object>() {
			
			@Override
			public Class<?> acceptChange(final ChangeMode mode) {
				if (mode == ChangeMode.SET)
					return ItemType.class;
				return ItemType[].class;
			}
			
			@Override
			public void change(final Block[] blocks, final Object delta, final ch.njol.skript.api.Changer.ChangeMode mode) {
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
			
		}));
		
		Skript.registerClass(new ClassInfo<Location>(Location.class, "location").defaultExpression(new EventValueExpression<Location>(Location.class)).parser(new Parser<Location>() {
			
			@Override
			public Location parse(final String s) {
				return null;
			}
			
			@Override
			public String toString(final Location l) {
				return "x: " + StringUtils.toString(l.getX(), Skript.NUMBERACCURACY) + ", y: " + StringUtils.toString(l.getY(), Skript.NUMBERACCURACY) + ", z: " + StringUtils.toString(l.getZ(), Skript.NUMBERACCURACY);
			}
			
			@Override
			public String getDebugMessage(final Location l) {
				return "(" + l.getWorld().getName() + "|" + l.getX() + "/" + l.getY() + "/" + l.getZ() + "|yaw=" + l.getYaw() + "/pitch=" + l.getPitch() + ")";
			}
			
		}).serializer(new Serializer<Location>() {
			
			@Override
			public String serialize(final Location l) {
				return l.getWorld().getName() + ":" + l.getX() + "/" + l.getY() + "/" + l.getZ() + "|" + l.getYaw() + "/" + l.getPitch();
			}
			
			@Override
			public Location deserialize(final String s) {
				final String[] split = s.split("[:/|]");
				if (split.length != 6)
					return null;
				final World w = Bukkit.getWorld(split[0]);
				if (w == null) {
					Skript.error("World '" + split[0] + "' does not exist anymore");
					return null;
				}
				try {
					final double[] l = new double[5];
					for (int i = 0; i < 5; i++)
						l[i] = Double.parseDouble(split[i + 1]);
					return new Location(w, l[0], l[1], l[2], (float) l[3], (float) l[4]);
				} catch (final NumberFormatException e) {
					return null;
				}
			}
			
		}));
		
		Skript.registerClass(new ClassInfo<World>(World.class, "world").user("world", "worlds?").defaultExpression(new EventValueExpression<World>(World.class)).parser(new Parser<World>() {
			@Override
			public World parse(final String s) {
				if (!s.matches("\".+\""))
					return null;
				if (s.matches("(?i)^\"world .+"))
					return Bukkit.getWorld(s.substring("\"world ".length(), s.length() - 1));
				return Bukkit.getWorld(s.substring(1, s.length() - 1));
			}
			
			@Override
			public String toString(final World w) {
				return w.getName();
			}
		}), "string");
		
		Skript.registerClass(new ClassInfo<Inventory>(Inventory.class, "inventory").defaultExpression(new EventValueExpression<Inventory>(Inventory.class)).parser(new Parser<Inventory>() {
			
			@Override
			public Inventory parse(final String s) {
				return null;
			}
			
			@Override
			public String toString(final Inventory i) {
				return "inventory of " + Skript.toString(i.getHolder());
			}
			
			@Override
			public String getDebugMessage(final Inventory i) {
				return "inventory of " + Skript.getDebugMessage(i.getHolder());
			}
		}).changer(DefaultChangers.inventoryChanger));
		
		Skript.registerClass(new ClassInfo<Player>(Player.class, "player").user("player", "player").defaultExpression(new EventValueExpression<Player>(Player.class)).parser(new Parser<Player>() {
			@Override
			public Player parse(final String s) {
				if (Skript.isLoading())
					return null;
				final List<Player> ps = Bukkit.matchPlayer(s);
				if (ps.size() == 1)
					return ps.get(0);
				return null;
			}
			
			@Override
			public String toString(final Player p) {
				return p.getDisplayName();
			}
			
			@Override
			public String getDebugMessage(final Player p) {
				return p.getName() + " " + Skript.getDebugMessage(p.getLocation());
			}
		}).changer(new ConvertedChanger<Player, ItemType[]>(Skript.getConverter(Player.class, Inventory.class), Inventory.class, DefaultChangers.inventoryChanger)));
		
		Skript.registerClass(new ClassInfo<OfflinePlayer>(OfflinePlayer.class, "offlineplayer").user("player").defaultExpression(new EventValueExpression<OfflinePlayer>(OfflinePlayer.class)).parser(new Parser<OfflinePlayer>() {
			@Override
			public OfflinePlayer parse(final String s) {
//			if (s.matches("\"\\S+\""))
//				return Bukkit.getOfflinePlayer(s.substring(1, s.length() - 1));
				return null;
			}
			
			@Override
			public String toString(final OfflinePlayer p) {
				return p.getName();
			}
			
			@Override
			public String getDebugMessage(final OfflinePlayer p) {
				if (p.isOnline())
					return Skript.getDebugMessage(p.getPlayer());
				return p.getName();
			}
		}));
		
		Skript.registerClass(new ClassInfo<CommandSender>(CommandSender.class, "commandsender").user("player/console").defaultExpression(new EventValueExpression<CommandSender>(CommandSender.class)).parser(new Parser<CommandSender>() {
			@Override
			public CommandSender parse(final String s) {
				if (s.equalsIgnoreCase("console") || s.equalsIgnoreCase("server"))
					return Bukkit.getConsoleSender();
//			return Bukkit.getServer().getPlayerExact(s);
				return null;
			}
			
			@Override
			public String toString(final CommandSender s) {
				if (s instanceof Player)
					return ((Player) s).getDisplayName();
				return s.getName();
			}
			
			@Override
			public String getDebugMessage(final CommandSender s) {
				if (s instanceof Player)
					return null;
				return s.getName();
			}
		}));
		
		Skript.registerClass(new ClassInfo<BlockFace>(BlockFace.class, "blockface").user("direction", "direction").parser(new Parser<BlockFace>() {
			@Override
			public BlockFace parse(final String s) {
				return Utils.getBlockFace(s, true);
			}
			
			@Override
			public String toString(final BlockFace o) {
				return o.toString().toLowerCase(Locale.ENGLISH).replace('_', ' ');
			}
		}));
		
		Skript.registerClass(new ClassInfo<InventoryHolder>(InventoryHolder.class, "inventoryholder").defaultExpression(new EventValueExpression<InventoryHolder>(InventoryHolder.class)));
		
		Skript.registerClass(new ClassInfo<GameMode>(GameMode.class, "gamemode").user("game mode", "game ?mode").defaultExpression(new SimpleLiteral<GameMode>(GameMode.SURVIVAL, true)).parser(new Parser<GameMode>() {
			@Override
			public GameMode parse(final String s) {
				try {
					return GameMode.valueOf(s.toUpperCase(Locale.ENGLISH));
				} catch (final IllegalArgumentException e) {
					return null;
				}
			}
			
			@Override
			public String toString(final GameMode m) {
				return m.toString().toLowerCase();
			}
		}));
		
		Skript.registerClass(new ClassInfo<ItemStack>(ItemStack.class, "itemstack").user("material", "item", "material").parser(new Parser<ItemStack>() {
			@Override
			public ItemStack parse(final String s) {
				ItemType t = Aliases.parseItemType(s);
				if (t == null)
					return null;
				t = t.getItem();
				if (t.numTypes() != 1) {
					Skript.error("'" + s + "' represents multiple materials");
					return null;
				}
				if (!t.getTypes().get(0).hasDataRange())
					return t.getRandom();
				if (t.getTypes().get(0).dataMin > 0) {
					Skript.error("'" + s + "' represents multiple materials");
					return null;
				}
				final ItemStack i = t.getRandom();
				i.setDurability((short) 0);
				return i;
			}
			
			@Override
			public String toString(final ItemStack i) {
				return ItemType.toString(i);
			}
		}));
	}
}
