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
import java.util.Locale;
import java.util.Map.Entry;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
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
import org.bukkit.potion.PotionEffectType;

import ch.njol.skript.Aliases;
import ch.njol.skript.Skript;
import ch.njol.skript.classes.ClassInfo;
import ch.njol.skript.classes.EnumParser;
import ch.njol.skript.classes.EnumSerializer;
import ch.njol.skript.classes.Parser;
import ch.njol.skript.classes.Serializer;
import ch.njol.skript.entity.EntityData;
import ch.njol.skript.expressions.base.EventValueExpression;
import ch.njol.skript.lang.ParseContext;
import ch.njol.skript.lang.util.SimpleLiteral;
import ch.njol.skript.registrations.Classes;
import ch.njol.skript.util.ItemType;
import ch.njol.skript.util.PotionEffectUtils;
import ch.njol.skript.util.StringMode;
import ch.njol.skript.util.Utils;
import ch.njol.util.StringUtils;

/**
 * @author Peter Güttinger
 */
public class BukkitClasses {
	
	public BukkitClasses() {}
	
	static {
		Classes.registerClass(new ClassInfo<Entity>(Entity.class, "entity", "entity")
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
					public String toVariableNameString(final Entity e) {
						return "entity:" + e.getUniqueId().toString();
					}
					
					@Override
					public String getVariableNamePattern() {
						return "entity:[0-9a-fA-F]{8}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{12}";
					}
					
					@Override
					public String toString(final Entity e) {
						return EntityData.toString(e);
					}
					
				})
				.changer(DefaultChangers.entityChanger));
		
		Classes.registerClass(new ClassInfo<LivingEntity>(LivingEntity.class, "livingentity", "living entity")
				.defaultExpression(new EventValueExpression<LivingEntity>(LivingEntity.class))
				.changer(DefaultChangers.entityChanger));
		
		Classes.registerClass(new ClassInfo<Projectile>(Projectile.class, "projectile", "projectile")
				.defaultExpression(new EventValueExpression<Projectile>(Projectile.class))
				.changer(DefaultChangers.nonLivingEntityChanger));
		
		Classes.registerClass(new ClassInfo<Block>(Block.class, "block", "block")
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
					public String toVariableNameString(final Block b) {
						return b.getWorld().getName() + ":" + b.getX() + "," + b.getY() + "," + b.getZ();
					}
					
					@Override
					public String getVariableNamePattern() {
						return "\\S:-?\\d+,-?\\d+,-?\\d+";
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
		
		Classes.registerClass(new ClassInfo<Location>(Location.class, "location", "location")
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
					public String toVariableNameString(final Location l) {
						return l.getWorld().getName() + ":" + l.getX() + "," + l.getY() + "," + l.getZ();
					}
					
					@Override
					public String getVariableNamePattern() {
						return "\\S:-?\\d+(\\.\\d+)?,-?\\d+(\\.\\d+)?,-?\\d+(\\.\\d+)?";
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
		
		Classes.registerClass(new ClassInfo<World>(World.class, "world", "world")
				.user("worlds?")
				.before("string")
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
					public String toVariableNameString(final World o) {
						return o.getName();
					}
					
					@Override
					public String getVariableNamePattern() {
						return "\\S+";
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
				}));
		
		Classes.registerClass(new ClassInfo<Inventory>(Inventory.class, "inventory", "inventory")
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
						return "inventory of " + Classes.toString(i.getHolder());
					}
					
					@Override
					public String getDebugMessage(final Inventory i) {
						return "inventory of " + Classes.getDebugMessage(i.getHolder());
					}
					
					@Override
					public String toVariableNameString(final Inventory o) {
						return "inventory of " + Classes.toString(o.getHolder(), StringMode.VARIABLE_NAME, false);
					}
					
					@Override
					public String getVariableNamePattern() {
						return "inventory of .+";
					}
				}).changer(DefaultChangers.inventoryChanger));
		
		Classes.registerClass(new ClassInfo<Player>(Player.class, "player", "player")
				.user("players?")
				.defaultExpression(new EventValueExpression<Player>(Player.class))
				.after("string")
				.parser(new Parser<Player>() {
					@Override
					public Player parse(final String s, final ParseContext context) {
						if (context == ParseContext.COMMAND) {
							final List<Player> ps = Bukkit.matchPlayer(s);
							if (ps.size() == 1)
								return ps.get(0);
							if (ps.size() == 0)
								Skript.error("There is no player online whose name starts with '" + s + "'");
							else
								Skript.error("There are several players online whose names start with '" + s + "'");
							return null;
						}
//						if (s.matches("\"\\S+\""))
//							return Bukkit.getPlayerExact(s.substring(1, s.length() - 1));
						assert false;
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
					public String toVariableNameString(final Player p) {
						return p.getName();
					}
					
					@Override
					public String getVariableNamePattern() {
						return "\\S+";
					}
					
					@Override
					public String getDebugMessage(final Player p) {
						return p.getName() + " " + Classes.getDebugMessage(p.getLocation());
					}
				})
				.changer(DefaultChangers.playerChanger)
				.serializeAs(OfflinePlayer.class));
		
		Classes.registerClass(new ClassInfo<OfflinePlayer>(OfflinePlayer.class, "offlineplayer", "player")
				.user("offline ?players?")
				.defaultExpression(new EventValueExpression<OfflinePlayer>(OfflinePlayer.class))
				.after("string")
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
						assert false;
						return null;
					}
					
					@Override
					public boolean canParse(final ParseContext context) {
						return context == ParseContext.COMMAND;
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
					public String toVariableNameString(final OfflinePlayer p) {
						return p.getName();
					}
					
					@Override
					public String getVariableNamePattern() {
						return "\\S+";
					}
					
					@Override
					public String getDebugMessage(final OfflinePlayer p) {
						if (p.isOnline())
							return Classes.getDebugMessage(p.getPlayer());
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
		
		Classes.registerClass(new ClassInfo<CommandSender>(CommandSender.class, "commandsender", "player/console")
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
					public String toVariableNameString(final CommandSender o) {
						return o.getName();
					}
					
					@Override
					public String getVariableNamePattern() {
						return "\\S+";
					}
				}));
		
		Classes.registerClass(new ClassInfo<BlockFace>(BlockFace.class, "blockface", "direction")
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
					public String toVariableNameString(final BlockFace o) {
						return o.toString().toLowerCase(Locale.ENGLISH).replace('_', ' ');
					}
					
					@Override
					public String getVariableNamePattern() {
						return "[a-z ]+";
					}
				}).serializer(new EnumSerializer<BlockFace>(BlockFace.class)));
		
		Classes.registerClass(new ClassInfo<InventoryHolder>(InventoryHolder.class, "inventoryholder", "inventory holder")
				.defaultExpression(new EventValueExpression<InventoryHolder>(InventoryHolder.class)));
		
		Classes.registerClass(new ClassInfo<GameMode>(GameMode.class, "gamemode", "game mode")
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
					public String toString(final GameMode m) {
						return m.toString().toLowerCase();
					}
					
					@Override
					public String toVariableNameString(final GameMode o) {
						return o.toString().toLowerCase(Locale.ENGLISH);
					}
					
					@Override
					public String getVariableNamePattern() {
						return "[a-z]+";
					}
				}).serializer(new EnumSerializer<GameMode>(GameMode.class)));
		
		Classes.registerClass(new ClassInfo<ItemStack>(ItemStack.class, "itemstack", "material")
				.user("item", "material")
				.after("number")
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
					public String toVariableNameString(final ItemStack i) {
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
					
					@Override
					public String getVariableNamePattern() {
						return "item:.+";
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
		
		Classes.registerClass(new ClassInfo<Biome>(Biome.class, "biome", "biome")
				.user("biomes?")
				.parser(new EnumParser<Biome>(Biome.class))
				.serializer(new EnumSerializer<Biome>(Biome.class)));
		
		Classes.registerClass(new ClassInfo<PotionEffectType>(PotionEffectType.class, "potioneffecttype", "potion")
				.user("potions?( ?effects?)?( ?types?)?")
				.parser(new Parser<PotionEffectType>() {
					@Override
					public PotionEffectType parse(final String s, final ParseContext context) {
						return PotionEffectUtils.parse(s);
					}
					
					@Override
					public String toString(final PotionEffectType o) {
						return PotionEffectUtils.toString(o);
					}
					
					@Override
					public String toVariableNameString(final PotionEffectType o) {
						return o.getName();
					}
					
					@Override
					public String getVariableNamePattern() {
						return ".+";
					}
				})
				.serializer(new Serializer<PotionEffectType>() {
					
					@Override
					public String serialize(final PotionEffectType o) {
						return o.getName();
					}
					
					@Override
					public PotionEffectType deserialize(final String s) {
						return PotionEffectType.getByName(s);
					}
				}));
		
	}
}
