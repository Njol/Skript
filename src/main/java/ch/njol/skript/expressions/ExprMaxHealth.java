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

import org.bukkit.entity.LivingEntity;
import org.bukkit.event.Event;

import ch.njol.skript.Skript;
import ch.njol.skript.classes.Changer.ChangeMode;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Examples;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import ch.njol.skript.expressions.base.SimplePropertyExpression;
import ch.njol.skript.util.HealthUtils;

/**
 * @author Peter Güttinger
 */
@SuppressWarnings("serial")
@Name("Max Health")
@Description("The maximum health of an entity, e.g. 10 for a player")
@Examples({"on join:",
		"	set the maximum health of the player to 100",
		"spawn a giant",
		"set the last spawned entity's max health to 1000"})
@Since("2.0")
public class ExprMaxHealth extends SimplePropertyExpression<LivingEntity, Double> {
	static {
		register(ExprMaxHealth.class, Double.class, "max[imum] health", "livingentities");
	}
	
	@Override
	public Double convert(final LivingEntity e) {
		return HealthUtils.getMaxHealth(e);
	}
	
	@Override
	public Class<? extends Double> getReturnType() {
		return Double.class;
	}
	
	@Override
	protected String getPropertyName() {
		return "max health";
	}
	
	@Override
	public Class<?>[] acceptChange(final ChangeMode mode) {
		if (!Skript.isRunningMinecraft(1, 6)) {
			Skript.error("The max health of an entity can only be changed in Minecraft 1.6 and later");
			return null;
		}
		if (mode != ChangeMode.DELETE && mode != ChangeMode.REMOVE_ALL)
			return new Class[] {Number.class};
		return null;
	}
	
	@Override
	public void change(final Event e, final Object delta, final ChangeMode mode) {
		double d = delta == null ? 0 : ((Number) delta).doubleValue();
		for (final LivingEntity en : getExpr().getArray(e)) {
			switch (mode) {
				case SET:
					en.setMaxHealth(2 * Math.max(0, d));
					break;
				case REMOVE:
					d = -d;
					//$FALL-THROUGH$
				case ADD:
					en.setMaxHealth(Math.max(0, en.getMaxHealth() + 2 * d));
					break;
				case RESET:
					en.resetMaxHealth();
					break;
				case DELETE:
				case REMOVE_ALL:
					assert false;
					
			}
		}
	}
	
}
