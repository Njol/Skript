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

import java.util.EnumMap;
import java.util.Map.Entry;

import org.bukkit.block.Biome;

import ch.njol.skript.localization.Message;

/**
 * @author Peter Güttinger
 */
public abstract class BiomeUtils {
	
	private BiomeUtils() {}
	
	private final static EnumMap<Biome, Message> names = new EnumMap<Biome, Message>(Biome.class);
	static {
		for (final Biome b : Biome.values()) {
			names.put(b, new Message("biomes." + b.name()));
		}
	}
	
	public final static Biome parse(final String s) {
		for (final Entry<Biome, Message> e : names.entrySet()) {
			if (e.getValue().toString().equalsIgnoreCase(s))
				return e.getKey();
		}
		return null;
	}
	
	public final static String toString(final Biome b) {
		return names.get(b).toString();
	}
	
}
