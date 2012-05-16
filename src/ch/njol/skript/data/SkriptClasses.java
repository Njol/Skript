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

import org.bukkit.World;
import org.bukkit.entity.Entity;
import org.bukkit.event.Event;

import ch.njol.skript.Aliases;
import ch.njol.skript.Skript;
import ch.njol.skript.api.Changer.ChangeMode;
import ch.njol.skript.api.ClassInfo;
import ch.njol.skript.api.Parser;
import ch.njol.skript.api.exception.ParseException;
import ch.njol.skript.lang.ExprParser;
import ch.njol.skript.lang.SimpleLiteral;
import ch.njol.skript.lang.Variable;
import ch.njol.skript.util.EntityType;
import ch.njol.skript.util.ItemType;
import ch.njol.skript.util.Offset;
import ch.njol.skript.util.Slot;
import ch.njol.skript.util.Time;
import ch.njol.skript.util.Timeperiod;
import ch.njol.skript.util.Timespan;
import ch.njol.skript.util.Utils;
import ch.njol.skript.util.VariableString;
import ch.njol.skript.util.WeatherType;
import ch.njol.skript.variables.base.EventValueVariable;

/**
 * @author Peter Güttinger
 * 
 */
public class SkriptClasses {
	
	public SkriptClasses() {}
	
	public static final class WeatherTypeDefaultVariable extends SimpleLiteral<WeatherType> {
		public WeatherTypeDefaultVariable() {
			super(WeatherType.clear);
		}
	}
	
	static {
		Skript.addClass(new ClassInfo<WeatherType>("weather type", "weathertype", WeatherType.class, WeatherTypeDefaultVariable.class, new Parser<WeatherType>() {
			
			@Override
			public WeatherType parse(final String s) {
				return WeatherType.parse(s);
			}
			
			@Override
			public String toString(final WeatherType o) {
				return o.toString();
			}
			
		}, "weather ?types?", "weather conditions", "weathers?"));
	}
	
	public static final class EntityTypeDefaultVariable extends SimpleLiteral<EntityType> {
		public EntityTypeDefaultVariable() {
			super(new EntityType(Entity.class, 1));
		}
	}
	
	static {
		Skript.addClass(new ClassInfo<EntityType>("entity type", "entitytype", EntityType.class, EntityTypeDefaultVariable.class, new Parser<EntityType>() {
			@Override
			public EntityType parse(final String s) {
				return Utils.getEntityType(s);
			}
			
			@Override
			public String toString(final EntityType o) {
				return o.toString();
			}
		}, "entity ?types?", "enit(y|ies)"));
	}
	
	static {
		Skript.addClass(new ClassInfo<VariableString>("variablestring", VariableString.class, null, new Parser<VariableString>() {
			
			@Override
			public VariableString parse(final String s) {
				if (!s.startsWith("\"") || !s.endsWith("\""))
					return null;
				if (!s.matches(ExprParser.stringMatcher)) {
					Skript.error(Skript.quotesError);
					return null;
				}
				try {
					return new VariableString(s.substring(1, s.length() - 1).replace("\"\"", "\""));
				} catch (final ParseException e) {
					return null;
				}
			}
			
			@Override
			public String toString(final VariableString o) {
				return o.getDebugMessage(null);
			}
			
		}));
	}
	
	static {
		Skript.addClass(new ClassInfo<ItemType>("item type", "itemtype", ItemType.class, null, new Parser<ItemType>() {
			@Override
			public ItemType parse(final String s) {
				return Aliases.parseItemType(s);
			}
			
			@Override
			public String toString(final ItemType o) {
				return o.toString();
			}
		}, "item ?type", "items", "materials"));
	}
	
	public static final class TimeDefaultVariable extends EventValueVariable<Time> {
		public TimeDefaultVariable() {
			super(Time.class);
		}
		
		@Override
		public Class<?> acceptChange(final ChangeMode mode) {
			return DefaultChangers.timeChanger.acceptChange(mode);
		}
		
		@Override
		public void change(final Event e, final Variable<?> delta, final ChangeMode mode) {
			DefaultChangers.timeChanger.change(e, new SimpleLiteral<World>(Skript.getEventValue(e, World.class)), delta, mode);
		}
	}
	
	static {
		Skript.addClass(new ClassInfo<Time>("time", "time", Time.class, TimeDefaultVariable.class, new Parser<Time>() {
			
			@Override
			public Time parse(final String s) {
				return Time.parse(s);
			}
			
			@Override
			public String toString(final Time t) {
				return t.toString();
			}
			
		}, "times?"));
	}
	
	static {
		new Timespan();
	}
	
	public static final class TimeperiodDefaultVariable extends SimpleLiteral<Timeperiod> {
		public TimeperiodDefaultVariable() {
			super(new Timeperiod(0, 23999));
		}
	}
	
	static {
		Skript.addClass(new ClassInfo<Timeperiod>("time period", "timeperiod", Timeperiod.class, TimeperiodDefaultVariable.class, new Parser<Timeperiod>() {
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
		}, "time ?periods?", "durations?"));
	}
	
	public static final class OffsetDefaultVariable extends SimpleLiteral<Offset> {
		public OffsetDefaultVariable() {
			super(new Offset(0, 0, 0));
		}
	}
	
	static {
		Skript.addClass(new ClassInfo<Offset>("offset", "offset", Offset.class, OffsetDefaultVariable.class, new Parser<Offset>() {
			
			@Override
			public Offset parse(final String s) {
				return Offset.parse(s);
			}
			
			@Override
			public String toString(final Offset o) {
				return o.toString();
			}
		}, "offset"));
	}
	
	public static final class SlotDefaultVariable extends EventValueVariable<Slot> {
		public SlotDefaultVariable() {
			super(Slot.class);
		}
	}
	
	static {
		Skript.addClass(new ClassInfo<Slot>("slot", Slot.class, SlotDefaultVariable.class, null));
	}
	
}
