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

package ch.njol.skript.loops;

import java.util.Iterator;

import org.bukkit.event.Event;

import ch.njol.skript.Skript;
import ch.njol.skript.api.LoopExpr;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.skript.util.ItemData;
import ch.njol.skript.util.ItemType;
import ch.njol.util.iterator.ArrayIterator;

/**
 * @author Peter Güttinger
 * 
 */
public class LoopIdsOf extends LoopExpr<Integer> {
	
	static {
		Skript.registerLoop(LoopIdsOf.class, Integer.class, "id[s] of %itemtypes%");
	}
	
	private Expression<ItemType> types;
	
	@SuppressWarnings("unchecked")
	@Override
	public boolean init(final Expression<?>[] vars, final int matchedPattern, final ParseResult parser) {
		types = (Expression<ItemType>) vars[0];
		return true;
	}
	
	@Override
	public String getLoopDebugMessage(final Event e) {
		return "ids of " + types.getDebugMessage(e);
	}
	
	@Override
	protected Iterator<Integer> iterator(final Event e) {
		return new Iterator<Integer>() {
			
			private final Iterator<ItemType> ts = new ArrayIterator<ItemType>(types.getArray(e));
			private Iterator<ItemData> ds = ts.next().iterator();
			
			@Override
			public boolean hasNext() {
				while (ts.hasNext() && !ds.hasNext()) {
					ds = ts.next().iterator();
				}
				return ts.hasNext() || ds.hasNext();
			}
			
			@Override
			public Integer next() {
				return ds.next().getId();
			}
			
			@Override
			public void remove() {}
			
		};
	}
	
	@Override
	public Class<? extends Integer> getReturnType() {
		return Integer.class;
	}
	
	@Override
	public String toString() {
		return "the loop-ID";
	}
	
	@Override
	public boolean isLoopOf(final String s) {
		return s.equalsIgnoreCase("id");
	}
	
}
