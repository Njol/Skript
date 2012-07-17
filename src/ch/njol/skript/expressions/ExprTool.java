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

import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.player.PlayerItemHeldEvent;
import org.bukkit.inventory.ItemStack;

import ch.njol.skript.Skript;
import ch.njol.skript.Skript.ExpressionType;
import ch.njol.skript.api.Getter;
import ch.njol.skript.expressions.base.PropertyExpression;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.skript.util.Slot;

/**
 * 
 * @author Peter Güttinger
 * 
 */
public class ExprTool extends PropertyExpression<Slot> {
	
	static {
		Skript.registerExpression(ExprTool.class, Slot.class, ExpressionType.PROPERTY, "[the] (tool|held item) [of %players%]", "%player%'[s] (tool|held item)");
	}
	
	private Expression<Player> players;
	
	@SuppressWarnings("unchecked")
	@Override
	public boolean init(final Expression<?>[] vars, final int matchedPattern, final ParseResult parser) {
		players = (Expression<Player>) vars[0];
		setExpr(players);
		return true;
	}
	
	@Override
	protected Slot[] get(final Event e) {
		if (e instanceof PlayerItemHeldEvent && players.isDefault()) {
			return players.getArray(e, Slot.class, new Getter<Slot, Player>() {
				@Override
				public Slot get(final Player p) {
					return new Slot(p.getInventory(), getTime() >= 0 ? ((PlayerItemHeldEvent) e).getNewSlot() : ((PlayerItemHeldEvent) e).getPreviousSlot());
				}
			});
		}
		return players.getArray(e, Slot.class, new Getter<Slot, Player>() {
			@Override
			public Slot get(final Player p) {
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
						return (getTime() == 1 ? "future " : getTime() == -1 ? "former " : "") + "tool of " + p.getName();
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
			return "the " + (getTime() == 1 ? "future " : getTime() == -1 ? "former " : "") + "tool of " + players.toString(e, debug);
		return Skript.getDebugMessage(getSingle(e));
	}
	
	@Override
	public boolean setTime(final int time) {
		return super.setTime(time, PlayerItemHeldEvent.class, players);
	}
}
