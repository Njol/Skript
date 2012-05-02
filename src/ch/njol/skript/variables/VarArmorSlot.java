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

import java.util.regex.Matcher;

import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.inventory.ItemStack;

import ch.njol.skript.Skript;
import ch.njol.skript.api.Converter;
import ch.njol.skript.api.exception.InitException;
import ch.njol.skript.api.exception.ParseException;
import ch.njol.skript.api.intern.Variable;
import ch.njol.skript.util.PlayerSlot;
import ch.njol.skript.util.Slot;

/**
 * @author Peter Güttinger
 * 
 */
public class VarArmorSlot extends Variable<Slot> {
	
	static {
		Skript.addVariable(VarArmorSlot.class, Slot.class,
				"(boots?|shoes?) slot( of %player%)?",
				"leg(ging)?s slot( of %player%)?",
				"chest(plate)? slot( of %player%)?",
				"helm(et)? slot( of %player%)?");
	}
	
	private Variable<Player> player;
	private int slot;
	
	private final static String[] slotNames = {"boots", "leggings", "chestplate", "helmet"};
	
	@SuppressWarnings("unchecked")
	@Override
	public void init(final Variable<?>[] vars, final int matchedPattern, final Matcher matcher) throws InitException, ParseException {
		player = (Variable<Player>) vars[0];
		slot = matchedPattern;
	}
	
	@Override
	public String getDebugMessage(final Event e) {
		return slotNames[slot] + " slot of " + player.getDebugMessage(e);
	}
	
	@Override
	protected Slot[] getAll(final Event e) {
		return get(e, player, new Converter<Player, Slot>() {
			@Override
			public Slot convert(final Player p) {
				return new PlayerSlot(p.getInventory()) {
					@Override
					public ItemStack getItem() {
						return p.getInventory().getArmorContents()[slot];
					}
					@Override
					public void setItem(ItemStack item) {
						ItemStack[] armour = p.getInventory().getArmorContents();
						armour[slot] = item;
						p.getInventory().setArmorContents(null);
					}
				};
			}
		}, false);
	}
	
	@Override
	public String toString() {
		return slotNames[slot] + " slot of " + player;
	}
	
	@Override
	public Class<? extends Slot> getReturnType() {
		return Slot.class;
	}
	
}
