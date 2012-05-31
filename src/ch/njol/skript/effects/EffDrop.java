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

import org.bukkit.Location;
import org.bukkit.event.Event;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.inventory.ItemStack;

import ch.njol.skript.Skript;
import ch.njol.skript.api.Effect;
import ch.njol.skript.lang.ExprParser.ParseResult;
import ch.njol.skript.lang.Variable;
import ch.njol.skript.util.ItemType;
import ch.njol.skript.util.Offset;

/**
 * 
 * @author Peter Güttinger
 * 
 */
public class EffDrop extends Effect {
	
	static {
		Skript.registerEffect(EffDrop.class, "drop %itemtypes% [%offsets% %-locations%]");
	}
	
	private Variable<ItemType> items;
	private Variable<Offset> offsets;
	private Variable<Location> locations;
	private boolean hasLoc = false;
	
	@SuppressWarnings("unchecked")
	@Override
	public void init(final Variable<?>[] vars, final int matchedPattern, final ParseResult parser) {
		items = (Variable<ItemType>) vars[0];
		offsets = (Variable<Offset>) vars[1];
		locations = (Variable<Location>) vars[2];
		hasLoc = locations != null;
		if (!hasLoc)
			locations = Skript.getDefaultVariable(Location.class);
	}
	
	@Override
	public void execute(final Event e) {
		if (!hasLoc && e instanceof EntityDeathEvent) {
			for (final ItemType type : items.getArray(e)) {
				type.addTo(((EntityDeathEvent) e).getDrops());
			}
			return;
		}
		for (final Location l : Offset.setOff(offsets.getArray(e), locations.getArray(e))) {
			for (final ItemType type : items.getArray(e)) {
				for (final ItemStack is : type.getItem().getAll())
					l.getWorld().dropItemNaturally(l, is);
			}
		}
	}
	
	@Override
	public String getDebugMessage(final Event e) {
		return "drop " + items.getDebugMessage(e) + (hasLoc ? offsets.getDebugMessage(e) + " " + locations.getDebugMessage(e) : "");
	}
	
}
