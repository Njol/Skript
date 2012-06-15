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

import java.lang.reflect.Array;

import org.bukkit.entity.Entity;
import org.bukkit.event.Event;

import ch.njol.skript.Skript;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.SimpleExpression;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.skript.util.EntityType;

/**
 * @author Peter Güttinger
 * 
 */
public class ExprAttacked extends SimpleExpression<Entity> {
	
	static {
		Skript.registerExpression(ExprAttacked.class, Entity.class, "[the] (attacked|damaged|victim) [<(.+)>]");
	}
	
	private EntityType type;
	private Entity[] array;
	
	@Override
	public boolean init(final Expression<?>[] vars, final int matchedPattern, final ParseResult parser) {
		final String type = parser.regexes.size() == 0 ? null : parser.regexes.get(0).group();
		if (type == null) {
			this.type = new EntityType(Entity.class, 1);
		} else {
			this.type = EntityType.parse(type);
			if (this.type == null) {
				Skript.error("'" + type + "' is not an entity type");
				return false;
			}
		}
		array = (Entity[]) Array.newInstance(this.type.c, 1);
		return true;
	}
	
	@Override
	protected Entity[] getAll(final Event e) {
		final Entity entity = Skript.getEventValue(e, Entity.class, 0);
		if (type.isInstance(entity)) {
			array[0] = entity;
			return array;
		}
		return null;
	}
	
	@Override
	public Class<? extends Entity> getReturnType() {
		return type.c;
	}
	
	@Override
	public String getDebugMessage(final Event e) {
		if (e == null)
			return "attacked " + type;
		return Skript.getDebugMessage(getSingle(e));
	}
	
	@Override
	public String toString() {
		return "the attacked " + type;
	}
	
	@Override
	public boolean isSingle() {
		return true;
	}
	
}
