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

package ch.njol.skript.data;

import org.bukkit.block.Block;
import org.bukkit.entity.Entity;
import org.bukkit.inventory.ItemStack;

import ch.njol.skript.Skript;
import ch.njol.skript.api.Comparator;
import ch.njol.skript.util.EntityType;
import ch.njol.skript.util.ItemType;
import ch.njol.skript.util.Time;
import ch.njol.skript.util.Timeperiod;

/**
 * @author Peter Güttinger
 * 
 */
public class DefaultComparators {
	
	public DefaultComparators() {}
	
	static {
		
		// IMPORTANT!! No Object - Object comparator
		
		// ItemStack - ItemType
		Skript.registerComparator(ItemStack.class, ItemType.class, new Comparator<ItemStack, ItemType>() {
			@Override
			public Relation compare(final ItemStack is, final ItemType it) {
				if (it == null)
					return Relation.NOT_EQUAL;
				return Relation.get(it.isOfType(is));
			}
			
			@Override
			public boolean supportsOrdering() {
				return false;
			}
		});
		
		// Block - ItemType
		Skript.registerComparator(Block.class, ItemType.class, new Comparator<Block, ItemType>() {
			@Override
			public Relation compare(final Block b, final ItemType it) {
				if (it == null)
					return Relation.NOT_EQUAL;
				return Relation.get(it.isOfType(b));
			}
			
			@Override
			public boolean supportsOrdering() {
				return false;
			}
		});
		
		// Block - Block
		Skript.registerComparator(Block.class, Block.class, new Comparator<Block, Block>() {
			@Override
			public ch.njol.skript.api.Comparator.Relation compare(final Block b1, final Block b2) {
				if (b1 == null || b2 == null)
					return Relation.NOT_EQUAL;
				return Relation.get(b1.equals(b2));
			}
			
			@Override
			public boolean supportsOrdering() {
				return false;
			}
			
		});
		
		// Number - Number
		Skript.registerComparator(Number.class, Number.class, new Comparator<Number, Number>() {
			@Override
			public Relation compare(final Number n1, final Number n2) {
				if (n1 == null || n2 == null)
					return Relation.NOT_EQUAL;
				return Relation.get(n1.doubleValue() - n2.doubleValue());
			}
			
			@Override
			public boolean supportsOrdering() {
				return true;
			}
		});
		
		// Entity - EntityType
		Skript.registerComparator(Entity.class, EntityType.class, new Comparator<Entity, EntityType>() {
			@Override
			public ch.njol.skript.api.Comparator.Relation compare(final Entity e, final EntityType t) {
				if (t == null || e == null)
					return Relation.NOT_EQUAL;
				return Relation.get(t.isInstance(e));
			}
			
			@Override
			public boolean supportsOrdering() {
				return false;
			}
		});
		
		// Time - Time
		Skript.registerComparator(Time.class, Time.class, new Comparator<Time, Time>() {
			@Override
			public Relation compare(final Time t1, final Time t2) {
				return Relation.get(t1.getTicks() - t2.getTicks());
			}
			
			@Override
			public boolean supportsOrdering() {
				return true;
			}
		});
		
		// Time - Timeperiod
		Skript.registerComparator(Time.class, Timeperiod.class, new Comparator<Time, Timeperiod>() {
			@Override
			public Relation compare(final Time t, final Timeperiod p) {
				if (p == null || t == null)
					return Relation.NOT_EQUAL;
				return Relation.get(p.contains(t));
			}
			
			@Override
			public boolean supportsOrdering() {
				return false;
			}
		});
		
		// String - String
		Skript.registerComparator(String.class, String.class, new Comparator<String, String>() {
			
			@Override
			public ch.njol.skript.api.Comparator.Relation compare(final String s1, final String s2) {
				if (s1 == null)
					return Relation.NOT_EQUAL;
				return Relation.get(s1.equalsIgnoreCase(s2));
			}
			
			@Override
			public boolean supportsOrdering() {
				return false;
			}
			
		});
		
	}
	
}
