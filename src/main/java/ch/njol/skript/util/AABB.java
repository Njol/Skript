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

import java.util.Iterator;
import java.util.NoSuchElementException;

import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.util.Vector;

import ch.njol.skript.Skript;

/**
 * @author Peter Güttinger
 */
public class AABB implements Iterable<Block> {
	
	private final World world;
	private final Vector lowerBound, upperBound;
	
	//	private final static Vector EPSILON = new Vector(Skript.EPSILON, Skript.EPSILON, Skript.EPSILON);
	
	public AABB(final Location l1, final Location l2) {
		if (l1.getWorld() != l2.getWorld())
			throw new IllegalArgumentException("Locations must be in the same world");
		world = l1.getWorld();
		lowerBound = new Vector(Math.min(l1.getX(), l2.getX()), Math.min(l1.getY(), l2.getY()), Math.min(l1.getZ(), l2.getZ()));
		upperBound = new Vector(Math.max(l1.getX(), l2.getX()), Math.max(l1.getY(), l2.getY()), Math.max(l1.getZ(), l2.getZ()));
	}
	
	public AABB(final Block b1, final Block b2) {
		if (b1.getWorld() != b2.getWorld())
			throw new IllegalArgumentException("Blocks must be in the same world");
		world = b1.getWorld();
		lowerBound = new Vector(Math.min(b1.getX(), b2.getX()), Math.min(b1.getY(), b2.getY()), Math.min(b1.getZ(), b2.getZ()));
		upperBound = new Vector(Math.max(b1.getX(), b2.getX()) + 1, Math.max(b1.getY(), b2.getY()) + 1, Math.max(b1.getZ(), b2.getZ()) + 1);
	}
	
	public AABB(final Location center, final double rX, final double rY, final double rZ) {
		world = center.getWorld();
		lowerBound = new Vector(center.getX() - rX, Math.max(center.getY() - rY, 0), center.getZ() - rZ);
		upperBound = new Vector(center.getX() + rX, Math.min(center.getY() + rY, world.getMaxHeight()), center.getZ() + rZ);
	}
	
	public boolean contains(final Location l) {
		if (l.getWorld() != world)
			return false;
		return lowerBound.getX() - Skript.EPSILON < l.getX() && l.getX() < upperBound.getX() + Skript.EPSILON
				&& lowerBound.getY() - Skript.EPSILON < l.getY() && l.getY() < upperBound.getY() + Skript.EPSILON
				&& lowerBound.getZ() - Skript.EPSILON < l.getZ() && l.getZ() < upperBound.getZ() + Skript.EPSILON;
	}
	
	public boolean contains(final Block b) {
		return contains(b.getLocation()) && contains(b.getLocation().add(1, 1, 1));
	}
	
	public Vector getDimensions() {
		return upperBound.clone().subtract(lowerBound);
	}
	
	public World getWorld() {
		return world;
	}
	
	/**
	 * Returns an iterator which iterates over all blocks that are
	 */
	@Override
	public Iterator<Block> iterator() {
		return new Iterator<Block>() {
			private final int minX = (int) Math.ceil(lowerBound.getX() - Skript.EPSILON),
					minY = (int) Math.ceil(lowerBound.getY() - Skript.EPSILON),
					minZ = (int) Math.ceil(lowerBound.getZ() - Skript.EPSILON);
			private final int maxX = (int) Math.floor(upperBound.getX() + Skript.EPSILON) - 1,
					maxY = (int) Math.floor(upperBound.getY() + Skript.EPSILON) - 1,
					maxZ = (int) Math.floor(upperBound.getZ() + Skript.EPSILON) - 1;
			
			private int x = minX - 1,// next() increases x by one immediately
					y = minY,
					z = minZ;
			
			@Override
			public boolean hasNext() {
				return x < maxX || y < maxY || z < maxZ;
			}
			
			@Override
			public Block next() {
				x++;
				if (x > maxX) {
					x = minX;
					z++;
					if (z > maxZ) {
						z = minZ;
						y++;
					}
				}
				if (y > maxY)
					throw new NoSuchElementException();
				return world.getBlockAt(x, y, z);
			}
			
			@Override
			public void remove() {
				throw new UnsupportedOperationException();
			}
		};
	}
}
