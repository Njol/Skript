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
import ch.njol.skript.lang.ExprParser.ParseResult;
import ch.njol.skript.lang.Variable;
import ch.njol.skript.util.Offset;

/**
 * 
 * @author Peter Güttinger
 * 
 */
public class EffExplosion extends Effect {
	
	static {
		Skript.registerEffect(EffExplosion.class, "[create] explosion (of|with) (force|strength|power) %float% [%offsets% %locations%]");
	}
	
	private Variable<Float> force;
	private Variable<Offset> offsets;
	private Variable<Location> locations;
	
	@SuppressWarnings("unchecked")
	@Override
	public void init(final Variable<?>[] vars, final int matchedPattern, final ParseResult parser) {
		force = (Variable<Float>) vars[0];
		offsets = (Variable<Offset>) vars[1];
		locations = (Variable<Location>) vars[2];
	}
	
	@Override
	public void execute(final Event e) {
		final float power = force.getSingle(e);
		for (final Location l : Offset.setOff(offsets.getArray(e), locations.getArray(e))) {
			l.getWorld().createExplosion(l, power);
		}
	}
	
	@Override
	public String getDebugMessage(final Event e) {
		return "create explosion of force " + force.getDebugMessage(e) + " " + offsets.getDebugMessage(e) + " " + locations.getDebugMessage(e);
	}
	
}
