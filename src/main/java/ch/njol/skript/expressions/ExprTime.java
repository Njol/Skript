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

import org.bukkit.World;
import org.bukkit.event.Event;

import ch.njol.skript.Skript;
import ch.njol.skript.Skript.ExpressionType;
import ch.njol.skript.classes.Changer.ChangeMode;
import ch.njol.skript.classes.data.DefaultChangers;
import ch.njol.skript.expressions.base.PropertyExpression;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.skript.util.Getter;
import ch.njol.skript.util.Time;

/**
 * 
 * @author Peter Güttinger
 * 
 */
public class ExprTime extends PropertyExpression<World, Time> {
	
	static {
		Skript.registerExpression(ExprTime.class, Time.class, ExpressionType.PROPERTY, "[the] time [(in|of) %worlds%]");
	}
	
	private Expression<World> worlds = null;
	
	@SuppressWarnings("unchecked")
	@Override
	public boolean init(final Expression<?>[] vars, final int matchedPattern, final boolean isDelayed, final ParseResult parser) {
		worlds = (Expression<World>) vars[0];
		setExpr(worlds);
		return true;
	}
	
	@Override
	protected Time[] get(final Event e, final World[] source) {
		return get(source, new Getter<Time, World>() {
			@Override
			public Time get(final World w) {
				return new Time((int) w.getTime());
			}
		});
	}
	
	@Override
	public Class<?> acceptChange(final ChangeMode mode) {
		return DefaultChangers.timeChanger.acceptChange(mode);
	}
	
	@Override
	public void change(final Event e, final Object delta, final ChangeMode mode) {
		DefaultChangers.timeChanger.change(worlds.getArray(e), delta, mode);
	}
	
	@Override
	public Class<Time> getReturnType() {
		return Time.class;
	}
	
	@Override
	public String toString(final Event e, final boolean debug) {
		if (e == null)
			return "the time in " + worlds.toString(e, debug);
		return Skript.getDebugMessage(getAll(e));
	}
	
}
