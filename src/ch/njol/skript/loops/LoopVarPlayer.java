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

package ch.njol.skript.loops;

import java.util.Arrays;
import java.util.Iterator;
import java.util.ListIterator;

import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;

import ch.njol.skript.Skript;
import ch.njol.skript.api.LoopVar;
import ch.njol.skript.lang.ExprParser.ParseResult;
import ch.njol.skript.lang.Variable;
import ch.njol.skript.util.Utils;

/**
 * @author Peter Güttinger
 * 
 */
public class LoopVarPlayer extends LoopVar<Player> {
	
	static {
		Skript.registerLoop(LoopVarPlayer.class, Player.class, "players", "players in world[s] %worlds%");
	}
	
	private Variable<World> worlds = null;
	
	@SuppressWarnings("unchecked")
	@Override
	public boolean init(final Variable<?>[] vars, final int matchedPattern, final ParseResult parser) {
		if (vars.length > 0)
			worlds = (Variable<World>) vars[0];
		return true;
	}
	
	@Override
	protected Iterator<Player> iterator(final Event e) {
		if (worlds == null)
			return Arrays.asList(Bukkit.getOnlinePlayers()).iterator();
		return new Iterator<Player>() {
			
			private final ListIterator<Player> players = Arrays.asList(Bukkit.getOnlinePlayers()).listIterator();
			
			private final World[] ws = worlds.getArray(e);
			
			@Override
			public boolean hasNext() {
				while (players.hasNext()) {
					if (Utils.indexOf(ws, players.next().getWorld()) != -1) {
						players.previous();
						return true;
					}
				}
				return false;
			}
			
			@Override
			public Player next() {
				return players.next();
			}
			
			@Override
			public void remove() {}
			
		};
	}
	
	@Override
	public Class<? extends Player> getReturnType() {
		return Player.class;
	}
	
	@Override
	public String getLoopDebugMessage(final Event e) {
		return "players in world " + worlds.getDebugMessage(e);
	}
	
	@Override
	public String toString() {
		return "the loop-player";
	}
	
	@Override
	public boolean isLoopOf(final String s) {
		return s.equalsIgnoreCase("player");
	}
	
}
