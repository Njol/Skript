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
import org.bukkit.inventory.ItemStack;

import ch.njol.skript.Skript;
import ch.njol.skript.Skript.ExpressionType;
import ch.njol.skript.classes.Converter;
import ch.njol.skript.expressions.base.PropertyExpression;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.skript.util.PlayerSlot;
import ch.njol.skript.util.Slot;

/**
 * @author Peter Güttinger
 */
public class ExprArmorSlot extends PropertyExpression<Player, Slot> {
	private static final long serialVersionUID = 5373470689647573757L;
	
	static {
		Skript.registerExpression(ExprArmorSlot.class, Slot.class, ExpressionType.PROPERTY,
				"[the] (boot[s]|shoe[s]) [slot] of %players%", "%player%'[s] (boot[s]|shoe[s])",
				"[the] leg[ging]s [slot] of %players%", "%player%'[s] leg[ging]s",
				"[the] chestplate[s] [slot] of %players%", "%player%'[s] chestplate[s]",
				"[the] helm[et][s] [slot] of %players%", "%player%'[s] helm[et][s]");
	}
	
	private Expression<Player> players;
	private int slot;
	
	private final static String[] slotNames = {"boots", "leggings", "chestplate", "helmet"};
	
	@SuppressWarnings("unchecked")
	@Override
	public boolean init(final Expression<?>[] vars, final int matchedPattern, final int isDelayed, final ParseResult parser) {
		players = (Expression<Player>) vars[0];
		setExpr(players);
		slot = matchedPattern / 2;
		return true;
	}
	
	@Override
	public String toString(final Event e, final boolean debug) {
		return slotNames[slot] + " of " + players.toString(e, debug);
	}
	
	@Override
	protected Slot[] get(final Event e, final Player[] source) {
		return get(source, new Converter<Player, Slot>() {
			@Override
			public Slot convert(final Player p) {
				return new PlayerSlot(p.getInventory()) {
					@Override
					public ItemStack getItem() {
						return p.getInventory().getArmorContents()[slot];
					}
					
					@Override
					public void setItem(final ItemStack item) {
						final ItemStack[] armour = p.getInventory().getArmorContents();
						armour[slot] = item;
						p.getInventory().setArmorContents(armour);
					}
					
					@Override
					public String toString(final Event e, final boolean debug) {
						return slotNames[slot] + " of " + p.getName();
					}
				};
			}
		});
	}
	
	@Override
	public Class<Slot> getReturnType() {
		return Slot.class;
	}
	
}
