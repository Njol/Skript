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

import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;

import ch.njol.skript.Skript;
import ch.njol.skript.effects.EffBan;
import ch.njol.skript.lang.Condition;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.util.Checker;

/**
 * @author Peter Güttinger
 */
public class CondIsBanned extends Condition {
	
	static {
		Skript.registerCondition(CondIsBanned.class,
				"%offlineplayers/strings% (is|are) banned", "%players/strings% (is|are) IP(-| |)banned",
				"%offlineplayers/strings% (isn't|is not|aren't|are not) banned", "%players/strings% (isn't|is not|aren't|are not) IP(-| |)banned");
	}
	
	private Expression<?> players;
	
	boolean ipBanned;
	
	@Override
	public boolean init(final Expression<?>[] exprs, final int matchedPattern, final int isDelayed, final ParseResult parseResult) {
		players = exprs[0];
		setNegated(matchedPattern >= 2);
		ipBanned = matchedPattern % 2 == 1;
		return true;
	}
	
	@Override
	public boolean check(final Event e) {
		return players.check(e, new Checker<Object>() {
			@Override
			public boolean check(final Object o) {
				if (o instanceof Player) {
					if (ipBanned) {
						return Bukkit.getIPBans().contains(((Player) o).getAddress().getAddress().getHostAddress());
					} else {
						return ((Player) o).isBanned();
					}
				} else if (o instanceof OfflinePlayer) {
					return ((OfflinePlayer) o).isBanned();
				} else if (o instanceof String) {
					if (EffBan.IPv4.matcher((String) o).matches()) {
						return Bukkit.getIPBans().contains(o);
					} else {
						return Bukkit.getOfflinePlayer((String) o).isBanned();
					}
				}
				assert false;
				return false;
			}
		}, this);
	}
	
	@Override
	public String toString(final Event e, final boolean debug) {
		return players.toString(e, debug) + (players.isSingle() ? " is " : " are ") + (isNegated() ? "not " : "") + "banned";
	}
	
}
