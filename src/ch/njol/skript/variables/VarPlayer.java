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
import org.bukkit.inventory.Inventory;

import ch.njol.skript.Skript;
import ch.njol.skript.api.Changer.ChangeMode;
import ch.njol.skript.data.DefaultChangers;
import ch.njol.skript.lang.SimpleLiteral;
import ch.njol.skript.lang.Variable;
import ch.njol.skript.variables.base.EventValueVariable;

/**
 * @author Peter Güttinger
 * 
 */
public class VarPlayer extends EventValueVariable<Player> {
	
	public VarPlayer() {
		super(Player.class);
	}
	
	static {
		Skript.registerVariable(VarPlayer.class, Player.class, "[the] player", "me");
	}
	
	@Override
	public String getDebugMessage(final Event e) {
		if (e == null)
			return "player";
		return Skript.getDebugMessage(getSingle(e));
	}
	
	@Override
	public Class<?> acceptChange(final ChangeMode mode) {
		return DefaultChangers.inventoryChanger.acceptChange(mode);
	}
	
	@Override
	public void change(final Event e, final Variable<?> delta, final ChangeMode mode) {
		final Player p = getSingle(e);
		if (p == null)
			return;
		DefaultChangers.inventoryChanger.change(e, new SimpleLiteral<Inventory>(p.getInventory(), false), delta, mode);
	}
	
	@Override
	public String toString() {
		return "the player";
	}
	
	@Override
	public boolean isSingle() {
		return true;
	}
	
}
