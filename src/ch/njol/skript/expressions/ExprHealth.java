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

package ch.njol.skript.expressions;

import org.bukkit.entity.LivingEntity;
import org.bukkit.event.Event;
import org.bukkit.event.entity.EntityDamageEvent;

import ch.njol.skript.Skript;
import ch.njol.skript.api.Changer.ChangeMode;
import ch.njol.skript.api.Getter;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.SimpleExpression;
import ch.njol.skript.lang.SkriptParser.ParseResult;

/**
 * @author Peter Güttinger
 * 
 */
public class ExprHealth extends SimpleExpression<Float> {
	
	static {
		Skript.registerExpression(ExprHealth.class, Float.class, "[the] health [of %livingentities%]", "%livingentities%'[s] health");
	}
	
	private Expression<LivingEntity> entities;
	
	@SuppressWarnings("unchecked")
	@Override
	public boolean init(final Expression<?>[] vars, final int matchedPattern, final ParseResult parser) {
		entities = (Expression<LivingEntity>) vars[0];
		return true;
	}
	
	@Override
	public String getDebugMessage(final Event e) {
		return "health of " + entities.getDebugMessage(e);
	}
	
	@Override
	protected Float[] getAll(final Event e) {
		if (e instanceof EntityDamageEvent && getTime() >= 0 && entities.isDefault()) {
			return entities.getArray(e, Float.class, new Getter<Float, LivingEntity>() {
				@Override
				public Float get(final LivingEntity entity) {
					return Float.valueOf(1f / 2 * (entity.getHealth() - ((EntityDamageEvent) e).getDamage()));
				}
			});
		}
		return entities.getArray(e, Float.class, new Getter<Float, LivingEntity>() {
			@Override
			public Float get(final LivingEntity entity) {
				return Float.valueOf(1f / 2 * entity.getHealth());
			}
		});
	}
	
	@Override
	public Class<?> acceptChange(final ChangeMode mode) {
		return Float.class;
	}
	
	@Override
	public void change(final Event e, final Object delta, final ChangeMode mode) {
		int s = 0;
		if (mode != ChangeMode.CLEAR)
			s = Math.round(((Float) delta).floatValue() * 2);
		switch (mode) {
			case CLEAR:
			case SET:
				for (final LivingEntity entity : entities.getArray(e)) {
					entity.setHealth(s);
				}
				return;
			case ADD:
				for (final LivingEntity entity : entities.getArray(e)) {
					entity.setHealth(entity.getHealth() + s);
				}
				return;
			case REMOVE:
				for (final LivingEntity entity : entities.getArray(e)) {
					entity.setHealth(entity.getHealth() - s);
				}
				return;
		}
	}
	
	@Override
	public Class<? extends Float> getReturnType() {
		return Float.class;
	}
	
	@Override
	public String toString() {
		return "the health of " + entities;
	}
	
	@Override
	public boolean isSingle() {
		return entities.isSingle();
	}
	
	@Override
	public boolean setTime(final int time) {
		return super.setTime(time, EntityDamageEvent.class, entities);
	}
}
