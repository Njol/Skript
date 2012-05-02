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

import java.util.Arrays;
import java.util.Iterator;

import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

import ch.njol.skript.Aliases;
import ch.njol.skript.Skript;
import ch.njol.util.iterator.SingleItemIterator;

/**
 * 
 * @author Peter Güttinger
 * 
 */
public class ItemData implements Cloneable {
	
	public int typeid = -1;
	public short dataMin = -1;
	public short dataMax = -1;
	
	public ItemData(final int typeid, final short data) {
		this.typeid = typeid;
		dataMin = dataMax = data;
	}
	
	public ItemData(final int typeid, final short dMin, final short dMax) {
		this.typeid = typeid;
		dataMin = dMin;
		dataMax = dMax;
	}
	
	public ItemData() {
		typeid = -1;
		dataMin = -1;
		dataMax = -1;
	}
	
	public ItemData(final ItemStack i) {
		typeid = i.getTypeId();
		dataMin = dataMax = i.getDurability();// <- getData() returns a new data object based on the durability (see source)
	}
	
	public ItemData(final ItemData other) {
		typeid = other.typeid;
		dataMax = other.dataMax;
		dataMin = other.dataMin;
	}
	
	/**
	 * Overrides all values of this ItemData with the values of the given item data if they are set there.
	 * 
	 * @param other
	 */
	public void merge(final ItemData other) {
		if (other == null)
			return;
		if (other.typeid != -1)
			typeid = other.typeid;
		if (other.dataMin != -1)
			dataMin = other.dataMin;
		if (other.dataMax != -1)
			dataMax = other.dataMax;
	}
	
	public boolean isOfType(final ItemStack item) {
		if (item == null)
			return typeid == 0;
		return (typeid == -1 || item.getTypeId() == typeid) && (dataMin == -1 || item.getData().getData() >= dataMin) && (dataMax == -1 || item.getData().getData() <= dataMax);
	}
	
	/**
	 * Returns {@link Skript#getMaterialName(int, short, short) Skript.getMaterialName(typeid, dataMin, dataMax)}
	 */
	@Override
	public String toString() {
		return Aliases.getMaterialName(typeid, dataMin, dataMax);
	}
	
	@Override
	public boolean equals(final Object obj) {
		if (obj == this)
			return true;
		if (!(obj instanceof ItemData))
			return false;
		final ItemData other = (ItemData) obj;
		return other.typeid == typeid && other.dataMin == dataMin && other.dataMax == dataMax;
	}
	
	@Override
	public int hashCode() {
		return typeid ^ dataMin << 8 ^ dataMax << 16;
	}
	
	/**
	 * Computes the intersection of two ItemDatas. The data range of the returned item data will be the real intersection of the two data ranges, and the type id will be the one
	 * set if any.
	 * 
	 * @param other
	 * 
	 * @return a new ItemData which is the intersection of the given types, or null if the intersection of the data ranges is empty or both datas have an id set which are not the
	 *         same.
	 */
	public ItemData intersection(final ItemData other) {
		if (other.dataMax != -1 && dataMin != -1 && other.dataMax < dataMin || other.typeid != -1 && typeid != -1 && other.typeid != typeid)
			return null;
		return new ItemData(typeid == -1 ? other.typeid : typeid,
				(short) Math.max(dataMin, other.dataMin),
				dataMax == -1 ? other.dataMax : (other.dataMax == -1 ? dataMax : (short) Math.min(dataMax, other.dataMax)));
	}
	
	public ItemStack getRandom(final ItemType type) {
		if (dataMin == -1 && dataMax == -1) {
			return new ItemStack(typeid == -1 ? Utils.getRandom(Material.values(), 1).getId() : typeid, type.getAmount());
		}
		final short dmin = dataMin == -1 ? 0 : dataMin;
		final short dmax = dataMax == -1 ? Short.MAX_VALUE : dataMax;
		return new ItemStack(typeid == -1 ? Utils.getRandom(Material.values(), 1).getId() : typeid,
				type.getAmount(),
				(short) (Skript.random.nextInt(dmax - dmin + 1) + dmin));
	}
	
	public Iterator<ItemStack> getAll(final ItemType type) {
		if (typeid == -1) {
			return new Iterator<ItemStack>() {
				
				private final Iterator<Material> iter = Arrays.asList(Material.values()).iterator();
				{
					iter.next();
				}
				
				@Override
				public boolean hasNext() {
					return iter.hasNext();
				}
				
				@Override
				public ItemStack next() {
					return new ItemStack(iter.next(), type.getAmount());
				}
				
				@Override
				public void remove() {}
				
			};
		}
		if (dataMin == -1 && dataMax == -1)
			return new SingleItemIterator<ItemStack>(new ItemStack(typeid, 1));
		return new Iterator<ItemStack>() {
			
			private short data = (short) (dataMin == -1 ? -1 : dataMin - 1);
			
			@Override
			public boolean hasNext() {
				return data < (dataMax == -1 ? 15 : dataMax);
			}
			
			@Override
			public ItemStack next() {
				data++;
				return new ItemStack(typeid, type.getAmount(), data, (byte) data);
			}
			
			@Override
			public void remove() {}
			
		};
	}
	
	@Override
	public ItemData clone() {
		return new ItemData(this);
	}
	
	public boolean hasDataRange() {
		return dataMin != dataMax;
	}
	
}
