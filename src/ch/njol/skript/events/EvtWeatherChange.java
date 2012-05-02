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

package ch.njol.skript.events;

import java.util.regex.Matcher;

import org.bukkit.event.Event;
import org.bukkit.event.weather.ThunderChangeEvent;
import org.bukkit.event.weather.WeatherChangeEvent;

import ch.njol.skript.Skript;
import ch.njol.skript.api.SkriptEvent;
import ch.njol.skript.util.Utils;
import ch.njol.skript.util.WeatherType;

/**
 * @author Peter Güttinger
 * 
 */
@SuppressWarnings("unchecked")
public class EvtWeatherChange extends SkriptEvent {
	
	static {
		Skript.addEvent(EvtWeatherChange.class, Skript.array(WeatherChangeEvent.class, ThunderChangeEvent.class), "weather change( to %weathertype%)?");
	}
	
	private WeatherType[] types;
	
	@Override
	public void init(final Object[][] args, final int matchedPattern, final Matcher matcher) {
		types = (WeatherType[]) args[0];
	}
	
	@Override
	public boolean check(final Event e) {
		final boolean rain = e instanceof WeatherChangeEvent ? ((WeatherChangeEvent) e).toWeatherState() : ((ThunderChangeEvent) e).getWorld().hasStorm();
		final boolean thunder = e instanceof ThunderChangeEvent ? ((ThunderChangeEvent) e).toThunderState() : ((WeatherChangeEvent) e).getWorld().isThundering();
		for (final WeatherType t : types) {
			if (t.isWeather(rain, thunder))
				return true;
		}
		return false;
	}
	
	@Override
	public String getDebugMessage(final Event e) {
		return "weather change to " + Utils.join(types);
	}
	
}
