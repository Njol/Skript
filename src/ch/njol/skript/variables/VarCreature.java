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

import org.bukkit.entity.Creature;
import org.bukkit.entity.Entity;
import org.bukkit.event.Event;

import ch.njol.skript.Skript;
import ch.njol.skript.api.exception.InitException;
import ch.njol.skript.api.exception.ParseException;
import ch.njol.skript.api.intern.Variable;

/**
 * @author Peter Güttinger
 * 
 */
public class VarCreature extends Variable<Creature> {
	
	static {
		Skript.addVariable(VarCreature.class, Creature.class, "creature");
	}
	
	@Override
	public void init(final Variable<?>[] vars, final int matchedPattern, final Matcher matcher) throws InitException, ParseException {}
	
	@Override
	public String getDebugMessage(final Event e) {
		if (e == null)
			return "creature";
		return Skript.toString(getFirst(e));
	}
	
	@Override
	protected Creature[] getAll(final Event e) {
		final Entity ent = Skript.getEventValue(e, Entity.class);
		if (ent instanceof Creature)
			return new Creature[] {(Creature) ent};
		return null;
	}
	
	@Override
	public Class<? extends Creature> getReturnType() {
		return Creature.class;
	}
	
	@Override
	public String toString() {
		return "the creature";
	}
	
}
