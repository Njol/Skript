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
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.util.Vector;

import ch.njol.skript.Skript;
import ch.njol.util.StringUtils;

/**
 * 
 * @author Peter Güttinger
 */
public class Offset {
	private final double[] mod;
	private final boolean isOffset;
	
	public Offset(final double modX, final double modY, final double modZ) {
		mod = new double[] {modX, modY, modZ};
		isOffset = modX != 0 || modY != 0 || modZ != 0;
	}
	
	public Offset(final double[] mod) {
		if (mod == null || mod.length != 3)
			throw new IllegalArgumentException();
		this.mod = mod;
		isOffset = mod[0] != 0 || mod[1] != 0 || mod[2] != 0;
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
				throw new IllegalArgumentException("There must be no null elements in the locations array");
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
				throw new IllegalArgumentException("There must be no null elements in the blocks array");
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
		final StringBuilder r = new StringBuilder();
		
		boolean printNumbers = false;
		for (int i = 0; i < 3; i++)
			if (mod[i] != 0 && mod[i] != 1 && mod[i] != -1)
				printNumbers = true;
		
		final BlockFace[] dirs = {BlockFace.SOUTH, BlockFace.UP, BlockFace.WEST};// blockfaces with only positive mods, in order (x,y,z)
		for (final int i : new int[] {0, 2, 1}) {
			if (mod[i] == 0)
				continue;
			if (mod[i] * Utils.getBlockFaceDir(dirs[i], i) > 0)
				r.append((r.length() == 0 ? "" : " ") + (printNumbers ? StringUtils.toString(mod[i], Skript.NUMBERACCURACY) + " " : "") + getFaceName(dirs[i]));
			else
				r.append((r.length() == 0 ? "" : " ") + (printNumbers ? StringUtils.toString(-mod[i], Skript.NUMBERACCURACY) + " " : "") + getFaceName(dirs[i].getOppositeFace()));
		}
		return r.toString();
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
				return face.toString().toLowerCase();
		}
		throw new IllegalArgumentException();
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
		if (!(obj instanceof Offset))
			return false;
		final Offset other = (Offset) obj;
		if (!isOffset && !other.isOffset)
			return true;
		return Arrays.equals(mod, other.mod);
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
	
	private final static Pattern offsetPattern = Pattern.compile(
			"( (\\d+(\\.\\d+)?( (block|meter)s?)? )?((to the )?((south|north)(-?(east|west))?|east|west)(ward(s|ly)?|er(n|ly))?( of)?|above|over|up(ward(s|ly)?)?|below|under(neath)?|beneath|down(ward(s|ly)?)?))+");
	
	private final static BlockFace scanForFace(final String s, final int i) {
		if (s.startsWith("east", i))
			return BlockFace.EAST;
		if (s.startsWith("west", i))
			return BlockFace.WEST;
		if (s.startsWith("south", i)) {
			if (s.startsWith("east", i + 5) || s.startsWith("east", i + 6))
				return BlockFace.SOUTH_EAST;
			if (s.startsWith("west", i + 5) || s.startsWith("west", i + 6))
				return BlockFace.SOUTH_WEST;
			return BlockFace.SOUTH;
		}
		if (s.startsWith("north", i)) {
			if (s.startsWith("east", i + 5) || s.startsWith("east", i + 6))
				return BlockFace.NORTH_EAST;
			if (s.startsWith("west", i + 5) || s.startsWith("west", i + 6))
				return BlockFace.NORTH_WEST;
			return BlockFace.NORTH;
		}
		if (s.startsWith("up", i) || s.startsWith("above", i) || s.startsWith("over", i))
			return BlockFace.UP;
		if (s.startsWith("down", i) || s.startsWith("below", i) || s.startsWith("under", i) || s.startsWith("beneath", i))
			return BlockFace.DOWN;
		return null;
	}
	
	/**
	 * 
	 * @param s trim()med string
	 * @return parsed offset or null if input is invalid
	 */
	public static Offset parse(final String s) {
		if (s.isEmpty())
			return null;
		String lower = s.toLowerCase();
		if (lower.equals("at"))
			return new Offset(0, 0, 0);
		if (lower.startsWith("at "))
			lower = lower.substring(3);
		final Matcher m = offsetPattern.matcher(" " + lower);
		if (!m.matches())
			return null;
		
		final double[] mod = new double[3];
		double amount = 1;
		
		for (int i = 0; i < lower.length(); i++) {
			if ('0' <= lower.charAt(i) && lower.charAt(i) <= '9') {
				final int i2 = lower.indexOf(' ', i + 1);
				amount = Double.parseDouble(lower.substring(i, i2));
				i = i2 + 1;
			}
			final BlockFace f = scanForFace(lower, i);
			if (f != null) {
				i += f.name().length();
				mod[0] += amount * f.getModX();
				mod[1] += amount * f.getModY();
				mod[2] += amount * f.getModZ();
				amount = 1;
			}
		}
		return new Offset(mod);
	}
	
	public Vector toVector() {
		return new Vector(mod[0], mod[1], mod[2]);
	}
	
}
