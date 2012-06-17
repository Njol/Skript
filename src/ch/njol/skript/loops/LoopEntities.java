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

package ch.njol.skript.loops;

import java.util.Iterator;

import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.entity.Entity;
import org.bukkit.event.Event;

import ch.njol.skript.Skript;
import ch.njol.skript.api.Converter;
import ch.njol.skript.api.Converter.ConverterUtils;
import ch.njol.skript.api.LoopExpr;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.Literal;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.skript.util.EntityType;

/**
 * @author Peter Güttinger
 * 
 */
public class LoopEntities extends LoopExpr<Entity> {
	
	static {
		Skript.registerLoop(LoopEntities.class, Entity.class, "%entitytypes% [in [world[s]] %worlds%]", "entities of type[s] %entitytypes% [in [world[s]] %worlds%]");
	}
	
	private Expression<EntityType> types;
	private Class<? extends Entity> returnType = Entity.class;
	private Expression<World> worlds;
	
	@SuppressWarnings("unchecked")
	@Override
	public boolean init(final Expression<?>[] exprs, final int matchedPattern, final ParseResult parseResult) {
		types = (Expression<EntityType>) exprs[0];
		worlds = (Expression<World>) exprs[1];
		
		if (types instanceof Literal && types.isSingle() && types.getAnd())
			returnType = ((Literal<EntityType>) types).getSingle().c;
		
		return true;
	}
	
	@Override
	public Class<? extends Entity> getReturnType() {
		return returnType;
	}
	
	@Override
	protected Iterator<? extends Entity> iterator(final Event e) {
		final World[] ws = worlds == null ? Bukkit.getWorlds().toArray(new World[0]) : worlds.getArray(e);
		return new Iterator<Entity>() {
			
			final Class<? extends Entity>[] classes = ConverterUtils.convertUnsafe(types.getArray(e), new Converter<EntityType, Class<? extends Entity>>() {
				@Override
				public Class<? extends Entity> convert(final EntityType t) {
					return t.c;
				}
			}, Class.class);
			
			private int w = 0;
			Iterator<Entity> iter = ws[w].getEntitiesByClasses(classes).iterator();
			
			@Override
			public boolean hasNext() {
				while (!iter.hasNext()) {
					w++;
					if (w >= ws.length)
						return false;
					iter = ws[w].getEntitiesByClasses(classes).iterator();
				}
				return true;
			}
			
			@Override
			public Entity next() {
				return iter.next();
			}
			
			@Override
			public void remove() {
				throw new UnsupportedOperationException();
			}
		};
	}
	
	@Override
	public boolean isLoopOf(final String s) {
		return EntityType.parse(s).c.isAssignableFrom(returnType);
	}
	
	@Override
	public String getLoopDebugMessage(final Event e) {
		return types + (worlds == null ? "" : " in worlds " + worlds);
	}
	
	@Override
	public String toString() {
		return "loop-" + EntityType.toString(returnType);
	}
	
}
