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

import ch.njol.skript.Skript;
import ch.njol.skript.lang.Condition;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.skript.registrations.Comparators;
import ch.njol.util.Kleenean;

/**
 * @author Peter Güttinger
 * 
 */
public class CondContains extends Condition {
	
	static {
		Skript.registerCondition(CondContains.class,
				"%objects% contain[s] %objects%",
				"%objects% do[es](n't| not) contain %objects%");
	}
	
	private Expression<?> container;
	private Expression<?> contained;
	
	@Override
	public boolean init(final Expression<?>[] exprs, final int matchedPattern, final Kleenean isDelayed, final ParseResult parseResult) {
		container = exprs[0].getConvertedExpression(Object.class);
		if (container == null)
			return false;
		contained = exprs[1].getConvertedExpression(Object.class);
		if (contained == null)
			return false;
		if (contained.getReturnType() != Object.class && container.getReturnType() != Object.class && Comparators.getComparator(contained.getReturnType(), container.getReturnType()) == null)
			return false;
		setNegated(matchedPattern == 1);
		return true;
	}
	
	@Override
	public boolean check(final Event e) {
		// TODO Auto-generated method stub
		return false;
	}
	
	@Override
	public String toString(final Event e, final boolean debug) {
		return container + " " + (isNegated() ? "does not contain" : "contain") + " " + contained;
	}
	
}
