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
import java.util.regex.Matcher;

import org.bukkit.event.Event;

import ch.njol.skript.Skript;
import ch.njol.skript.api.exception.InitException;
import ch.njol.skript.api.exception.ParseException;
import ch.njol.skript.api.intern.Literal;
import ch.njol.skript.api.intern.Variable;
import ch.njol.skript.util.ItemData;
import ch.njol.skript.util.ItemType;

/**
 * @author Peter Güttinger
 * 
 */
public class VarIdOf extends Variable<Integer> {
	
	static {
		Skript.addVariable(VarIdOf.class, Integer.class, "id(s)? of %itemtype%");
	}
	
	private Variable<ItemType> types;
	
	@SuppressWarnings("unchecked")
	@Override
	public void init(final Variable<?>[] vars, final int matchedPattern, final Matcher matcher) throws InitException, ParseException {
		types = (Variable<ItemType>) vars[0];
		if (matcher.group(1) != null && types instanceof Literal) {
			if (((Literal<ItemType>) types).getAll().length > 1 ||
					((Literal<ItemType>) types).getAll().length != 0 && ((Literal<ItemType>) types).getAll()[0] != null && ((Literal<ItemType>) types).getAll()[0].numTypes() > 1) {
				throw new ParseException("The alias you specified has multiple IDs, thus use 'ids of ...', not 'id of ...'");
			}
		}
	}
	
	@Override
	public String getDebugMessage(final Event e) {
		return "id of " + types.getDebugMessage(e);
	}
	
	@Override
	protected Integer[] getAll(final Event e) {
		final ArrayList<Integer> r = new ArrayList<Integer>();
		for (final ItemType t : types.get(e, false)) {
			for (final ItemData d : t) {
				r.add(Integer.valueOf(d.typeid));
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
		return "the id(s) of " + types;
	}
	
}
