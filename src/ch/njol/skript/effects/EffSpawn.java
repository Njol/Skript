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

import org.bukkit.Location;
import org.bukkit.event.Event;

import ch.njol.skript.Skript;
import ch.njol.skript.api.Effect;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.skript.util.EntityType;
import ch.njol.skript.util.Offset;

/**
 * 
 * @author Peter Güttinger
 * 
 */
public class EffSpawn extends Effect {
	
	static {
		Skript.registerEffect(EffSpawn.class,
				"spawn %entitytypes% [%offset% %locations%]");
	}
	
	private Expression<Location> locations;
	private Expression<Offset> offsets = null;
	private Expression<EntityType> types;
	
	@SuppressWarnings("unchecked")
	@Override
	public boolean init(final Expression<?>[] vars, final int matchedPattern, final ParseResult parser) {
		types = (Expression<EntityType>) vars[0];
		offsets = (Expression<Offset>) vars[1];
		locations = (Expression<Location>) vars[2];
		return true;
	}
	
	@Override
	public void execute(final Event e) {
		final EntityType[] ts = types.getArray(e);
		for (final Location l : Offset.setOff(offsets.getArray(e), locations.getArray(e))) {
			for (final EntityType type : ts) {
				for (int i = 0; i < type.amount; i++)
					l.getWorld().spawn(l, type.c);
			}
		}
	}
	
	@Override
	public String getDebugMessage(final Event e) {
		return "spawn " + types.getDebugMessage(e) + " " + offsets.getDebugMessage(e) + " " + locations.getDebugMessage(e);
	}
	
}
