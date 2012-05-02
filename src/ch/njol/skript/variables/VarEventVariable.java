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

import java.util.regex.Matcher;

import org.bukkit.event.Event;

import ch.njol.skript.Skript;
import ch.njol.skript.api.exception.ParseException;
import ch.njol.skript.api.intern.Variable;
import ch.njol.skript.variables.base.VarVariable;

/**
 * Provided for convenience: one can write 'event-world' instead of only 'world' to distinguish between the event-world and the loop-world.
 * 
 * @author Peter Güttinger
 */
public class VarEventVariable extends VarVariable<Object> {
	
	static {
		Skript.addVariable(VarEventVariable.class, Object.class, "event-(\\S+)");
	}
	
	@Override
	public void init(final Variable<?>[] vars, final int matchedPattern, final Matcher matcher) throws ParseException {
		var = Variable.parseNoLiteral(matcher.group(1), Skript.getVariables().iterator());
		if (var == null)
			throw new ParseException("'" + matcher.group() + "' is invalid");
	}
	
	@Override
	public String getDebugMessage(final Event e) {
		return var.getDebugMessage(e);
	}
	
}
