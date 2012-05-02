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

/**
 * 
 * @author Peter Güttinger
 * 
 */
public class EffExplosion extends Effect {
	
	static {
		Skript.addEffect(EffExplosion.class, "(create )?explosion (of|with) (force|strength|power) %float%( at %location%)?");
	}
	
	private Variable<Float> forces;
	private Variable<Location> locations;
	
	@SuppressWarnings("unchecked")
	@Override
	public void init(final Variable<?>[] vars, final int matchedPattern, final Matcher matcher) {
		forces = (Variable<Float>) vars[0];
		locations = (Variable<Location>) vars[1];
	}
	
	@Override
	public void execute(final Event e) {
		for (final Float force : forces.get(e, false)) {
			for (final Location l : locations.get(e, false)) {
				l.getWorld().createExplosion(l, force);
			}
		}
	}
	
	@Override
	public String getDebugMessage(final Event e) {
		return "create explosion of force " + forces.getDebugMessage(e) + " at " + locations.getDebugMessage(e);
	}
	
}
