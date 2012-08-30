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
import org.bukkit.util.Vector;

import ch.njol.skript.Skript;
import ch.njol.skript.lang.Effect;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.skript.util.Offset;

/**
 * @author Peter Güttinger
 */
public class EffPush extends Effect {
	
	static {
		Skript.registerEffect(EffPush.class, "(push|thrust) %entities% %offsets% [(at|with) (speed|velocity) %-double%]");
	}
	
	private Expression<Entity> entities;
	private Expression<Offset> directions;
	private Expression<Double> speed = null;
	
	@SuppressWarnings("unchecked")
	@Override
	public boolean init(final Expression<?>[] exprs, final int matchedPattern, final int isDelayed, final ParseResult parseResult) {
		entities = (Expression<Entity>) exprs[0];
		directions = (Expression<Offset>) exprs[1];
		speed = (Expression<Double>) exprs[2];
		return true;
	}
	
	@Override
	protected void execute(final Event e) {
		final Offset o = Offset.combine(directions.getArray(e));
		if (o == null)
			return;
		final Vector mod = o.toVector();
		final Double v = speed == null ? null : speed.getSingle(e);
		if (speed != null && v == null)
			return;
		if (v != null)
			mod.normalize().multiply(v);
		final Entity[] ents = entities.getArray(e);
		for (final Entity ent : ents) {
			ent.setVelocity(ent.getVelocity().add(mod));
		}
	}
	
	@Override
	public String toString(final Event e, final boolean debug) {
		return "push " + entities.toString(e, debug) + " " + directions.toString(e, debug) + (speed == null ? "" : " at speed " + speed.toString(e, debug));
	}
	
}
