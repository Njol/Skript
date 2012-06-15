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

package ch.njol.skript.expressions;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.Furnace;
import org.bukkit.event.Event;
import org.bukkit.event.inventory.FurnaceBurnEvent;
import org.bukkit.event.inventory.FurnaceSmeltEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import ch.njol.skript.Skript;
import ch.njol.skript.api.Getter;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.SimpleExpression;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.skript.util.Slot;

/**
 * @author Peter Güttinger
 * 
 */
public class ExprFurnaceSlot extends SimpleExpression<Slot> {
	
	// Slot IDs:
	// ore: 0, fuel: 1, result: 2
	
	static {
		Skript.registerExpression(ExprFurnaceSlot.class, Slot.class,
				"[the] ore [slot] [of %blocks%]", "%block%'[s] ore [slot]",
				"[the] fuel [slot] [of %blocks%]", "%block%'[s] fuel [slot]",
				"[the] result [slot] [of %blocks%]", "%block%'[s] result [slot]");
	}
	
	private Expression<Block> blocks;
	private int slot;
	
	private final static String[] slotNames = {"ore", "fuel", "result"};
	
	@SuppressWarnings("unchecked")
	@Override
	public boolean init(final Expression<?>[] vars, final int matchedPattern, final ParseResult parser) {
		blocks = (Expression<Block>) vars[0];
		slot = matchedPattern / 2;
		return true;
	}
	
	private final class FurnaceEventSlot extends Slot {
		
		public FurnaceEventSlot(final Inventory invi) {
			super(invi, slot);
		}
		
		@Override
		public ItemStack getItem() {
			final ItemStack i = super.getItem();
			if (getTime() == -1)
				return i;
			i.setAmount(i.getAmount() - 1);
			if (i.getAmount() == 0)
				return new ItemStack(0, 1);
			return i;
		}
		
		// FIXME 
		@Override
		public void setItem(final ItemStack item) {
			Bukkit.getScheduler().scheduleSyncDelayedTask(Skript.getInstance(), new Runnable() {
				@Override
				public void run() {
					item.setAmount(item.getAmount() + 1);
					FurnaceEventSlot.super.setItem(item);
				}
			});
		}
		
	}
	
	@Override
	protected Slot[] getAll(final Event e) {
		if (blocks.isDefault() && (e instanceof FurnaceSmeltEvent || e instanceof FurnaceBurnEvent)) {
			final Block b = blocks.getSingle(e);
			if (b.getType() != Material.FURNACE && b.getType() != Material.BURNING_FURNACE)
				return null;
			return new Slot[] {new FurnaceEventSlot(((Furnace) b.getState()).getInventory())};
		}
		return blocks.getArray(e, Slot.class, new Getter<Slot, Block>() {
			@Override
			public Slot get(final Block b) {
				if (b.getType() != Material.FURNACE && b.getType() != Material.BURNING_FURNACE)
					return null;
				return new Slot(((Furnace) b.getState()).getInventory(), slot);
			}
		});
	}
	
	@Override
	public Class<? extends Slot> getReturnType() {
		return Slot.class;
	}
	
	@Override
	public String getDebugMessage(final Event e) {
		if (e == null)
			return slotNames[slot] + " slot of " + blocks.getDebugMessage(e);
		return Skript.getDebugMessage(getSingle(e));
	}
	
	@Override
	public String toString() {
		return "the " + slotNames[slot] + " slot of " + blocks;
	}
	
	@Override
	public boolean isSingle() {
		return blocks.isSingle();
	}
	
	@SuppressWarnings("unchecked")
	@Override
	public boolean setTime(final int time) {
		return super.setTime(time, blocks, FurnaceSmeltEvent.class, FurnaceBurnEvent.class);
	}
	
}
