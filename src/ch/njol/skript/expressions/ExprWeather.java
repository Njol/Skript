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
import ch.njol.skript.api.Changer.ChangeMode;
import ch.njol.skript.api.Getter;
import ch.njol.skript.expressions.base.PropertyExpression;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.skript.util.WeatherType;

/**
 * @author Peter Güttinger
 * 
 */
public class ExprWeather extends PropertyExpression<WeatherType> {
	
	static {
		Skript.registerExpression(ExprWeather.class, WeatherType.class, ExpressionType.PROPERTY, "[the] weather [(in|of) %worlds%]");
	}
	
	private Expression<World> worlds;
	
	@SuppressWarnings("unchecked")
	@Override
	public boolean init(final Expression<?>[] vars, final int matchedPattern, final ParseResult parser) {
		worlds = (Expression<World>) vars[0];
		setExpr(worlds);
		return true;
	}
	
	@Override
	public String toString(final Event e, final boolean debug) {
		return "the weather in " + worlds.toString(e, debug);
	}
	
	@Override
	protected WeatherType[] get(final Event e) {
		return worlds.getArray(e, WeatherType.class, new Getter<WeatherType, World>() {
			@Override
			public WeatherType get(final World w) {
				return WeatherType.fromWorld(w);
			}
		});
	}
	
	@Override
	public Class<?> acceptChange(final ChangeMode mode) {
		if (mode == ChangeMode.CLEAR || mode == ChangeMode.SET)
			return WeatherType.class;
		return null;
	}
	
	@SuppressWarnings("incomplete-switch")
	@Override
	public void change(final Event e, final Object delta, final ChangeMode mode) {
		switch (mode) {
			case CLEAR:
				for (final World w : worlds.getArray(e)) {
					w.setStorm(false);
					w.setThundering(false);
				}
			break;
			case SET:
				final WeatherType t = (WeatherType) delta;
				for (final World w : worlds.getArray(e)) {
					t.setWeather(w);
				}
			break;
		}
	}
	
	@Override
	public Class<? extends WeatherType> getReturnType() {
		return WeatherType.class;
	}
	
}
