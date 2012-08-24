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
import org.bukkit.inventory.FurnaceInventory;
import org.bukkit.inventory.ItemStack;

import ch.njol.skript.Skript;
import ch.njol.skript.Skript.ExpressionType;
import ch.njol.skript.effects.Delay;
import ch.njol.skript.expressions.base.PropertyExpression;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.skript.util.Getter;
import ch.njol.skript.util.Slot;

/**
 * @author Peter Güttinger
 * 
 */
public class ExprFurnaceSlot extends PropertyExpression<Block, Slot> {
	
	private final static int ORE = 0, FUEL = 1, RESULT = 2;
	private final static String[] slotNames = {"ore", "fuel", "result"};
	
	static {
		Skript.registerExpression(ExprFurnaceSlot.class, Slot.class, ExpressionType.PROPERTY,
				"[the] ore[s] [slot[s]] of %blocks%", "%block%'[s] ore[s] [slot[s]]",
				"[the] fuel[s] [slot[s]] of %blocks%", "%block%'[s] fuel[s] [slot[s]]",
				"[the] result[s] [slot[s]] of %blocks%", "%block%'[s] result[s] [slot[s]]");
	}
	
	private Expression<Block> blocks;
	private int slot;
	
	@SuppressWarnings("unchecked")
	@Override
	public boolean init(final Expression<?>[] vars, final int matchedPattern, final int isDelayed, final ParseResult parser) {
		blocks = (Expression<Block>) vars[0];
		setExpr(blocks);
		slot = matchedPattern / 2;
		return true;
	}
	
	private final class FurnaceEventSlot extends Slot {
		
		private final Event e;
		
		public FurnaceEventSlot(final Event e, final FurnaceInventory invi) {
			super(invi, slot);
			this.e = e;
		}
		
		@Override
		public ItemStack getItem() {
			if (e instanceof FurnaceSmeltEvent) {
				if (slot == RESULT) {
					if (getTime() >= 0)
						return ((FurnaceSmeltEvent) e).getResult().clone();
					else
						return super.getItem();
				} else if (slot == ORE) {
					if (getTime() <= 0) {
						return super.getItem();
					} else {
						final ItemStack i = super.getItem();
						i.setAmount(i.getAmount() - 1);
						return i.getAmount() == 0 ? new ItemStack(0, 1) : i;
					}
				} else {
					return super.getItem();
				}
			} else {
				if (slot == FUEL) {
					if (getTime() <= 0) {
						return super.getItem();
					} else {
						final ItemStack i = super.getItem();
						i.setAmount(i.getAmount() - 1);
						return i.getAmount() == 0 ? new ItemStack(0, 1) : i;
					}
				} else {
					return super.getItem();
				}
			}
		}
		
		@Override
		public void setItem(final ItemStack item) {
			if (e instanceof FurnaceSmeltEvent) {
				if (slot == RESULT && getTime() >= 0) {
					if (item == null || item.getTypeId() == 0) { // null/air crashes the server on account of a NPE if using event.setResult(...)
						Bukkit.getScheduler().scheduleSyncDelayedTask(Skript.getInstance(), new Runnable() {
							@Override
							public void run() {
								FurnaceEventSlot.super.setItem(null);
							}
						});
					} else {
						((FurnaceSmeltEvent) e).setResult(item);
					}
				} else if (slot == ORE && getTime() >= 0) {
					Bukkit.getScheduler().scheduleSyncDelayedTask(Skript.getInstance(), new Runnable() {
						@Override
						public void run() {
							FurnaceEventSlot.super.setItem(item);
						}
					});
				} else {
					super.setItem(item);
				}
			} else {
				if (slot == FUEL && getTime() >= 0) {
					Bukkit.getScheduler().scheduleSyncDelayedTask(Skript.getInstance(), new Runnable() {
						@Override
						public void run() {
							FurnaceEventSlot.super.setItem(item);
						}
					});
				} else {
					super.setItem(item);
				}
			}
		}
		
	}
	
	@Override
	protected Slot[] get(final Event e, final Block[] source) {
		if (blocks.isDefault() && (e instanceof FurnaceSmeltEvent || e instanceof FurnaceBurnEvent) && !Delay.isDelayed(e)) {
			final Block b = blocks.getSingle(e);
			if (b.getType() != Material.FURNACE && b.getType() != Material.BURNING_FURNACE)
				return null;
			return new Slot[] {new FurnaceEventSlot(e, ((Furnace) b.getState()).getInventory())};
		}
		return get(source, new Getter<Slot, Block>() {
			@Override
			public Slot get(final Block b) {
				if (b.getType() != Material.FURNACE && b.getType() != Material.BURNING_FURNACE)
					return null;
				return new Slot(((Furnace) b.getState()).getInventory(), slot);
			}
		});
	}
	
	@Override
	public Class<Slot> getReturnType() {
		return Slot.class;
	}
	
	@Override
	public String toString(final Event e, final boolean debug) {
		if (e == null)
			return "the " + (getTime() == -1 ? "past " : getTime() == 1 ? "future " : "") + slotNames[slot] + " slot of " + blocks.toString(e, debug);
		return Skript.getDebugMessage(getSingle(e));
	}
	
	@SuppressWarnings("unchecked")
	@Override
	public boolean setTime(final int time) {
		return super.setTime(time, blocks, FurnaceSmeltEvent.class, FurnaceBurnEvent.class);
	}
	
}
