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

package ch.njol.skript.util.entity;

import org.bukkit.entity.Creeper;

import ch.njol.skript.lang.ExprParser.ParseResult;
import ch.njol.skript.util.EntityType;

/**
 * @author Peter Güttinger
 *
 */
public class CreeperData implements EntityData<Creeper> {

	private boolean powered = false;
	
	@Override
	public void set(Creeper c) {
		c.setPowered(powered);
	}

	@Override
	public boolean parse(String s) {
		if (s.equalsIgnoreCase("creeper")) {
			return true;
		} else if (s.equalsIgnoreCase("powered creeper")) {
			powered = true;
			return true;
		}
		return false;
	}
	// or
	static {
		EntityType.registerType(Creeper.class, CreeperData.class, "powered creeper", "creeper");
	}
	@Override
	public boolean init(int matchedPattern, ParseResult res) {
		powered = matchedPattern == 0;
	}

	@Override
	public String toString() {
		return (powered ? "powered " : "")+"creeper";
	}
	
	@Override
	public String getDebugMessage() {
		return toString();
	}
	
}
