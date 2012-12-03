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

import org.bukkit.entity.LivingEntity;
import org.bukkit.event.Event;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import ch.njol.skript.Skript;
import ch.njol.skript.lang.Effect;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.skript.util.Timespan;
import ch.njol.util.Kleenean;

/**
 * @author Peter Güttinger
 * 
 */
public class EffPotion extends Effect {
	private static final long serialVersionUID = 1515902809488915494L;
	
	static {
		Skript.registerEffect(EffPotion.class,
				"apply [potion of] %potioneffecttypes% [potion] [[[of] tier] %-number%] to %livingentities% [for %-timespan%]",
				"remove %potioneffecttypes% from %livingentities%");
	}
	
	private final static int DEFAULT_DURATION = 15 * 20; // 15 seconds
	
	private Expression<PotionEffectType> types;
	private Expression<Number> tier;
	private Expression<LivingEntity> entities;
	private Expression<Timespan> duration;
	private boolean apply;
	
	@SuppressWarnings("unchecked")
	@Override
	public boolean init(final Expression<?>[] exprs, final int matchedPattern, final Kleenean isDelayed, final ParseResult parseResult) {
		apply = matchedPattern == 0;
		if (apply) {
			types = (Expression<PotionEffectType>) exprs[0];
			tier = (Expression<Number>) exprs[1];
			entities = (Expression<LivingEntity>) exprs[2];
			duration = (Expression<Timespan>) exprs[3];
		} else {
			types = (Expression<PotionEffectType>) exprs[0];
			entities = (Expression<LivingEntity>) exprs[1];
		}
		return true;
	}
	
	@Override
	public String toString(final Event e, final boolean debug) {
		if (apply)
			return "apply " + types.toString(e, debug) + (tier == null ? "" : " of tier " + tier.toString(e, debug)) + " to " + entities.toString(e, debug) + (duration == null ? "" : " for " + duration.toString(e, debug));
		else
			return "remove " + types.toString(e, debug) + " from " + entities.toString(e, debug);
	}
	
	@Override
	protected void execute(final Event e) {
		final PotionEffectType[] ts = types.getArray(e);
		if (ts.length == 0)
			return;
		if (!apply) {
			for (final LivingEntity en : entities.getArray(e)) {
				for (final PotionEffectType t : ts)
					en.removePotionEffect(t);
			}
			return;
		}
		int a = 0;
		if (tier != null) {
			final Number amp = tier.getSingle(e);
			if (amp == null)
				return;
			a = amp.intValue() - 1;
		}
		int d = DEFAULT_DURATION;
		if (duration != null) {
			final Timespan dur = duration.getSingle(e);
			if (dur == null)
				return;
			d = dur.getTicks();
		}
		for (final LivingEntity en : entities.getArray(e)) {
			for (final PotionEffectType t : ts) {
				int duration = d;
				if (en.hasPotionEffect(t)) {
					for (final PotionEffect eff : en.getActivePotionEffects()) {
						if (eff.getType() == t) {
							duration += eff.getDuration();
							break;
						}
					}
				}
				en.addPotionEffect(new PotionEffect(t, duration, a), true);
			}
		}
	}
	
}
