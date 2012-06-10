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

package ch.njol.skript.events;

import org.bukkit.entity.Entity;
import org.bukkit.event.Event;
import org.bukkit.event.entity.CreatureSpawnEvent;
import org.bukkit.event.entity.EntityDeathEvent;

import ch.njol.skript.Skript;
import ch.njol.skript.api.SkriptEvent;
import ch.njol.skript.lang.ExprParser.ParseResult;
import ch.njol.skript.lang.Literal;
import ch.njol.skript.util.EntityType;
import ch.njol.util.Checker;

/**
 * @author Peter Güttinger
 * 
 */
public class EvtEntity extends SkriptEvent {
	
	static {
		Skript.registerEvent(EvtEntity.class, EntityDeathEvent.class, "death [of %entitytypes%]");
		Skript.registerEvent(EvtEntity.class, CreatureSpawnEvent.class, "spawn[ing] [of %entitytypes%]");
	}
	
	Literal<EntityType> types;
	
	@SuppressWarnings("unchecked")
	@Override
	public boolean init(final Literal<?>[] args, final int matchedPattern, final ParseResult parser) {
		types = (Literal<EntityType>) args[0];
		return true;
	}
	
	@Override
	public boolean check(final Event e) {
		if (types == null)
			return true;
		final Entity en = Skript.getEventValue(e, Entity.class, 0);
		if (en == null)
			throw new RuntimeException("no entity event value for entity death/spawn");
		return types.check(e, new Checker<EntityType>() {
			@Override
			public boolean check(final EntityType t) {
				return t.isInstance(en);
			}
		});
	}
	
	@Override
	public String getDebugMessage(final Event e) {
		return "death/spawn" + (types == null ? "" : " of " + types);
	}
	
}
