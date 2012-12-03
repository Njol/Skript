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

import org.bukkit.entity.LivingEntity;
import org.bukkit.event.Event;
import org.bukkit.inventory.ItemStack;

import ch.njol.skript.Skript;
import ch.njol.skript.lang.Effect;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.skript.util.Slot;
import ch.njol.util.Kleenean;

/**
 * 
 * @author Peter Güttinger
 */
public class EffHealth extends Effect {
	
	private static final long serialVersionUID = 6404302201372383022L;
	
	static {
		Skript.registerEffect(EffHealth.class,
				"damage %slots% by %number%",
				"damage %livingentities% by %number% [heart[s]]",
				"heal %livingentities% [by %-number% [heart[s]]]",
				"repair %slots% [by %-number%]");
	}
	
	private Expression<?> damageables;
	private Expression<Number> damage;
	private boolean heal = false;
	
	@SuppressWarnings("unchecked")
	@Override
	public boolean init(final Expression<?>[] vars, final int matchedPattern, final Kleenean isDelayed, final ParseResult parser) {
		damageables = vars[0];
		damage = (Expression<Number>) vars[1];
		heal = (matchedPattern >= 2);
		return true;
	}
	
	@Override
	public void execute(final Event e) {
		double damage = 0;
		if (this.damage != null) {
			final Number n = this.damage.getSingle(e);
			if (n == null)
				return;
			damage = n.doubleValue();
		}
		for (final Object damageable : damageables.getArray(e)) {
			if (damageable instanceof Slot) {
				ItemStack is = ((Slot) damageable).getItem();
				if (this.damage == null) {
					is.setDurability((short) 0);
				} else {
					is.setDurability((short) Math.max(0, is.getDurability() + (heal ? -damage : damage)));
					if (is.getDurability() >= is.getType().getMaxDurability())
						is = null;
				}
				((Slot) damageable).setItem(is);
			} else if (damageable instanceof LivingEntity) {
				if (this.damage == null) {
					((LivingEntity) damageable).setHealth(((LivingEntity) damageable).getMaxHealth());
				} else {
					if (!heal) {
						((LivingEntity) damageable).damage((int) Math.round(2. * damage));
					} else {
						((LivingEntity) damageable).setHealth(Math.max(0, Math.min(((LivingEntity) damageable).getMaxHealth(),
								((LivingEntity) damageable).getHealth() + (int) Math.round(2. * damage))));
					}
				}
			}
		}
	}
	
	@Override
	public String toString(final Event e, final boolean debug) {
		return (heal ? "heal " : "damage ") + damageables.toString(e, debug) + (damage == null ? "" : " by " + damage.toString(e, debug));
	}
	
}
