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

import java.io.Serializable;
import java.util.Arrays;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Entity;
import org.bukkit.material.Directional;
import org.bukkit.util.Vector;

import ch.njol.skript.Language;
import ch.njol.skript.Skript;

/**
 * 
 * @author Peter Güttinger
 */
public class Direction implements Serializable {
	
	private static final long serialVersionUID = 8472701621458780052L;
	
	/**
	 * A direction that doesn't point anywhere
	 */
	public final static Direction ZERO = new Direction(new double[] {0, 0, 0});
	/**
	 * A direction that points in the direction of the object(s) passed to it's various <tt>getDirection</tt> methods.
	 */
	public final static Direction IDENTITY = new Direction(0, 0, 1);
	
	// These two would be in a union if this were C
	// relative offset
	private final double[] mod;
	// relative rotation
	private final double pitch, yaw, length;
	// whether this direction is rotational (i.e. depends on some object) or translational/directional (i.e. depends on the coordinate system but nothing else)
	private final boolean relative;
	
	public Direction(final double[] mod) {
		if (mod == null || mod.length != 3)
			throw new IllegalArgumentException();
		this.mod = mod;
		relative = false;
		pitch = yaw = length = 0;
	}
	
	public Direction(final double pitch, final double yaw, final double length) {
		this.pitch = pitch;
		this.yaw = yaw;
		this.length = length;
		relative = true;
		mod = null;
	}
	
	public Direction(final BlockFace f) {
		this(new Vector(f.getModX(), f.getModY(), f.getModZ()).normalize());
	}
	
	public Direction(final Vector v) {
		mod = new double[] {v.getX(), v.getY(), v.getZ()};
		relative = false;
		pitch = yaw = length = 0;
	}
	
	public Location getRelative(final Location l) {
		return l.clone().add(getDirection(l));
	}
	
	public Location getRelative(final Entity e) {
		return e.getLocation().add(getDirection(e.getLocation()));
	}
	
	public Location getRelative(final Block b) {
		return b.getLocation().add(getDirection(b));
	}
	
	public Vector getDirection(final Location l) {
		if (!relative)
			return new Vector(mod[0], mod[1], mod[2]);
		return getDirection(pitch + pitchToRadians(l.getPitch()), yaw + yawToRadians(l.getYaw())).multiply(length);
	}
	
	public Vector getDirection(final Entity e) {
		return getDirection(e.getLocation());
	}
	
	public Vector getDirection(final Block b) {
		if (!relative)
			return new Vector(mod[0], mod[1], mod[2]);
		final Material m = b.getType();
		if (!Directional.class.isAssignableFrom(m.getData()))
			return new Vector();
		final BlockFace f = ((Directional) m.getNewData(b.getData())).getFacing(); // TODO costly? instantiation of the MaterialData using reflection
		return getDirection(pitch + f.getModZ() * Math.PI / 2 /* only up and down have a z mod */, yaw + Math.atan2(f.getModZ(), f.getModX())).multiply(length);
	}
	
	@Override
	public int hashCode() {
		return (relative ? 7 : 3) * Arrays.hashCode(mod);
	}
	
	@Override
	public boolean equals(final Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (!(obj instanceof Direction))
			return false;
		final Direction other = (Direction) obj;
		return relative == other.relative && Arrays.equals(mod, other.mod) && pitch == other.pitch && yaw == other.yaw && other.length == length;
	}
	
	/**
	 * 
	 * @return Whether this Direction rotates the direction of a given object or only translates it.
	 */
	public boolean isRelative() {
		return relative;
	}
	
	/**
	 * @param pitch Notch-pitch
	 * @return Mathematical pitch oriented positively (from x/y to z axis) with the origin in the x/y plane
	 */
	public final static double pitchToRadians(final float pitch) {
		return -Math.toRadians(pitch);
	}
	
	/**
	 * @param yaw Notch-yaw
	 * @return mathematical yaw oriented positively (from x to z axis) with the origin at the x axis
	 */
	public final static double yawToRadians(final float yaw) {
		return -Math.toRadians(yaw) - Math.PI / 2;
	}
	
	public final static Vector getDirection(final double pitch, final double yaw) {
		final double lxz = Math.cos(pitch);
		return new Vector(Math.cos(yaw) * lxz, Math.sin(pitch), Math.sin(yaw) * lxz);
	}
	
	public final static Vector getDirection(final double pitch, final double yaw, final double length) {
		final double lxz = Math.cos(pitch) * length;
		return new Vector(Math.cos(yaw) * lxz, Math.sin(pitch) * length, Math.sin(yaw) * lxz);
	}
	
	public final static BlockFace getFacing(final Block b) {
		final Material m = b.getType();
		if (!Directional.class.isAssignableFrom(m.getData()))
			return BlockFace.SELF;
		return ((Directional) m.getNewData(b.getData())).getFacing(); // TODO costly? instantiation of the MaterialData using reflection
	}
	
	public final static Location[] getRelatives(final Block[] blocks, final Direction[] directions) {
		final Location[] r = new Location[blocks.length * directions.length];
		if (r.length == 0)
			return r;
		for (int i = 0; i < blocks.length; i++) {
			r[i] = blocks[i].getLocation();
			for (int j = 0; j < directions.length; j++) {
				r[i].add(directions[j].getDirection(blocks[i]));
			}
		}
		return r;
	}
	
	public final static Location[] getRelatives(final Location[] locations, final Direction[] directions) {
		final Location[] r = new Location[locations.length * directions.length];
		if (r.length == 0)
			return r;
		for (int i = 0; i < locations.length; i++) {
			r[i] = locations[i].clone();
			for (int j = 0; j < directions.length; j++) {
				r[i].add(directions[j].getDirection(locations[i]));
			}
		}
		return r;
	}
	
	@Override
	public String toString() {
		return relative ? toString(pitch, yaw, length) : toString(mod);
	}
	
	private final static String[] relativeDirections = {"front", "behind", "left", "right", "above", "below"};
	
	public final static String toString(final double pitch, final double yaw, final double length) {
		final double front = Math.cos(pitch) * Math.cos(yaw) * length;
		final double left = Math.cos(pitch) * Math.sin(yaw) * length;
		final double above = Math.sin(pitch) * length;
		return toString(new double[] {front, left, above}, relativeDirections);
	}
	
	// x,y,z = south, up, west
	private final static String[] absoluteDirections = {"south", "north", "up", "down", "west", "east"};
	
	public final static String toString(final double[] mod) {
		if (mod[0] == 0 && mod[1] == 0 && mod[2] == 0)
			return Language.get("directions.at");
		return toString(mod, absoluteDirections);
	}
	
	public final static String toString(final Vector dir) {
		if (dir.getX() == 0 && dir.getY() == 0 && dir.getZ() == 0)
			return Language.get("directions.at");
		return toString(new double[] {dir.getX(), dir.getY(), dir.getZ()}, absoluteDirections);
	}
	
	private final static String toString(final double[] mod, final String[] names) {
		assert mod.length == 3 && names.length == 6;
		final StringBuilder b = new StringBuilder();
		for (int i = 0; i < 3; i++) {
			toString(b, mod[i], names[2 * i], names[2 * i + 1], b.length() != 0);
		}
		return b.toString();
	}
	
	private final static void toString(final StringBuilder b, final double d, final String direction, final String oppositeDirection, final boolean prependAnd) {
		if (d == 0)
			return;
		if (prependAnd)
			b.append(Language.getSpaced("and"));
		b.append(Skript.toString(Math.abs(d)));
		b.append(" ");
		b.append(Language.get("directions.meter", d != 1));
		b.append(" ");
		b.append(Language.get(d > 0 ? "directions." + direction : "directions." + oppositeDirection));
	}
	
	public String serialize() {
		return "" + relative + ":" + (relative ? pitch + "," + yaw : mod[0] + "," + mod[1] + "," + mod[2]);
	}
	
	public static Direction deserialize(final String s) {
		final String[] split = s.split(":");
		if (split.length != 2)
			return null;
		final boolean relative = Boolean.parseBoolean(split[0]);
		if (relative) {
			final String[] split2 = s.split(",");
			if (split.length != 3)
				return null;
			try {
				return new Direction(Double.parseDouble(split2[0]), Double.parseDouble(split2[1]), Double.parseDouble(split2[2]));
			} catch (final NumberFormatException e) {
				return null;
			}
		} else {
			final String[] split2 = s.split(",");
			if (split.length != 3)
				return null;
			try {
				return new Direction(new Vector(Double.parseDouble(split2[0]), Double.parseDouble(split2[1]), Double.parseDouble(split2[2])));
			} catch (final NumberFormatException e) {
				return null;
			}
		}
	}
	
}
