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

import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.util.BlockIterator;
import org.bukkit.util.Vector;

import ch.njol.util.Checker;
import ch.njol.util.coll.iterator.StoppableIterator;

/**
 * @author Peter Güttinger
 */
public class BlockLineIterator extends StoppableIterator<Block> {
	
	/**
	 * @param start
	 * @param end
	 * @throws IllegalStateException randomly (Bukkit bug)
	 */
	public BlockLineIterator(final Block start, final Block end) throws IllegalStateException {
		super(new BlockIterator(start.getWorld(), start.getLocation().add(0.5, 0.5, 0.5).toVector(),
				end.equals(start) ? new Vector(1, 0, 0) : end.getLocation().subtract(start.getLocation()).toVector(), 0, 0), // should prevent an error if start = end
		new Checker<Block>() {
			private final double overshotSq = Math.pow(start.getLocation().distance(end.getLocation()) + 2, 2);
			
			@Override
			public boolean check(final Block b) {
				if (b.getLocation().distanceSquared(start.getLocation()) > overshotSq)
					throw new IllegalStateException("BlockLineIterator missed the end block!");
				return b.equals(end);
			}
		}, true);
	}
	
	/**
	 * @param start
	 * @param dir
	 * @param dist
	 * @throws IllegalStateException randomly (Bukkit bug)
	 */
	public BlockLineIterator(final Location start, final Vector dir, final double dist) throws IllegalStateException {
		super(new BlockIterator(start.getWorld(), start.toVector(), dir, 0, 0), new Checker<Block>() {
			private final double distSq = dist * dist;
			
			@Override
			public boolean check(final Block b) {
				return b.getLocation().add(0.5, 0.5, 0.5).distanceSquared(start) >= distSq;
			}
		}, false);
	}
	
	/**
	 * @param start
	 * @param dir
	 * @param dist
	 * @throws IllegalStateException randomly (Bukkit bug)
	 */
	public BlockLineIterator(final Block start, final Vector dir, final double dist) throws IllegalStateException {
		this(start.getLocation().add(0.5, 0.5, 0.5), dir, dist);
	}
}
