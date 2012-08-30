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
import ch.njol.skript.Skript.ExpressionType;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.skript.lang.util.SimpleExpression;
import ch.njol.skript.util.Utils;

/**
 * @author Peter Güttinger
 */
public class ExprRandom extends SimpleExpression<Object> {
	
	static {
		Skript.registerExpression(ExprRandom.class, Object.class, ExpressionType.COMBINED, "[a[n]] random <.+> [out] of %objects%");
	}
	
	private Expression<?> expr;
	
	@Override
	public boolean init(final Expression<?>[] exprs, final int matchedPattern, final int isDelayed, final ParseResult parseResult) {
		Class<?> c = Skript.getClassFromUserInput(parseResult.regexes.get(0).group());
		if (c == null)
			c = Skript.getClassByName(parseResult.regexes.get(0).group());
		if (c == null)
			return false;
		expr = exprs[0];
		return c.isAssignableFrom(expr.getReturnType());
	}
	
	@Override
	protected Object[] get(final Event e) {
		final Object[] set = expr.getAll(e);
		if (set.length <= 1)
			return set;
		final Object[] one = (Object[]) Array.newInstance(set.getClass().getComponentType(), 1);
		one[0] = Utils.getRandom(set);
		return one;
	}
	
	@Override
	public Class<? extends Object> getReturnType() {
		return expr.getReturnType();
	}
	
	@Override
	public String toString(final Event e, final boolean debug) {
		// TODO Auto-generated method stub
		return null;
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
