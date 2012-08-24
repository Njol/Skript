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
import org.bukkit.event.entity.EntityDamageByBlockEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.entity.EntityEvent;

import ch.njol.skript.ScriptLoader;
import ch.njol.skript.Skript;
import ch.njol.skript.Skript.ExpressionType;
import ch.njol.skript.entity.EntityData;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.skript.lang.util.SimpleExpression;
import ch.njol.skript.util.Utils;

/**
 * @author Peter Güttinger
 * 
 */
public class ExprAttacked extends SimpleExpression<Entity> {
	
	static {
		Skript.registerExpression(ExprAttacked.class, Entity.class, ExpressionType.SIMPLE, "[the] (attacked|damaged|victim) [<(.+)>]");
	}
	
	private EntityData<?> type;
	private Entity[] one;
	
	@SuppressWarnings("unchecked")
	@Override
	public boolean init(final Expression<?>[] vars, final int matchedPattern, final int isDelayed, final ParseResult parser) {
		if (!Utils.containsAny(ScriptLoader.currentEvents, EntityDamageEvent.class, EntityDamageByBlockEvent.class, EntityDamageByEntityEvent.class, EntityDeathEvent.class)) {
			Skript.error("Cannot use 'damaged'/'victim' outside of a damage or death event");
			return false;
		}
		final String type = parser.regexes.size() == 0 ? null : parser.regexes.get(0).group();
		if (type == null) {
			this.type = EntityData.fromClass(Entity.class);
		} else {
			this.type = EntityData.parse(type);
			if (this.type == null) {
				Skript.error("'" + type + "' is not an entity type");
				return false;
			}
		}
		one = (Entity[]) Array.newInstance(this.type.getType(), 1);
		return true;
	}
	
	@Override
	protected Entity[] get(final Event e) {
		final Entity entity = Utils.validate(((EntityEvent) e).getEntity());
		if (type.isInstance(entity)) {
			one[0] = entity;
			return one;
		}
		return null;
	}
	
	@Override
	public Class<? extends Entity> getReturnType() {
		return type.getType();
	}
	
	@Override
	public String toString(final Event e, final boolean debug) {
		if (e == null)
			return "the attacked " + type;
		return Skript.getDebugMessage(getSingle(e));
	}
	
	@Override
	public boolean isSingle() {
		return true;
	}
	
	@Override
	public boolean getAnd() {
		return true;
	}
	
}
