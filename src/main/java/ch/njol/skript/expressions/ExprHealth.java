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

package ch.njol.skript.expressions;

import org.bukkit.entity.LivingEntity;
import org.bukkit.event.Event;

import ch.njol.skript.Skript;
import ch.njol.skript.classes.Changer.ChangeMode;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Examples;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import ch.njol.skript.expressions.base.PropertyExpression;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.ExpressionType;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.skript.util.Getter;
import ch.njol.skript.util.Utils;
import ch.njol.util.Kleenean;
import ch.njol.util.Math2;

/**
 * @author Peter Güttinger
 */
@SuppressWarnings("serial")
@Name("Health")
@Description("The health of a creature, e.g. a player, mob, villager, etc. from 0 to the creature's max health, e.g. 10 for players.")
@Examples({"message \"You have %health% HP left.\""})
@Since("1.0")
public class ExprHealth extends PropertyExpression<LivingEntity, Float> {
	
	static {
		Skript.registerExpression(ExprHealth.class, Float.class, ExpressionType.PROPERTY, "[the] health [of %livingentities%]", "%livingentities%'[s] health");
	}
	
	@SuppressWarnings("unchecked")
	@Override
	public boolean init(final Expression<?>[] vars, final int matchedPattern, final Kleenean isDelayed, final ParseResult parser) {
		setExpr((Expression<LivingEntity>) vars[0]);
		return true;
	}
	
	@Override
	public String toString(final Event e, final boolean debug) {
		return "the health of " + getExpr().toString(e, debug);
	}
	
	@Override
	protected Float[] get(final Event e, final LivingEntity[] source) {
//		if (e instanceof EntityDamageEvent && getTime() > 0 && entities.getSource() instanceof ExprAttacked && !Delay.isDelayed(e)) {
//			return ConverterUtils.convert(entities.getArray(e), Float.class, new Getter<Float, LivingEntity>() {
//				@Override
//				public Float get(final LivingEntity entity) {
//					return Float.valueOf(0.5f * (entity.getHealth() - ((EntityDamageEvent) e).getDamage()));
//				}
//			});
//		}
		return get(source, new Getter<Float, LivingEntity>() {
			@Override
			public Float get(final LivingEntity entity) {
				if (entity.isDead())
					return Float.valueOf(0);
				return Float.valueOf(0.5f * entity.getHealth());
			}
		});
	}
	
//	@Override
//	public Class<?>[] acceptChange() {
//		return Skript.array(Number.class);
//	}
//	
//	@Override
//	public void change(Event e, final Changer2<Number> changer) throws UnsupportedOperationException {
//		getExpr().change(e, new Changer2<LivingEntity>() {
//			@Override
//			public LivingEntity change(LivingEntity e) {
//				e.setHealth(Math2.fit(0, Math.round(2*changer.change(e.getHealth()/2f).doubleValue()), e.getMaxHealth()));
//				return e;
//			}
//		});
//	}
	
	@SuppressWarnings("unchecked")
	@Override
	public Class<?>[] acceptChange(final ChangeMode mode) {
		return Utils.array(Number.class);
	}
	
	@Override
	public void change(final Event e, final Object delta, final ChangeMode mode) {
		int s = 0;
		if (mode != ChangeMode.DELETE)
			s = Math.round(((Number) delta).floatValue() * 2);
		switch (mode) {
			case DELETE:
			case SET:
				for (final LivingEntity entity : getExpr().getArray(e)) {
					entity.setHealth(Math2.fit(0, s, entity.getMaxHealth()));
				}
				break;
			case ADD:
				for (final LivingEntity entity : getExpr().getArray(e)) {
					entity.setHealth(Math2.fit(0, entity.getHealth() + s, entity.getMaxHealth()));
				}
				break;
			case REMOVE:
				for (final LivingEntity entity : getExpr().getArray(e)) {
					entity.setHealth(Math2.fit(0, entity.getHealth() - s, entity.getMaxHealth()));
				}
				break;
		}
	}
	
	@Override
	public Class<Float> getReturnType() {
		return Float.class;
	}
	
//	@Override
//	public boolean setTime(final int time) {
//		if (time > 0 && !delayed && entities.getSource() instanceof ExprAttacked) {
//			Skript.warning("The future state of 'health of victim' likely returns an invalid value. If you're interested in the actual health you should add a delay of 1 tick though the entity might be dead by then.");
//		}
//		return entities.getSource() instanceof ExprAttacked && super.setTime(time, EntityDamageEvent.class);
//	}
}
