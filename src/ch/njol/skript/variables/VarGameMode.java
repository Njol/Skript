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

import org.bukkit.GameMode;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;

import ch.njol.skript.Skript;
import ch.njol.skript.api.Changer.ChangeMode;
import ch.njol.skript.api.Converter;
import ch.njol.skript.api.exception.InitException;
import ch.njol.skript.api.exception.ParseException;
import ch.njol.skript.api.intern.Variable;

/**
 * @author Peter Güttinger
 * 
 */
public class VarGameMode extends Variable<GameMode> {
	
	static {
		Skript.addVariable(VarGameMode.class, GameMode.class, "game ?mode of %player%", "%player%'s game ?mode");
	}
	
	private Variable<Player> players;
	
	@SuppressWarnings("unchecked")
	@Override
	public void init(final Variable<?>[] vars, final int matchedPattern, final Matcher matcher) throws InitException, ParseException {
		players = (Variable<Player>) vars[0];
	}
	
	@Override
	public String getDebugMessage(final Event e) {
		return "gamemode of " + players.getDebugMessage(e);
	}
	
	@Override
	protected GameMode[] getAll(final Event e) {
		return super.get(e, players, new Converter<Player, GameMode>() {
			@Override
			public GameMode convert(final Player p) {
				return p.getGameMode();
			}
		}, false);
	}
	
	@Override
	public Class<? extends GameMode> getReturnType() {
		return GameMode.class;
	}
	
	@Override
	public Class<?> acceptChange(final ChangeMode mode) {
		if (mode == ChangeMode.SET)
			return GameMode.class;
		return null;
	}
	
	@Override
	public void change(final Event e, final Variable<?> delta, final ChangeMode mode) throws UnsupportedOperationException {
		final GameMode m = (GameMode) delta.getFirst(e);
		for (final Player p : players.get(e, false))
			p.setGameMode(m);
	}
	
	@Override
	public String toString() {
		return "the gamemode of " + players;
	}
	
}
