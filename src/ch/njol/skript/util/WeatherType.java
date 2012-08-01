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

package ch.njol.skript.util;

import java.util.HashMap;
import java.util.Map;

import org.apache.commons.lang.Validate;
import org.bukkit.World;

/**
 * 
 * @author Peter Güttinger
 * 
 */
public enum WeatherType {
	
	CLEAR("clear", "sun", "sunny"), RAIN("rain", "rainy", "raining"), THUNDER("thunder", "thundering", "thunderstorm");
	
	private final String[] names;
	
	private final static Map<String, WeatherType> byName = new HashMap<String, WeatherType>();
	
	private WeatherType(final String... names) {
		this.names = names;
	}
	
	static {
		for (final WeatherType t : values()) {
			for (final String name : t.names) {
				byName.put(name, t);
			}
		}
	}
	
	public static final WeatherType parse(final String s) {
		return byName.get(s);
	}
	
	public static WeatherType fromWorld(final World world) {
		Validate.notNull(world, "world");
		if (world.isThundering())
			return THUNDER;
		if (world.hasStorm())
			return RAIN;
		return CLEAR;
	}
	
	@Override
	public String toString() {
		return names[0];
	}
	
	public String adjective() {
		switch (this) {
			case CLEAR:
				return "sunny";
			case RAIN:
				return "raining";
			case THUNDER:
				return "thundering";
		}
		assert false;
		return null;
	}
	
	public boolean isWeather(final World w) {
		return isWeather(w.hasStorm(), w.isThundering());
	}
	
	public boolean isWeather(final boolean rain, final boolean thunder) {
		switch (this) {
			case CLEAR:
				return !thunder && !rain;
			case RAIN:
				return !thunder && rain;
			case THUNDER:
				return thunder && rain;
		}
		assert false;
		return false;
	}
	
	public void setWeather(final World w) {
		if (w.isThundering() != (this == THUNDER))
			w.setThundering(this == THUNDER);
		if (w.hasStorm() != (this != CLEAR))
			w.setStorm(this != CLEAR);
	}
	
}
