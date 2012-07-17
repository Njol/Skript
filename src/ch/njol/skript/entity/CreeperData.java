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

package ch.njol.skript.entity;

import org.bukkit.entity.Creeper;

import ch.njol.skript.lang.Literal;
import ch.njol.skript.lang.SkriptParser.ParseResult;

/**
 * @author Peter Güttinger
 * 
 */
public class CreeperData extends EntityData<Creeper> {
	
	static {
		EntityData.register(CreeperData.class, "creeper", Creeper.class, "unpowered creeper[s]", "creeper[s]", "powered creeper[s]");
	}
	
	private int powered = 0;
	
	@Override
	protected boolean init(final Literal<?>[] exprs, final int matchedPattern, final ParseResult parseResult) {
		powered = matchedPattern - 1;
		return true;
	}
	
	@Override
	public void set(final Creeper c) {
		if (powered != 0)
			c.setPowered(powered == 1);
	}
	
	@Override
	public boolean match(final Creeper entity) {
		return powered == 0 || entity.isPowered() == (powered == 1);
	}
	
	@Override
	public Class<Creeper> getType() {
		return Creeper.class;
	}
	
	@Override
	public String toString() {
		return (powered == 1 ? "powered " : powered == -1 ? "unpowered " : "") + "creeper";
	}
	
}
