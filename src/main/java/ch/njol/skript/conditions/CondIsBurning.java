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

package ch.njol.skript.conditions;

import org.bukkit.entity.Entity;

import ch.njol.skript.conditions.base.PropertyCondition;

/**
 * @author Peter Güttinger
 */
public class CondIsBurning extends PropertyCondition<Entity> {
	
	private static final long serialVersionUID = 696041723474529334L;
	
	static {
		register(CondIsBurning.class, "(burning|ignited|on fire)", "entities");
	}
	
	@Override
	public boolean check(final Entity e) {
		return e.getFireTicks() > 0;
	}
	
	@Override
	protected String getPropertyName() {
		return "burning";
	}
	
}
