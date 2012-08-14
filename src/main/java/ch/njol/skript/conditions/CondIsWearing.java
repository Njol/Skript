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

package ch.njol.skript.conditions;

import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.inventory.ItemStack;

import ch.njol.skript.Skript;
import ch.njol.skript.lang.Condition;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.skript.util.ItemType;
import ch.njol.util.Checker;

/**
 * @author Peter Güttinger
 * 
 */
public class CondIsWearing extends Condition {
	
	static {
		Skript.registerCondition(CondIsWearing.class, "%players% (is|are) wearing %itemtypes%", "%players% (isn't|is not|aren't|are not) wearing %itemtypes%");
	}
	
	private Expression<Player> players;
	private Expression<ItemType> types;
	
	@SuppressWarnings("unchecked")
	@Override
	public boolean init(final Expression<?>[] vars, final int matchedPattern, final boolean isDelayed, final ParseResult parseResult) {
		players = (Expression<Player>) vars[0];
		types = (Expression<ItemType>) vars[1];
		setNegated(matchedPattern == 1);
		return true;
	}
	
	@Override
	public String toString(final Event e, final boolean debug) {
		return players.toString(e, debug) + (players.isSingle() ? " is" : " are") + " wearing " + types;
	}
	
	@Override
	public boolean check(final Event e) {
		return players.check(e, new Checker<Player>() {
			@Override
			public boolean check(final Player p) {
				return types.check(e, new Checker<ItemType>() {
					@Override
					public boolean check(final ItemType t) {
						for (final ItemStack is : p.getInventory().getArmorContents()) {
							if (t.isOfType(is))
								return true;
						}
						return false;
					}
				});
			}
		}, this);
	}
	
}
