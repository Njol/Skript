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

package ch.njol.skript.effects;

import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.inventory.Inventory;

import ch.njol.skript.Skript;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Examples;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import ch.njol.skript.lang.Effect;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.util.Kleenean;

/**
 * @author Peter Güttinger
 */
@SuppressWarnings("serial")
@Name("Open Inventory")
@Description({"Opens an inventory to a player. The player can then access and modify the inventory as if it was a chest that he just opened.",
		"Please note that currently 'show' and 'open' have the same effect, but 'show' will eventually show an unmodifiable view of the inventory in the future."})
@Examples({"show the victim's inventory to the player",
		"open the player's inventory for the player"})
@Since("2.0")
public class EffOpenInventory extends Effect {
	static {
		Skript.registerEffect(EffOpenInventory.class, "(0¦open|1¦show) %inventory% (to|for) %players%");
	}
	
	private Expression<Inventory> invi;
	private Expression<Player> players;
	
	@SuppressWarnings("unchecked")
	@Override
	public boolean init(final Expression<?>[] exprs, final int matchedPattern, final Kleenean isDelayed, final ParseResult parseResult) {
		if (parseResult.mark == 1) {
			Skript.warning("Using 'show' inventory instead of 'open' is not recommended as it will eventually show an unmodifiable view of the inventory in the future.");
		}
		invi = (Expression<Inventory>) exprs[0];
		players = (Expression<Player>) exprs[1];
		return true;
	}
	
	@Override
	protected void execute(final Event e) {
		final Inventory i = invi.getSingle(e);
		if (i == null)
			return;
		for (final Player p : players.getArray(e)) {
			p.openInventory(i);
		}
	}
	
	@Override
	public String toString(final Event e, final boolean debug) {
		return "open " + invi.toString(e, debug) + " to " + players.toString(e, debug);
	}
	
}
