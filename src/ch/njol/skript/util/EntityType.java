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

package ch.njol.skript.util;

import org.bukkit.entity.Entity;

import ch.njol.util.Validate;

/**
 * 
 * @author Peter Güttinger
 * 
 */
public class EntityType {
	public Class<? extends Entity> c;
	// TODO: powered creepers, angry wolves, etc.
	// creepers: bool
	// wolves: bool (+String (owner))?
	public int amount = 1;
	
	public EntityType(final Class<? extends Entity> c, final int amount) {
		Validate.notNull(c);
		this.c = c;
		this.amount = amount;
	}
	
	public boolean isInstance(final Entity entity) {
		return c.isInstance(entity);
	}
	
	@Override
	public String toString() {
		return amount + " " + c.getSimpleName();
	}
	
	public boolean sameType(final EntityType other) {
		if (other == null)
			return false;
		return c == other.c;
	}
	
	@Override
	public int hashCode() {
		return amount * c.hashCode();
	}
	
	@Override
	public boolean equals(final Object obj) {
		if (obj == this)
			return true;
		if (!(obj instanceof EntityType))
			return false;
		final EntityType other = (EntityType) obj;
		return (amount == other.amount && c == other.c);
	}
	
}
