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

import java.util.regex.Matcher;

import org.bukkit.Location;
import org.bukkit.event.Event;

import ch.njol.skript.Skript;
import ch.njol.skript.api.Effect;
import ch.njol.skript.api.intern.Variable;
import ch.njol.skript.util.EntityType;
import ch.njol.skript.util.Offset;

/**
 * 
 * @author Peter Güttinger
 * 
 */
public class EffSpawn extends Effect {
	
	static {
		Skript.addEffect(EffSpawn.class,
				"spawn %entitytype%( at %location%)?",
				"spawn %entitytype% %offset% %location%");
	}
	
	private Variable<Location> locations;
	private Variable<Offset> offsets = null;
	private Variable<EntityType> types;
	
	@SuppressWarnings("unchecked")
	@Override
	public void init(final Variable<?>[] vars, final int matchedPattern, final Matcher matcher) {
		types = (Variable<EntityType>) vars[0];
		locations = (Variable<Location>) vars[vars.length - 1];
		if (matchedPattern == 1)
			offsets = (Variable<Offset>) vars[1];
	}
	
	@Override
	public void execute(final Event e) {
		final Iterable<EntityType> ts = types.get(e, false);
		if (offsets != null) {
			for (final Location l : Offset.setOff(offsets.get(e), locations.get(e))) {
				for (final EntityType type : ts) {
					for (int i = 0; i < type.amount; i++)
						l.getWorld().spawn(l, type.c);
				}
			}
		}
		for (final Location l : locations.get(e, false)) {
			for (final EntityType type : ts) {
				for (int i = 0; i < type.amount; i++)
					l.getWorld().spawn(l, type.c);
			}
		}
	}
	
	@Override
	public String getDebugMessage(final Event e) {
		return "spawn " + types.getDebugMessage(e) + " at " + locations.getDebugMessage(e);
	}
	
}
