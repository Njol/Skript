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

import java.util.ArrayList;

import org.bukkit.event.Event;

import ch.njol.skript.Skript;
import ch.njol.skript.Skript.ExpressionType;
import ch.njol.skript.expressions.base.PropertyExpression;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.Literal;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.skript.util.ItemData;
import ch.njol.skript.util.ItemType;

/**
 * @author Peter Güttinger
 * 
 */
public class ExprIdOf extends PropertyExpression<ItemType, Integer> {
	
	static {
		Skript.registerExpression(ExprIdOf.class, Integer.class, ExpressionType.PROPERTY, "[the] id[<s>] of %itemtype%", "%itemtype%'[s] id[<s>]");
	}
	
	private Expression<ItemType> types;
	
	private boolean single = false;
	
	@SuppressWarnings("unchecked")
	@Override
	public boolean init(final Expression<?>[] vars, final int matchedPattern, final int isDelayed, final ParseResult parser) {
		types = (Expression<ItemType>) vars[0];
		setExpr(types);
		if (parser.regexes.isEmpty()) {
			single = true;
			if (!types.isSingle() || (types instanceof Literal && ((Literal<ItemType>) types).getSingle().getTypes().size() != 1)) {
				Skript.warning("'" + types + "' has multiple ids");
				single = false;
			}
		}
		return true;
	}
	
	@Override
	public String toString(final Event e, final boolean debug) {
		return "the id" + (single ? "" : "s") + " of " + types.toString(e, debug);
	}
	
	@Override
	protected Integer[] get(final Event e, final ItemType[] source) {
		if (single) {
			final ItemType t = types.getSingle(e);
			if (t == null)
				return null;
			return new Integer[] {t.getTypes().get(0).getId()};
		}
		final ArrayList<Integer> r = new ArrayList<Integer>();
		for (final ItemType t : source) {
			for (final ItemData d : t) {
				r.add(Integer.valueOf(d.getId()));
			}
		}
		return r.toArray(new Integer[0]);
	}
	
	@Override
	public Class<Integer> getReturnType() {
		return Integer.class;
	}
	
	@Override
	public boolean canLoop() {
		return !single;
	}
	
	@Override
	public boolean isLoopOf(final String s) {
		return s.equalsIgnoreCase("id");
	}
	
}
