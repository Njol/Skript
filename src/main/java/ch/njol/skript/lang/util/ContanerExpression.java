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

package ch.njol.skript.lang.util;

import java.util.Iterator;
import java.util.NoSuchElementException;

import org.bukkit.event.Event;

import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.skript.util.Container;

/**
 * @author Peter Güttinger
 */
public class ContanerExpression extends SimpleExpression<Object> {
	private static final long serialVersionUID = 309689225239372629L;
	
	private final Expression<?> expr;
	private final Class<?> c;
	
	public ContanerExpression(final Expression<?> expr, final Class<?> c) {
		this.expr = expr;
		this.c = c;
	}
	
	@Override
	protected Object[] get(final Event e) {
		throw new UnsupportedOperationException("ContanerExpression must only be used by Loops");
	}
	
	@Override
	public Iterator<Object> iterator(final Event e) {
		return new Iterator<Object>() {
			
			private final Iterator<? extends Container<?>> iter = (Iterator<? extends Container<?>>) expr.iterator(e);
			private Iterator<?> current;
			
			@Override
			public boolean hasNext() {
				while (iter.hasNext() && (current == null || !current.hasNext())) {
					current = iter.next().containerIterator();
				}
				return current != null && current.hasNext();
			}
			
			@Override
			public Object next() {
				if (!hasNext())
					throw new NoSuchElementException();
				return current.next();
			}
			
			@Override
			public void remove() {
				throw new UnsupportedOperationException();
			}
		};
	}
	
	@Override
	public boolean isSingle() {
		return false;
	}
	
	@Override
	public Class<? extends Object> getReturnType() {
		return c;
	}
	
	@Override
	public boolean init(final Expression<?>[] exprs, final int matchedPattern, final int isDelayed, final ParseResult parseResult) {
		throw new UnsupportedOperationException();
	}
	
	@Override
	public String toString(final Event e, final boolean debug) {
		return expr.toString(e, debug);
	}
	
	@Override
	public boolean getAnd() {
		return true;
	}
	
}
