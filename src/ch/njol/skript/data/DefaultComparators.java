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
		
		// IMPORTANT!! NO Object - Object comparator
		
		// ItemStack - ItemType
		Skript.addComparator(ItemStack.class, ItemType.class, new Comparator<ItemStack, ItemType>() {
			@Override
			public Relation compare(final ItemStack is, final ItemType it) {
				if (it == null)
					return Relation.NOT_EQUAL;
				return Relation.get(it.isOfType(is));
			}
			
			@Override
			public boolean supportsRelation(final ch.njol.skript.api.Comparator.Relation r) {
				return r.isEqualOrInverse();
			}
		});
		
		// Block - ItemType
		Skript.addComparator(Block.class, ItemType.class, new Comparator<Block, ItemType>() {
			@Override
			public Relation compare(final Block b, final ItemType it) {
				if (it == null)
					return Relation.NOT_EQUAL;
				return Relation.get(it.isOfType(b));
			}
			
			@Override
			public boolean supportsRelation(final ch.njol.skript.api.Comparator.Relation r) {
				return r.isEqualOrInverse();
			}
		});
		
		// Block - Block
		Skript.addComparator(Block.class, Block.class, new Comparator<Block, Block>() {
			@Override
			public ch.njol.skript.api.Comparator.Relation compare(final Block b1, final Block b2) {
				if (b1 == null || b2 == null)
					return Relation.NOT_EQUAL;
				return Relation.get(b1.equals(b2));
			}
			
			@Override
			public boolean supportsRelation(final ch.njol.skript.api.Comparator.Relation r) {
				return r.isEqualOrInverse();
			}
			
		});
		
		// Number - Number
		Skript.addComparator(Number.class, Number.class, new Comparator<Number, Number>() {
			@Override
			public Relation compare(final Number n1, final Number n2) {
				if (n1 == null || n2 == null)
					return Relation.NOT_EQUAL;
				return Relation.get(n1.doubleValue() - n2.doubleValue());
			}
			
			@Override
			public boolean supportsRelation(final ch.njol.skript.api.Comparator.Relation r) {
				return true;
			}
		});
		
		// Entity - EntityType
		Skript.addComparator(Entity.class, EntityType.class, new Comparator<Entity, EntityType>() {
			@Override
			public ch.njol.skript.api.Comparator.Relation compare(final Entity e, final EntityType t) {
				if (t == null || e == null)
					return Relation.NOT_EQUAL;
				return Relation.get(t.isInstance(e));
			}
			
			@Override
			public boolean supportsRelation(final ch.njol.skript.api.Comparator.Relation r) {
				return r.isEqualOrInverse();
			}
		});
		
		// Time - Timeperiod
		Skript.addComparator(Time.class, Timeperiod.class, new Comparator<Time, Timeperiod>() {
			@Override
			public ch.njol.skript.api.Comparator.Relation compare(final Time t, final Timeperiod p) {
				if (p == null || t == null)
					return Relation.NOT_EQUAL;
				return Relation.get(p.contains(t));
			}
			
			@Override
			public boolean supportsRelation(final ch.njol.skript.api.Comparator.Relation r) {
				return r.isEqualOrInverse();
			}
		});
		
		// String - String
		Skript.addComparator(String.class, String.class, new Comparator<String, String>() {
			
			@Override
			public ch.njol.skript.api.Comparator.Relation compare(final String s1, final String s2) {
				if (s1 == null)
					return Relation.NOT_EQUAL;
				return Relation.get(s1.equalsIgnoreCase(s2));
			}
			
			@Override
			public boolean supportsRelation(final ch.njol.skript.api.Comparator.Relation r) {
				return r.isEqualOrInverse();
			}
			
		});
		
	}
	
}
