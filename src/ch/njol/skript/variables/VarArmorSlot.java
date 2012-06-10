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

package ch.njol.skript.variables;

import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.inventory.ItemStack;

import ch.njol.skript.Skript;
import ch.njol.skript.api.Converter;
import ch.njol.skript.lang.ExprParser.ParseResult;
import ch.njol.skript.lang.SimpleVariable;
import ch.njol.skript.lang.Variable;
import ch.njol.skript.util.PlayerSlot;
import ch.njol.skript.util.Slot;

/**
 * @author Peter Güttinger
 * 
 */
public class VarArmorSlot extends SimpleVariable<Slot> {
	
	static {
		Skript.registerVariable(VarArmorSlot.class, Slot.class,
				"[the] (boot[s]|shoe[s]) [slot] of %players%", "%player%'[s] (boot[s]|shoe[s])",
				"[the] leg[ging]s [slot] of %players%", "%player%'[s] leg[ging]s",
				"[the] chestplate [slot] of %players%", "%player%'[s] chestplate",
				"[the] helm[et] [slot] of %players%", "%player%'[s] helm[et]");
	}
	
	private Variable<Player> players;
	private int slot;
	
	private final static String[] slotNames = {"boots", "leggings", "chestplate", "helmet"};
	
	@SuppressWarnings("unchecked")
	@Override
	public boolean init(final Variable<?>[] vars, final int matchedPattern, final ParseResult parser) {
		players = (Variable<Player>) vars[0];
		slot = matchedPattern / 2;
		return true;
	}
	
	@Override
	public String getDebugMessage(final Event e) {
		return slotNames[slot] + " of " + players.getDebugMessage(e);
	}
	
	@Override
	protected Slot[] getAll(final Event e) {
		return players.getArray(e, Slot.class, new Converter<Player, Slot>() {
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
					public String getDebugMessage(final Event e) {
						return slotNames[slot] + " of " + p.getName();
					}
				};
			}
		});
	}
	
	@Override
	public String toString() {
		return slotNames[slot] + " of " + players;
	}
	
	@Override
	public Class<? extends Slot> getReturnType() {
		return Slot.class;
	}
	
	@Override
	public boolean isSingle() {
		return true;
	}
	
}
