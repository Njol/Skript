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

import java.util.Iterator;

import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;

import ch.njol.skript.Skript;
import ch.njol.skript.Skript.ExpressionType;
import ch.njol.skript.SkriptLogger;
import ch.njol.skript.SkriptLogger.SubLog;
import ch.njol.skript.entity.EntityData;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.Literal;
import ch.njol.skript.lang.ParseContext;
import ch.njol.skript.lang.SkriptParser;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.skript.lang.util.SimpleExpression;
import ch.njol.skript.util.Utils;
import ch.njol.util.iterator.NonNullIterator;

/**
 * @author Peter Güttinger
 * 
 */
public class ExprEntities extends SimpleExpression<Entity> {
	
	static {
		Skript.registerExpression(ExprEntities.class, Entity.class, ExpressionType.NORMAL, "all <.+> [(in|of) [world[s]] %-worlds%]", "[all] entities of type[s] %entitydatas% [(in|of) [world[s]] %-worlds%]");
	}
	
	private Expression<? extends EntityData<?>> types;
	private Expression<World> worlds;
	
	private Class<? extends Entity> returnType = Entity.class;
	
	private int matchedPattern;
	
	@SuppressWarnings("unchecked")
	@Override
	public boolean init(final Expression<?>[] exprs, final int matchedPattern, final boolean isDelayed, final ParseResult parseResult) {
		this.matchedPattern = matchedPattern;
		if (matchedPattern == 0) {
			final SubLog log = SkriptLogger.startSubLog();
			types = (Expression<? extends EntityData<?>>) SkriptParser.parseLiteral(parseResult.regexes.get(0).group(), EntityData.class, ParseContext.DEFAULT);
			SkriptLogger.stopSubLog(log);
			if (types == null)
				return false;
			log.printLog();
		} else {
			types = (Expression<? extends EntityData<?>>) exprs[0];
		}
		if (types instanceof Literal && ((Literal<EntityData<?>>) types).getAll().length == 1) {
			returnType = ((Literal<EntityData<?>>) types).getSingle().getType();
		}
		worlds = (Expression<World>) exprs[exprs.length - 1];
		return true;
	}
	
	@Override
	public boolean isSingle() {
		return false;
	}
	
	@Override
	public Class<? extends Entity> getReturnType() {
		return returnType;
	}
	
	@Override
	protected Entity[] get(final Event e) {
		return EntityData.getAll(types.getAll(e), returnType, worlds == null ? null : worlds.getArray(e));
	}
	
	@Override
	public Iterator<Entity> iterator(final Event e) {
		if (worlds == null && returnType == Player.class)
			return super.iterator(e);
		return new NonNullIterator<Entity>() {
			
			private final World[] ws = worlds == null ? Bukkit.getWorlds().toArray(new World[0]) : worlds.getArray(e);
			private int w = -1;
			
			private final EntityData<?>[] ts = types.getAll(e);
			
			private Iterator<? extends Entity> curIter = null;
			
			@Override
			protected Entity getNext() {
				while (true) {
					while (curIter == null || !curIter.hasNext()) {
						w++;
						if (w == ws.length)
							return null;
						curIter = ws[w].getEntitiesByClass(returnType).iterator();
					}
					while (curIter.hasNext()) {
						final Entity current = curIter.next();
						for (final EntityData<?> t : ts) {
							if (t.isInstance(current))
								return current;
						}
					}
				}
			}
		};
	}
	
	@Override
	public boolean canLoop() {
		return true;
	}
	
	@Override
	public String toString(final Event e, final boolean debug) {
		return "all " + (matchedPattern == 1 ? "entities of types " + types.toString(e, debug) : Utils.toPlural(types.toString(e, debug))) + (worlds == null ? "" : " in " + worlds.toString(e, debug));
	}
	
	@Override
	public boolean getAnd() {
		return true;
	}
	
}
