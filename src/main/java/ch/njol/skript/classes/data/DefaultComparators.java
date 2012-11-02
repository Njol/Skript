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
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.entity.Entity;
import org.bukkit.inventory.ItemStack;

import ch.njol.skript.Skript;
import ch.njol.skript.classes.Comparator;
import ch.njol.skript.entity.EntityData;
import ch.njol.skript.registrations.Comparators;
import ch.njol.skript.util.Date;
import ch.njol.skript.util.ItemType;
import ch.njol.skript.util.StructureType;
import ch.njol.skript.util.Time;
import ch.njol.skript.util.Timeperiod;
import ch.njol.skript.util.Timespan;
import ch.njol.skript.util.Utils;

/**
 * @author Peter Güttinger
 */
@SuppressWarnings("rawtypes")
public class DefaultComparators {
	
	public DefaultComparators() {}
	
	static {
		
		// IMPORTANT!! No Object - Object comparator
		//  why?
		
		// Number - Number
		Comparators.registerComparator(Number.class, Number.class, new Comparator<Number, Number>() {
			private static final long serialVersionUID = -6345259176086215473L;
			
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
			private static final long serialVersionUID = -6057967941800919748L;
			
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
			private static final long serialVersionUID = -7249014324178703668L;
			
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
		Comparators.registerComparator(Block.class, Block.class, new Comparator<Block, Block>() {
			private static final long serialVersionUID = -1909160103741575822L;
			
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
			private static final long serialVersionUID = -8977780425174837488L;
			
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
			private static final long serialVersionUID = -236163145922471389L;
			
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
		Comparators.registerComparator(Date.class, Date.class, new Comparator<Date, Date>() {
			private static final long serialVersionUID = 3594484102475563679L;
			
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
			private static final long serialVersionUID = -2764424944652985572L;
			
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
			private static final long serialVersionUID = 7242706836233865191L;
			
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
			private static final long serialVersionUID = -8119546793395571359L;
			
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
			private static final long serialVersionUID = -2533811470175803047L;
			
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
			private static final long serialVersionUID = -7724878030033153076L;
			
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
			private static final long serialVersionUID = -4803766183428148678L;
			
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
			private static final long serialVersionUID = -4861991453918248942L;
			
			@Override
			public Relation compare(final StructureType s1, final StructureType s2) {
				return Relation.get(Utils.containsAll(s2.getTypes(), s2.getTypes()));
			}
			
			@Override
			public boolean supportsOrdering() {
				return false;
			}
		});
		
	}
	
}
