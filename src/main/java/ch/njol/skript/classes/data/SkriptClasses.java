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

import java.util.Locale;
import java.util.Map.Entry;

import org.bukkit.block.BlockFace;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemStack;

import ch.njol.skript.Aliases;
import ch.njol.skript.Skript;
import ch.njol.skript.classes.Arithmetic;
import ch.njol.skript.classes.ClassInfo;
import ch.njol.skript.classes.EnumSerializer;
import ch.njol.skript.classes.Parser;
import ch.njol.skript.classes.SerializableChanger;
import ch.njol.skript.classes.Serializer;
import ch.njol.skript.expressions.base.EventValueExpression;
import ch.njol.skript.lang.ParseContext;
import ch.njol.skript.lang.util.SimpleLiteral;
import ch.njol.skript.registrations.Classes;
import ch.njol.skript.util.Color;
import ch.njol.skript.util.Date;
import ch.njol.skript.util.Direction;
import ch.njol.skript.util.EnchantmentType;
import ch.njol.skript.util.ItemData;
import ch.njol.skript.util.ItemType;
import ch.njol.skript.util.Slot;
import ch.njol.skript.util.StructureType;
import ch.njol.skript.util.Time;
import ch.njol.skript.util.Timeperiod;
import ch.njol.skript.util.Timespan;
import ch.njol.skript.util.WeatherType;

/**
 * @author Peter Güttinger
 */
public class SkriptClasses {
	
	public SkriptClasses() {}
	
	static {
		Classes.registerClass(new ClassInfo<WeatherType>(WeatherType.class, "weathertype", "weather type")
				.user("weather ?types?", "weather conditions?", "weathers?")
				.defaultExpression(new SimpleLiteral<WeatherType>(WeatherType.CLEAR, true))
				.parser(new Parser<WeatherType>() {
					
					@Override
					public WeatherType parse(final String s, final ParseContext context) {
						return WeatherType.parse(s);
					}
					
					@Override
					public String toString(final WeatherType o) {
						return o.toString();
					}
					
					@Override
					public String toVariableNameString(final WeatherType o) {
						return o.name().toLowerCase();
					}
					
					@Override
					public String getVariableNamePattern() {
						return "[a-z]+";
					}
					
				})
				.serializer(new EnumSerializer<WeatherType>(WeatherType.class)));
		
		Classes.registerClass(new ClassInfo<ItemType>(ItemType.class, "itemtype", "item type")
				.user("item ?types?", "items", "materials")
				.before("itemstack")
				.after("number")
				.parser(new Parser<ItemType>() {
					@Override
					public ItemType parse(final String s, final ParseContext context) {
						return Aliases.parseItemType(s);
					}
					
					@Override
					public String toString(final ItemType t) {
						return t.toString();
					}
					
					@Override
					public String getDebugMessage(final ItemType t) {
						return t.getDebugMessage();
					}
					
					@Override
					public String toVariableNameString(final ItemType t) {
						final StringBuilder b = new StringBuilder("itemtype:");
						b.append(t.getInternalAmount());
						b.append("," + t.isAll());
						for (final ItemData d : t.getTypes()) {
							b.append("," + d.getId());
							b.append(":" + d.dataMin);
							b.append("/" + d.dataMax);
						}
						if (!t.getEnchantments().isEmpty()) {
							b.append("|");
							for (final Entry<Enchantment, Integer> e : t.getEnchantments().entrySet()) {
								b.append("#" + e.getKey().getId());
								b.append(":" + e.getValue());
							}
						}
						return b.toString();
					}
					
					@Override
					public String getVariableNamePattern() {
						return "itemtype:.+";
					}
				})
				.serializer(new Serializer<ItemType>() {
					@Override
					public String serialize(final ItemType t) {
						final StringBuilder b = new StringBuilder();
						b.append(t.getInternalAmount());
						b.append("," + t.isAll());
						for (final ItemData d : t.getTypes()) {
							b.append("," + d.getId());
							b.append(":" + d.dataMin);
							b.append("/" + d.dataMax);
						}
						if (t.getEnchantments() != null) {
							b.append("|");
							for (final Entry<Enchantment, Integer> e : t.getEnchantments().entrySet()) {
								b.append("#" + e.getKey().getId());
								b.append(":" + e.getValue());
							}
						}
						return b.toString();
					}
					
					@Override
					public ItemType deserialize(final String s) {
						final String[] ss = s.split("\\|");
						if (ss.length > 2)
							return null;
						final String[] split = ss[0].split("[,:/]");
						if (split.length < 5 || (split.length - 2) % 3 != 0)
							return null;
						final ItemType t = new ItemType();
						try {
							t.setAmount(Integer.parseInt(split[0]));
							if (split[1].equals("true"))
								t.setAll(true);
							else if (split[1].equals("false"))
								t.setAll(false);
							else
								return null;
							for (int i = 2; i < split.length; i += 3) {
								t.add(new ItemData(Integer.parseInt(split[i]), Short.parseShort(split[i + 1]), Short.parseShort(split[i + 2])));
							}
						} catch (final NumberFormatException e) {
							return null;
						}
						if (ss.length == 2) {
							if (ss[1].isEmpty()) {
								t.emptyEnchantments();
							} else {
								final String[] es = ss[1].split("#");
								for (final String e : es) {
									if (e.isEmpty())
										continue;
									final String[] en = e.split(":");
									if (en.length != 2)
										return null;
									try {
										final Enchantment ench = Enchantment.getById(Integer.parseInt(en[0]));
										if (ench == null)
											return null;
										t.addEnchantment(ench, Integer.parseInt(en[1]));
									} catch (final NumberFormatException ex) {
										return null;
									}
								}
							}
						}
						return t;
					}
				}));
		
		Classes.registerClass(new ClassInfo<Time>(Time.class, "time", "time")
				.user("times?")
				.defaultExpression(new EventValueExpression<Time>(Time.class))
				.parser(new Parser<Time>() {
					
					@Override
					public Time parse(final String s, final ParseContext context) {
						return Time.parse(s);
					}
					
					@Override
					public String toString(final Time t) {
						return t.toString();
					}
					
					@Override
					public String toVariableNameString(final Time o) {
						return "time:" + o.getTicks();
					}
					
					@Override
					public String getVariableNamePattern() {
						return "time:\\d+";
					}
					
				}).serializer(new Serializer<Time>() {
					@Override
					public String serialize(final Time t) {
						return "" + t.getTicks();
					}
					
					@Override
					public Time deserialize(final String s) {
						try {
							return new Time(Integer.parseInt(s));
						} catch (final NumberFormatException e) {
							return null;
						}
					}
				}));
		
		Classes.registerClass(new ClassInfo<Timespan>(Timespan.class, "timespan", "time span")
				.user("time ?spans?")
				.parser(new Parser<Timespan>() {
					@Override
					public Timespan parse(final String s, final ParseContext context) {
						return Timespan.parse(s);
					}
					
					@Override
					public String toString(final Timespan t) {
						return t.toString();
					}
					
					@Override
					public String toVariableNameString(final Timespan o) {
						return "timespan:" + o.getMilliSeconds();
					}
					
					@Override
					public String getVariableNamePattern() {
						return "timespan:\\d+";
					}
				}).serializer(new Serializer<Timespan>() {
					@Override
					public String serialize(final Timespan t) {
						return "" + t.getMilliSeconds();
					}
					
					@Override
					public Timespan deserialize(final String s) {
						try {
							return new Timespan(Integer.parseInt(s));
						} catch (final NumberFormatException e) {
							return null;
						}
					}
				})
				.math(Timespan.class, new Arithmetic<Timespan, Timespan>() {
					private static final long serialVersionUID = -6433286220445847658L;
					
					@Override
					public Timespan difference(final Timespan t1, final Timespan t2) {
						return new Timespan(Math.abs(t1.getMilliSeconds() - t2.getMilliSeconds()));
					}
				}));
		
		Classes.registerClass(new ClassInfo<Timeperiod>(Timeperiod.class, "timeperiod", "time period")
				.user("time ?periods?", "durations?")
				.defaultExpression(new SimpleLiteral<Timeperiod>(new Timeperiod(0, 23999), true))
				.parser(new Parser<Timeperiod>() {
					@Override
					public Timeperiod parse(final String s, final ParseContext context) {
						if (s.equalsIgnoreCase("day")) {
							return new Timeperiod(0, 11999);
						} else if (s.equalsIgnoreCase("dusk")) {
							return new Timeperiod(12000, 13799);
						} else if (s.equalsIgnoreCase("night")) {
							return new Timeperiod(13800, 22199);
						} else if (s.equalsIgnoreCase("dawn")) {
							return new Timeperiod(22200, 23999);
						}
						final int c = s.indexOf('-');
						if (c == -1) {
							final Time t = Time.parse(s);
							if (t == null)
								return null;
							return new Timeperiod(t.getTicks());
						}
						final Time t1 = Time.parse(s.substring(0, c).trim());
						final Time t2 = Time.parse(s.substring(c + 1).trim());
						if (t1 == null || t2 == null)
							return null;
						return new Timeperiod(t1.getTicks(), t2.getTicks());
					}
					
					@Override
					public String toString(final Timeperiod o) {
						return o.toString();
					}
					
					@Override
					public String toVariableNameString(final Timeperiod o) {
						return "timeperiod:" + o.start + "-" + o.end;
					}
					
					@Override
					public String getVariableNamePattern() {
						return "timeperiod:\\d+-\\d+";
					}
				}).serializer(new Serializer<Timeperiod>() {
					@Override
					public String serialize(final Timeperiod t) {
						return t.start + "-" + t.end;
					}
					
					@Override
					public Timeperiod deserialize(final String s) {
						final String[] split = s.split("-");
						if (split.length != 2)
							return null;
						try {
							return new Timeperiod(Integer.parseInt(split[0]), Integer.parseInt(split[1]));
						} catch (final NumberFormatException e) {
							return null;
						}
					}
				}));
		
		Classes.registerClass(new ClassInfo<Date>(Date.class, "date", "date")
				.serializer(new Serializer<Date>() {
					@Override
					public String serialize(final Date d) {
						return "" + d.getTimestamp();
					}
					
					@Override
					public Date deserialize(final String s) {
						try {
							return new Date(Long.parseLong(s));
						} catch (final NumberFormatException e) {
							return null;
						}
					}
				}).math(Timespan.class, new Arithmetic<Date, Timespan>() {
					private static final long serialVersionUID = 1335999541703875909L;
					
					@Override
					public Timespan difference(final Date first, final Date second) {
						return first.difference(second);
					}
				}).changer(new SerializableChanger<Date, Timespan>() {
					private static final long serialVersionUID = 5598732197804454663L;
					
					@SuppressWarnings("incomplete-switch")
					@Override
					public void change(final Date[] what, final Timespan delta, final ChangeMode mode) {
						switch (mode) {
							case ADD:
								for (final Date d : what)
									d.add(delta);
								break;
							case REMOVE:
								for (final Date d : what)
									d.subtract(delta);
								break;
						}
					}
					
					@SuppressWarnings("unchecked")
					@Override
					public Class<? extends Timespan>[] acceptChange(final ChangeMode mode) {
						if (mode == ChangeMode.CLEAR || mode == ChangeMode.SET)
							return null;
						return Skript.array(Timespan.class);
					}
				}));
		
		Classes.registerClass(new ClassInfo<Direction>(Direction.class, "direction", "direction")
				.user("direction")
				.defaultExpression(new SimpleLiteral<Direction>(new Direction(new double[] {0,0,0}), true))
				.parser(new Parser<Direction>() {
					
					@Override
					public Direction parse(final String s, final ParseContext context) {
						return null;
					}
					
					@Override
					public boolean canParse(final ParseContext context) {
						return false;
					}
					
					@Override
					public String toString(final Direction o) {
						return o.toString();
					}
					
					@Override
					public String toVariableNameString(final Direction o) {
						return "direction:" + o.serialize();
					}
					
					@Override
					public String getVariableNamePattern() {
						return "direction:.+";
					}
				})
				.serializer(new Serializer<Direction>() {
					@Override
					public String serialize(final Direction o) {
						return o.serialize();
					}
					
					@Override
					public Direction deserialize(final String s) {
						return Direction.deserialize(s);
					}
				}));
		
		Classes.registerClass(new ClassInfo<Slot>(Slot.class, "slot", "slot")
				.defaultExpression(new EventValueExpression<Slot>(Slot.class))
				.changer(new SerializableChanger<Slot, ItemType>() {
					private static final long serialVersionUID = 392597470786335167L;
					
					@SuppressWarnings("unchecked")
					@Override
					public Class<ItemType>[] acceptChange(final ch.njol.skript.classes.Changer.ChangeMode mode) {
						return Skript.array(ItemType.class);
					}
					
					@Override
					public void change(final Slot[] slots, final ItemType delta, final ch.njol.skript.classes.Changer.ChangeMode mode) {
						final ItemType type = delta;
						if (type == null && mode != ChangeMode.CLEAR)
							return;
						for (final Slot slot : slots) {
							switch (mode) {
								case SET:
									slot.setItem(type.getItem().getRandom());
									break;
								case ADD:
									slot.setItem(type.getItem().addTo(slot.getItem()));
									break;
								case REMOVE:
									slot.setItem(type.removeFrom(slot.getItem()));
									break;
								case CLEAR:
									slot.setItem(null);
							}
						}
					}
					
				}).serializeAs(ItemStack.class));
		
		Classes.registerClass(new ClassInfo<Color>(Color.class, "color", "color")
				.user("colou?rs?")
				.parser(new Parser<Color>() {
					@Override
					public String toString(final Color c) {
						return c.toString();
					}
					
					@Override
					public Color parse(final String s, final ParseContext context) {
						return Color.byName(s);
					}
					
					@Override
					public String toVariableNameString(final Color o) {
						return o.name().toLowerCase(Locale.ENGLISH).replace('_', ' ');
					}
					
					@Override
					public String getVariableNamePattern() {
						return "[a-z ]+";
					}
				}).serializer(new EnumSerializer<Color>(Color.class)));
		
		Classes.registerClass(new ClassInfo<StructureType>(StructureType.class, "structuretype", "tree type")
				.user("tree ?types?", "trees?")
				.parser(new Parser<StructureType>() {
					
					@Override
					public StructureType parse(final String s, final ParseContext context) {
						return StructureType.fromName(s);
					}
					
					@Override
					public String toString(final StructureType o) {
						return o.toString().toLowerCase().replace('_', ' ');
					}
					
					@Override
					public String toVariableNameString(final StructureType o) {
						return o.toString().toLowerCase().replace('_', ' ');
					}
					
					@Override
					public String getVariableNamePattern() {
						return "[a-z ]+";
					}
					
				}).serializer(new EnumSerializer<StructureType>(StructureType.class)));
		
		Classes.registerClass(new ClassInfo<EnchantmentType>(EnchantmentType.class, "enchantmenttype", "enchantment")
				.parser(new Parser<EnchantmentType>() {
					@Override
					public EnchantmentType parse(final String s, final ParseContext context) {
						return EnchantmentType.parse(s);
					}
					
					@Override
					public String toString(final EnchantmentType t) {
						return t.toString();
					}
					
					@Override
					public String toVariableNameString(final EnchantmentType o) {
						return o.toString();
					}
					
					@Override
					public String getVariableNamePattern() {
						return ".+";
					}
				})
				.serializer(new Serializer<EnchantmentType>() {
					@Override
					public String serialize(final EnchantmentType o) {
						return o.getType().getId() + ":" + o.getLevel();
					}
					
					@Override
					public EnchantmentType deserialize(final String s) {
						final String[] split = s.split(":");
						if (split.length != 2)
							return null;
						try {
							final Enchantment ench = Enchantment.getById(Integer.parseInt(split[0]));
							if (ench == null)
								return null;
							return new EnchantmentType(ench, Integer.parseInt(split[1]));
						} catch (final NumberFormatException e) {
							return null;
						}
					}
				}));
		
	}
	
}
