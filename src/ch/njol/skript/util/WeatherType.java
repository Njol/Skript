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

import org.bukkit.World;

/**
 * 
 * @author Peter Güttinger
 * 
 */
public enum WeatherType {
	
	clear("clear", "sun", "sunny"), rain("rain", "rainy", "raining"), thunder("thunder", "thundering");
	
	private final String[] names;
	
	private final static HashMap<String, WeatherType> byName = new HashMap<String, WeatherType>();
	
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
		if (world == null)
			return null;
		if (world.isThundering())
			return thunder;
		if (world.hasStorm())
			return rain;
		return clear;
	}
	
	@Override
	public String toString() {
		switch (this) {
			case clear:
				return "clear";
			case rain:
				return "rain";
			case thunder:
				return "thunder";
		}
		throw new IllegalStateException();
	}
	
	public String adjective() {
		switch (this) {
			case clear:
				return "sunny";
			case rain:
				return "raining";
			case thunder:
				return "thundering";
		}
		throw new IllegalStateException();
	}
	
	public boolean isWeather(final World w) {
		return isWeather(w.hasStorm(), w.isThundering());
	}
	
	public boolean isWeather(final boolean rain, final boolean thunder) {
		switch (this) {
			case clear:
				return !thunder && !rain;
			case rain:
				return !thunder && rain;
			case thunder:
				return thunder && rain;
			default:
				throw new RuntimeException();
		}
	}
	
	public void setWeather(final World w) {
		w.setStorm(this == rain || this == thunder);
		w.setThundering(this == thunder);
	}
	
}
