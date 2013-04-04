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
import org.bukkit.event.Event;

import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Examples;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import ch.njol.skript.expressions.base.SimplePropertyExpression;

/**
 * @author Peter Güttinger
 */
@SuppressWarnings("serial")
@Name("Vehicle")
@Description({"The vehicle an entity is in, if any. This can actually be any entity, e.g. spider jockeys are skeletons that ride on a spider, so the spider is the 'vehicle' of the skeleton.",
		"See also: <a href='#ExprPassenger'>passenger</a>"})
@Examples({"vehicle of the player is a minecart"})
@Since("2.0")
public class ExprVehicle extends SimplePropertyExpression<Entity, Entity> {
	static {
		register(ExprVehicle.class, Entity.class, "vehicle[s]", "entities");
	}
	
	@Override
	protected Entity[] get(final Event e, final Entity[] source) {
		// TODO find out when entity.getVehicle() is changed
//		if (getTime() == -1 && e instanceof VehicleEnterEvent && !Delay.isDelayed(e)) {
//			((VehicleEnterEvent) e).
//		}
//		if (getTime() == -1 && e instanceof VehicleExitEvent && !Delay.isDelayed(e)) {
//			((VehicleExitEvent) e).
//		}
		return super.get(e, source);
	}
	
	@Override
	public Entity convert(final Entity e) {
		return e.getVehicle();
	}
	
	@Override
	public Class<? extends Entity> getReturnType() {
		return Entity.class;
	}
	
	@Override
	protected String getPropertyName() {
		return "vehicle";
	}
	
//	@SuppressWarnings("unchecked")
//	@Override
//	public boolean setTime(int time) {
//		return super.setTime(time, getExpr(), VehicleEnterEvent.class, VehicleExitEvent.class);
//	}
	
}
