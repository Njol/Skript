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

import java.util.regex.Matcher;

import org.bukkit.entity.LivingEntity;
import org.bukkit.event.Event;

import ch.njol.skript.Skript;
import ch.njol.skript.api.Effect;
import ch.njol.skript.api.intern.Variable;

/**
 * @author Peter Güttinger
 * 
 */
public class EffKill extends Effect {
	
	static {
		Skript.addEffect(EffKill.class, "kill %livingentity%");
	}
	
	private Variable<LivingEntity> entities;
	
	@SuppressWarnings("unchecked")
	@Override
	public void init(final Variable<?>[] vars, final int matchedPattern, final Matcher matcher) {
		entities = (Variable<LivingEntity>) vars[0];
	}
	
	@Override
	protected void execute(final Event e) {
		for (final LivingEntity entity : entities.get(e, false)) {
			entity.setHealth(0);
		}
	}
	
	@Override
	public String getDebugMessage(final Event e) {
		return "kill " + entities.getDebugMessage(e);
	}
	
}
