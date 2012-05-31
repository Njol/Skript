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

package ch.njol.skript.variables;

import org.bukkit.World;
import org.bukkit.event.Event;

import ch.njol.skript.Skript;
import ch.njol.skript.api.Changer.ChangeMode;
import ch.njol.skript.api.Getter;
import ch.njol.skript.api.exception.InitException;
import ch.njol.skript.api.exception.ParseException;
import ch.njol.skript.lang.ExprParser.ParseResult;
import ch.njol.skript.lang.SimpleVariable;
import ch.njol.skript.lang.Variable;
import ch.njol.skript.util.WeatherType;

/**
 * @author Peter Güttinger
 * 
 */
public class VarWeather extends SimpleVariable<WeatherType> {
	
	static {
		Skript.registerVariable(VarWeather.class, WeatherType.class, "weather [(in|of) %worlds%]");
	}
	
	private Variable<World> worlds;
	
	@SuppressWarnings("unchecked")
	@Override
	public void init(final Variable<?>[] vars, final int matchedPattern, final ParseResult parser) throws InitException, ParseException {
		worlds = (Variable<World>) vars[0];
	}
	
	@Override
	public String getDebugMessage(final Event e) {
		return "weather in " + worlds.getDebugMessage(e);
	}
	
	@Override
	protected WeatherType[] getAll(final Event e) {
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
	public void change(final Event e, final Variable<?> delta, final ChangeMode mode) {
		switch (mode) {
			case CLEAR:
				for (final World w : worlds.getArray(e)) {
					w.setStorm(false);
					w.setThundering(false);
				}
			break;
			case SET:
				final WeatherType t = (WeatherType) delta.getSingle(e);
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
	
	@Override
	public String toString() {
		return "the weather in " + worlds;
	}
	
	@Override
	public boolean isSingle() {
		return worlds.isSingle();
	}
	
}
