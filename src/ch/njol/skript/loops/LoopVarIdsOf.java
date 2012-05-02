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
import java.util.regex.Matcher;

import org.bukkit.event.Event;

import ch.njol.skript.Skript;
import ch.njol.skript.api.LoopVar;
import ch.njol.skript.api.exception.InitException;
import ch.njol.skript.api.exception.ParseException;
import ch.njol.skript.api.intern.Variable;
import ch.njol.skript.util.ItemData;
import ch.njol.skript.util.ItemType;

/**
 * @author Peter Güttinger
 * 
 */
public class LoopVarIdsOf extends LoopVar<Integer> {
	
	static {
		Skript.addLoop(LoopVarIdsOf.class, Integer.class, "ids of %itemtype%");
	}
	
	private Variable<ItemType> types;
	
	@SuppressWarnings("unchecked")
	@Override
	public void init(final Variable<?>[] vars, final int matchedPattern, final Matcher matcher) throws InitException, ParseException {
		types = (Variable<ItemType>) vars[0];
	}
	
	@Override
	public String getLoopDebugMessage(final Event e) {
		return "ids of " + types.getDebugMessage(e);
	}
	
	@Override
	protected Iterator<Integer> iterator(final Event e) {
		return new Iterator<Integer>() {
			
			private final Iterator<ItemType> ts = types.get(e, false).iterator();
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
				return ds.next().typeid;
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
