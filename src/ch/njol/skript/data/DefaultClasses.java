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
import ch.njol.skript.util.ItemType;
import ch.njol.skript.util.Utils;
import ch.njol.skript.variables.base.EventValueVariable;

/**
 * @author Peter Güttinger
 * 
 */
public class DefaultClasses {
	
	public DefaultClasses() {}
	
	static {
		Skript.addClass(new ClassInfo<Object>("object", Object.class, null, null));
	}
	
	public static final class FloatDefaultVariable extends SimpleLiteral<Float> {
		public FloatDefaultVariable() {
			super(Float.valueOf(1));
		}
	}
	
	static {
		Skript.addClass(new ClassInfo<Float>("float", Float.class, FloatDefaultVariable.class, new Parser<Float>() {
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
			public String toString(final Float o) {
				return o.toString();
			}
		}));
	}
	
	public static final class DoubleDefaultVariable extends SimpleLiteral<Double> {
		public DoubleDefaultVariable() {
			super(Double.valueOf(1));
		}
	}
	
	static {
		Skript.addClass(new ClassInfo<Double>("number", "double", Double.class, DoubleDefaultVariable.class, new Parser<Double>() {
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
			public String toString(final Double o) {
				return o.toString();
			}
		}, "number"));
	}
	
	public static final class BooleanDefaultVariable extends SimpleLiteral<Boolean> {
		public BooleanDefaultVariable() {
			super(Boolean.TRUE);
		}
	}
	
	static {
		Skript.addClass(new ClassInfo<Boolean>("boolean", Boolean.class, BooleanDefaultVariable.class, new Parser<Boolean>() {
			@Override
			public Boolean parse(final String s) {
				final int i = Utils.parseBooleanNoError(s);
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
	
	public static final class ByteDefaultVariable extends SimpleLiteral<Byte> {
		public ByteDefaultVariable() {
			super(Byte.valueOf((byte) 1));
		}
	}
	
	static {
		Skript.addClass(new ClassInfo<Byte>("byte", Byte.class, ByteDefaultVariable.class, new Parser<Byte>() {
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
	
	public static final class ShortDefaultVariable extends SimpleLiteral<Short> {
		public ShortDefaultVariable() {
			super(Short.valueOf((short) 1));
		}
	}
	
	static {
		Skript.addClass(new ClassInfo<Short>("short", Short.class, ShortDefaultVariable.class, new Parser<Short>() {
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
	
	public static final class IntegerDefaultVariable extends SimpleLiteral<Integer> {
		public IntegerDefaultVariable() {
			super(Integer.valueOf(1));
		}
	}
	
	static {
		Skript.addClass(new ClassInfo<Integer>("integer", "integer", Integer.class, IntegerDefaultVariable.class, new Parser<Integer>() {
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
	
	public static final class LongDefaultVariable extends SimpleLiteral<Long> {
		public LongDefaultVariable() {
			super(Long.valueOf(1));
		}
	}
	
	static {
		Skript.addClass(new ClassInfo<Long>("long", Long.class, LongDefaultVariable.class, new Parser<Long>() {
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
		Skript.addClass(new ClassInfo<String>("string", String.class, null, new Parser<String>() {
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
	
	public static final class TreeTypeDefaultVariable extends SimpleLiteral<TreeType> {
		public TreeTypeDefaultVariable() {
			super(TreeType.TREE);
		}
	}
	
	static {
		Skript.addClass(new ClassInfo<TreeType>("tree type", "treetype", TreeType.class, TreeTypeDefaultVariable.class, new Parser<TreeType>() {
			
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
	
	public static final class EntityDefaultVariable extends EventValueVariable<Entity> {
		public EntityDefaultVariable() {
			super(Entity.class);
		}
	}
	
	static {
		Skript.addClass(new ClassInfo<Entity>("entity", Entity.class, EntityDefaultVariable.class, null));
	}
	
	public static final class LivingEntityDefaultVariable extends EventValueVariable<LivingEntity> {
		public LivingEntityDefaultVariable() {
			super(LivingEntity.class);
		}
	}
	
	static {
		Skript.addClass(new ClassInfo<LivingEntity>("livingentity", LivingEntity.class, LivingEntityDefaultVariable.class, null));
	}
	
	public static final class BlockDefaultVariable extends EventValueVariable<Block> {
		public BlockDefaultVariable() {
			super(Block.class);
		}
	}
	
	static {
		Skript.addClass(new ClassInfo<Block>("block", Block.class, BlockDefaultVariable.class, new Parser<Block>() {
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
				return toString(b) + " block (" + b.getWorld().getName() + "|" + b.getX() + "," + b.getY() + "," + b.getZ() + ")";
			}
		}));
	}
	
	public static final class LocationDefaultVariable extends EventValueVariable<Location> {
		public LocationDefaultVariable() {
			super(Location.class);
		}
	}
	
	static {
		Skript.addClass(new ClassInfo<Location>("location", Location.class, LocationDefaultVariable.class, new Parser<Location>() {
			
			@Override
			public Location parse(final String s) {
				return null;
			}
			
			@Override
			public String toString(final Location l) {
				return getDebugMessage(l);
			}
			
			@Override
			public String getDebugMessage(final Location l) {
				return "(" + l.getWorld() + "|" + l.getX() + "," + l.getY() + "," + l.getZ() + ";yaw=" + l.getYaw() + ",pitch=" + l.getPitch() + ")";
			}
			
		}));
	}
	
	public static final class WorldDefaultVariable extends EventValueVariable<World> {
		public WorldDefaultVariable() {
			super(World.class);
		}
	}
	
	static {
		Skript.addClassBefore(new ClassInfo<World>("world", "world", World.class, WorldDefaultVariable.class, new Parser<World>() {
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
	
	public static final class InventoryDefaultVariable extends EventValueVariable<Inventory> {
		public InventoryDefaultVariable() {
			super(Inventory.class);
		}
	}
	
	static {
		Skript.addClass(new ClassInfo<Inventory>("inventory", Inventory.class, InventoryDefaultVariable.class, null));
	}
	
	public static final class PlayerDefaultVariable extends EventValueVariable<Player> {
		public PlayerDefaultVariable() {
			super(Player.class);
		}
	}
	
	static {
		Skript.addClass(new ClassInfo<Player>("online player", "player", Player.class, PlayerDefaultVariable.class, new Parser<Player>() {
			@Override
			public Player parse(final String s) {
				// if (!s.matches("\"\\S+\""))
				return null;
				// return Bukkit.getPlayerExact(s.substring(1, s.length() - 1));
			}
			
			@Override
			public String toString(final Player p) {
				return p.getDisplayName();
			}
		}));
	}
	
	public static final class OfflinePlayerDefaultVariable extends EventValueVariable<OfflinePlayer> {
		public OfflinePlayerDefaultVariable() {
			super(OfflinePlayer.class);
		}
	}
	
	static {
		Skript.addClass(new ClassInfo<OfflinePlayer>("player", "offlineplayer", OfflinePlayer.class, OfflinePlayerDefaultVariable.class, new Parser<OfflinePlayer>() {
			@Override
			public OfflinePlayer parse(final String s) {
				if (!s.matches("\"\\S+\""))
					return null;
				return Bukkit.getOfflinePlayer(s.substring(1, s.length() - 1));
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
	
	public static final class CommandSenderDefaultVariable extends EventValueVariable<CommandSender> {
		public CommandSenderDefaultVariable() {
			super(CommandSender.class);
		}
	}
	
	static {
		Skript.addClass(new ClassInfo<CommandSender>("commandsender", CommandSender.class, CommandSenderDefaultVariable.class, new Parser<CommandSender>() {
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
	
	public static final class BlockFaceDefaultVariable extends EventValueVariable<BlockFace> {
		public BlockFaceDefaultVariable() {
			super(BlockFace.class);
		}
	}
	
	static {
		Skript.addClass(new ClassInfo<BlockFace>("direction", "blockface", BlockFace.class, BlockFaceDefaultVariable.class, new Parser<BlockFace>() {
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
	
	public static final class InventoryHolderDefaultVariable extends EventValueVariable<InventoryHolder> {
		public InventoryHolderDefaultVariable() {
			super(InventoryHolder.class);
		}
	}
	
	static {
		Skript.addClass(new ClassInfo<InventoryHolder>("inventoryholder", InventoryHolder.class, InventoryHolderDefaultVariable.class, null));
	}
	
	public static final class GameModeDefaultVariable extends SimpleLiteral<GameMode> {
		public GameModeDefaultVariable() {
			super(GameMode.SURVIVAL);
		}
	}
	
	static {
		Skript.addClass(new ClassInfo<GameMode>("game mode", "gamemode", GameMode.class, GameModeDefaultVariable.class, new Parser<GameMode>() {
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
		Skript.addClass(new ClassInfo<ItemStack>("material", "_itemstack", ItemStack.class, null, new Parser<ItemStack>() {
			@Override
			public ItemStack parse(final String s) {
				final ItemType t = Aliases.parseItemType(s);
				if (t == null)
					return null;
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
