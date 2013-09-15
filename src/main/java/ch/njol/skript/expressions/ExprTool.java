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

package ch.njol.skript.expressions;

import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.player.PlayerBucketEmptyEvent;
import org.bukkit.event.player.PlayerBucketEvent;
import org.bukkit.event.player.PlayerBucketFillEvent;
import org.bukkit.event.player.PlayerItemHeldEvent;
import org.bukkit.inventory.ItemStack;

import ch.njol.skript.Skript;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Examples;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import ch.njol.skript.effects.Delay;
import ch.njol.skript.expressions.base.PropertyExpression;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.ExpressionType;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.skript.registrations.Classes;
import ch.njol.skript.util.Getter;
import ch.njol.skript.util.Slot;
import ch.njol.util.Kleenean;

/**
 * @author Peter Güttinger
 */
@SuppressWarnings("serial")
@Name("Tool")
@Description("The item a player is holding.")
@Examples({"player is holding a pickaxe",
		"# is the same as",
		"player's tool is a pickaxe"})
@Since("1.0")
public class ExprTool extends PropertyExpression<Player, Slot> { // TODO for mobs as well
	static {
		Skript.registerExpression(ExprTool.class, Slot.class, ExpressionType.PROPERTY, "[the] (tool|held item|weapon) [of %players%]", "%player%'[s] (tool|held item)");
	}
	
	@SuppressWarnings("unchecked")
	@Override
	public boolean init(final Expression<?>[] exprs, final int matchedPattern, final Kleenean isDelayed, final ParseResult parser) {
		setExpr((Expression<Player>) exprs[0]);
		return true;
	}
	
	@Override
	protected Slot[] get(final Event e, final Player[] source) {
		final boolean delayed = Delay.isDelayed(e);
		return get(source, new Getter<Slot, Player>() {
			@Override
			public Slot get(final Player p) {
				if (!delayed) {
					if (e instanceof PlayerItemHeldEvent && ((PlayerItemHeldEvent) e).getPlayer() == p) {
						return new Slot(((PlayerItemHeldEvent) e).getPlayer().getInventory(), getTime() >= 0 ? ((PlayerItemHeldEvent) e).getNewSlot() : ((PlayerItemHeldEvent) e).getPreviousSlot());
					} else if (e instanceof PlayerBucketEvent && ((PlayerBucketEvent) e).getPlayer() == p) {
						return new Slot(((PlayerBucketEvent) e).getPlayer().getInventory(), ((PlayerBucketEvent) e).getPlayer().getInventory().getHeldItemSlot()) {
							@Override
							public ItemStack getItem() {
								return getTime() <= 0 ? super.getItem() : ((PlayerBucketEvent) e).getItemStack();
							}
							
							@Override
							public void setItem(final ItemStack item) {
								if (getTime() >= 0) {
									((PlayerBucketEvent) e).setItemStack(item);
								} else {
									super.setItem(item);
								}
							}
						};
					}
				}
				return new Slot(p.getInventory(), p.getInventory().getHeldItemSlot()) {
					@Override
					public void setItem(final ItemStack item) {
						p.setItemInHand(item);
					}
					
					@Override
					public ItemStack getItem() {
						return p.getItemInHand();
					}
					
					@Override
					public String toString(final Event e, final boolean debug) {
						return "the " + (getTime() == 1 ? "future " : getTime() == -1 ? "former " : "") + "tool of " + p.getName();
					}
				};
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
			return "the " + (getTime() == 1 ? "future " : getTime() == -1 ? "former " : "") + "tool of " + getExpr().toString(e, debug);
		return Classes.getDebugMessage(getSingle(e));
	}
	
	@SuppressWarnings("unchecked")
	@Override
	public boolean setTime(final int time) {
		return super.setTime(time, getExpr(), PlayerItemHeldEvent.class, PlayerBucketFillEvent.class, PlayerBucketEmptyEvent.class);
	}
}
