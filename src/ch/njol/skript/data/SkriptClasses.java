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

import org.bukkit.entity.Entity;

import ch.njol.skript.Aliases;
import ch.njol.skript.Skript;
import ch.njol.skript.api.ClassInfo;
import ch.njol.skript.api.Parser;
import ch.njol.skript.lang.SimpleLiteral;
import ch.njol.skript.util.EntityType;
import ch.njol.skript.util.ItemType;
import ch.njol.skript.util.Offset;
import ch.njol.skript.util.Slot;
import ch.njol.skript.util.Time;
import ch.njol.skript.util.Timeperiod;
import ch.njol.skript.util.Timespan;
import ch.njol.skript.util.VariableString;
import ch.njol.skript.util.WeatherType;
import ch.njol.skript.variables.base.EventValueVariable;

/**
 * @author Peter Güttinger
 * 
 */
public class SkriptClasses {
	
	public SkriptClasses() {}
	
	static {
		Skript.registerClass(new ClassInfo<WeatherType>("weather type", "weathertype", WeatherType.class, new SimpleLiteral<WeatherType>(WeatherType.CLEAR, true), new Parser<WeatherType>() {
			
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
	
	static {
		Skript.registerClass(new ClassInfo<EntityType>("entity type", "entitytype", EntityType.class, new SimpleLiteral<EntityType>(new EntityType(Entity.class, 1), true), new Parser<EntityType>() {
			@Override
			public EntityType parse(final String s) {
				return EntityType.parse(s);
			}
			
			@Override
			public String toString(final EntityType t) {
				return t.toString();
			}
		}, "entity ?types?", "enit(y|ies)"));
	}
	
	static {
		Skript.registerClass(new ClassInfo<VariableString>("_variablestring", VariableString.class, null, new Parser<VariableString>() {
			
			@Override
			public VariableString parse(final String s) {
				return null;
			}
			
			@Override
			public String toString(final VariableString vs) {
				return vs.getDebugMessage(null);
			}
			
		}));
	}
	
	static {
		Skript.registerClass(new ClassInfo<ItemType>("item type", "itemtype", ItemType.class, null, new Parser<ItemType>() {
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
		}, "item ?type", "items", "materials"));
	}
	
	static {
		Skript.registerClass(new ClassInfo<Time>("time", "time", Time.class, new EventValueVariable<Time>(Time.class), new Parser<Time>() {
			
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
	
	static {
		Skript.registerClass(new ClassInfo<Timeperiod>("time period", "timeperiod", Timeperiod.class, new SimpleLiteral<Timeperiod>(new Timeperiod(0, 23999), true), new Parser<Timeperiod>() {
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
	
	static {
		Skript.registerClass(new ClassInfo<Offset>("offset", "offset", Offset.class, new SimpleLiteral<Offset>(new Offset(0, 0, 0), true), new Parser<Offset>() {
			
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
	
	static {
		Skript.registerClass(new ClassInfo<Slot>("slot", Slot.class, new EventValueVariable<Slot>(Slot.class), null));
	}
	
}
