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
import org.bukkit.inventory.Inventory;

import ch.njol.skript.Skript;
import ch.njol.skript.api.Changer.ChangeMode;
import ch.njol.skript.api.exception.InitException;
import ch.njol.skript.api.intern.Literal;
import ch.njol.skript.api.intern.Variable;
import ch.njol.skript.data.DefaultChangers;

/**
 * @author Peter Güttinger
 * 
 */
public class VarPlayer extends Variable<Player> {
	
	static {
		Skript.addVariable(VarPlayer.class, Player.class, "player", "me");
	}
	
	@Override
	public void init(final Variable<?>[] vars, final int matchedPattern, final Matcher matcher) throws InitException {}
	
	@Override
	protected Player[] getAll(final Event e) {
		return new Player[] {Skript.getEventValue(e, Player.class)};
	}
	
	@Override
	public Class<Player> getReturnType() {
		return Player.class;
	}
	
	@Override
	public String getDebugMessage(final Event e) {
		if (e == null)
			return "player";
		return Skript.toString(getFirst(e));
	}
	
	@Override
	public Class<?> acceptChange(final ChangeMode mode) {
		return DefaultChangers.inventoryChanger.acceptChange(mode);
	}
	
	@Override
	public void change(final Event e, final Variable<?> delta, final ChangeMode mode) {
		final Player p = getFirst(e);
		if (p == null)
			return;
		DefaultChangers.inventoryChanger.change(e, new Literal<Inventory>(p.getInventory()), delta, mode);
	}
	
	@Override
	public String toString() {
		return "the player";
	}
	
}
