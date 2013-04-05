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
 * Copyright 2011-2013 Peter Güttinger
 * 
 */

package ch.njol.skript.effects;

import org.bukkit.Location;
import org.bukkit.event.Event;

import ch.njol.skript.Skript;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Examples;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import ch.njol.skript.lang.Effect;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.skript.util.Direction;
import ch.njol.util.Kleenean;

/**
 * TODO explosion without block damage (1.4.5)
 * 
 * @author Peter Güttinger
 */
@SuppressWarnings("serial")
@Name("Explosion")
@Description({"Creates an explosion of a given force. The Minecraft Wiki has an <a href='http://www.minecraftwiki.net/wiki/Explosion'>article on explosions</a> " +
		"which contains the explosion forces of TNT, creepers, etc.",
		"Hint: use a force of 0 to create a fake explosion that does not damage whatsoever."})
@Examples({"create an explosion of force 10 at the player",
		"create an explosion of force 0 at the victim"})
@Since("1.0")
public class EffExplosion extends Effect {
	
	static {
		Skript.registerEffect(EffExplosion.class, "[create] [an] explosion (of|with) (force|strength|power) %number% [%directions% %locations%]");
	}
	
	private Expression<Number> force;
	private Expression<Location> locations;
	
	@SuppressWarnings("unchecked")
	@Override
	public boolean init(final Expression<?>[] exprs, final int matchedPattern, final Kleenean isDelayed, final ParseResult parser) {
		force = (Expression<Number>) exprs[0];
		locations = Direction.combine((Expression<? extends Direction>) exprs[1], (Expression<? extends Location>) exprs[2]);
		return true;
	}
	
	@Override
	public void execute(final Event e) {
		final Number power = force.getSingle(e);
		if (power == null)
			return;
		for (final Location l : locations.getArray(e)) {
			l.getWorld().createExplosion(l, power.floatValue());
		}
	}
	
	@Override
	public String toString(final Event e, final boolean debug) {
		return "create explosion of force " + force.toString(e, debug) + " " + locations.toString(e, debug);
	}
	
}
