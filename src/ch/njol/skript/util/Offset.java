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

import java.util.Arrays;
import java.util.Locale;

import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.util.Vector;

/**
 * 
 * @author Peter Güttinger
 * 
 */
public class Offset {
	private final double[] mod;
	private boolean isOffset;
	
	private void checkIsOffset() {
		isOffset = mod[0] != 0 || mod[1] != 0 || mod[2] != 0;
	}
	
	public Offset(final double modX, final double modY, final double modZ) {
		mod = new double[] {modX, modY, modZ};
		checkIsOffset();
	}
	
	public Offset(final double[] mod) {
		if (mod == null || mod.length != 3)
			throw new IllegalArgumentException();
		this.mod = mod;
		checkIsOffset();
	}
	
	public Block getRelative(final Block block) {
		if (block == null || !isOffset)
			return block;
		return block.getRelative((int) Math.round(mod[0]), (int) Math.round(mod[1]), (int) Math.round(mod[2]));
	}
	
	/**
	 * @param location the Location to get a relative to
	 * @return a new location
	 */
	public Location getRelative(final Location location) {
		if (location == null)
			return null;
		if (!isOffset)
			return location.clone();
		return location.clone().add(mod[0], mod[1], mod[2]);
	}
	
	/**
	 * @param location the Location to change
	 * @return the same location
	 */
	public Location setOff(final Location location) {
		if (location == null || !isOffset)
			return location;
		return location.add(mod[0], mod[1], mod[2]);
	}
	
	public static Location[] setOff(final Offset[] offsets, final Location[] locations) {
		final Location[] off = new Location[locations.length * offsets.length];
		for (int i = 0; i < locations.length; i++) {
			if (locations[i] == null)
				continue;
			for (int j = 0; j < offsets.length; j++) {
				off[offsets.length * i + j] = offsets[j].getRelative(locations[i]);
			}
		}
		return off;
	}
	
	public static Block[] setOff(final Offset[] offsets, final Block[] blocks) {
		final Block[] off = new Block[blocks.length * offsets.length];
		for (int i = 0; i < blocks.length; i++) {
			if (blocks[i] == null)
				continue;
			for (int j = 0; j < offsets.length; j++) {
				off[offsets.length * i + j] = offsets[j].getRelative(blocks[i]);
			}
		}
		return off;
	}
	
	public boolean isOffset() {
		return isOffset;
	}
	
	@Override
	public String toString() {
		if (!isOffset)
			return "at";
		String r = "";
		final BlockFace[] dirs = {BlockFace.NORTH, BlockFace.UP, BlockFace.EAST};
		for (final int i : new int[] {0, 2, 1}) {
			if (mod[i] == 0)
				continue;
			if (mod[i] * Utils.getBlockFaceDir(dirs[i], i) > 0)
				r += (r.isEmpty() ? "" : " ") + mod[i] + " " + getFaceName(dirs[i]);
			else
				r += (r.isEmpty() ? "" : " ") + -mod[i] + " " + getFaceName(dirs[i].getOppositeFace());
		}
		return r;
	}
	
	@SuppressWarnings("incomplete-switch")
	private static String getFaceName(final BlockFace face) {
		switch (face) {
			case DOWN:
				return "below";
			case UP:
				return "above";
			case EAST:
			case NORTH:
			case SOUTH:
			case WEST:
				return face.toString().toLowerCase(Locale.ENGLISH);
		}
		return null;
	}
	
	@Override
	public int hashCode() {
		return Arrays.hashCode(mod);
	}
	
	@Override
	public boolean equals(final Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		final Offset other = (Offset) obj;
		if (!isOffset && !other.isOffset)
			return true;
		if (!Arrays.equals(mod, other.mod))
			return false;
		return true;
	}
	
	public static Offset combine(final Offset[] offsets) {
		if (offsets == null || offsets.length == 0)
			return null;
		if (offsets.length == 1)
			return offsets[0];
		final double[] mod = new double[3];
		for (final Offset o : offsets) {
			for (int axis = 0; axis < 3; axis++) {
				mod[axis] += o.mod[axis];
			}
		}
		return new Offset(mod);
	}
	
	/**
	 * 
	 * @param s trim()med string
	 * @return parsed offset or null if input is invalid
	 */
	public static Offset parse(final String s) {
		if (s.isEmpty())
			return null;
		final double[] mod = new double[3];
		final String[] args = s.split(" ");
		double amount = -1;
		final int end = s.endsWith("of") ? args.length - 1 : args.length;
		for (int i = 0; i < end; i++) {
			final String arg = args[i];
			try {
				final double n = Double.parseDouble(arg);
				if (amount != -1)
					return null;
				amount = n;
				continue;
			} catch (final NumberFormatException e) {}
			final BlockFace f = Utils.getBlockFace(arg, false);
			if (f != null) {
				if (amount == -1)
					amount = 1;
				mod[0] += f.getModX() * amount;
				mod[1] += f.getModY() * amount;
				mod[2] += f.getModZ() * amount;
				amount = -1;
			} else {
				return null;
			}
		}
		return new Offset(mod);
	}
	
	public Vector toVector() {
		return new Vector(mod[0], mod[1], mod[2]);
	}
	
}
