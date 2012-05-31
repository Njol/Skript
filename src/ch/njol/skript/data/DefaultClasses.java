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

import java.util.Locale;

import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.OfflinePlayer;
import org.bukkit.TreeType;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;

import ch.njol.skript.Aliases;
import ch.njol.skript.Skript;
import ch.njol.skript.api.ClassInfo;
import ch.njol.skript.api.Parser;
import ch.njol.skript.lang.ExprParser;
import ch.njol.skript.lang.SimpleLiteral;
import ch.njol.skript.util.EntityType;
import ch.njol.skript.util.ItemType;
import ch.njol.skript.util.Utils;
import ch.njol.skript.variables.base.EventValueVariable;
import ch.njol.util.StringUtils;

/**
 * @author Peter Güttinger
 * 
 */
public class DefaultClasses {
	
	public DefaultClasses() {}
	
	static {
		Skript.registerClass(new ClassInfo<Object>("object", Object.class, null, null));
	}
	
	static {
		Skript.registerClass(new ClassInfo<Float>("float", Float.class, new SimpleLiteral<Float>(1f), new Parser<Float>() {
			@Override
			public Float parse(final String s) {
				try {
					if (s.endsWith("%")) {
						return Float.valueOf(Float.parseFloat(s.substring(0, s.length() - 1)) / 100);
					}
					return Float.valueOf(s);
				} catch (final NumberFormatException e) {
					return null;
				}
			}
			
			@Override
			public String toString(final Float f) {
				return StringUtils.toString(f, Skript.NUMBERACCURACY);
			}
		}));
	}
	
	static {
		Skript.registerClass(new ClassInfo<Double>("number", "double", Double.class, new SimpleLiteral<Double>(1.), new Parser<Double>() {
			@Override
			public Double parse(final String s) {
				try {
					if (s.endsWith("%")) {
						return Double.valueOf(Double.parseDouble(s.substring(0, s.length() - 1)) / 100);
					}
					return Double.valueOf(s);
				} catch (final NumberFormatException e) {
					return null;
				}
			}
			
			@Override
			public String toString(final Double d) {
				return StringUtils.toString(d, Skript.NUMBERACCURACY);
			}
		}, "number"));
	}
	
	static {
		Skript.registerClass(new ClassInfo<Boolean>("boolean", Boolean.class, null, new Parser<Boolean>() {
			@Override
			public Boolean parse(final String s) {
				final byte i = Utils.parseBooleanNoError(s);
				if (i == 1)
					return Boolean.TRUE;
				if (i == 0)
					return Boolean.FALSE;
				return null;
			}
			
			@Override
			public String toString(final Boolean o) {
				return o.toString();
			}
		}));
	}
	
	static {
		Skript.registerClass(new ClassInfo<Byte>("byte", Byte.class, new SimpleLiteral<Byte>((byte) 1), new Parser<Byte>() {
			@Override
			public Byte parse(final String s) {
				try {
					return Byte.valueOf(s);
				} catch (final NumberFormatException e) {
					return null;
				}
			}
			
			@Override
			public String toString(final Byte o) {
				return o.toString();
			}
		}));
	}
	
	static {
		Skript.registerClass(new ClassInfo<Short>("short", Short.class, new SimpleLiteral<Short>((short) 1), new Parser<Short>() {
			@Override
			public Short parse(final String s) {
				try {
					return Short.valueOf(s);
				} catch (final NumberFormatException e) {
					return null;
				}
			}
			
			@Override
			public String toString(final Short o) {
				return o.toString();
			}
		}));
	}
	
	static {
		Skript.registerClass(new ClassInfo<Integer>("integer", "integer", Integer.class, new SimpleLiteral<Integer>(1), new Parser<Integer>() {
			@Override
			public Integer parse(final String s) {
				try {
					return Integer.valueOf(s);
				} catch (final NumberFormatException e) {
					return null;
				}
			}
			
			@Override
			public String toString(final Integer o) {
				return o.toString();
			}
		}, "integers?"));
	}
	
	static {
		Skript.registerClass(new ClassInfo<Long>("long", Long.class, new SimpleLiteral<Long>((long) 1), new Parser<Long>() {
			@Override
			public Long parse(final String s) {
				try {
					return Long.valueOf(s);
				} catch (final NumberFormatException e) {
					return null;
				}
			}
			
			@Override
			public String toString(final Long o) {
				return o.toString();
			}
		}));
	}
	
	static {
		Skript.registerClass(new ClassInfo<String>("string", String.class, null, new Parser<String>() {
			@Override
			public String parse(final String s) {
				if (!s.startsWith("\"") || !s.endsWith("\""))
					return null;
				if (!s.matches(ExprParser.stringMatcher)) {
					Skript.error(Skript.quotesError);
					return null;
				}
				return s.substring(1, s.length() - 1).replace("\"\"", "\"");
			}
			
			@Override
			public String toString(final String s) {
				return '"' + s + '"';
			}
		}));
	}
	
	static {
		Skript.registerClass(new ClassInfo<TreeType>("tree type", "treetype", TreeType.class, new SimpleLiteral<TreeType>(TreeType.TREE), new Parser<TreeType>() {
			
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
			
		}, "tree ?type", "tree"));
	}
	
	static {
		Skript.registerClass(new ClassInfo<Entity>("entity", Entity.class, new EventValueVariable<Entity>(Entity.class), new Parser<Entity>() {
			@Override
			public Entity parse(final String s) {
				return null;
			}
			
			@Override
			public String toString(final Entity e) {
				return EntityType.toString(e);
			}
		}));
	}
	
	static {
		Skript.registerClass(new ClassInfo<LivingEntity>("livingentity", LivingEntity.class, new EventValueVariable<LivingEntity>(LivingEntity.class), null));
	}
	
	static {
		Skript.registerClass(new ClassInfo<Block>("block", Block.class, new EventValueVariable<Block>(Block.class), new Parser<Block>() {
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
		}));
	}
	
	static {
		Skript.registerClass(new ClassInfo<Location>("location", Location.class, new EventValueVariable<Location>(Location.class), new Parser<Location>() {
			
			@Override
			public Location parse(final String s) {
				return null;
			}
			
			@Override
			public String toString(final Location l) {
				return "x: " + StringUtils.toString(l.getX(), Skript.NUMBERACCURACY) + ", " + "y: " + StringUtils.toString(l.getY(), Skript.NUMBERACCURACY) + ", " + "z: " + StringUtils.toString(l.getZ(), Skript.NUMBERACCURACY);
			}
			
			@Override
			public String getDebugMessage(final Location l) {
				return "(" + l.getWorld().getName() + "|" + l.getX() + "/" + l.getY() + "/" + l.getZ() + "|yaw=" + l.getYaw() + "/pitch=" + l.getPitch() + ")";
			}
			
		}));
	}
	
	static {
		Skript.registerClass(new ClassInfo<World>("world", "world", World.class, new EventValueVariable<World>(World.class), new Parser<World>() {
			@Override
			public World parse(final String s) {
				if (!s.matches("\".+\""))
					return null;
				if (s.matches("(?i)^\"world .+"))
					return Bukkit.getWorld(s.substring("\"world ".length(), s.length() - 1));
				return Bukkit.getWorld(s.substring(1, s.length() - 1));
			}
			
			@Override
			public String toString(final World o) {
				return o.getName();
			}
		}, "worlds?"), true, "string");
	}
	
	static {
		Skript.registerClass(new ClassInfo<Inventory>("inventory", Inventory.class, new EventValueVariable<Inventory>(Inventory.class), new Parser<Inventory>() {
			
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
		}));
	}
	
	static {
		Skript.registerClass(new ClassInfo<Player>("player", "player", Player.class, new EventValueVariable<Player>(Player.class), new Parser<Player>() {
			@Override
			public Player parse(final String s) {
//				if (s.matches("\"\\S+\""))
//					return Bukkit.getPlayerExact(s.substring(1, s.length() - 1));
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
		}));
	}
	
	static {
		Skript.registerClass(new ClassInfo<OfflinePlayer>("player", "offlineplayer", OfflinePlayer.class, new EventValueVariable<OfflinePlayer>(OfflinePlayer.class), new Parser<OfflinePlayer>() {
			@Override
			public OfflinePlayer parse(final String s) {
//				if (s.matches("\"\\S+\""))
//					return Bukkit.getOfflinePlayer(s.substring(1, s.length() - 1));
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
		}, "player"));
	}
	
	static {
		Skript.registerClass(new ClassInfo<CommandSender>("player", "commandsender", CommandSender.class, new EventValueVariable<CommandSender>(CommandSender.class), new Parser<CommandSender>() {
			@Override
			public CommandSender parse(final String s) {
				if (s.equalsIgnoreCase("console") || s.equalsIgnoreCase("server"))
					return Bukkit.getConsoleSender();
				return Bukkit.getServer().getPlayerExact(s);
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
	}
	
	static {
		Skript.registerClass(new ClassInfo<BlockFace>("direction", "blockface", BlockFace.class, null, new Parser<BlockFace>() {
			@Override
			public BlockFace parse(final String s) {
				return Utils.getBlockFace(s, true);
			}
			
			@Override
			public String toString(final BlockFace o) {
				return o.toString().toLowerCase(Locale.ENGLISH).replace('_', ' ');
			}
		}, "direction"));
	}
	
	static {
		Skript.registerClass(new ClassInfo<InventoryHolder>("inventoryholder", InventoryHolder.class, new EventValueVariable<InventoryHolder>(InventoryHolder.class), null));
	}
	
	static {
		Skript.registerClass(new ClassInfo<GameMode>("game mode", "gamemode", GameMode.class, new SimpleLiteral<GameMode>(GameMode.SURVIVAL), new Parser<GameMode>() {
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
		}, "game ?mode"));
	}
	
	static {
		Skript.registerClass(new ClassInfo<ItemStack>("material", "_itemstack", ItemStack.class, null, new Parser<ItemStack>() {
			@Override
			public ItemStack parse(final String s) {
				ItemType t = Aliases.parseItemType(s);
				if (t == null)
					return null;
				t = t.getItem();
				if (t.numTypes() != 1)
					return null;
				if (!t.getTypes().get(0).hasDataRange())
					return t.getRandom();
				if (t.getTypes().get(0).dataMin > 0)
					return null;
				final ItemStack i = t.getRandom();
				i.setDurability((short) 0);
				return i;
			}
			
			@Override
			public String toString(final ItemStack i) {
				return ItemType.toString(i);
			}
		}, "item", "material"));
	}
}
