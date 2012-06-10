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

import org.bukkit.event.Event;

import ch.njol.skript.Skript;
import ch.njol.skript.lang.ExprParser;
import ch.njol.skript.lang.ExprParser.ParseResult;
import ch.njol.skript.lang.Variable;
import ch.njol.skript.variables.base.VarVariable;

/**
 * Provided for convenience: one can write 'event-world' instead of only 'world' to distinguish between the event-world and the loop-world.
 * 
 * @author Peter Güttinger
 */
public class VarEventVariable extends VarVariable<Object> {
	
	static {
		Skript.registerVariable(VarEventVariable.class, Object.class, "[the] event-<.+>");
	}
	
	@Override
	public boolean init(final Variable<?>[] vars, final int matchedPattern, final ParseResult parser) {
		var = (Variable<?>) ExprParser.parse(parser.regexes.get(0).group(), Skript.getVariables().iterator(), false, "'" + parser.expr + "' is no an event variable");
		return var != null;
	}
	
	@Override
	public String getDebugMessage(final Event e) {
		return var.getDebugMessage(e);
	}
	
}
