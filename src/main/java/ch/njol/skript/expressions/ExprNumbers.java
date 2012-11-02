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
 */
public class ExprNumbers extends SimpleExpression<Number> {
	private static final long serialVersionUID = -8168866962252682002L;
	
	static {
		Skript.registerExpression(ExprNumbers.class, Number.class, ExpressionType.NORMAL,
				"[(all|the)] (0¦numbers|1¦integers) (between|from) %number% (and|to) %number%",
				"%number% times");
	}
	
	private Expression<Number> start, end;
	private boolean integer;
	
	@SuppressWarnings("unchecked")
	@Override
	public boolean init(final Expression<?>[] exprs, final int matchedPattern, final int isDelayed, final ParseResult parseResult) {
		start = matchedPattern == 0 ? (Expression<Number>) exprs[0] : new SimpleLiteral<Number>(1, false);
		end = (Expression<Number>) exprs[1 - matchedPattern];
		integer = parseResult.mark == 1;
		return true;
	}
	
	@Override
	protected Number[] get(final Event e) {
		final Number s = start.getSingle(e), f = end.getSingle(e);
		if (s == null || f == null || f.doubleValue() < s.doubleValue())
			return null;
		final Number[] array = integer ? new Integer[(int) (Math.floor(f.doubleValue()) - Math.ceil(s.doubleValue()) + 1)] : new Double[(int) Math.floor(f.doubleValue() - s.doubleValue() + 1)];
		final double low = integer ? Math.ceil(s.doubleValue()) : s.doubleValue();
		for (int i = 0; i < array.length; i++) {
			if (integer)
				array[i] = Integer.valueOf((int) low + i);
			else
				array[i] = Double.valueOf(low + i);
		}
		return array;
	}
	
	@Override
	public Iterator<Number> iterator(final Event e) {
		final Number s = start.getSingle(e), f = end.getSingle(e);
		if (s == null || f == null)
			return null;
		return new Iterator<Number>() {
			double i = integer ? Math.ceil(s.doubleValue()) : s.doubleValue(), max = integer ? Math.floor(f.doubleValue()) : f.doubleValue();
			
			@Override
			public boolean hasNext() {
				return i <= max;
			}
			
			@Override
			public Number next() {
				if (!hasNext())
					throw new NoSuchElementException();
				if (integer)
					return Integer.valueOf((int) i++);
				else
					return Double.valueOf(i++);
			}
			
			@Override
			public void remove() {
				throw new UnsupportedOperationException();
			}
		};
	}
	
	@Override
	public String toString(final Event e, final boolean debug) {
		return (integer ? "integers" : "numbers") + " from " + start.toString(e, debug) + " to " + end.toString(e, debug);
	}
	
	@Override
	public boolean isLoopOf(final String s) {
		return integer && (s.equalsIgnoreCase("integer") || s.equalsIgnoreCase("int"));
	}
	
	@Override
	public boolean isSingle() {
		return false;
	}
	
	@Override
	public Class<? extends Number> getReturnType() {
		return integer ? Integer.class : Double.class;
	}
	
	@Override
	public boolean getAnd() {
		return true;
	}
	
}
