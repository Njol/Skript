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

import java.util.List;
import java.util.Locale;
import java.util.Map.Entry;
import java.util.concurrent.Callable;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Chunk;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.World;
import org.bukkit.block.Biome;
import org.bukkit.block.Block;
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

import ch.njol.skript.Skript;
import ch.njol.skript.aliases.Aliases;
import ch.njol.skript.aliases.ItemType;
import ch.njol.skript.classes.ClassInfo;
import ch.njol.skript.classes.ConfigurationSerializer;
import ch.njol.skript.classes.EnumParser;
import ch.njol.skript.classes.EnumSerializer;
import ch.njol.skript.classes.Parser;
import ch.njol.skript.classes.Serializer;
import ch.njol.skript.entity.EntityData;
import ch.njol.skript.expressions.base.EventValueExpression;
import ch.njol.skript.lang.ParseContext;
import ch.njol.skript.lang.util.SimpleLiteral;
import ch.njol.skript.registrations.Classes;
import ch.njol.skript.util.EnchantmentType;
import ch.njol.skript.util.PotionEffectUtils;
import ch.njol.skript.util.StringMode;
import ch.njol.skript.util.Task;
import ch.njol.util.StringUtils;

/**
 * @author Peter Güttinger
 */
public class BukkitClasses {
	
	public BukkitClasses() {}
	
	static {
		Classes.registerClass(new ClassInfo<Entity>(Entity.class, "entity")
				.user("entit(y|ies)")
				.name("Entity")
				.description("An entity is something in a <a href='#world'>world</a> that's not a <a href='#block'>block</a>, " +
						"e.g. a <a href='#player'>player</a>, a skeleton, or a zombie, but also <a href='#projectile'>projectiles</a> like arrows, fireballs or thrown potions, " +
						"or special entities like dropped items, falling blocks or paintings.")
				.usage("player, op, wolf, tamed ocelot, powered creeper, zombie, unsaddled pig, fireball, arrow, dropped item, item frame, etc.")
				.examples("entity is a zombie or creeper",
						"player is an op",
						"projectile is an arrow",
						"shoot a fireball from the player")
				.since("1.0")
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
						return "entity:" + e.getUniqueId().toString().toLowerCase(Locale.ENGLISH);
					}
					
					@Override
					public String getVariableNamePattern() {
						return "entity:[0-9a-f]{8}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{12}";
					}
					
					@Override
					public String toString(final Entity e) {
						return EntityData.toString(e);
					}
				})
				.changer(DefaultChangers.entityChanger));
		
		Classes.registerClass(new ClassInfo<LivingEntity>(LivingEntity.class, "livingentity")
				.user("living ?entit(y|ies)")
				.name("Living Entity")
				.description("A living <a href='#entity'>entity</a>, i.e. a mob or <a href='#player'>player</a>, not inanimate entities like <a href='#projectile'>projectiles</a> or dropped items.")
				.usage("see <a href='#entity'>entity</a>, but ignore inanimate objects")
				.examples("spawn 5 powered creepers",
						"shoot a zombie from the creeper")
				.since("1.0")
				.defaultExpression(new EventValueExpression<LivingEntity>(LivingEntity.class))
				.changer(DefaultChangers.entityChanger));
		
		Classes.registerClass(new ClassInfo<Projectile>(Projectile.class, "projectile")
				.user("projectiles?")
				.name("Projectile")
				.description("A projectile, e.g. an arrow, snowball or thrown potion.")
				.usage("arrow, fireball, snowball, thrown potion, etc.")
				.examples("projectile is a snowball",
						"shoot an arrow at speed 5 from the player")
				.since("1.0")
				.defaultExpression(new EventValueExpression<Projectile>(Projectile.class))
				.changer(DefaultChangers.nonLivingEntityChanger));
		
		Classes.registerClass(new ClassInfo<Block>(Block.class, "block")
				.user("blocks?")
				.name("Block")
				.description("A block in a <a href='#world'>world</a>. It has a <a href='#location'>location</a> and a <a href='#itemstack'>type</a>, " +
						"and can also have a <a href='#direction'>direction</a> (mostly a <a href='../expressions/#ExprFacing'>facing</a>), an <a href='#inventory'>inventory</a>, or other special properties.")
				.usage("")
				.examples("")
				.since("1.0")
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
						return Task.callSync(new Callable<Block>() {
							@Override
							public Block call() throws Exception {
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
						});
					}
				}));
		
		Classes.registerClass(new ClassInfo<Location>(Location.class, "location")
				.user("locations?")
				.name("Location")
				.description("A location in a <a href='#world'>world</a>. Locations are world-specific and even store a <a href='#direction'>direction</a>, " +
						"e.g. if you save a location and later teleport to it you will face the exact same direction you did when you saved the location.")
				.usage("")
				.examples("")
				.since("1.0")
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
						return "x: " + Skript.toString(l.getX()) + ", y: " + Skript.toString(l.getY()) + ", z: " + Skript.toString(l.getZ());
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
						return Task.callSync(new Callable<Location>() {
							@Override
							public Location call() throws Exception {
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
						});
					}
				}));
		
		Classes.registerClass(new ClassInfo<World>(World.class, "world")
				.user("worlds?")
				.name("World")
				.description("One of the server's worlds. Worlds can be put into scripts by surrounding their name with double quotes, e.g. \"world_nether\", " +
						"but this might not work reliably as <a href='#string'>text</a> uses the same syntax.")
				.usage("<code>\"world_name\"</code>, e.g. \"world\"")
				.examples("broadcast \"Hello!\" to \"world_nether\"")
				.since("1.0")
				.after("string")
				.defaultExpression(new EventValueExpression<World>(World.class))
				.parser(new Parser<World>() {
					@Override
					public World parse(final String s, final ParseContext context) {
						if (context == ParseContext.COMMAND)
							return Bukkit.getWorld(s);
						if (s.matches("\".+\""))
							return Bukkit.getWorld(s.substring(1, s.length() - 1));
						return null;
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
						return Task.callSync(new Callable<World>() {
							@Override
							public World call() throws Exception {
								return Bukkit.getWorld(s);
							}
						});
					}
				}));
		
		Classes.registerClass(new ClassInfo<Inventory>(Inventory.class, "inventory")
				.user("inventor(y|ies)")
				.name("Inventory")
				.description("An inventory of a <a href='#player'>player</a> or <a href='#block'>block</a>. Inventories have many effects and conditions regarding the items contained.",
						"An inventory has a fixed amount of <a href='#slot'>slots</a> which represent a specific place in the inventory, " +
								"e.g. the <a href='../expressions/#ExprArmorSlot'>helmet slot</a> for players (Please note that slot support is still very limited but will be improved eventually).")
				.usage("")
				.examples("")
				.since("1.0")
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
		
		Classes.registerClass(new ClassInfo<Player>(Player.class, "player")
				.user("players?")
				.name("Player")
				.description("A player. Depending on whether a player is online or offline several actions can be performed with them, " +
						"though you won't get any errors when using effects that only work if the player is online (e.g. changing his inventory) on an offline player.",
						"You have two possibilities to use players as command arguments: &lt;player&gt; and &lt;offline player&gt;. " +
								"The first requires that the player is online and also accepts only part of the name, " +
								"while the latter doesn't require that the player is online, but the player's name has to be entered exactly.")
				.usage("")
				.examples("")
				.since("1.0")
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
		
		Classes.registerClass(new ClassInfo<OfflinePlayer>(OfflinePlayer.class, "offlineplayer")
				.user("offline ?players?")
				.name("Offlineplayer")
				.description("A player that is possibly offline. See <a href='#player'>player</a> for more information.")
				.usage("")
				.examples("")
				.since("")
				.defaultExpression(new EventValueExpression<OfflinePlayer>(OfflinePlayer.class))
				.after("string")
				.parser(new Parser<OfflinePlayer>() {
					@Override
					public OfflinePlayer parse(final String s, final ParseContext context) {
						if (context == ParseContext.COMMAND) {
							if (!s.matches("\\S+") || s.length() > 16)
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
						return Task.callSync(new Callable<OfflinePlayer>() {
							@Override
							public OfflinePlayer call() throws Exception {
								return Bukkit.getOfflinePlayer(s);
							}
						});
					}
				}));
		
		Classes.registerClass(new ClassInfo<CommandSender>(CommandSender.class, "commandsender")
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
		
		Classes.registerClass(new ClassInfo<InventoryHolder>(InventoryHolder.class, "inventoryholder")
				.defaultExpression(new EventValueExpression<InventoryHolder>(InventoryHolder.class)));
		
		Classes.registerClass(new ClassInfo<GameMode>(GameMode.class, "gamemode")
				.user("game ?modes?")
				.name("Game Mode")
				.description("The game modes survival, creative and adventure.")
				.usage("creative/survival/adventure")
				.examples("player's gamemode is survival",
						"set the player argument's game mode to creative")
				.since("1.0")
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
		
		Classes.registerClass(new ClassInfo<ItemStack>(ItemStack.class, "itemstack")
				.user("item", "material")
				.name("Item / Material")
				.description("An item, e.g. a stack of torches, a furnace, or a wooden sword of sharpness 2. " +
						"Unlike <a href='#itemtype'>item type</a> an item can only represent exactly one item (e.g. an upside-down cobblestone stair facing west), " +
						"while an item type can represent a whole range of items (e.g. any cobble stone stairs regardless of direction).",
						"You don't usually need this type except when you want to make a command that only accepts an exact item.",
						"Please note that currently 'material' is exactly the same as 'item', i.e. can have an amount & enchantments.")
				.usage("<code>[&lt;number&gt; [of]] &lt;alias&gt; [of &lt;enchantment&gt; &lt;level&gt;]</code>, Where &lt;alias&gt; must be an alias that represents exactly one item " +
						"(i.e cannot be a general alias like 'sword' or 'plant')")
				.examples("set {_item} to type of the targeted block",
						"{_item} is a torch")
				.since("1.0")
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
						return ConfigurationSerializer.serializeCS(i);
						// old
//						final StringBuilder b = new StringBuilder();
//						b.append(i.getTypeId());
//						b.append(":" + i.getDurability());
//						b.append("*" + i.getAmount());
//						for (final Entry<Enchantment, Integer> e : i.getEnchantments().entrySet()) {
//							b.append("#" + e.getKey().getId());
//							b.append(":" + e.getValue());
//						}
//						return b.toString();
					}
					
					@Override
					public ItemStack deserialize(final String s) {
						final ItemStack i = deserializeOld(s);
						if (i != null)
							return i;
						return ConfigurationSerializer.deserializeCS(s, ItemStack.class);
					}
					
					private ItemStack deserializeOld(final String s) {
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
		
		Classes.registerClass(new ClassInfo<Biome>(Biome.class, "biome")
				.user("biomes?")
				.name("Biome")
				.description("All possible biomes Minecraft uses to generate a world.")
				.usage(ClassInfo.ENUM_USAGE)
				.examples("biome at the player is desert")
				.since("1.4.4")
				.parser(new EnumParser<Biome>(Biome.class))
				.serializer(new EnumSerializer<Biome>(Biome.class)));
		
		Classes.registerClass(new ClassInfo<PotionEffectType>(PotionEffectType.class, "potioneffecttype")
				.user("potions?( ?effects?)?( ?types?)?")
				.name("Potion Effect Type")
				.description("A potion effect, e.g. 'strength' or 'swiftness'.")
				.usage(StringUtils.join(PotionEffectUtils.getNames(), ", "))
				.examples("apply swiftness 5 to the player",
						"remove invisibility from the victim")
				.since("")
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
		
		// TODO make my own damagecause class (that e.g. stores the attacker entity, the projectile, or the attacking block)
//		Classes.registerClass(new ClassInfo<DamageCause>(DamageCause.class, "damagecause")
//				.user("damage causes?")
//				.parser(null)
//				.serializer(new EnumSerializer<DamageCause>(DamageCause.class)));
		
		Classes.registerClass(new ClassInfo<Chunk>(Chunk.class, "chunk")
				.user("chunks?")
				.name("Chunk")
				.description("A chunk is a cuboid of 16x16x128 blocks. Chunks are spread on a fixed rectangular grid in their world.")
				.usage("")
				.examples("")
				.since("2.0")
				.parser(new Parser<Chunk>() {
					@Override
					public Chunk parse(final String s, final ParseContext context) {
						return null;
					}
					
					@Override
					public boolean canParse(final ParseContext context) {
						return false;
					}
					
					@Override
					public String toString(final Chunk c) {
						return "chunk (" + c.getX() + "," + c.getZ() + ") of " + c.getWorld().getName();
					}
					
					@Override
					public String toVariableNameString(final Chunk c) {
						return c.getWorld().getName() + ":" + c.getX() + "," + c.getZ();
					}
					
					@Override
					public String getVariableNamePattern() {
						return ".+:-?[0-9]+,-?[0-9]+";
					}
				})
				.serializer(new Serializer<Chunk>() {
					@Override
					public String serialize(final Chunk c) {
						return c.getWorld().getName() + ":" + c.getX() + "," + c.getZ();
					}
					
					@Override
					public Chunk deserialize(final String s) {
						final String[] split = s.split("[:,]");
						if (split.length != 3)
							return null;
						return Task.callSync(new Callable<Chunk>() {
							@Override
							public Chunk call() throws Exception {
								final World w = Bukkit.getWorld(split[0]);
								if (w == null)
									return null;
								try {
									final int x = Integer.parseInt(split[1]);
									final int z = Integer.parseInt(split[1]);
									return w.getChunkAt(x, z);
								} catch (final NumberFormatException e) {
									return null;
								}
							}
						});
					}
				}));
		
		Classes.registerClass(new ClassInfo<Enchantment>(Enchantment.class, "enchantment")
				.user("enchantments?")
				.name("Enchantment")
				.description("An enchantment, e.g. 'sharpness' or 'furtune'. Unlike <a href='#enchantmenttype'>enchantment type</a> this type has no level, but you usually don't need to use this type anyways.")
				.usage(StringUtils.join(EnchantmentType.getNames(), ", "))
				.examples("")
				.since("1.4.6")
				.before("enchantmenttype")
				.parser(new Parser<Enchantment>() {
					@Override
					public Enchantment parse(final String s, final ParseContext context) {
						return EnchantmentType.parseEnchantment(s);
					}
					
					@Override
					public String toString(final Enchantment e) {
						return EnchantmentType.toString(e);
					}
					
					@Override
					public String toVariableNameString(final Enchantment e) {
						return e.getName();
					}
					
					@Override
					public String getVariableNamePattern() {
						return ".+";
					}
				})
				.serializer(new Serializer<Enchantment>() {
					@Override
					public String serialize(final Enchantment e) {
						return "" + e.getId();
					}
					
					@Override
					public Enchantment deserialize(final String s) {
						try {
							return Enchantment.getById(Integer.parseInt(s));
						} catch (final NumberFormatException e) {
							return null;
						}
					}
				}));
		
	}
}
