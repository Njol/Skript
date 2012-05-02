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

import java.util.regex.Matcher;

import org.bukkit.event.Event;
import org.bukkit.inventory.Inventory;

import ch.njol.skript.Skript;
import ch.njol.skript.api.Condition;
import ch.njol.skript.api.intern.Variable;
import ch.njol.skript.util.ItemType;
import ch.njol.util.Checker;

/**
 * @author Peter Güttinger
 * 
 */
public class CondCanHold extends Condition {
	
	static {
		Skript.addCondition(CondCanHold.class,
				"(%inventory% )?(can hold|ha(s|ve) (enough )?space (for|to hold)) %itemtype%",
				"(%inventory% )?(cann['o]t hold|(ha(s|ve) not|ha(s|ve)n't|do(es)?n't have) (enough )?space (for|to hold)) %itemtype%");
	}
	
	private Variable<Inventory> invis;
	private Variable<ItemType> items;
	
	@SuppressWarnings("unchecked")
	@Override
	public void init(final Variable<?>[] vars, final int matchedPattern, final Matcher matcher) {
		invis = (Variable<Inventory>) vars[0];
		items = (Variable<ItemType>) vars[1];
		setNegated(matchedPattern == 1);
	}
	
	@Override
	public boolean run(final Event e) {
		return invis.check(e, new Checker<Inventory>() {
			@Override
			public boolean check(final Inventory i) {
				return items.check(e, new Checker<ItemType>() {
					@Override
					public boolean check(final ItemType t) {
						return t.hasSpace(i);
					}
				}, false);
			}
		}, this, false);
	}
	
	@Override
	public String getDebugMessage(final Event e) {
		return invis.getDebugMessage(e) + " can hold " + items.getDebugMessage(e);
	}
	
}
