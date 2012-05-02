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

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;

import org.bukkit.block.Block;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;

import ch.njol.skript.Skript;
import ch.njol.skript.util.Container.ContainerType;
import ch.njol.util.iterator.IteratorIterable;
import ch.njol.util.iterator.SingleItemIterator;

@ContainerType(ItemStack.class)
public class ItemType implements Cloneable, Iterable<ItemData>, Container<ItemStack> {
	
	private final ArrayList<ItemData> types = new ArrayList<ItemData>();
	
	private ItemType item = null;
	private ItemType block = null;
	
	public boolean all = false;
	
	public int amount = -1;
	
	public ItemType() {}
	
	public ItemType(final ItemStack i) {
		amount = i.getAmount();
		types.add(new ItemData(i));
	}
	
	public ItemType(final Block block) {
		final ItemData d = new ItemData();
		d.typeid = block.getTypeId();
		d.dataMin = d.dataMax = block.getData();
		types.add(d);
	}
	
	public ItemType(final ItemType i) {
		item = i.item;
		block = i.block;
		all = i.all;
		amount = i.amount;
		for (final ItemData d : i)
			types.add(d.clone());
	}
	
	public void setItem(final ItemType item) {
		this.item = item;
	}
	
	public void setBlock(final ItemType block) {
		this.block = block;
	}
	
	public ItemType getItem() {
		return item == null ? this : item;
	}
	
	public ItemType getBlock() {
		return block == null ? this : block;
	}
	
	public boolean isOfType(final ItemStack item) {
		for (final ItemData type : types) {
			if (type.isOfType(item))
				return true;
		}
		return false;
	}
	
	/**
	 * @return amount or 1 if amount == -1
	 */
	public int getAmount() {
		return amount == -1 ? 1 : amount;
	}
	
	public boolean isOfType(final Block block) {
		if (block == null)
			return false;
		for (final ItemData d : types) {
			if ((d.typeid == -1 || block.getTypeId() == d.typeid) && (d.dataMin == -1 || block.getData() >= d.dataMin) && (d.dataMax == -1 || block.getData() <= d.dataMax))
				return true;
		}
		return false;
	}
	
	@Override
	public String toString() {
		final StringBuilder b = new StringBuilder();
		if (types.size() == 1 && !types.get(0).hasDataRange()) {
			if (getAmount() != 1)
				b.append(amount + " ");
			if (all)
				b.append(getAmount() == 1 ? "every " : "of all ");
		} else {
			if (getAmount() != 1)
				b.append(amount + " of ");
			b.append(all ? "every " : "any ");
		}
		for (int i = 0; i < types.size(); i++) {
			if (i != 0) {// this belongs here as size-1 can be 0
				if (i == types.size() - 1)
					b.append(all ? " and " : " or ");
				else
					b.append(", ");
			}
			final String p = types.get(i).toString();
			b.append(amount > 1 ? Utils.toPlural(p) : p);
		}
		return b.toString();
	}
	
	public static String toString(final ItemStack i) {
		return new ItemType(i).toString();
	}
	
	public boolean setBlock(final Block block, final boolean applyPhysics) {
		final ItemStack i = getBlock().getRandom();
		if (i.getTypeId() > Skript.MAXBLOCKID)
			return false;
		block.setTypeIdAndData(i.getTypeId(), i.getData() == null ? 0 : i.getData().getData(), applyPhysics);
		return true;
	}
	
	/**
	 * Intersects all ItemDatas with all ItemDatas of the given ItemType, returning an ItemType with n*m ItemDatas, where n = #ItemDatas of this ItemType, and m = #ItemDatas of the
	 * argument. <br/>
	 * more info: {@link ItemData#intersection(ItemData)}
	 * 
	 * @param other
	 * @return a new item type which is the intersection of the two item types or null if the intersection is empty.
	 */
	public ItemType intersection(final ItemType other) {
		final ItemType r = new ItemType();
		for (final ItemData d1 : types) {
			for (final ItemData d2 : other.types) {
				r.add(d1.intersection(d2));
			}
		}
		if (r.types.isEmpty())
			return null;
		return r;
	}
	
	public void add(final ItemData type) {
		if (type != null)
			types.add(type);
	}
	
	public void remove(final ItemData d) {
		types.remove(d);
	}
	
	@Override
	public Iterator<ItemStack> containerIterator() {
		return getAll().iterator();
	}
	
	public Iterable<ItemStack> getAll() {
		if (!all) {
			final ItemStack i = getRandom();
			return new Iterable<ItemStack>() {
				@Override
				public Iterator<ItemStack> iterator() {
					return new SingleItemIterator<ItemStack>(i);
				}
			};
		}
		final ItemType siht = this;
		return new Iterable<ItemStack>() {
			@Override
			public Iterator<ItemStack> iterator() {
				return new Iterator<ItemStack>() {
					
					ListIterator<ItemData> iter = types.listIterator();
					Iterator<ItemStack> currentDataIter;
					
					@Override
					public boolean hasNext() {
						return iter.hasNext() || currentDataIter.hasNext();
					}
					
					@Override
					public ItemStack next() {
						while (currentDataIter == null || !currentDataIter.hasNext()) {
							currentDataIter = iter.next().getAll(siht);
						}
						return currentDataIter.next();
					}
					
					@Override
					public void remove() {}
					
				};
			}
		};
	}
	
	/**
	 * removes this type from the item stack if applicable
	 * 
	 * @param item
	 * @return the passed ItemStack
	 */
	public ItemStack removeFrom(final ItemStack item) {
		if (item == null)
			return null;
		for (final ItemData type : types) {
			if (type.isOfType(item)) {
				item.setAmount(Math.max(item.getAmount() - getAmount(), 0));
				return item;
			}
		}
		return item;
	}
	
	public ItemStack addTo(final ItemStack item) {
		if (item == null)
			return null;
		for (final ItemData type : types) {
			if (type.isOfType(item)) {
				item.setAmount(Math.max(item.getAmount() + getAmount(), 0));
				return item;
			}
		}
		return item;
	}
	
	public boolean removeFrom(final Inventory invi) {
		if (invi == null)
			return false;
		boolean ok = true;
		int removed = 0;
		for (final ItemData id : types) {
			final boolean playerInvi = invi instanceof PlayerInventory;
			for (int i = 0; i < (playerInvi ? invi.getSize() + 4 : invi.getSize()); i++) {
				final ItemStack is = i < invi.getSize() ? invi.getItem(i) : ((PlayerInventory) invi).getArmorContents()[i - invi.getSize()];
				if (id.isOfType(is)) {
					final int d = all && amount == -1 ? is.getAmount() : Math.min(is.getAmount(), getAmount() - removed);
					if (d > 0) {
						removed += d;
						is.setAmount(is.getAmount() - d);
						invi.setItem(i, is);
						if ((!all || amount != -1) && removed == getAmount())
							break;
					}
				}
			}
			ok &= removed == getAmount() || all;
		}
		return ok;
	}
	
	public boolean removeFrom(final List<ItemStack> list) {
		if (list == null)
			return false;
		for (final ItemData id : types) {
			int removed = 0;
			for (int i = 0; i < list.size(); i++) {
				final ItemStack is = list.get(i);
				if (id.isOfType(is)) {
					final int d = all && amount == -1 ? is.getAmount() : Math.min(is.getAmount(), getAmount() - removed);
					if (d > 0) {
						removed += d;
						is.setAmount(is.getAmount() - d);
						list.set(i, is);
						if ((!all || amount != -1) && removed == getAmount())
							break;
					}
				}
			}
		}
		return false;
	}
	
	public void addTo(final List<ItemStack> list) {
		if (!all) {
			list.add(getItem().getRandom());
			return;
		}
		for (final ItemData d : getItem().types) {
			for (final ItemStack is : new IteratorIterable<ItemStack>(d.getAll(this)))
				list.add(is);
		}
	}
	
	public boolean addTo(final Inventory invi) {
		if (!all) {
			return invi.addItem(getItem().getRandom()).isEmpty();
		}
		boolean ok = true;
		//		for (final ItemData d : getItem().types) {
		//			for (final ItemStack is : new IteratorIterable<ItemStack>(d.getAll(this)))
		for (final ItemStack is : getItem().getAll()) {
			ok &= invi.addItem(is).isEmpty();
		}
		return ok;
	}
	
	@Override
	public ItemType clone() {
		return new ItemType(this);
	}
	
	/**
	 * @return ONE random ItemStack. If you have a List or an Inventory, use {@link #addTo(Inventory)} or {@link #addTo(List)} respectively.
	 */
	public ItemStack getRandom() {
		return Utils.getRandom(types).getRandom(this);
	}
	
	// FIXME nochmals überlegen!
	public boolean hasSpace(final Inventory invi) {
		if (!all) {
			if (types.size() != 1 || types.get(0).hasDataRange() || types.get(0).typeid == -1)
				return false;// if there's no good solution, disable it completely
			final ItemStack b = types.get(0).getRandom(this); // not actually random
			int added = 0;
			for (final ItemStack is : invi.getContents()) {
				if (Utils.itemStacksEqual(is, b)) {
					final int d = Math.min(is.getMaxStackSize() - is.getAmount(), b.getAmount());
					if (d > 0) {
						added += d;
						if (added == b.getAmount())
							return true;
					}
				}
			}
			return false;
		}
		final ItemStack[] buffer = invi.getContents();
		final boolean ok = addTo(invi);
		invi.setContents(buffer);
		return ok;
	}
	
	@Override
	public Iterator<ItemData> iterator() {
		return types.iterator();
	}
	
	public boolean hasTypes() {
		return !types.isEmpty();
	}
	
	public int numTypes() {
		return types.size();
	}
	
	/**
	 * 
	 * @return The internal list of ItemDatas, thus do not modify it!
	 */
	public List<ItemData> getTypes() {
		return types;
	}
	
	/**
	 * @param invi
	 * @return
	 */
	public boolean isContainedIn(final Inventory invi) {
		final ItemStack[] clone = invi.getContents();
		if (!all) {
			for (final ItemData d : types) {
				int amount = 0;
				for (final ItemStack i : clone) {
					if (d.isOfType(i)) {
						amount += i == null ? 1 : i.getAmount();
						if (amount >= this.amount)
							return true;
					}
				}
			}
			return false;
		}
		for (final ItemData d : types) {
			int amount = 0;
			for (final ItemStack i : clone) {
				if (d.isOfType(i)) {
					final int toTake = Math.min(i == null ? 1 : i.getAmount(), this.amount - amount);
					amount += toTake;
					i.setAmount(i.getAmount() - toTake);
					if (amount >= this.amount)
						continue;
				}
			}
			if (amount > 0)
				return false;
		}
		return true;
	}
	
}
