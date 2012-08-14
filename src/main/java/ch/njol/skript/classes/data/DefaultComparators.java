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

package ch.njol.skript.classes.data;

import org.bukkit.OfflinePlayer;
import org.bukkit.block.Block;
import org.bukkit.entity.Entity;
import org.bukkit.inventory.ItemStack;

import ch.njol.skript.Skript;
import ch.njol.skript.classes.Comparator;
import ch.njol.skript.entity.EntityData;
import ch.njol.skript.util.Date;
import ch.njol.skript.util.ItemType;
import ch.njol.skript.util.Time;
import ch.njol.skript.util.Timeperiod;
import ch.njol.skript.util.Timespan;

/**
 * @author Peter Güttinger
 * 
 */
@SuppressWarnings("rawtypes")
public class DefaultComparators {
	
	public DefaultComparators() {}
	
	static {
		
		// IMPORTANT!! No Object - Object comparator
		//  why?
		
		// Double - Double
		// Number - Number doesn't work...
		Skript.registerComparator(Double.class, Double.class, new Comparator<Double, Double>() {
			@Override
			public Relation compare(final Double d1, final Double d2) {
				if (Math.abs(d1.doubleValue() - d2.doubleValue()) < Skript.EPSILON)
					return Relation.EQUAL;
				return Relation.get(d1.doubleValue() - d2.doubleValue());
			}
			
			@Override
			public boolean supportsOrdering() {
				return true;
			}
		});
		
		// ItemStack - ItemType
		Skript.registerComparator(ItemStack.class, ItemType.class, new Comparator<ItemStack, ItemType>() {
			@Override
			public Relation compare(final ItemStack is, final ItemType it) {
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
			public Relation compare(final Block b1, final Block b2) {
				return Relation.get(b1.equals(b2));
			}
			
			@Override
			public boolean supportsOrdering() {
				return false;
			}
			
		});
		
		// Entity - EntityData
		Skript.registerComparator(Entity.class, EntityData.class, new Comparator<Entity, EntityData>() {
			@Override
			public Relation compare(final Entity e, final EntityData t) {
				return Relation.get(t.isInstance(e));
			}
			
			@Override
			public boolean supportsOrdering() {
				return false;
			}
		});
		// EntityData - EntityData
		Skript.registerComparator(EntityData.class, EntityData.class, new Comparator<EntityData, EntityData>() {
			@Override
			public Relation compare(final EntityData t1, final EntityData t2) {
				return Relation.get(t2.getType().isAssignableFrom(t1.getType()));
			}
			
			@Override
			public boolean supportsOrdering() {
				return false;
			}
		});
		
		// Date - Date
		Skript.registerComparator(Date.class, Date.class, new Comparator<Date, Date>() {
			@Override
			public Relation compare(final Date d1, final Date d2) {
				return Relation.get(d1.compareTo(d2));
			}
			
			@Override
			public boolean supportsOrdering() {
				return true;
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
		
		// Timespan - Timespan
		Skript.registerComparator(Timespan.class, Timespan.class, new Comparator<Timespan, Timespan>() {
			@Override
			public Relation compare(final Timespan t1, final Timespan t2) {
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
				return Relation.get(p.contains(t));
			}
			
			@Override
			public boolean supportsOrdering() {
				return false;
			}
		});
		
		// OfflinePlayer - String
		Skript.registerComparator(OfflinePlayer.class, String.class, new Comparator<OfflinePlayer, String>() {
			@Override
			public Relation compare(final OfflinePlayer p, final String name) {
				return Relation.get(p.getName().equalsIgnoreCase(name));
			}
			
			@Override
			public boolean supportsOrdering() {
				return false;
			}
		});
		
		// String - String
		Skript.registerComparator(String.class, String.class, new Comparator<String, String>() {
			@Override
			public Relation compare(final String s1, final String s2) {
				return Relation.get(s1.equalsIgnoreCase(s2));
			}
			
			@Override
			public boolean supportsOrdering() {
				return false;
			}
		});
		
	}
	
}
