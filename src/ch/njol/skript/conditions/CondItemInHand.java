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

import ch.njol.skript.Skript;
import ch.njol.skript.api.Condition;
import ch.njol.skript.lang.ExprParser.ParseResult;
import ch.njol.skript.lang.Variable;
import ch.njol.skript.util.ItemType;
import ch.njol.util.Checker;

/**
 * @author Peter Güttinger
 * 
 */
public class CondItemInHand extends Condition {
	
	static {
		Skript.registerCondition(CondItemInHand.class,
				"[%players%] ha(s|ve) %itemtypes% in hand",
				"[%players%] (is|are) holding %itemtypes%",
				"[%players%] (ha(s|ve) not|do[es]n't have) %itemtypes% in hand",
				"[%players%] (is not|isn't) holding %itemtypes%");
	}
	
	private Variable<Player> players;
	private Variable<ItemType> types;
	
	@SuppressWarnings("unchecked")
	@Override
	public void init(final Variable<?>[] vars, final int matchedPattern, final ParseResult parser) {
		players = (Variable<Player>) vars[0];
		types = (Variable<ItemType>) vars[1];
		setNegated(matchedPattern >= 2);
	}
	
	@Override
	public boolean run(final Event e) {
		return players.check(e, new Checker<Player>() {
			@Override
			public boolean check(final Player p) {
				return types.check(e, new Checker<ItemType>() {
					@Override
					public boolean check(final ItemType type) {
						return type.isOfType(p.getItemInHand());
					}
				});
			}
		}, this);
	}
	
	@Override
	public String getDebugMessage(final Event e) {
		return players.getDebugMessage(e) + " is holding " + types.getDebugMessage(e);
	}
	
}
