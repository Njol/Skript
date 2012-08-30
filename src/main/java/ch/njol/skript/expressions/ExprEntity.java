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

import org.bukkit.entity.Entity;
import org.bukkit.event.Event;

import ch.njol.skript.Aliases;
import ch.njol.skript.Skript;
import ch.njol.skript.Skript.ExpressionType;
import ch.njol.skript.entity.EntityData;
import ch.njol.skript.expressions.base.EventValueExpression;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.skript.lang.util.SimpleExpression;
import ch.njol.skript.log.SkriptLogger;
import ch.njol.skript.log.SubLog;
import ch.njol.skript.util.ItemType;
import ch.njol.util.StringUtils;

/**
 * @author Peter Güttinger
 */
public class ExprEntity extends SimpleExpression<Entity> {
	
	static {
		Skript.registerExpression(ExprEntity.class, Entity.class, ExpressionType.SIMPLE, "[the] [event-]<.+>");
	}
	
	private EntityData<?> type;
	
	private EventValueExpression<Entity> entity;
	
	@Override
	public boolean init(final Expression<?>[] exprs, final int matchedPattern, final int isDelayed, final ParseResult parseResult) {
		final SubLog log = SkriptLogger.startSubLog();
		final ItemType item = Aliases.parseItemType(parseResult.regexes.get(0).group());
		if (item != null && !StringUtils.startsWithIgnoreCase(parseResult.expr, "the ")) {
			log.stop();
			return false;
		}
		log.clear();
		type = EntityData.parseWithoutAnOrAny(parseResult.regexes.get(0).group());
		log.stop();
		if (type == null || type.isPlural())
			return false;
		log.printLog();
		entity = new EventValueExpression<Entity>(type.getType());
		return entity.init();
	}
	
	@Override
	public boolean isSingle() {
		return true;
	}
	
	@Override
	public Class<? extends Entity> getReturnType() {
		return type.getType();
	}
	
	@Override
	protected Entity[] get(final Event e) {
		final Entity[] es = entity.getArray(e);
		if (es.length == 0 || type.isInstance(es[0]))
			return es;
		return null;
	}
	
	@Override
	public String toString(final Event e, final boolean debug) {
		return "the " + type;
	}
	
	@Override
	public boolean getAnd() {
		return true;
	}
	
}
