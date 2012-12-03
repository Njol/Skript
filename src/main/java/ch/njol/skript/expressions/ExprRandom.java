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

package ch.njol.skript.expressions;

import java.lang.reflect.Array;

import org.bukkit.event.Event;

import ch.njol.skript.Skript;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.ExpressionType;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.skript.lang.util.SimpleExpression;
import ch.njol.skript.registrations.Classes;
import ch.njol.skript.util.Utils;
import ch.njol.util.Kleenean;

/**
 * @author Peter Güttinger
 */
public class ExprRandom extends SimpleExpression<Object> {
	private static final long serialVersionUID = -4549744102145885982L;
	
	static {
		Skript.registerExpression(ExprRandom.class, Object.class, ExpressionType.COMBINED, "[a] random <.+> [out] of %objects%");
	}
	
	private Expression<?> expr;
	
	@Override
	public boolean init(final Expression<?>[] exprs, final int matchedPattern, final Kleenean isDelayed, final ParseResult parseResult) {
		Class<?> c = Classes.getClassFromUserInput(parseResult.regexes.get(0).group());
		if (c == null)
			c = Classes.getClassByName(parseResult.regexes.get(0).group());
		if (c == null)
			return false;
		expr = exprs[0];
		if (!c.isAssignableFrom(expr.getReturnType()))
			return false;
		return true;
	}
	
	@Override
	protected Object[] get(final Event e) {
		final Object[] set = expr.getAll(e);
		if (set.length <= 1)
			return set;
		final Object[] one = (Object[]) Array.newInstance(set.getClass().getComponentType(), 1);
		one[0] = Utils.random(set);
		return one;
	}
	
	@Override
	public Class<? extends Object> getReturnType() {
		return expr.getReturnType();
	}
	
	@Override
	public String toString(final Event e, final boolean debug) {
		return "a random element out of " + expr.toString(e, debug);
	}
	
	@Override
	public boolean isSingle() {
		return true;
	}
	
	@Override
	public boolean getAnd() {
		return false;
	}
	
}
