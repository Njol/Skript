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

import java.util.Iterator;
import java.util.NoSuchElementException;

import org.bukkit.event.Event;

import ch.njol.skript.Skript;
import ch.njol.skript.Skript.ExpressionType;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.skript.lang.util.SimpleExpression;
import ch.njol.skript.lang.util.SimpleLiteral;

/**
 * @author Peter Güttinger
 * 
 */
public class ExprNumbers extends SimpleExpression<Integer> {
	
	static {
		Skript.registerExpression(ExprNumbers.class, Integer.class, ExpressionType.NORMAL,
				"[(all|the)] (numbers|integers) (between|from) %integer% (and|to) %integer%",
				"%integer% times");
	}
	
	private Expression<Integer> start, end;
	
	@SuppressWarnings("unchecked")
	@Override
	public boolean init(final Expression<?>[] exprs, final int matchedPattern, final int isDelayed, final ParseResult parseResult) {
		start = matchedPattern == 0 ? (Expression<Integer>) exprs[0] : new SimpleLiteral<Integer>(1, false);
		end = (Expression<Integer>) exprs[1 - matchedPattern];
		return true;
	}
	
	@Override
	protected Integer[] get(final Event e) {
		final Integer s = start.getSingle(e), f = end.getSingle(e);
		if (s == null || f == null)
			return null;
		final Integer[] array = new Integer[f - s + 1];
		final int low = s;
		for (int i = 0; i < array.length; i++) {
			array[i] = Integer.valueOf(low + i);
		}
		return array;
	}
	
	@Override
	public Iterator<Integer> iterator(final Event e) {
		final Integer s = start.getSingle(e), f = end.getSingle(e);
		if (s == null || f == null)
			return null;
		return new Iterator<Integer>() {
			int i = s, max = f;
			
			@Override
			public boolean hasNext() {
				return i <= max;
			}
			
			@Override
			public Integer next() {
				if (!hasNext())
					throw new NoSuchElementException();
				return Integer.valueOf(i++);
			}
			
			@Override
			public void remove() {
				throw new UnsupportedOperationException();
			}
		};
	}
	
	@Override
	public String toString(final Event e, final boolean debug) {
		return "all integers from " + start.toString(e, debug) + " to " + end.toString(e, debug);
	}
	
	@Override
	public boolean canLoop() {
		return true;
	}
	
	@Override
	public boolean isSingle() {
		return false;
	}
	
	@Override
	public Class<? extends Integer> getReturnType() {
		return Integer.class;
	}
	
	@Override
	public boolean getAnd() {
		return true;
	}
	
}
