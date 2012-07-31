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
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.event.Event;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.vehicle.VehicleDamageEvent;
import org.bukkit.event.vehicle.VehicleDestroyEvent;
import org.bukkit.inventory.Inventory;

import ch.njol.skript.Skript;
import ch.njol.skript.Skript.ExpressionType;
import ch.njol.skript.api.Changer.ChangeMode;
import ch.njol.skript.classes.DefaultChangers;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.SimpleExpression;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.skript.util.ItemType;

/**
 * 
 * @author Peter Güttinger
 * 
 */
public class ExprAttacker extends SimpleExpression<Entity> {
	
	static {
		Skript.registerExpression(ExprAttacker.class, Entity.class, ExpressionType.SIMPLE, "[the] (attacker|damager)");
	}
	
	@Override
	public boolean init(final Expression<?>[] vars, final int matchedPattern, final ParseResult parser) {
		return true;
	}
	
	@Override
	protected Entity[] get(final Event e) {
		return new Entity[] {getAttacker(e)};
	}
	
	private static Entity getAttacker(final Event e) {
		if (e instanceof EntityDamageByEntityEvent) {
			if (((EntityDamageByEntityEvent) e).getDamager() instanceof Projectile) {
				return ((Projectile) ((EntityDamageByEntityEvent) e).getDamager()).getShooter();
			}
			return ((EntityDamageByEntityEvent) e).getDamager();
//		} else if (e instanceof EntityDamageByBlockEvent) {
//			return ((EntityDamageByBlockEvent) e).getDamager();
		} else if (e instanceof EntityDeathEvent) {
			return getAttacker(((EntityDeathEvent) e).getEntity().getLastDamageCause());
		} else if (e instanceof VehicleDamageEvent) {
			return ((VehicleDamageEvent) e).getAttacker();
		} else if (e instanceof VehicleDestroyEvent) {
			return ((VehicleDestroyEvent) e).getAttacker();
		}
		return null;
	}
	
	@Override
	public Class<? extends Entity> getReturnType() {
		return Entity.class;
	}
	
	@Override
	public String toString(final Event e, final boolean debug) {
		if (e == null)
			return "the attacker";
		return Skript.getDebugMessage(getSingle(e));
	}
	
	@Override
	public boolean isSingle() {
		return true;
	}
	
	@Override
	public boolean getAnd() {
		return true;
	}
	
	@Override
	public Class<?> acceptChange(final ChangeMode mode) {
		return DefaultChangers.inventoryChanger.acceptChange(mode);
	}
	
	@Override
	public void change(final Event e, final Object delta, final ChangeMode mode) throws UnsupportedOperationException {
		final Entity a = getAttacker(e);
		if (a == null || !(a instanceof Player))
			return;
		DefaultChangers.inventoryChanger.change(new Inventory[] {((Player) a).getInventory()}, (ItemType[]) delta, mode);
	}
	
}
