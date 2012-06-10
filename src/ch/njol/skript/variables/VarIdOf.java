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

package ch.njol.skript.variables;

import java.util.ArrayList;

import org.bukkit.event.Event;

import ch.njol.skript.Skript;
import ch.njol.skript.lang.ExprParser.ParseResult;
import ch.njol.skript.lang.Literal;
import ch.njol.skript.lang.SimpleVariable;
import ch.njol.skript.lang.Variable;
import ch.njol.skript.util.ItemData;
import ch.njol.skript.util.ItemType;

/**
 * @author Peter Güttinger
 * 
 */
public class VarIdOf extends SimpleVariable<Integer> {
	
	static {
		Skript.registerVariable(VarIdOf.class, Integer.class, "[the] id[<s>] of %itemtype%", "%itemtype%'[s] id[<s>]");
	}
	
	private Variable<ItemType> types;
	
	private boolean single = false;
	
	@SuppressWarnings("unchecked")
	@Override
	public boolean init(final Variable<?>[] vars, final int matchedPattern, final ParseResult parser) {
		types = (Variable<ItemType>) vars[0];
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
	public String getDebugMessage(final Event e) {
		return "id" + (single ? "" : "s") + " of " + types.getDebugMessage(e);
	}
	
	@Override
	protected Integer[] getAll(final Event e) {
		if (single) {
			final ItemType t = types.getSingle(e);
			if (t == null)
				return null;
			return new Integer[] {t.getTypes().get(0).getId()};
		}
		final ArrayList<Integer> r = new ArrayList<Integer>();
		for (final ItemType t : types.getArray(e)) {
			for (final ItemData d : t) {
				r.add(Integer.valueOf(d.getId()));
			}
		}
		return r.toArray(new Integer[0]);
	}
	
	@Override
	public Class<? extends Integer> getReturnType() {
		return Integer.class;
	}
	
	@Override
	public String toString() {
		return "the ids of " + types;
	}
	
	@Override
	public boolean isSingle() {
		return single;
	}
	
}
