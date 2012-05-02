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

package ch.njol.skript.variables;

import java.util.ArrayList;
import java.util.regex.Matcher;

import org.bukkit.entity.Creature;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.event.Event;

import ch.njol.skript.Skript;
import ch.njol.skript.api.Changer.ChangeMode;
import ch.njol.skript.api.intern.Variable;
import ch.njol.skript.util.EntityType;
import ch.njol.skript.util.Utils;

/**
 * @author Peter Güttinger
 * 
 */
public class VarTarget extends Variable<Entity> {
	
	static {
		Skript.addVariable(VarTarget.class, Entity.class, "target(ed %entitytype%)( of %livingentity%)?");
	}
	
	private Variable<EntityType> types;
	private Variable<LivingEntity> entities;
	
	@SuppressWarnings("unchecked")
	@Override
	public void init(final Variable<?>[] vars, final int matchedPattern, final Matcher matcher) {
		types = (Variable<EntityType>) vars[0];
		entities = (Variable<LivingEntity>) vars[1];
	}
	
	@Override
	protected Entity[] getAll(final Event evt) {
		final ArrayList<Entity> targets = new ArrayList<Entity>();
		final Iterable<EntityType> types = this.types.get(evt, false);
		for (final LivingEntity e : entities.get(evt, false)) {
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
		return Skript.toString(getAll(e));
	}
	
	@Override
	public Class<?> acceptChange(final ChangeMode mode) {
		if (mode == ChangeMode.CLEAR || mode == ChangeMode.SET)
			return LivingEntity.class;
		return null;
	}
	
	@Override
	public void change(final Event e, final Variable<?> delta, final ChangeMode mode) {
		final LivingEntity target = (LivingEntity) delta.getFirst(e);
		for (final LivingEntity entity : entities.get(e, false)) {
			if (!(entity instanceof Creature))
				continue;
			((Creature) entity).setTarget(target);
		}
	}
	
	@Override
	public String toString() {
		return "the targeted " + types + " of " + entities;
	}
	
}
