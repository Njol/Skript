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

import java.util.Iterator;

import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.event.Event;

import ch.njol.skript.Skript;
import ch.njol.skript.api.LoopExpr;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.SkriptParser.ParseResult;

/**
 * @author Peter Güttinger
 * 
 */
public class LoopVarWorld extends LoopExpr<World> {
	
	static {
		Skript.registerLoop(LoopVarWorld.class, World.class, "worlds");
	}
	
	@Override
	public boolean init(final Expression<?>[] vars, final int matchedPattern, final ParseResult parser) {
		return true;
	}
	
	@Override
	protected Iterator<World> iterator(final Event e) {
		return Bukkit.getWorlds().iterator();
	}
	
	@Override
	public Class<? extends World> getReturnType() {
		return World.class;
	}
	
	@Override
	public String getLoopDebugMessage(final Event e) {
		return "worlds";
	}
	
	@Override
	public String toString() {
		return "the loop-world";
	}
	
	@Override
	public boolean isLoopOf(final String s) {
		return s.equalsIgnoreCase("world");
	}
	
}
