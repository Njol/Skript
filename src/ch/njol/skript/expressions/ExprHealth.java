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
import ch.njol.skript.Skript.ExpressionType;
import ch.njol.skript.api.Changer.ChangeMode;
import ch.njol.skript.api.Getter;
import ch.njol.skript.expressions.base.PropertyExpression;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.SkriptParser.ParseResult;

/**
 * @author Peter Güttinger
 * 
 */
public class ExprHealth extends PropertyExpression<Float> {
	
	static {
		Skript.registerExpression(ExprHealth.class, Float.class, ExpressionType.PROPERTY, "[the] health [of %livingentities%]", "%livingentities%'[s] health");
	}
	
	private Expression<LivingEntity> entities;
	
	@SuppressWarnings("unchecked")
	@Override
	public boolean init(final Expression<?>[] vars, final int matchedPattern, final ParseResult parser) {
		entities = (Expression<LivingEntity>) vars[0];
		setExpr(entities);
		return true;
	}
	
	@Override
	public String toString(final Event e, final boolean debug) {
		return "the health of " + entities.toString(e, debug);
	}
	
	@Override
	protected Float[] get(final Event e) {
		if (e instanceof EntityDamageEvent && getTime() > 0 && entities.getSource() instanceof ExprAttacked) {
			return entities.getArray(e, Float.class, new Getter<Float, LivingEntity>() {
				@Override
				public Float get(final LivingEntity entity) {
					return Float.valueOf(0.5f * (entity.getHealth() - ((EntityDamageEvent) e).getDamage()));// FIXME this is not the actual damage taken!
				}
			});
		}
		return entities.getArray(e, Float.class, new Getter<Float, LivingEntity>() {
			@Override
			public Float get(final LivingEntity entity) {
				return Float.valueOf(0.5f * entity.getHealth());
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
					entity.setHealth(Math.max(0, Math.min(entity.getMaxHealth(), s)));
				}
			break;
			case ADD:
				for (final LivingEntity entity : entities.getArray(e)) {
					entity.setHealth(Math.max(0, Math.min(entity.getMaxHealth(), entity.getHealth() + s)));
				}
			break;
			case REMOVE:
				for (final LivingEntity entity : entities.getArray(e)) {
					entity.setHealth(Math.max(0, Math.min(entity.getMaxHealth(), entity.getHealth() - s)));
				}
			break;
		}
	}
	
	@Override
	public Class<? extends Float> getReturnType() {
		return Float.class;
	}
	
	@Override
	public boolean setTime(final int time) {
		return entities.getSource() instanceof ExprAttacked && super.setTime(time, EntityDamageEvent.class);
	}
}
