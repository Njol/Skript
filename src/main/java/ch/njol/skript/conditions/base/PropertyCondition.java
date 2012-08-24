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

package ch.njol.skript.conditions.base;

import org.bukkit.event.Event;

import ch.njol.skript.Skript;
import ch.njol.skript.lang.Condition;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.util.Checker;

/**
 * @author Peter Güttinger
 * 
 */
public abstract class PropertyCondition<T> extends Condition {
	
	private final Checker<? super T> checker;
	private Expression<? extends T> expr;
	private final String property;
	
	public PropertyCondition(final String property, final Checker<? super T> checker) {
		this.property = property;
		this.checker = checker;
	}
	
	/**
	 * @param c
	 * @param property
	 * @param type must be plural
	 */
	protected static void register(final Class<? extends PropertyCondition<?>> c, final String property, final String type) {
		Skript.registerCondition(c, "%" + type + "% (is|are) " + property, "%" + type + "% (isn't|is not|aren't|are not) " + property);
	}
	
	@SuppressWarnings("unchecked")
	@Override
	public boolean init(final Expression<?>[] exprs, final int matchedPattern, final int isDelayed, final ParseResult parseResult) {
		expr = (Expression<? extends T>) exprs[0];
		setNegated(matchedPattern == 1);
		return true;
	}
	
	@Override
	public boolean check(final Event e) {
		return expr.check(e, checker, this);
	}
	
	@Override
	public String toString(final Event e, final boolean debug) {
		return expr.toString(e, debug) + (expr.isSingle() ? " is " : " are ") + (isNegated() ? "not " : "") + property;
	}
	
}
