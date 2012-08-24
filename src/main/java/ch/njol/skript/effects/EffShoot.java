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
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Projectile;
import org.bukkit.event.Event;

import ch.njol.skript.Skript;
import ch.njol.skript.entity.EntityData;
import ch.njol.skript.lang.Effect;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.SkriptParser.ParseResult;

/**
 * @author Peter Güttinger
 * 
 */
public class EffShoot extends Effect {
	
	static {
		Skript.registerEffect(EffShoot.class,
				"shoot %entitydatas% [from %livingentity%] [(at|with) (speed|velocity) %-double%]",
				"(make|let) %livingentity% shoot %entitydatas% [(at|with) (speed|velocity) %-double%]");
	}
	
	private final static double DEFAULT_SPEED = 5;
	
	private Expression<EntityData<?>> types;
	private Expression<LivingEntity> shooters;
	private Expression<Double> velocity;
	
	@SuppressWarnings("unchecked")
	@Override
	public boolean init(final Expression<?>[] exprs, final int matchedPattern, final int isDelayed, final ParseResult parseResult) {
		types = (Expression<EntityData<?>>) exprs[matchedPattern];
		shooters = (Expression<LivingEntity>) exprs[1 - matchedPattern];
		velocity = (Expression<Double>) exprs[2];
		return true;
	}
	
	@Override
	protected void execute(final Event e) {
		final Double v = velocity == null ? DEFAULT_SPEED : velocity.getSingle(e);
		if (v == null)
			return;
		for (final LivingEntity shooter : shooters.getArray(e)) {
			for (final EntityData<?> d : types.getArray(e)) {
				if (Projectile.class.isAssignableFrom(d.getType())) {
					final Projectile projectile = shooter.launchProjectile((Class<? extends Projectile>) d.getType());
					set(projectile, d);
					if (velocity != null)
						projectile.setVelocity(projectile.getVelocity().normalize().multiply(v));
				} else {
					Location loc = shooter.getLocation();
					loc.setY(loc.getY() + shooter.getEyeHeight()/2);
					final Entity projectile = d.spawn(loc);
					if (projectile != null)
						projectile.setVelocity(shooter.getLocation().getDirection().multiply(v));
				}
			}
		}
	}
	
	@SuppressWarnings("unchecked")
	private final static <E extends Entity> void set(final Entity e, final EntityData<E> d) {
		d.set((E) e);
	}
	
	@Override
	public String toString(final Event e, final boolean debug) {
		return "shoot " + types.toString(e, debug) + " from " + shooters.toString(e, debug);
	}
	
}
