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

import org.bukkit.entity.LivingEntity;
import org.bukkit.event.Event;
import org.bukkit.event.entity.EntityCombustEvent;

import ch.njol.skript.Skript;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Examples;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import ch.njol.skript.lang.Effect;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.skript.util.Timespan;
import ch.njol.util.Kleenean;

/**
 * @author Peter Güttinger
 */
@SuppressWarnings("serial")
@Name("Ignite/Extinguish")
@Description("Set a player ablaze, or extinguish them.")
@Examples({"ignite the player",
		"extinguish the player"})
@Since("1.4")
public class EffIgnite extends Effect {
	
	static {
		Skript.registerEffect(EffIgnite.class,
				"(ignite|set fire to) %livingentities% [for %-timespan%]", "(set|light) %livingentities% on fire [for %-timespan%]",
				"extinguish %livingentities%");
	}
	
	private final static int DEFAULT_DURATION = 8 * 20; // default is 8 seconds for lava and fire, I didn't test other sources
	
	private Expression<LivingEntity> entities;
	private boolean fire;
	private Expression<Timespan> duration = null;
	
	@SuppressWarnings("unchecked")
	@Override
	public boolean init(final Expression<?>[] exprs, final int matchedPattern, final Kleenean isDelayed, final ParseResult parseResult) {
		entities = (Expression<LivingEntity>) exprs[0];
		fire = exprs.length > 1;
		if (fire)
			duration = (Expression<Timespan>) exprs[1];
		return true;
	}
	
	@Override
	protected void execute(final Event e) {
		if (entities.isDefault() && e instanceof EntityCombustEvent && !Delay.isDelayed(e)) {
			((EntityCombustEvent) e).setCancelled(true);// can't change the duration, thus simply cancel the event (and create a new one)
		}
		if (fire) {
			int d = DEFAULT_DURATION;
			if (duration != null) {
				final Timespan t = duration.getSingle(e);
				if (t == null)
					return;
				d = t.getTicks();
			}
			for (final LivingEntity en : entities.getArray(e)) {
				en.setFireTicks(d);
			}
		} else {
			for (final LivingEntity en : entities.getArray(e)) {
				en.setFireTicks(0);
			}
		}
	}
	
	@Override
	public String toString(final Event e, final boolean debug) {
		return fire ? "set " + entities.toString(e, debug) + " on fire" : "extinguish " + entities.toString(e, debug);
	}
	
}
