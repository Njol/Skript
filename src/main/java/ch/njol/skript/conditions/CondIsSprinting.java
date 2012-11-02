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

import org.bukkit.entity.Player;

import ch.njol.skript.conditions.base.PropertyCondition;

/**
 * @author Peter Güttinger
 */
public class CondIsSprinting extends PropertyCondition<Player> {
	
	private static final long serialVersionUID = 5613251351286394976L;
	
	static {
		register(CondIsSprinting.class, "sprinting", "players");
	}
	
	@Override
	public boolean check(final Player p) {
		return p.isSprinting();
	}
	
	@Override
	protected String getPropertyName() {
		return "sprinting";
	}
	
}
