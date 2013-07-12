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
 * Copyright 2011-2013 Peter Güttinger
 * 
 */

package ch.njol.skript.util;

import java.io.Serializable;
import java.util.Arrays;
import java.util.Locale;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Entity;
import org.bukkit.event.Event;
import org.bukkit.material.Directional;
import org.bukkit.util.Vector;

import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.Literal;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.skript.lang.util.SimpleExpression;
import ch.njol.skript.localization.GeneralWords;
import ch.njol.skript.localization.Language;
import ch.njol.skript.localization.Message;
import ch.njol.skript.localization.Noun;
import ch.njol.util.Kleenean;

/**
 * @author Peter Güttinger
 */
@SuppressWarnings("serial")
public class Direction implements Serializable {
	
	/**
	 * A direction that doesn't point anywhere, i.e. equal to 'at'.
	 */
	public final static Direction ZERO = new Direction(new double[] {0, 0, 0});
	/**
	 * A direction that points in the direction of the object(s) passed to the various <tt>getDirection</tt> methods.
	 */
	public final static Direction IDENTITY = new Direction(0, 0, 1);
	
	public final static BlockFace BF_X = findFace(1, 0, 0), BF_Y = findFace(0, 1, 0), BF_Z = findFace(0, 0, 1);
	
	private final static BlockFace findFace(final int x, final int y, final int z) {
		for (final BlockFace f : BlockFace.values()) {
			if (f.getModX() == x && f.getModY() == y && f.getModZ() == z)
				return f;
		}
		assert false;
		return null;
	}
	
	public final static Noun m_meter = new Noun("directions.meter");
	
	// These two would be in a union if this were written in C
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
	
	/**
	 * Use this as pitch to force a horizontal direction
	 */
	public final static double IGNORE_PITCH = 0xF1A7; // FLAT
	
	public Direction(final double pitch, final double yaw, final double length) {
		this.pitch = pitch;
		this.yaw = yaw;
		this.length = length;
		relative = true;
		mod = null;
	}
	
	public Direction(final BlockFace f, final double length) {
		this(new Vector(f.getModX(), f.getModY(), f.getModZ()).normalize().multiply(length));
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
		return getDirection(pitch == IGNORE_PITCH ? 0 : pitchToRadians(l.getPitch()), yawToRadians(l.getYaw()));
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
		final BlockFace f = ((Directional) m.getNewData(b.getData())).getFacing();
		return getDirection(pitch == IGNORE_PITCH ? 0 : f.getModZ() * Math.PI / 2 /* only up and down have a z mod */, Math.atan2(f.getModZ(), f.getModX()));
	}
	
	private Vector getDirection(final double p, final double y) {
		if (pitch == IGNORE_PITCH)
			return new Vector(Math.cos(y + yaw) * length, 0, Math.sin(y + yaw) * length);
		final double lxz = Math.cos(p + pitch) * length;
		return new Vector(Math.cos(y + yaw) * lxz, Math.sin(p + pitch) * Math.cos(yaw) * length, Math.sin(y + yaw) * lxz);
	}
	
	@Override
	public int hashCode() {
		return relative ? Arrays.asList(pitch, yaw, length).hashCode() : Arrays.hashCode(mod);
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
		return relative == other.relative && (relative ? pitch == other.pitch && yaw == other.yaw && other.length == length : Arrays.equals(mod, other.mod));
	}
	
	/**
	 * @return Whether this Direction rotates the direction of a given object or only translates it.
	 */
	public boolean isRelative() {
		return relative;
	}
	
	/**
	 * @param pitch Notch-pitch
	 * @return Mathematical pitch oriented from x/z to y axis (with the origin in the x/z plane)
	 */
	public final static double pitchToRadians(final float pitch) {
		return -Math.toRadians(pitch);
	}
	
	/**
	 * @param pitch Mathematical pitch oriented from x/z to y axis (with the origin in the x/z plane)
	 * @return Notch-pitch
	 */
	public final static float getPitch(final double pitch) {
		return (float) Math.toDegrees(-pitch);
	}
	
	/**
	 * @param yaw Notch-yaw
	 * @return Mathematical yaw oriented from x to z axis (with the origin at the x axis)
	 */
	public final static double yawToRadians(final float yaw) {
		return Math.toRadians(yaw) + Math.PI / 2;
	}
	
	/**
	 * @param yaw Mathematical yaw oriented from x to z axis (with the origin at the x axis)
	 * @return Notch-yaw
	 */
	public final static float getYaw(final double yaw) {
		return (float) Math.toDegrees(yaw - Math.PI / 2);
	}
	
	/**
	 * @param b
	 * @return The facing of the block or {@link BlockFace#SELF} if the block doesn't have a facing.
	 */
	public final static BlockFace getFacing(final Block b) {
		final Material m = b.getType();
		if (!Directional.class.isAssignableFrom(m.getData()))
			return BlockFace.SELF;
		return ((Directional) m.getNewData(b.getData())).getFacing();
	}
	
	public final static BlockFace getFacing(final Location l, final boolean horizontal) {
		final double yaw = (yawToRadians(l.getYaw()) + 2 * Math.PI) % (2 * Math.PI);
		final double pitch = horizontal ? 0 : pitchToRadians(l.getPitch());
		if (horizontal || -Math.PI / 4 < pitch && pitch < Math.PI / 4) {
			if (yaw < Math.PI / 4 || yaw >= Math.PI * 7 / 4)
				return BF_X;
			if (yaw < Math.PI * 3 / 4)
				return BF_Z;
			if (yaw < Math.PI * 5 / 4)
				return BF_X.getOppositeFace();
			assert yaw < Math.PI * 7 / 4;
			return BF_Z.getOppositeFace();
		}
		if (pitch > 0)
			return BlockFace.UP;
		return BlockFace.DOWN;
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
		return relative ? toString(pitch == IGNORE_PITCH ? 0 : pitch, yaw, length) : toString(mod);
	}
	
	public final static String toString(final double pitch, final double yaw, final double length) {
		final double front = Math.cos(pitch) * Math.cos(yaw) * length;
		final double left = Math.cos(pitch) * Math.sin(yaw) * length;
		final double above = Math.sin(pitch) * length;
		return toString(new double[] {front, left, above}, relativeDirections);
	}
	
	private final static Message m_at = new Message("directions.at");
	private final static Message[] absoluteDirections = new Message[6];
	private final static Message[] relativeDirections = new Message[6];
	static {
		final String[] rd = {"front", "behind", "left", "right", "above", "below"};
		for (int i = 0; i < rd.length; i++) {
			relativeDirections[i] = new Message("directions." + rd[i]);
		}
		final String[] ad = {
				BF_X.name().toLowerCase(Locale.ENGLISH), BF_X.getOppositeFace().name().toLowerCase(Locale.ENGLISH),
				BF_Y.name().toLowerCase(Locale.ENGLISH), BF_Y.getOppositeFace().name().toLowerCase(Locale.ENGLISH),
				BF_Z.name().toLowerCase(Locale.ENGLISH), BF_Z.getOppositeFace().name().toLowerCase(Locale.ENGLISH)};
		for (int i = 0; i < ad.length; i++) {
			absoluteDirections[i] = new Message("directions." + ad[i]);
		}
	}
	
	public final static String toString(final double[] mod) {
		if (mod[0] == 0 && mod[1] == 0 && mod[2] == 0)
			return m_at.toString();
		return toString(mod, absoluteDirections);
	}
	
	public final static String toString(final Vector dir) {
		if (dir.getX() == 0 && dir.getY() == 0 && dir.getZ() == 0)
			return Language.get("directions.at");
		return toString(new double[] {dir.getX(), dir.getY(), dir.getZ()}, absoluteDirections);
	}
	
	private final static String toString(final double[] mod, final Message[] names) {
		assert mod.length == 3 && names.length == 6;
		final StringBuilder b = new StringBuilder();
		for (int i = 0; i < 3; i++) {
			toString(b, mod[i], names[2 * i], names[2 * i + 1], b.length() != 0);
		}
		return b.toString();
	}
	
	private final static void toString(final StringBuilder b, final double d, final Message direction, final Message oppositeDirection, final boolean prependAnd) {
		if (d == 0)
			return;
		if (prependAnd)
			b.append(" ").append(GeneralWords.and).append(" ");
		if (d != 1 && d != -1) {
			b.append(m_meter.withAmount(Math.abs(d)));
			b.append(" ");
		}
		b.append(d > 0 ? direction : oppositeDirection);
	}
	
	public String serialize() {
		return "" + relative + ":" + (relative ? pitch + "," + yaw + "," + length : mod[0] + "," + mod[1] + "," + mod[2]);
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
				return new Direction(new double[] {Double.parseDouble(split2[0]), Double.parseDouble(split2[1]), Double.parseDouble(split2[2])});
			} catch (final NumberFormatException e) {
				return null;
			}
		}
	}
	
	public final static Expression<Location> combine(final Expression<? extends Direction> dirs, final Expression<? extends Location> locs) {
		return new SimpleExpression<Location>() {
			private static final long serialVersionUID = 1316369726663020906L;
			
			@Override
			protected Location[] get(final Event e) {
				final Direction[] ds = dirs.getArray(e);
				final Location[] ls = locs.getArray(e);
				final Location[] r = ds.length == 1 ? ls : new Location[ds.length * ls.length];
				for (int i = 0; i < ds.length; i++) {
					for (int j = 0; j < ls.length; j++) {
						r[i + j * ds.length] = ds[i].getRelative(ls[j]);
					}
				}
				return r;
			}
			
			@Override
			public Location[] getAll(final Event e) {
				final Direction[] ds = dirs.getAll(e);
				final Location[] ls = locs.getAll(e);
				final Location[] r = ds.length == 1 ? ls : new Location[ds.length * ls.length];
				for (int i = 0; i < ds.length; i++) {
					for (int j = 0; j < ls.length; j++) {
						r[i + j * ds.length] = ds[i].getRelative(ls[j]);
					}
				}
				return r;
			}
			
			@Override
			public boolean getAnd() {
				return (dirs.isSingle() || dirs.getAnd()) && (locs.isSingle() || locs.getAnd());
			}
			
			@Override
			public boolean isSingle() {
				return dirs.isSingle() && locs.isSingle();
			}
			
			@Override
			public Class<? extends Location> getReturnType() {
				return Location.class;
			}
			
			@Override
			public boolean init(final Expression<?>[] exprs, final int matchedPattern, final Kleenean isDelayed, final ParseResult parseResult) {
				throw new UnsupportedOperationException();
			}
			
			@Override
			public String toString(final Event e, final boolean debug) {
				return dirs.toString(e, debug) + " " + locs.toString(e, debug);
			}
			
			@Override
			public Expression<? extends Location> simplify() {
				if (dirs instanceof Literal && dirs.isSingle() && dirs.getSingle(null).equals(Direction.ZERO)) {
					return locs;
				}
				return this;
			}
		};
	}
	
}
