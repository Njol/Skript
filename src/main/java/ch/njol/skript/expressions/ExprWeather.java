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
import org.bukkit.event.weather.ThunderChangeEvent;
import org.bukkit.event.weather.WeatherChangeEvent;

import ch.njol.skript.Skript;
import ch.njol.skript.Skript.ExpressionType;
import ch.njol.skript.classes.Changer.ChangeMode;
import ch.njol.skript.effects.Delay;
import ch.njol.skript.expressions.base.PropertyExpression;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.skript.util.Getter;
import ch.njol.skript.util.WeatherType;

/**
 * @author Peter Güttinger
 */
public class ExprWeather extends PropertyExpression<World, WeatherType> {
	
	static {
		Skript.registerExpression(ExprWeather.class, WeatherType.class, ExpressionType.PROPERTY, "[the] weather [(in|of) %worlds%]");
	}
	
	private Expression<World> worlds;
	
	@SuppressWarnings("unchecked")
	@Override
	public boolean init(final Expression<?>[] vars, final int matchedPattern, final int isDelayed, final ParseResult parser) {
		worlds = (Expression<World>) vars[0];
		setExpr(worlds);
		return true;
	}
	
	@Override
	public String toString(final Event e, final boolean debug) {
		return "the weather in " + worlds.toString(e, debug);
	}
	
	@Override
	protected WeatherType[] get(final Event e, final World[] source) {
		if (getTime() >= 0 && (e instanceof WeatherChangeEvent || e instanceof ThunderChangeEvent) && worlds.isDefault() && !Delay.isDelayed(e)) {
			if (e instanceof WeatherChangeEvent) {
				if (!((WeatherChangeEvent) e).toWeatherState())
					return new WeatherType[] {WeatherType.CLEAR};
				return new WeatherType[] {((WeatherChangeEvent) e).getWorld().isThundering() ? WeatherType.THUNDER : WeatherType.RAIN};
			} else {
				if (((ThunderChangeEvent) e).toThunderState())
					return new WeatherType[] {WeatherType.THUNDER};
				return new WeatherType[] {((ThunderChangeEvent) e).getWorld().hasStorm() ? WeatherType.RAIN : WeatherType.CLEAR};
			}
		}
		return get(source, new Getter<WeatherType, World>() {
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
	
	@Override
	public void change(final Event e, final Object delta, final ChangeMode mode) {
		final WeatherType t = mode == ChangeMode.CLEAR ? WeatherType.CLEAR : (WeatherType) delta;
		if (getTime() >= 0 && (e instanceof WeatherChangeEvent || e instanceof ThunderChangeEvent) && worlds.isDefault() && !Delay.isDelayed(e)) {
			if (e instanceof WeatherChangeEvent) {
				if (((WeatherChangeEvent) e).toWeatherState() && t == WeatherType.CLEAR)
					((WeatherChangeEvent) e).setCancelled(true);
				if (((WeatherChangeEvent) e).getWorld().isThundering() != (t == WeatherType.THUNDER))
					((WeatherChangeEvent) e).getWorld().setThundering(t == WeatherType.THUNDER);
			} else {
				if (((ThunderChangeEvent) e).toThunderState() && t != WeatherType.THUNDER)
					((ThunderChangeEvent) e).setCancelled(true);
				if (((ThunderChangeEvent) e).getWorld().hasStorm() != (t != WeatherType.CLEAR))
					((ThunderChangeEvent) e).getWorld().setStorm(t != WeatherType.CLEAR);
			}
		} else {
			for (final World w : worlds.getArray(e)) {
				t.setWeather(w);
			}
		}
	}
	
	@Override
	public Class<WeatherType> getReturnType() {
		return WeatherType.class;
	}
	
	@SuppressWarnings("unchecked")
	@Override
	public boolean setTime(final int time) {
		return super.setTime(time, worlds, WeatherChangeEvent.class, ThunderChangeEvent.class);
	}
	
}
