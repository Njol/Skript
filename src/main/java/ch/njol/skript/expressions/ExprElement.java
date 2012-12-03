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
import java.util.Iterator;

import org.bukkit.event.Event;

import ch.njol.skript.Skript;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.ExpressionType;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.skript.lang.util.SimpleExpression;
import ch.njol.skript.util.Utils;
import ch.njol.util.Kleenean;

/**
 * @author Peter Güttinger
 * 
 */
public class ExprElement extends SimpleExpression<Object> {
	private static final long serialVersionUID = 611157266196537628L;
	
	static {
		Skript.registerExpression(ExprElement.class, Object.class, ExpressionType.PROPERTY, "([the] first|[the] last|[a] random) element [out] of %objects%");
	}
	
	private int element;
	
	private Expression<?> expr;
	
	@Override
	public boolean init(final Expression<?>[] exprs, final int matchedPattern, final Kleenean isDelayed, final ParseResult parseResult) {
		expr = exprs[0];
		String e = parseResult.expr.substring(0, parseResult.expr.indexOf(' '));
		if (e.equalsIgnoreCase("the") || e.equalsIgnoreCase("a"))
			e = parseResult.expr.substring(e.length() + 1, parseResult.expr.indexOf(' ', e.length()));
		element = e.equalsIgnoreCase("last") ? 1 : e.equalsIgnoreCase("first") ? -1 : 0;
		return true;
	}
	
	@Override
	protected Object[] get(final Event e) {
		final Object o;
		if (element == -1) {
			final Iterator<?> iter = expr.iterator(e);
			if (iter == null || !iter.hasNext())
				return null;
			o = iter.next();
		} else if (element == 1) {
			final Object[] os = expr.getArray(e);
			if (os.length == 0)
				return null;
			o = os[os.length - 1];
		} else {
			final Object[] os = expr.getArray(e);
			if (os.length == 0)
				return null;
			o = Utils.random(os);
		}
		final Object[] r = (Object[]) Array.newInstance(getReturnType(), 1);
		r[0] = o;
		return r;
	}
	
	@Override
	public boolean isSingle() {
		return true;
	}
	
	@Override
	public Class<? extends Object> getReturnType() {
		return expr.getReturnType();
	}
	
	@Override
	public boolean getAnd() {
		return false;
	}
	
	@Override
	public String toString(final Event e, final boolean debug) {
		return (element == 0 ? "a " : "the ") + (element == -1 ? "first" : element == 1 ? "last" : "random") + " element of " + expr.toString(e, debug);
	}
	
}
