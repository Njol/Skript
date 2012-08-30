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
import org.bukkit.event.Event;
import org.bukkit.event.weather.WeatherEvent;

import ch.njol.skript.Skript;
import ch.njol.skript.effects.Delay;
import ch.njol.skript.lang.Condition;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.skript.util.WeatherType;
import ch.njol.util.Checker;

/**
 * @author Peter Güttinger
 */
public class CondWeather extends Condition {
	
	static {
		Skript.registerCondition(CondWeather.class, "is %weathertypes% [in %worlds%]");
	}
	
	private Expression<WeatherType> weathers;
	private Expression<World> worlds;
	
	@SuppressWarnings("unchecked")
	@Override
	public boolean init(final Expression<?>[] vars, final int matchedPattern, final int isDelayed, final ParseResult parser) {
		weathers = (Expression<WeatherType>) vars[0];
		worlds = (Expression<World>) vars[1];
		return true;
	}
	
	@Override
	public boolean check(final Event e) {
		if (worlds.isDefault() && e instanceof WeatherEvent && !Delay.isDelayed(e)) {
			return weathers.check(e, new Checker<WeatherType>() {
				@Override
				public boolean check(final WeatherType t) {
					return t == WeatherType.fromEvent((WeatherEvent) e);
				}
			}, this);
		}
		return weathers.check(e, new Checker<WeatherType>() {
			@Override
			public boolean check(final WeatherType wt) {
				return worlds.check(e, new Checker<World>() {
					@Override
					public boolean check(final World w) {
						return wt.isWeather(w);
					}
				});
			}
		}, this);
	}
	
	@Override
	public String toString(final Event e, final boolean debug) {
		return "is " + weathers.toString(e, debug) + " in " + worlds.toString(e, debug);
	}
	
}
