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

import java.util.regex.Matcher;

import org.bukkit.entity.Entity;
import org.bukkit.event.Event;
import org.bukkit.event.entity.CreatureSpawnEvent;
import org.bukkit.event.entity.EntityDeathEvent;

import ch.njol.skript.Skript;
import ch.njol.skript.api.SkriptEvent;
import ch.njol.skript.util.EntityType;

/**
 * @author Peter Güttinger
 * 
 */
public class EvtEntity extends SkriptEvent {
	
	static {
		Skript.addEvent(EvtEntity.class, EntityDeathEvent.class, "death( of %entitytype%)?");
		Skript.addEvent(EvtEntity.class, CreatureSpawnEvent.class, "spawn(ing)?( of %entitytype%)");
	}
	
	EntityType[] types;
	
	@Override
	public void init(final Object[][] args, final int matchedPattern, final Matcher matcher) {
		types = (EntityType[]) args[0];
	}
	
	@Override
	public boolean check(final Event e) {
		if (types == null)
			return true;
		final Entity en = Skript.getEventValue(e, Entity.class);
		for (final EntityType type : types) {
			if (type.isInstance(en))
				return true;
		}
		return false;
	}
	
	@Override
	public String getDebugMessage(final Event e) {
		return "death/spawn of " + Skript.toString(types);
	}
	
}
