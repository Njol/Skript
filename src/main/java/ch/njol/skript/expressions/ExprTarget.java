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

import java.util.ArrayList;

import org.bukkit.entity.Creature;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.event.Event;

import ch.njol.skript.Skript;
import ch.njol.skript.Skript.ExpressionType;
import ch.njol.skript.classes.Changer.ChangeMode;
import ch.njol.skript.entity.EntityData;
import ch.njol.skript.expressions.base.PropertyExpression;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.skript.util.Utils;

/**
 * @author Peter Güttinger
 * 
 */
public class ExprTarget extends PropertyExpression<LivingEntity, Entity> {
	
	static {
		Skript.registerExpression(ExprTarget.class, Entity.class, ExpressionType.NORMAL, "[the] target[[ed] %entitydatas%] [of %livingentities%]", "%livingentities%'[s] target[[ed] %entitydatas%]");
	}
	
	private Expression<EntityData<?>> types;
	private Expression<LivingEntity> entities;
	
	@SuppressWarnings("unchecked")
	@Override
	public boolean init(final Expression<?>[] vars, final int matchedPattern, final boolean isDelayed, final ParseResult parser) {
		types = (Expression<EntityData<?>>) vars[matchedPattern];
		entities = (Expression<LivingEntity>) vars[1 - matchedPattern];
		setExpr(entities);
		return true;
	}
	
	@Override
	protected Entity[] get(final Event evt, final LivingEntity[] source) {
		final ArrayList<Entity> targets = new ArrayList<Entity>();
		final EntityData<?>[] types = this.types.getAll(evt);
		for (final LivingEntity e : source) {
			for (final EntityData<?> type : types) {
				final Entity t = Utils.getTargetEntity(e, type.getType());
				if (t != null)
					targets.add(t);
			}
		}
		return targets.toArray(new Entity[0]);
	}
	
	@Override
	public Class<Entity> getReturnType() {
		return Entity.class;
	}
	
	@Override
	public String toString(final Event e, final boolean debug) {
		if (e == null)
			return "the targeted " + types.toString(e, debug) + " of " + entities.toString(e, debug);
		return Skript.getDebugMessage(getAll(e));
	}
	
	@Override
	public Class<?> acceptChange(final ChangeMode mode) {
		if (mode == ChangeMode.CLEAR || mode == ChangeMode.SET)
			return LivingEntity.class;
		return null;
	}
	
	@Override
	public void change(final Event e, final Object delta, final ChangeMode mode) {
		final LivingEntity target = (LivingEntity) delta;
		for (final LivingEntity entity : entities.getArray(e)) {
			if (!(entity instanceof Creature))
				continue;
			((Creature) entity).setTarget(target);
		}
	}
	
}
