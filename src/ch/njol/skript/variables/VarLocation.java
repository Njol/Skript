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
import ch.njol.skript.lang.Variable;
import ch.njol.skript.variables.base.VarVariable;

/**
 * @author Peter Güttinger
 * 
 */
public class VarLocation extends VarVariable<Location> {
	
	static {
		Skript.registerVariable(VarLocation.class, Location.class, "location of %location%", "%location%'[s] location");
	}
	
	@SuppressWarnings("unchecked")
	@Override
	public boolean init(final Variable<?>[] vars, final int matchedPattern, final ParseResult parseResult) {
		var = (Variable<Location>) vars[0];
		return true;
	}
	
	@Override
	public String getDebugMessage(final Event e) {
		return "location of " + var.getDebugMessage(e);
	}
	
	@Override
	public String toString() {
		return "location of " + var;
	}
	
}
