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

import java.util.regex.Matcher;

import org.bukkit.entity.LivingEntity;
import org.bukkit.event.Event;
import org.bukkit.inventory.ItemStack;

import ch.njol.skript.Skript;
import ch.njol.skript.api.Effect;
import ch.njol.skript.api.intern.Variable;
import ch.njol.skript.util.Slot;

/**
 * 
 * @author Peter Güttinger
 * 
 */
public class EffHealth extends Effect {
	
	static {
		Skript.addEffect(EffHealth.class,
				"damage %object% by %-integer%",
				"heal %livingentity%( by %-integer%)?",
				"repair %slot%( by %-integer%)?");
	}
	
	private Variable<Object> damageables;
	private Variable<Integer> damages;
	private boolean heal = false;
	
	@SuppressWarnings("unchecked")
	@Override
	public void init(final Variable<?>[] vars, final int matchedPattern, final Matcher matcher) {
		damageables = (Variable<Object>) vars[0];
		damages = (Variable<Integer>) vars[1];
		heal = (matchedPattern != 0);
	}
	
	@Override
	public void execute(final Event e) {
		for (final Object damageable : damageables.get(e, false)) {
			if (damageable instanceof Slot) {
				final ItemStack is = ((Slot) damageable).getItem();
				if (damages == null) {
					is.setDurability((short) 0);
				} else {
					for (final Integer damage : damages.get(e, false))
						is.setDurability((short) (is.getDurability() + (heal ? -damage.shortValue() : damage.shortValue())));
				}
				((Slot) damageable).setItem(is);
			} else if (damageable instanceof LivingEntity) {
				if (damages == null) {
					((LivingEntity) damageable).setHealth(((LivingEntity) damageable).getMaxHealth());
				} else {
					for (final Integer damage : damages.get(e, false))
						((LivingEntity) damageable).setHealth(((LivingEntity) damageable).getHealth() + (heal ? damage : -damage));
				}
			}
		}
	}
	
	@Override
	public String getDebugMessage(final Event e) {
		return (heal ? "heal " : "damage ") + damageables.getDebugMessage(e) + (damages == null ? "" : " by " + damages.getDebugMessage(e));
	}
	
}
