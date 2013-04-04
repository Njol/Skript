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

import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;

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
@Name("Ban")
@Description("Bans/unbans a player or IP.")
@Examples({"unban player",
		"ban \"127.0.0.1\""})
@Since("1.4")
public class EffBan extends Effect {
	
	static {
		Skript.registerEffect(EffBan.class,
				"ban %strings/offlineplayers%", "unban %strings/offlineplayers%",
				"ban %players% by IP", "unban %players% by IP");
	}
	
	private Expression<?> players;
	
	private boolean ban;
	private boolean ipBan;
	
	@Override
	public boolean init(final Expression<?>[] exprs, final int matchedPattern, final Kleenean isDelayed, final ParseResult parseResult) {
		players = exprs[0];
		ban = matchedPattern % 2 == 0;
		ipBan = matchedPattern >= 2;
		return true;
	}
	
	@Override
	protected void execute(final Event e) {
		for (final Object o : players.getArray(e)) {
			if (o instanceof Player) {
				if (ipBan) {
					if (ban)
						Bukkit.banIP(((Player) o).getAddress().getAddress().getHostAddress());
					else
						Bukkit.unbanIP(((Player) o).getAddress().getAddress().getHostAddress());
				} else {
					((Player) o).setBanned(ban);
				}
			} else if (o instanceof OfflinePlayer) {
				((OfflinePlayer) o).setBanned(ban);
			} else if (o instanceof String) {
				if (ban)
					Bukkit.banIP((String) o);
				else
					Bukkit.unbanIP((String) o);
				Bukkit.getOfflinePlayer((String) o).setBanned(ban);
			} else {
				assert false;
			}
		}
	}
	
	@Override
	public String toString(final Event e, final boolean debug) {
		return (ban ? "" : "un") + "ban " + players.toString(e, debug);
	}
	
}
