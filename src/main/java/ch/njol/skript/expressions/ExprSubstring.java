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

import org.bukkit.event.Event;

import ch.njol.skript.Skript;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.ExpressionType;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.skript.lang.util.SimpleExpression;
import ch.njol.util.Kleenean;

/**
 * @author Peter Güttinger
 */
@SuppressWarnings("serial")
public class ExprSubstring extends SimpleExpression<String> {
	static {
		Skript.registerExpression(ExprSubstring.class, String.class, ExpressionType.COMBINED,
				"split %string% between %number% and %number%)");
	}
	
	private Expression<String> string;
	private Expression<Number> start, end;
	
	@Override
	public boolean init(final Expression<?>[] exprs, final int matchedPattern, final Kleenean isDelayed, final ParseResult parseResult) {
		// TODO Auto-generated method stub
		return true;
	}
	
	@Override
	protected String[] get(final Event e) {
		final String s = string.getSingle(e);
		if (s == null)
			return null;
		final Number d1 = start == null ? 0 : start.getSingle(e), d2 = end == null ? s.length() : end.getSingle(e);
		if (d1 == null || d2 == null)
			return null;
		final int i1 = (int) Math.round(d1.doubleValue()), i2 = (int) Math.round(d2.doubleValue());
		if (i1 > i2)
			return null;
		return new String[] {s.substring(Math.max(0, i1 - 1), Math.min(i2, s.length()))};
	}
	
	@Override
	public boolean isSingle() {
		return true;
	}
	
	@Override
	public Class<? extends String> getReturnType() {
		return String.class;
	}
	
	@Override
	public String toString(final Event e, final boolean debug) {
		// TODO Auto-generated method stub
		return null;
	}
	
}
