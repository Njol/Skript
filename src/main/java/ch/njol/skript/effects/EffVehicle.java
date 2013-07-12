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

import org.bukkit.entity.Entity;
import org.bukkit.event.Event;

import ch.njol.skript.Skript;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Examples;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import ch.njol.skript.entity.EntityData;
import ch.njol.skript.lang.Effect;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.util.Kleenean;

/**
 * @author Peter Güttinger
 */
@SuppressWarnings("serial")
@Name("Vehicle")
@Description("Makes an entity ride another entity, e.g. a minecart, a saddled pig, an arrow, etc.")
@Examples({"make the player ride a saddled pig",
		"make the attacker ride the victim"})
@Since("2.0")
public class EffVehicle extends Effect {
	static {
		Skript.registerEffect(EffVehicle.class,
				"make %entity% ride [(in|on)] %entity/entitydata%"); // TODO eject effect
	}
	
	private Expression<Entity> passenger;
	private Expression<?> vehicle;
	
	@SuppressWarnings("unchecked")
	@Override
	public boolean init(final Expression<?>[] exprs, final int matchedPattern, final Kleenean isDelayed, final ParseResult parseResult) {
		passenger = (Expression<Entity>) exprs[0];
		vehicle = exprs[1];
		return true;
	}
	
	@Override
	protected void execute(final Event e) {
		final Object v = vehicle.getSingle(e);
		if (v == null)
			return;
		final Entity p = passenger.getSingle(e);
		if (p == null)
			return;
		if (v instanceof Entity) {
			((Entity) v).eject();
			((Entity) v).setPassenger(p);
		} else {
			final Entity en = ((EntityData<?>) v).spawn(p.getLocation());
			if (en == null)
				return;
			en.setPassenger(p);
		}
	}
	
	@Override
	public String toString(final Event e, final boolean debug) {
		return "make " + passenger.toString(e, debug) + " ride " + vehicle.toString(e, debug);
	}
	
}
