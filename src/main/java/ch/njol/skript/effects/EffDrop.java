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
 * Copyright 2011-2013 Peter Güttinger
 * 
 */

package ch.njol.skript.effects;

import org.bukkit.Location;
import org.bukkit.entity.ExperienceOrb;
import org.bukkit.event.Event;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.inventory.ItemStack;

import ch.njol.skript.Skript;
import ch.njol.skript.aliases.ItemType;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Examples;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import ch.njol.skript.lang.Effect;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.skript.util.Direction;
import ch.njol.skript.util.Experience;
import ch.njol.util.Kleenean;

/**
 * @author Peter Güttinger
 */
@SuppressWarnings("serial")
@Name("Drop")
@Description("Drops one or more items.")
@Examples({"on death of creeper:",
		"	drop 1 TNT"})
@Since("1.0")
public class EffDrop extends Effect {
	static {
		Skript.registerEffect(EffDrop.class, "drop %itemtypes/experience% [%directions% %locations%]");
	}
	
	private Expression<?> drops = null;
	private Expression<Location> locations;
	
	@SuppressWarnings("unchecked")
	@Override
	public boolean init(final Expression<?>[] exprs, final int matchedPattern, final Kleenean isDelayed, final ParseResult parser) {
		drops = exprs[0];
		locations = Direction.combine((Expression<? extends Direction>) exprs[1], (Expression<? extends Location>) exprs[2]);
		return true;
	}
	
	@Override
	public void execute(final Event e) {
		final Object[] os = drops.getArray(e);
		if (e instanceof EntityDeathEvent && ((EntityDeathEvent) e).getEntity().getLocation().equals(locations.getSingle(e)) && !Delay.isDelayed(e)) {
			for (Object o : os) {
				if (o instanceof Experience) {
					((EntityDeathEvent) e).setDroppedExp(((EntityDeathEvent) e).getDroppedExp() + ((Experience) o).getXP());
				} else {
					((ItemType) o).addTo(((EntityDeathEvent) e).getDrops());
				}
			}
			return;
		}
		for (final Location l : locations.getArray(e)) {
			for (Object o : os) {
				if (o instanceof Experience) {
					final ExperienceOrb orb = l.getWorld().spawn(l, ExperienceOrb.class);
					orb.setExperience(((Experience) o).getXP());
				} else {
					for (final ItemStack is : ((ItemType) o).getItem().getAll()) {
						if (is.getTypeId() != 0)
							l.getWorld().dropItemNaturally(l, is);
					}
				}
			}
		}
	}
	
	@Override
	public String toString(final Event e, final boolean debug) {
		return "drop " + drops.toString(e, debug) + " " + locations.toString(e, debug);
	}
	
}
