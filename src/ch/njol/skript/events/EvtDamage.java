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

package ch.njol.skript.events;

import org.bukkit.entity.EnderDragon;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.entity.EntityDamageByBlockEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;

import ch.njol.skript.Skript;
import ch.njol.skript.api.SkriptEvent;
import ch.njol.skript.lang.Literal;
import ch.njol.skript.lang.SkriptParser.ParseResult;

/**
 * @author Peter Güttinger
 * 
 */
@SuppressWarnings("unchecked")
public class EvtDamage extends SkriptEvent {
	
	static {
		Skript.registerEvent(EvtDamage.class, Skript.array(EntityDamageEvent.class, EntityDamageByBlockEvent.class, EntityDamageByEntityEvent.class), "damag(e|ing)");
	}
	
	@Override
	public boolean init(final Literal<?>[] args, final int matchedPattern, final ParseResult parser) {
		return true;
	}
	
	@Override
	public boolean check(final Event evt) {
		final EntityDamageEvent e = (EntityDamageEvent) evt;
		return /*e.getDamage() != 0 && */!(e.getEntity() instanceof Player && ((LivingEntity) e.getEntity()).getNoDamageTicks() > 0) // only players can be invulnerable
				&& !(e instanceof EntityDamageByEntityEvent && ((EntityDamageByEntityEvent) e).getDamager() instanceof EnderDragon && ((EntityDamageByEntityEvent) e).getEntity() instanceof EnderDragon);
	}
	
	@Override
	public String toString(final Event e, final boolean debug) {
		return "damage";
	}
	
}
