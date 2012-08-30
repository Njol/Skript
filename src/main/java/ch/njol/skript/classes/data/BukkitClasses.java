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

import java.util.List;
import java.util.Map.Entry;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.TreeType;
import org.bukkit.World;
import org.bukkit.block.Biome;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.command.CommandSender;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;

import ch.njol.skript.Aliases;
import ch.njol.skript.Skript;
import ch.njol.skript.classes.ClassInfo;
import ch.njol.skript.classes.EnumParser;
import ch.njol.skript.classes.EnumSerializer;
import ch.njol.skript.classes.Parser;
import ch.njol.skript.classes.Serializer;
import ch.njol.skript.classes.Validator;
import ch.njol.skript.entity.EntityData;
import ch.njol.skript.expressions.base.EventValueExpression;
import ch.njol.skript.lang.ParseContext;
import ch.njol.skript.lang.util.SimpleLiteral;
import ch.njol.skript.util.ItemType;
import ch.njol.skript.util.StringMode;
import ch.njol.skript.util.Utils;
import ch.njol.util.StringUtils;

/**
 * @author Peter Güttinger
 */
@SuppressWarnings("unchecked")
public class BukkitClasses {
	
	public BukkitClasses() {}
	
	public final static Validator<? extends Entity> entityValidator = new Validator<Entity>() {
		@Override
		public <C extends Entity> C validate(final C e) {
			return Utils.validate(e);
		}
	};
	
	static {
		Skript.registerClass(new ClassInfo<TreeType>(TreeType.class, "treetype", "tree type")
				.user("tree ?types?", "trees?")
				.defaultExpression(new SimpleLiteral<TreeType>(TreeType.TREE, true))
				.parser(new Parser<TreeType>() {
					
					@Override
					public TreeType parse(String s, final ParseContext context) {
						s = s.toLowerCase();
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
							return TreeType.valueOf(s.toUpperCase().replace(' ', '_'));
						} catch (final IllegalArgumentException e) {
							return null;
						}
					}
					
					@Override
					public String toString(final TreeType o) {
						return o.toString().toLowerCase().replace('_', ' ');
					}
					
					@Override
					public String toCodeString(final TreeType o) {
						return o.toString().toLowerCase().replace('_', ' ');
					}
					
				}).serializer(new EnumSerializer<TreeType>(TreeType.class)));
		
		Skript.registerClass(new ClassInfo<Entity>(Entity.class, "entity", "entity")
				.defaultExpression(new EventValueExpression<Entity>(Entity.class))
				.parser(new Parser<Entity>() {
					@Override
					public Entity parse(final String s, final ParseContext context) {
						return null;
					}
					
					@Override
					public boolean canParse(final ParseContext context) {
						return false;
					}
					
					@Override
					public String toCodeString(final Entity e) {
						return "entity:" + e.getUniqueId().toString();
					}
					
					@Override
					public String toString(final Entity e) {
						return EntityData.toString(e);
					}
				})
				.changer(DefaultChangers.entityChanger)
				.validator((Validator<Entity>) entityValidator));
		
		Skript.registerClass(new ClassInfo<LivingEntity>(LivingEntity.class, "livingentity", "living entity")
				.defaultExpression(new EventValueExpression<LivingEntity>(LivingEntity.class))
				.changer(DefaultChangers.entityChanger)
				.validator((Validator<LivingEntity>) entityValidator));
		
		Skript.registerClass(new ClassInfo<Projectile>(Projectile.class, "projectile", "projectile")
				.defaultExpression(new EventValueExpression<Projectile>(Projectile.class))
				.changer(DefaultChangers.nonLivingEntityChanger)
				.validator((Validator<Projectile>) entityValidator));
		
		Skript.registerClass(new ClassInfo<Block>(Block.class, "block", "block")
				.user("block")
				.defaultExpression(new EventValueExpression<Block>(Block.class))
				.parser(new Parser<Block>() {
					@Override
					public Block parse(final String s, final ParseContext context) {
						return null;
					}
					
					@Override
					public boolean canParse(final ParseContext context) {
						return false;
					}
					
					@Override
					public String toString(final Block b) {
						return ItemType.toString(new ItemStack(b.getTypeId(), 1, b.getState().getRawData()));
					}
					
					@Override
					public String toCodeString(final Block b) {
						return b.getWorld().getName() + ":" + b.getX() + "," + b.getY() + "," + b.getZ();
					}
					
					@Override
					public String getDebugMessage(final Block b) {
						return toString(b) + " block (" + b.getWorld().getName() + ":" + b.getX() + "," + b.getY() + "," + b.getZ() + ")";
					}
				})
				.changer(DefaultChangers.blockChanger)
				.serializer(new Serializer<Block>() {
					@Override
					public String serialize(final Block b) {
						return b.getWorld().getName() + ":" + b.getX() + "," + b.getY() + "," + b.getZ();
					}
					
					@Override
					public Block deserialize(final String s) {
						final String[] split = s.split("[:,]");
						if (split.length != 4)
							return null;
						final World w = Bukkit.getWorld(split[0]);
						if (w == null) {
							return null;
						}
						try {
							final int[] l = new int[3];
							for (int i = 0; i < 3; i++)
								l[i] = Integer.parseInt(split[i + 1]);
							return w.getBlockAt(l[0], l[1], l[2]);
						} catch (final NumberFormatException e) {
							return null;
						}
					}
				}));
		
		Skript.registerClass(new ClassInfo<Location>(Location.class, "location", "location")
				.defaultExpression(new EventValueExpression<Location>(Location.class))
				.parser(new Parser<Location>() {
					
					@Override
					public Location parse(final String s, final ParseContext context) {
						return null;
					}
					
					@Override
					public boolean canParse(final ParseContext context) {
						return false;
					}
					
					@Override
					public String toString(final Location l) {
						return "x: " + StringUtils.toString(l.getX(), Skript.NUMBERACCURACY) + ", y: " + StringUtils.toString(l.getY(), Skript.NUMBERACCURACY) + ", z: " + StringUtils.toString(l.getZ(), Skript.NUMBERACCURACY);
					}
					
					@Override
					public String toCodeString(final Location l) {
						return l.getWorld().getName() + ":" + l.getX() + "," + l.getY() + "," + l.getZ();
					}
					
					@Override
					public String getDebugMessage(final Location l) {
						return "(" + l.getWorld().getName() + ":" + l.getX() + "," + l.getY() + "," + l.getZ() + "|yaw=" + l.getYaw() + "/pitch=" + l.getPitch() + ")";
					}
					
				}).serializer(new Serializer<Location>() {
					
					@Override
					public String serialize(final Location l) {
						return l.getWorld().getName() + ":" + l.getX() + "," + l.getY() + "," + l.getZ() + "|" + l.getYaw() + "/" + l.getPitch();
					}
					
					@Override
					public Location deserialize(final String s) {
						final String[] split = s.split("[:,|/]");
						if (split.length != 6)
							return null;
						final World w = Bukkit.getWorld(split[0]);
						if (w == null)
							return null;
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
		
		Skript.registerClass(new ClassInfo<World>(World.class, "world", "world")
				.user("worlds?")
				.defaultExpression(new EventValueExpression<World>(World.class))
				.parser(new Parser<World>() {
					@Override
					public World parse(final String s, final ParseContext context) {
						if (context == ParseContext.COMMAND)
							return Bukkit.getWorld(s);
						if (!s.matches("\".+\""))
							return null;
						return Bukkit.getWorld(s.substring(1, s.length() - 1));
					}
					
					@Override
					public String toString(final World w) {
						return w.getName();
					}
					
					@Override
					public String toCodeString(final World o) {
						return o.getName();
					}
				}).serializer(new Serializer<World>() {
					@Override
					public String serialize(final World w) {
						return w.getName();
					}
					
					@Override
					public World deserialize(final String s) {
						return Bukkit.getWorld(s);
					}
				}), "string");
		
		Skript.registerClass(new ClassInfo<Inventory>(Inventory.class, "inventory", "inventory")
				.defaultExpression(new EventValueExpression<Inventory>(Inventory.class))
				.parser(new Parser<Inventory>() {
					@Override
					public Inventory parse(final String s, final ParseContext context) {
						return null;
					}
					
					@Override
					public boolean canParse(final ParseContext context) {
						return false;
					}
					
					@Override
					public String toString(final Inventory i) {
						return "inventory of " + Skript.toString(i.getHolder());
					}
					
					@Override
					public String getDebugMessage(final Inventory i) {
						return "inventory of " + Skript.getDebugMessage(i.getHolder());
					}
					
					@Override
					public String toCodeString(final Inventory o) {
						return "inventory of " + Skript.toString(o.getHolder(), StringMode.VARIABLE_NAME, false);
					}
				}).changer(DefaultChangers.inventoryChanger));
		
		Skript.registerClass(new ClassInfo<Player>(Player.class, "player", "player")
				.user("players?")
				.defaultExpression(new EventValueExpression<Player>(Player.class))
				.parser(new Parser<Player>() {
					@Override
					public Player parse(final String s, final ParseContext context) {
						if (context != ParseContext.COMMAND)
							return null;
						final List<Player> ps = Bukkit.matchPlayer(s);
						if (ps.size() == 1)
							return ps.get(0);
						if (ps.size() == 0)
							Skript.error("There is no player online whose name starts with '" + s + "'");
						else
							Skript.error("There are several players online whose names starts with '" + s + "'");
						return null;
					}
					
					@Override
					public boolean canParse(final ParseContext context) {
						return context == ParseContext.COMMAND;
					}
					
					@Override
					public String toString(final Player p) {
						return p.getDisplayName() + ChatColor.RESET;
					}
					
					@Override
					public String toCommandString(final Player p) {
						return p.getName();
					}
					
					@Override
					public String toCodeString(final Player p) {
						return p.getName();
					}
					
					@Override
					public String getDebugMessage(final Player p) {
						return p.getName() + " " + Skript.getDebugMessage(p.getLocation());
					}
				})
				.changer(DefaultChangers.playerChanger)
				.serializeAs(OfflinePlayer.class)
				.validator((Validator<Player>) entityValidator));
		
		Skript.registerClass(new ClassInfo<OfflinePlayer>(OfflinePlayer.class, "offlineplayer", "player")
				.defaultExpression(new EventValueExpression<OfflinePlayer>(OfflinePlayer.class))
				.user("offline ?players?")
				.parser(new Parser<OfflinePlayer>() {
					@Override
					public OfflinePlayer parse(final String s, final ParseContext context) {
						if (context == ParseContext.COMMAND) {
							if (!s.matches("\\S+"))
								return null;
							return Bukkit.getOfflinePlayer(s);
						}
//						if (s.matches("\"\\S+\""))
//							return Bukkit.getOfflinePlayer(s.substring(1, s.length() - 1));
						return null;
					}
					
					@Override
					public boolean canParse(final ParseContext context) {
						return false;
					}
					
					@Override
					public String toString(final OfflinePlayer p) {
						if (p.isOnline())
							return p.getPlayer().getDisplayName() + ChatColor.RESET;
						return p.getName();
					}
					
					@Override
					public String toCommandString(final OfflinePlayer p) {
						return p.getName();
					}
					
					@Override
					public String toCodeString(final OfflinePlayer p) {
						return p.getName();
					}
					
					@Override
					public String getDebugMessage(final OfflinePlayer p) {
						if (p.isOnline())
							return Skript.getDebugMessage(p.getPlayer());
						return p.getName();
					}
				}).serializer(new Serializer<OfflinePlayer>() {
					@Override
					public String serialize(final OfflinePlayer p) {
						return p.getName();
					}
					
					@Override
					public OfflinePlayer deserialize(final String s) {
						return Bukkit.getOfflinePlayer(s);
					}
				}));
		
		Skript.registerClass(new ClassInfo<CommandSender>(CommandSender.class, "commandsender", "player/console")
				.defaultExpression(new EventValueExpression<CommandSender>(CommandSender.class))
				.parser(new Parser<CommandSender>() {
					@Override
					public CommandSender parse(final String s, final ParseContext context) {
						return null;
					}
					
					@Override
					public boolean canParse(final ParseContext context) {
						return false;
					}
					
					@Override
					public String toString(final CommandSender s) {
						return s.getName();
					}
					
					@Override
					public String toCodeString(final CommandSender o) {
						return o.getName();
					}
				}));
		
		Skript.registerClass(new ClassInfo<BlockFace>(BlockFace.class, "blockface", "direction")
				.user("directions?")
				.parser(new Parser<BlockFace>() {
					@Override
					public BlockFace parse(final String s, final ParseContext context) {
						return Utils.getBlockFace(s, true);
					}
					
					@Override
					public String toString(final BlockFace o) {
						return o.toString().toLowerCase().replace('_', ' ');
					}
					
					@Override
					public String toCodeString(final BlockFace o) {
						return o.toString().toLowerCase().replace('_', ' ');
					}
				}).serializer(new EnumSerializer<BlockFace>(BlockFace.class)));
		
		Skript.registerClass(new ClassInfo<InventoryHolder>(InventoryHolder.class, "inventoryholder", "inventory holder")
				.defaultExpression(new EventValueExpression<InventoryHolder>(InventoryHolder.class)));
		
		Skript.registerClass(new ClassInfo<GameMode>(GameMode.class, "gamemode", "game mode")
				.user("game ?modes?")
				.defaultExpression(new SimpleLiteral<GameMode>(GameMode.SURVIVAL, true))
				.parser(new Parser<GameMode>() {
					@Override
					public GameMode parse(final String s, final ParseContext context) {
						try {
							return GameMode.valueOf(s.toUpperCase());
						} catch (final IllegalArgumentException e) {
							return null;
						}
					}
					
					@Override
					public String toCodeString(final GameMode o) {
						return o.toString().toLowerCase();
					}
					
					@Override
					public String toString(final GameMode m) {
						return m.toString().toLowerCase();
					}
				}).serializer(new EnumSerializer<GameMode>(GameMode.class)));
		
		Skript.registerClass(new ClassInfo<ItemStack>(ItemStack.class, "itemstack", "material")
				.user("item", "material")
				.parser(new Parser<ItemStack>() {
					@Override
					public ItemStack parse(final String s, final ParseContext context) {
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
					
					@Override
					public String toCodeString(final ItemStack i) {
						final StringBuilder b = new StringBuilder("item:");
						b.append(i.getType().name());
						b.append(":" + i.getDurability());
						b.append("*" + i.getAmount());
						for (final Entry<Enchantment, Integer> e : i.getEnchantments().entrySet()) {
							b.append("#" + e.getKey().getId());
							b.append(":" + e.getValue());
						}
						return b.toString();
					}
				}).serializer(new Serializer<ItemStack>() {
					@Override
					public String serialize(final ItemStack i) {
						final StringBuilder b = new StringBuilder();
						b.append(i.getTypeId());
						b.append(":" + i.getDurability());
						b.append("*" + i.getAmount());
						for (final Entry<Enchantment, Integer> e : i.getEnchantments().entrySet()) {
							b.append("#" + e.getKey().getId());
							b.append(":" + e.getValue());
						}
						return b.toString();
					}
					
					@Override
					public ItemStack deserialize(final String s) {
						final String[] split = s.split("[:*#]");
						if (split.length < 3 || split.length % 2 != 1)
							return null;
						int typeId = -1;
						try {
							typeId = Material.valueOf(split[0]).getId();
						} catch (final IllegalArgumentException e) {}
						try {
							final ItemStack is = new ItemStack(
									typeId == -1 ? Integer.parseInt(split[0]) : typeId,
									Integer.parseInt(split[2]),
									Short.parseShort(split[1]));
							for (int i = 3; i < split.length; i += 2) {
								final Enchantment ench = Enchantment.getById(Integer.parseInt(split[i]));
								if (ench == null)
									return null;
								is.addUnsafeEnchantment(ench, Integer.parseInt(split[i + 1]));
							}
							return is;
						} catch (final NumberFormatException e) {
							return null;
						} catch (final IllegalArgumentException e) {
							return null;
						}
					}
				}));
		
		Skript.registerClass(new ClassInfo<Biome>(Biome.class, "biome", "biome")
				.user("biomes?")
				.parser(new EnumParser<Biome>(Biome.class))
				.serializer(new EnumSerializer<Biome>(Biome.class)));
		
	}
}
