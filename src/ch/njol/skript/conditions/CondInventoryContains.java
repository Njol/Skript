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

package ch.njol.skript.conditions;

import org.bukkit.event.Event;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import ch.njol.skript.Skript;
import ch.njol.skript.api.Condition;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.skript.util.ItemType;
import ch.njol.util.Checker;

/**
 * 
 * @author Peter Güttinger
 * 
 */
public class CondInventoryContains extends Condition {
	
	static {
		Skript.registerCondition(CondInventoryContains.class,
				"[%inventories%] ha(s|ve) %itemtypes% [in inventory]",
				"[%inventories%] contain[s] %itemtypes%",
				"[%inventories%] (ha(s|ve) not|do[es]n't have) %itemtypes% [in inventory]",
				"[%inventories%] do[es](n't| not) contain %itemtypes%");
	}
	
	private Expression<Inventory> invis;
	private Expression<ItemType> items;
	
	@SuppressWarnings("unchecked")
	@Override
	public boolean init(final Expression<?>[] vars, final int matchedPattern, final ParseResult parser) {
		invis = (Expression<Inventory>) vars[0];
		items = (Expression<ItemType>) vars[1];
		setNegated(matchedPattern >= 2);
		return true;
	}
	
	@Override
	public boolean check(final Event e) {
		return invis.check(e, new Checker<Inventory>() {
			@Override
			public boolean check(final Inventory invi) {
				final ItemStack[] buf = invi.getContents();
				return items.check(e, new Checker<ItemType>() {
					@Override
					public boolean check(final ItemType type) {
						return type.isContainedIn(buf);
					}
				});
			}
		}, this);
	}
	
	@Override
	public String getDebugMessage(final Event e) {
		return invis.getDebugMessage(e) + (isNegated() ? " doesn't have " : " has ") + items.getDebugMessage(e);
	}
	
}
