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

package ch.njol.skript.conditions;

import org.bukkit.World;
import org.bukkit.entity.Entity;
import org.bukkit.event.Event;

import ch.njol.skript.Skript;
import ch.njol.skript.lang.Condition;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.util.Checker;

/**
 * @author Peter Güttinger
 * 
 */
public class CondIsInWorld extends Condition {
	
	static {
		Skript.registerCondition(CondIsInWorld.class, "%entities% (is|are) in [[the] world] %worlds%", "%entities% (is not|isn't|are not|aren't) in [[the] world] %worlds%");
	}
	
	private Expression<Entity> entities;
	private Expression<World> worlds;
	
	@SuppressWarnings("unchecked")
	@Override
	public boolean init(final Expression<?>[] exprs, final int matchedPattern, final boolean isDelayed, final ParseResult parseResult) {
		entities = (Expression<Entity>) exprs[0];
		worlds = (Expression<World>) exprs[1];
		setNegated(matchedPattern == 1);
		return true;
	}
	
	@Override
	public boolean check(final Event e) {
		return entities.check(e, new Checker<Entity>() {
			@Override
			public boolean check(final Entity en) {
				return worlds.check(e, new Checker<World>() {
					@Override
					public boolean check(final World w) {
						return en.getWorld() == w;
					}
				});
			}
		}, this);
	}
	
	@Override
	public String toString(final Event e, final boolean debug) {
		return entities.toString(e, debug) + " " + (entities.isSingle() ? "is" : "are") + " " + (isNegated() ? "not" : "") + " in world " + worlds.toString(e, debug);
	}
	
}
