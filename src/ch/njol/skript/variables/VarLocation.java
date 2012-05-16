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

import org.bukkit.Location;
import org.bukkit.event.Event;

import ch.njol.skript.Skript;
import ch.njol.skript.lang.ExprParser.ParseResult;
import ch.njol.skript.lang.SimpleVariable;
import ch.njol.skript.lang.Variable;
import ch.njol.skript.util.Offset;

/**
 * @author Peter Güttinger
 * 
 */
public class VarLocation extends SimpleVariable<Location> {
	
	static {
		Skript.addVariable(VarLocation.class, Location.class, "%offsets% %locations%");
	}
	
	private Variable<Offset> offsets;
	private Variable<Location> locations;
	
	@SuppressWarnings("unchecked")
	@Override
	public void init(final Variable<?>[] vars, final int matchedPattern, final ParseResult parser) {
		offsets = (Variable<Offset>) vars[0];
		locations = (Variable<Location>) vars[1];
	}
	
	@Override
	public String getDebugMessage(final Event e) {
		return offsets.getDebugMessage(e) + " " + locations.getDebugMessage(e);
	}
	
	@Override
	protected Location[] getAll(final Event e) {
		return Offset.setOff(offsets.getArray(e), locations.getArray(e));
	}
	
	@Override
	public Class<Location> getReturnType() {
		return Location.class;
	}
	
	@Override
	public String toString() {
		return offsets + " " + locations;
	}
	
	@Override
	public boolean isSingle() {
		return offsets.isSingle() && locations.isSingle();
	}
	
}
