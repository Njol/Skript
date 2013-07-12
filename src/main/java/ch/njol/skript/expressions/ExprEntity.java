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
 * Copyright 2011-2013 Peter Güttinger
 * 
 */

package ch.njol.skript.expressions;

import org.bukkit.entity.Entity;
import org.bukkit.event.Event;

import ch.njol.skript.Skript;
import ch.njol.skript.aliases.Aliases;
import ch.njol.skript.aliases.ItemType;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Examples;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import ch.njol.skript.entity.EntityData;
import ch.njol.skript.expressions.base.EventValueExpression;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.ExpressionType;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.skript.lang.util.SimpleExpression;
import ch.njol.skript.log.RetainingLogHandler;
import ch.njol.skript.log.SkriptLogger;
import ch.njol.util.Kleenean;
import ch.njol.util.StringUtils;

/**
 * @author Peter Güttinger
 */
@SuppressWarnings("serial")
@Name("Creature/Entity/Player/Projectile/Villager/Powered Creeper/etc.")
@Description({"The entity involved in an event (an entity is a player, a creature or an inanimate object like ignited TNT, a dropped item or an arrow).",
		"You can use the specific type of the entity that's involved in the event, e.g. in a 'death of a creeper' event you can use 'the creeper' instead of 'the entity'."})
@Examples({"give a diamond sword of sharpness 3 to the player",
		"kill the creeper",
		"kill all powered creepers in the wolf's world",
		"projectile is an arrow"})
@Since("1.0")
public class ExprEntity extends SimpleExpression<Entity> {
	static {
		Skript.registerExpression(ExprEntity.class, Entity.class, ExpressionType.PATTERN_MATCHES_EVERYTHING, "[the] [event-]<.+>");
	}
	
	private EntityData<?> type;
	
	private EventValueExpression<Entity> entity;
	
	@Override
	public boolean init(final Expression<?>[] exprs, final int matchedPattern, final Kleenean isDelayed, final ParseResult parseResult) {
		final RetainingLogHandler log = SkriptLogger.startRetainingLog();
		try {
			if (!StringUtils.startsWithIgnoreCase(parseResult.expr, "the ") && !StringUtils.startsWithIgnoreCase(parseResult.expr, "event-")) {
				final ItemType item = Aliases.parseItemType(parseResult.regexes.get(0).group());
				if (item != null) {
					log.clear();
					log.printLog();
					return false;
				}
				log.clear();
			}
			type = EntityData.parseWithoutIndefiniteArticle(parseResult.regexes.get(0).group());
			if (type == null || type.isPlural().isTrue()) {
				log.clear();
				log.printLog();
				return false;
			}
			log.printLog();
		} finally {
			log.stop();
		}
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
	
}
