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

import org.bukkit.entity.Entity;
import org.bukkit.inventory.ItemStack;

import ch.njol.skript.Aliases;
import ch.njol.skript.Skript;
import ch.njol.skript.api.Changer;
import ch.njol.skript.api.Parser;
import ch.njol.skript.expressions.base.EventValueExpression;
import ch.njol.skript.lang.SimpleLiteral;
import ch.njol.skript.util.Color;
import ch.njol.skript.util.EntityType;
import ch.njol.skript.util.ItemData;
import ch.njol.skript.util.ItemType;
import ch.njol.skript.util.Offset;
import ch.njol.skript.util.Slot;
import ch.njol.skript.util.Time;
import ch.njol.skript.util.Timeperiod;
import ch.njol.skript.util.Timespan;
import ch.njol.skript.util.VariableString;
import ch.njol.skript.util.WeatherType;

/**
 * @author Peter Güttinger
 * 
 */
public class SkriptClasses {
	
	public SkriptClasses() {}
	
	static {
		Skript.registerClass(new ClassInfo<WeatherType>(WeatherType.class, "weathertype")
				.user("weather type", "weather ?types?", "weather conditions?", "weathers?")
				.defaultExpression(new SimpleLiteral<WeatherType>(WeatherType.CLEAR, true))
				.parser(new Parser<WeatherType>() {
					
					@Override
					public WeatherType parse(final String s) {
						return WeatherType.parse(s);
					}
					
					@Override
					public String toString(final WeatherType o) {
						return o.toString();
					}
					
				}).serializer(new EnumSerializer<WeatherType>(WeatherType.class)));
		
		Skript.registerClass(new ClassInfo<EntityType>(EntityType.class, "entitytype")
				.user("entity type", "entity ?types?", "enit(y|ies)")
				.defaultExpression(new SimpleLiteral<EntityType>(new EntityType(Entity.class, 1), true))
				.parser(new Parser<EntityType>() {
					@Override
					public EntityType parse(final String s) {
						return EntityType.parse(s);
					}
					
					@Override
					public String toString(final EntityType t) {
						return t.toString();
					}
				}).serializer(new Serializer<EntityType>() {
					@Override
					public String serialize(final EntityType t) {
						return t.c.getName() + "*" + t.amount;
					}
					
					@Override
					public EntityType deserialize(final String s) {
						final String[] split = s.split("\\*");
						if (split.length != 2)
							return null;
						try {
							return new EntityType(Class.forName(split[0]).asSubclass(Entity.class), Integer.parseInt(split[1]));
						} catch (final LinkageError e) {
							return null;
						} catch (final ClassNotFoundException e) {
							return null;
						} catch (final ClassCastException e) {
							return null;
						} catch (final NumberFormatException e) {
							return null;
						}
					}
				}));
		
		Skript.registerClass(new ClassInfo<VariableString>(VariableString.class, "variablestring")
				.parser(new Parser<VariableString>() {
					
					@Override
					public VariableString parse(final String s) {
						return null;
					}
					
					@Override
					public String toString(final VariableString vs) {
						return vs.getDebugMessage(null);
					}
					
				}));
		
		Skript.registerClass(new ClassInfo<ItemType>(ItemType.class, "itemtype")
				.user("item type", "item ?types?", "items", "materials")
				.parser(new Parser<ItemType>() {
					@Override
					public ItemType parse(final String s) {
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
				}).serializer(new Serializer<ItemType>() {
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
						return t.toString();
					}
					
					@Override
					public ItemType deserialize(final String s) {
						final String[] split = s.split("[,:/]");
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
							return t;
						} catch (final NumberFormatException e) {
							return null;
						}
					}
				}));
		
		Skript.registerClass(new ClassInfo<Time>(Time.class, "time")
				.user("time", "times?")
				.defaultExpression(new EventValueExpression<Time>(Time.class))
				.parser(new Parser<Time>() {
					
					@Override
					public Time parse(final String s) {
						return Time.parse(s);
					}
					
					@Override
					public String toString(final Time t) {
						return t.toString();
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
		
		new Timespan(0);
		
		Skript.registerClass(new ClassInfo<Timeperiod>(Timeperiod.class, "timeperiod")
				.user("time period", "time ?periods?", "durations?")
				.defaultExpression(new SimpleLiteral<Timeperiod>(new Timeperiod(0, 23999), true))
				.parser(new Parser<Timeperiod>() {
					@Override
					public Timeperiod parse(final String s) {
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
		
		Skript.registerClass(new ClassInfo<Offset>(Offset.class, "offset")
				.user("offset", "offset")
				.defaultExpression(new SimpleLiteral<Offset>(new Offset(0, 0, 0), true))
				.parser(new Parser<Offset>() {
					
					@Override
					public Offset parse(final String s) {
						return Offset.parse(s);
					}
					
					@Override
					public String toString(final Offset o) {
						return o.toString();
					}
				}));
		
		Skript.registerClass(new ClassInfo<Slot>(Slot.class, "slot")
				.defaultExpression(new EventValueExpression<Slot>(Slot.class))
				.changer(new Changer<Slot, ItemType>() {
					
					@Override
					public Class<ItemType> acceptChange(final ch.njol.skript.api.Changer.ChangeMode mode) {
						return ItemType.class;
					}
					
					@Override
					public void change(final Slot[] slots, final ItemType delta, final ch.njol.skript.api.Changer.ChangeMode mode) {
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
		
		Skript.registerClass(new ClassInfo<Color>(Color.class, "color")
				.user("color", "colou?rs?")
				.parser(new Parser<Color>() {
					@Override
					public String toString(final Color c) {
						return c.toString();
					}
					
					@Override
					public Color parse(final String s) {
						return Color.byName(s);
					}
				}).serializer(new EnumSerializer<Color>(Color.class)));
	}
	
}
