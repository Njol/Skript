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
import ch.njol.skript.api.Effect;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.skript.util.Timeperiod;
import ch.njol.skript.util.Timespan;

/**
 * @author Peter Güttinger
 *
 */
public class EffPoison extends Effect {
	
	static {
		Skript.registerEffect(EffPoison.class,
				"poison %livingentities% [for %-timespan%]",
				"(cure|unpoison) %livingentities% [from poison]");
	}
	
	private final static int DEFAULT_DURATION = 15*20; // 15 seconds on hard difficulty

	private Expression<LivingEntity> entites;
	private Expression<Timespan> duration;
	
	private boolean cure;
	
	@SuppressWarnings("unchecked")
	@Override
	public boolean init(Expression<?>[] exprs, int matchedPattern, ParseResult parseResult) {
		entites = (Expression<LivingEntity>) exprs[0];
		if (matchedPattern == 0)
			duration = (Expression<Timespan>) exprs[1];
		cure = matchedPattern == 1;
		return true;
	}
	
	@Override
	public String toString(Event e, boolean debug) {
		return "poison "+entites.toString(e, debug);
	}
	
	@Override
	protected void execute(Event e) {
		for (LivingEntity le : entites.getArray(e)) {
			if (!cure) {
				int d = duration == null || duration.getSingle(e) == null ? DEFAULT_DURATION : duration.getSingle(e).getTicks();
				le.addPotionEffect(new PotionEffect(PotionEffectType.POISON, d, 0), true);
			} else {
				le.removePotionEffect(PotionEffectType.POISON);
			}
		}
	}
	
}
