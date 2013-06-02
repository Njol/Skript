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

package ch.njol.skript.classes.data;

import org.bukkit.OfflinePlayer;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.entity.Entity;
import org.bukkit.inventory.ItemStack;

import ch.njol.skript.Skript;
import ch.njol.skript.aliases.ItemType;
import ch.njol.skript.classes.ClassInfo;
import ch.njol.skript.classes.Comparator;
import ch.njol.skript.entity.EntityData;
import ch.njol.skript.registrations.Comparators;
import ch.njol.skript.util.Date;
import ch.njol.skript.util.StructureType;
import ch.njol.skript.util.Time;
import ch.njol.skript.util.Timeperiod;
import ch.njol.skript.util.Timespan;
import ch.njol.util.CollectionUtils;

/**
 * @author Peter Güttinger
 */
@SuppressWarnings({"rawtypes", "serial"})
public class DefaultComparators {
	
	public DefaultComparators() {}
	
	static {
		
		// Number - Number
		Comparators.registerComparator(Number.class, Number.class, new Comparator<Number, Number>() {
			@Override
			public Relation compare(final Number n1, final Number n2) {
				final double diff = n1.doubleValue() - n2.doubleValue();
				if (Math.abs(diff) < Skript.EPSILON)
					return Relation.EQUAL;
				return Relation.get(diff);
			}
			
			@Override
			public boolean supportsOrdering() {
				return true;
			}
		});
		
		// ItemStack - ItemType
		Comparators.registerComparator(ItemStack.class, ItemType.class, new Comparator<ItemStack, ItemType>() {
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
		Comparators.registerComparator(Block.class, ItemType.class, new Comparator<Block, ItemType>() {
			@Override
			public Relation compare(final Block b, final ItemType it) {
				return Relation.get(it.isOfType(b));
			}
			
			@Override
			public boolean supportsOrdering() {
				return false;
			}
		});
		
		// ItemType - ItemType
		Comparators.registerComparator(ItemType.class, ItemType.class, new Comparator<ItemType, ItemType>() {
			@Override
			public Relation compare(final ItemType i1, final ItemType i2) {
				return Relation.get(i2.isSupertypeOf(i1));
			}
			
			@Override
			public boolean supportsOrdering() {
				return false;
			}
		});
		
		// Block - Block
		Comparators.registerComparator(Block.class, Block.class, new Comparator<Block, Block>() {
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
		Comparators.registerComparator(Entity.class, EntityData.class, new Comparator<Entity, EntityData>() {
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
		Comparators.registerComparator(EntityData.class, EntityData.class, new Comparator<EntityData, EntityData>() {
			@Override
			public Relation compare(final EntityData t1, final EntityData t2) {
				return Relation.get(t2.isSupertypeOf(t1));
			}
			
			@Override
			public boolean supportsOrdering() {
				return false;
			}
		});
		
		// Date - Date
		Comparators.registerComparator(Date.class, Date.class, new Comparator<Date, Date>() {
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
		Comparators.registerComparator(Time.class, Time.class, new Comparator<Time, Time>() {
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
		Comparators.registerComparator(Timespan.class, Timespan.class, new Comparator<Timespan, Timespan>() {
			@Override
			public Relation compare(final Timespan t1, final Timespan t2) {
				return Relation.get(t1.getMilliSeconds() - t2.getMilliSeconds());
			}
			
			@Override
			public boolean supportsOrdering() {
				return true;
			}
		});
		
		// Time - Timeperiod
		Comparators.registerComparator(Time.class, Timeperiod.class, new Comparator<Time, Timeperiod>() {
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
		Comparators.registerComparator(OfflinePlayer.class, String.class, new Comparator<OfflinePlayer, String>() {
			@Override
			public Relation compare(final OfflinePlayer p, final String name) {
				return Relation.get(p.getName().equalsIgnoreCase(name));
			}
			
			@Override
			public boolean supportsOrdering() {
				return false;
			}
		});
		
		// World - String
		Comparators.registerComparator(World.class, String.class, new Comparator<World, String>() {
			@Override
			public Relation compare(final World w, final String name) {
				return Relation.get(w.getName().equalsIgnoreCase(name));
			}
			
			@Override
			public boolean supportsOrdering() {
				return false;
			}
		});
		
		// String - String
		Comparators.registerComparator(String.class, String.class, new Comparator<String, String>() {
			@Override
			public Relation compare(final String s1, final String s2) {
				return Relation.get(s1.equalsIgnoreCase(s2));
			}
			
			@Override
			public boolean supportsOrdering() {
				return false;
			}
		});
		
		// StructureType - StructureType
		Comparators.registerComparator(StructureType.class, StructureType.class, new Comparator<StructureType, StructureType>() {
			@Override
			public Relation compare(final StructureType s1, final StructureType s2) {
				return Relation.get(CollectionUtils.containsAll(s2.getTypes(), s2.getTypes()));
			}
			
			@Override
			public boolean supportsOrdering() {
				return false;
			}
		});
		
		// Object - ClassInfo
		Comparators.registerComparator(Object.class, ClassInfo.class, new Comparator<Object, ClassInfo>() {
			@Override
			public Relation compare(final Object o, final ClassInfo c) {
				return Relation.get(c.getC().isInstance(o));
			}
			
			@Override
			public boolean supportsOrdering() {
				return false;
			}
		});
		
	}
	
}
