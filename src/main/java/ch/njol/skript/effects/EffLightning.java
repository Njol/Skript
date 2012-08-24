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
import ch.njol.skript.lang.Effect;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.skript.util.Offset;

/**
 * @author Peter Güttinger
 * 
 */
public class EffLightning extends Effect {
	
	static {
		Skript.registerEffect(EffLightning.class, "(create|strike) lightning %offsets% %locations%", "(create|strike) lightning[ ]effect %offsets% %locations%");
	}
	
	private Expression<Offset> offsets;
	private Expression<Location> locations;
	
	private boolean effectOnly;
	
	@SuppressWarnings("unchecked")
	@Override
	public boolean init(final Expression<?>[] exprs, final int matchedPattern, final int isDelayed, final ParseResult parseResult) {
		offsets = (Expression<Offset>) exprs[0];
		locations = (Expression<Location>) exprs[1];
		effectOnly = matchedPattern == 1;
		return true;
	}
	
	@Override
	protected void execute(final Event e) {
		for (final Location l : Offset.setOff(offsets.getArray(e), locations.getArray(e))) {
			if (effectOnly)
				l.getWorld().strikeLightningEffect(l);
			else
				l.getWorld().strikeLightning(l);
		}
	}
	
	@Override
	public String toString(final Event e, final boolean debug) {
		return "strike lightning " + (effectOnly ? "effect " : "") + offsets.toString(e, debug) + locations.toString(e, debug);
	}
	
}
