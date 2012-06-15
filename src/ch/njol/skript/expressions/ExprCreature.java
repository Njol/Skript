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

package ch.njol.skript.expressions;

import org.bukkit.entity.Creature;
import org.bukkit.entity.Entity;
import org.bukkit.event.Event;

import ch.njol.skript.Skript;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.SimpleExpression;
import ch.njol.skript.lang.SkriptParser.ParseResult;

/**
 * @author Peter Güttinger
 * 
 */
public class ExprCreature extends SimpleExpression<Creature> {
	
	static {
		Skript.registerExpression(ExprCreature.class, Creature.class, "[the] creature");
	}
	
	@Override
	public boolean init(final Expression<?>[] vars, final int matchedPattern, final ParseResult parser) {
		return true;
	}
	
	@Override
	public String getDebugMessage(final Event e) {
		if (e == null)
			return "creature";
		return Skript.getDebugMessage(getSingle(e));
	}
	
	@Override
	protected Creature[] getAll(final Event e) {
		final Entity ent = Skript.getEventValue(e, Entity.class, 0);
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
	
	@Override
	public boolean isSingle() {
		return true;
	}
	
}
