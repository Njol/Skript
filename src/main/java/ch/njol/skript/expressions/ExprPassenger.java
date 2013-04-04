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

import org.bukkit.entity.Entity;

import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Examples;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import ch.njol.skript.expressions.base.SimplePropertyExpression;

/**
 * @author Peter Güttinger
 */
@SuppressWarnings("serial")
@Name("Passenger")
@Description({"The passenger of a vehicle, or the rider of a mob.",
		"See also: <a href='#ExprVehicle'>vehicle</a>"})
@Examples({"passenger of the minecart is a creeper or a cow",
		"the saddled pig's passenger is a player"})
@Since("2.0")
public class ExprPassenger extends SimplePropertyExpression<Entity, Entity> {
	static {
		register(ExprPassenger.class, Entity.class, "passenger[s]", "entities");
	}
	
	@Override
	public Entity convert(final Entity e) {
		return e.getPassenger();
	}
	
	@Override
	public Class<? extends Entity> getReturnType() {
		return Entity.class;
	}
	
	@Override
	protected String getPropertyName() {
		return "passenger";
	}
	
}
