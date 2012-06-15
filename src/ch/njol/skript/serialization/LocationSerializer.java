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

package ch.njol.skript.serialization;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;

import ch.njol.skript.Serializer;
import ch.njol.skript.Skript;

/**
 * @author Peter Güttinger
 * 
 */
public class LocationSerializer implements Serializer<Location> {
	
	@Override
	public String serialize(final Location l) {
		return l.getWorld().getName() + ":" + l.getX() + "/" + l.getY() + "/" + l.getZ() + "|" + l.getYaw() + "/" + l.getPitch();
	}
	
	@Override
	public Location deserialize(final String s) {
		final String[] split = s.split("[:/|]");
		if (split.length != 6)
			return null;
		final World w = Bukkit.getWorld(split[0]);
		if (w == null) {
			Skript.error("World '" + split[0] + "' does not exist anymore");
			return null;
		}
		try {
			final double[] l = new double[5];
			for (int i = 0; i < 5; i++)
				l[i] = Double.parseDouble(split[i + 1]);
			return new Location(w, l[0], l[1], l[2], (float) l[3], (float) l[4]);
		} catch (final NumberFormatException e) {
			return null;
		}
	}
	
}
