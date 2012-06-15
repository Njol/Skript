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
import ch.njol.skript.api.Changer.ChangeMode;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.SimpleExpression;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.skript.util.EntityType;
import ch.njol.skript.util.Utils;

/**
 * @author Peter Güttinger
 * 
 */
public class ExprTarget extends SimpleExpression<Entity> {
	
	static {
		Skript.registerExpression(ExprTarget.class, Entity.class, "[the] target[[ed] %entitytypes%] [of %livingentities%]", "%livingentities%'[s] target[[ed] %entitytypes%]");
	}
	
	private Expression<EntityType> types;
	private Expression<LivingEntity> entities;
	
	@SuppressWarnings("unchecked")
	@Override
	public boolean init(final Expression<?>[] vars, final int matchedPattern, final ParseResult parser) {
		types = (Expression<EntityType>) vars[matchedPattern];
		entities = (Expression<LivingEntity>) vars[1 - matchedPattern];
		return true;
	}
	
	@Override
	protected Entity[] getAll(final Event evt) {
		final ArrayList<Entity> targets = new ArrayList<Entity>();
		final EntityType[] types = this.types.getArray(evt);
		for (final LivingEntity e : entities.getArray(evt)) {
			for (final EntityType type : types) {
				final Entity t = Utils.getTargetEntity(e, type.c);
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
	public String getDebugMessage(final Event e) {
		if (e == null)
			return "targeted " + types.getDebugMessage(e) + " of " + entities.getDebugMessage(e);
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
	
	@Override
	public String toString() {
		return "the targeted " + types + " of " + entities;
	}
	
	@Override
	public boolean isSingle() {
		return entities.isSingle();
	}
	
}
