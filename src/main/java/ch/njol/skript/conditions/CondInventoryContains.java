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
import ch.njol.skript.lang.Condition;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.skript.util.ItemType;
import ch.njol.util.Checker;

/**
 * 
 * @author Peter Güttinger
 */
public class CondInventoryContains extends Condition {
	
	private static final long serialVersionUID = -5748739007049607102L;
	
	static {
		Skript.registerCondition(CondInventoryContains.class,
				"%inventories% ha(s|ve) %itemtypes% [in [(the[ir]|his|her|its)] inventory]",
				"%inventories/strings% contain[s] %itemtypes/strings%",
				"%inventories% do[es](n't| not) have %itemtypes% [in [(the[ir]|his|her|its)] inventory]",
				"%inventories/strings% do[es](n't| not) contain %itemtypes/strings%");
	}
	
	private Expression<?> invis;
	private Expression<?> items;
	
	@Override
	public boolean init(final Expression<?>[] exprs, final int matchedPattern, final int isDelayed, final ParseResult parser) {
		invis = exprs[0];
		items = exprs[1];
		setNegated(matchedPattern >= 2);
		return true;
	}
	
	@Override
	public boolean check(final Event e) {
		return invis.check(e, new Checker<Object>() {
			@Override
			public boolean check(final Object invi) {
				if (invi instanceof Inventory) {
					final ItemStack[] buf = ((Inventory) invi).getContents();
					return items.check(e, new Checker<Object>() {
						@Override
						public boolean check(final Object type) {
							return type instanceof ItemType && ((ItemType) type).isContainedIn(buf);
						}
					});
				} else {
					final String s = ((String) invi).toLowerCase();
					return items.check(e, new Checker<Object>() {
						@Override
						public boolean check(final Object type) {
							return type instanceof String && s.contains(((String) type).toLowerCase());
						}
					});
				}
			}
		}, this);
	}
	
	@Override
	public String toString(final Event e, final boolean debug) {
		return invis.toString(e, debug) + (isNegated() ? " doesn't have " : " has ") + items.toString(e, debug);
	}
	
}
