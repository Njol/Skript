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

import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.util.Vector;

/**
 * @author Peter Güttinger
 * 
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
	
	public AABB(final Location center, final double dimX, final double dimY, final double dimZ) {
		world = center.getWorld();
		lowerBound = new Vector(center.getX() - dimX / 2, center.getY() - dimY / 2, center.getZ() - dimZ / 2);
		upperBound = new Vector(center.getX() + dimX / 2, center.getY() + dimY / 2, center.getZ() + dimZ / 2);
	}
	
	public boolean contains(final Location l) {
		if (l.getWorld() != world)
			return false;
		return lowerBound.getX() <= l.getX() && l.getX() <= upperBound.getX()
				&& lowerBound.getY() <= l.getY() && l.getY() <= upperBound.getY()
				&& lowerBound.getZ() <= l.getZ() && l.getZ() <= upperBound.getZ();
	}

	public boolean containsCompletely(final Block b) {
		return contains(b.getLocation()) && contains(b.getLocation().add(1, 1, 1));
	}

	public boolean contains(final Block b) {
		return contains(b.getLocation().add(0.5, 0.5, 0.5));
	}
	
	public Vector getDimensions() {
		return upperBound.clone().subtract(lowerBound);
	}
	
	@Override
	public Iterator<Block> iterator() {
		return new Iterator<Block>() {
			private int x = (int) Math.round(lowerBound.getX()) - 1,// next() increases x by one immediately
					y = (int) Math.round(lowerBound.getY()),
					z = (int) Math.round(lowerBound.getZ());
			private final int maxX = (int) Math.round(upperBound.getX()) - 1,
					maxY = (int) Math.round(upperBound.getY()) - 1,
					maxZ = (int) Math.round(upperBound.getZ()) - 1;
			
			@Override
			public boolean hasNext() {
				return z < maxZ || y < maxY || x < maxX;
			}
			
			@Override
			public Block next() {
				x++;
				if (x > maxX) {
					x = 0;
					y++;
					if (y > maxY) {
						y = 0;
						z++;
						if (z > maxZ)
							throw new IllegalStateException();
					}
				}
				return world.getBlockAt(x, y, z);
			}
			
			@Override
			public void remove() {
				throw new UnsupportedOperationException();
			}
		};
	}
}
