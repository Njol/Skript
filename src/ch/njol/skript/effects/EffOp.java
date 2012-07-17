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

package ch.njol.skript.effects;

import org.bukkit.entity.Player;
import org.bukkit.event.Event;

import ch.njol.skript.Skript;
import ch.njol.skript.api.Effect;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.SkriptParser.ParseResult;

/**
 * @author Peter Güttinger
 * 
 */
public class EffOp extends Effect {
	
	static {
		Skript.registerEffect(EffOp.class, "[de[-]]op %players%");
	}
	
	private Expression<Player> players;
	private boolean op;
	
	@SuppressWarnings("unchecked")
	@Override
	public boolean init(final Expression<?>[] exprs, final int matchedPattern, final ParseResult parseResult) {
		players = (Expression<Player>) exprs[0];
		op = !parseResult.expr.substring(0, 2).equalsIgnoreCase("de");
		return true;
	}
	
	@Override
	public String toString(final Event e, final boolean debug) {
		return (op ? "" : "de") + "op " + players.toString(e, debug);
	}
	
	@Override
	protected void execute(final Event e) {
		for (final Player p : players.getArray(e)) {
			p.setOp(op);
		}
	}
	
}
