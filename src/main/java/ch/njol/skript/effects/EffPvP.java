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

import org.bukkit.World;
import org.bukkit.event.Event;

import ch.njol.skript.Skript;
import ch.njol.skript.lang.Effect;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.util.Kleenean;

/**
 * @author Peter Güttinger
 */
public class EffPvP extends Effect {
	
	private static final long serialVersionUID = 1566222140734268666L;
	
	static {
		Skript.registerEffect(EffPvP.class, "enable PvP [in %worlds%]", "disable PVP [in %worlds%]");
	}
	
	private Expression<World> worlds;
	private boolean enable;
	
	@SuppressWarnings("unchecked")
	@Override
	public boolean init(final Expression<?>[] exprs, final int matchedPattern, final Kleenean isDelayed, final ParseResult parseResult) {
		worlds = (Expression<World>) exprs[0];
		enable = matchedPattern == 0;
		return true;
	}
	
	@Override
	public String toString(final Event e, final boolean debug) {
		return (enable ? "enable" : "disable") + " PvP in " + worlds.toString(e, debug);
	}
	
	@Override
	protected void execute(final Event e) {
		for (final World w : worlds.getArray(e)) {
			w.setPVP(enable);
		}
	}
	
}
