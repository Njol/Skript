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

import org.bukkit.entity.Entity;
import org.bukkit.event.Event;
import org.bukkit.inventory.ItemStack;

import ch.njol.skript.Skript;
import ch.njol.skript.entity.EntityData;
import ch.njol.skript.lang.Condition;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.skript.util.ItemType;
import ch.njol.util.Checker;

/**
 * @author Peter Güttinger
 */
public class CondIsOfType extends Condition {
	
	static {
		Skript.registerCondition(CondIsOfType.class,
				"%itemstacks% (is|are) of type[s] %itemtypes%", "%itemstacks% (isn't|is not|aren't|are not) of type[s] %itemtypes%",
				"%entities% (is|are) of type[s] %entitydatas%", "%entities% (isn't|is not|aren't|are not) of type[s] %entitydatas%");
	}
	
	private Expression<?> what, types;
	private boolean item;
	
	@Override
	public boolean init(final Expression<?>[] exprs, final int matchedPattern, final int isDelayed, final ParseResult parseResult) {
		what = exprs[0];
		types = exprs[1];
		item = matchedPattern <= 1;
		setNegated(matchedPattern % 2 == 1);
		return true;
	}
	
	@Override
	public boolean check(final Event e) {
		return what.check(e, new Checker<Object>() {
			@Override
			public boolean check(final Object o1) {
				return types.check(e, new Checker<Object>() {
					@Override
					public boolean check(final Object o2) {
						if (item) {
							return ((ItemType) o2).isOfType((ItemStack) o1);
						} else {
							return ((EntityData<?>) o2).isInstance((Entity) o1);
						}
					}
				});
			}
		}, this);
	}
	
	@Override
	public String toString(final Event e, final boolean debug) {
		return what.toString(e, debug) + (what.isSingle() ? " is " : " are ") + (isNegated() ? "not " : "") + "of type " + types.toString(e, debug);
	}
	
}
