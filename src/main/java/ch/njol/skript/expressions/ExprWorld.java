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

import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Entity;
import org.bukkit.event.Event;
import org.bukkit.event.player.PlayerTeleportEvent;

import ch.njol.skript.Skript;
import ch.njol.skript.classes.Converter;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Examples;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import ch.njol.skript.effects.Delay;
import ch.njol.skript.expressions.base.EventValueExpression;
import ch.njol.skript.expressions.base.PropertyExpression;
import ch.njol.skript.lang.DefaultExpression;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.ExpressionType;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.util.Kleenean;

/**
 * @author Peter Güttinger
 */
@SuppressWarnings("serial")
@Name("World")
@Description("The world the event occurred in.")
@Examples({"world is \"world_nether\"",
		"teleport the player to the world's spawn",
		"set the weather in the player's world to rain"})
@Since("1.0")
public class ExprWorld extends PropertyExpression<Object, World> {
	static {
		Skript.registerExpression(ExprWorld.class, World.class, ExpressionType.PROPERTY, "[the] world [of %-entity/location%]", "%entity/location%'[s] world");
	}
	
	@Override
	public boolean init(final Expression<?>[] exprs, final int matchedPattern, final Kleenean isDelayed, final ParseResult parser) {
		if (exprs[0] == null) {
			exprs[0] = new EventValueExpression<World>(World.class);
			if (!((DefaultExpression<?>) exprs[0]).init())
				return false;
		}
		setExpr(exprs[0]);
		return true;
	}
	
	@Override
	public String toString(final Event e, final boolean debug) {
		return "the world" + (getExpr().isDefault() ? "" : " of " + getExpr().toString(e, debug));
	}
	
	@Override
	public Class<World> getReturnType() {
		return World.class;
	}
	
	@Override
	protected World[] get(final Event e, final Object[] source) {
		if (getExpr().isDefault() && e instanceof PlayerTeleportEvent && getTime() > 0 && !Delay.isDelayed(e)) {
			return new World[] {((PlayerTeleportEvent) e).getTo().getWorld()};
		}
		if (source instanceof World[]) // event value (see init)
			return (World[]) source;
		return get(source, new Converter<Object, World>() {
			@Override
			public World convert(final Object o) {
				if (o instanceof Entity)
					return ((Entity) o).getWorld();
				if (o instanceof Location)
					return ((Location) o).getWorld();
				assert false : o;
				return null;
			}
		});
	}
	
	@SuppressWarnings("unchecked")
	@Override
	public boolean setTime(final int time) {
		return super.setTime(time, getExpr(), PlayerTeleportEvent.class);
	}
	
}
