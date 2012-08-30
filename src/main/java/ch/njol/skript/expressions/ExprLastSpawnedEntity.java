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
import ch.njol.skript.Skript.ExpressionType;
import ch.njol.skript.effects.EffSpawn;
import ch.njol.skript.entity.EntityData;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.skript.lang.util.SimpleExpression;
import ch.njol.skript.log.SkriptLogger;
import ch.njol.skript.log.SubLog;
import ch.njol.skript.util.Utils;

/**
 * @author Peter Güttinger
 */
public class ExprLastSpawnedEntity extends SimpleExpression<Entity> {
	
	static {
		Skript.registerExpression(ExprLastSpawnedEntity.class, Entity.class, ExpressionType.SIMPLE, "[last[ly]] spawned <.+>");
	}
	
	private EntityData<?> type;
	
	@Override
	public boolean init(final Expression<?>[] exprs, final int matchedPattern, final int isDelayed, final ParseResult parseResult) {
		final SubLog log = SkriptLogger.startSubLog();
		type = EntityData.parseWithoutAnOrAny(parseResult.regexes.get(0).group());
		log.stop();
		if (type == null)
			return false;
		log.printLog();
		return true;
	}
	
	@Override
	protected Entity[] get(final Event e) {
		final Entity en = Utils.validate(EffSpawn.lastSpawned);
		if (en == null)
			return null;
		if (!type.isInstance(en))
			return null;
		final Entity[] one = (Entity[]) Array.newInstance(type.getType(), 1);
		one[0] = en;
		return one;
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
	public boolean getAnd() {
		return false;
	}
	
	@Override
	public String toString(final Event e, final boolean debug) {
		return "last spawned " + type;
	}
	
}
