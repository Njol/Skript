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

package ch.njol.skript.effects;

import java.util.regex.Matcher;

import org.bukkit.event.Event;

import ch.njol.skript.Skript;
import ch.njol.skript.api.Changer.ChangeMode;
import ch.njol.skript.api.Effect;
import ch.njol.skript.api.exception.ParseException;
import ch.njol.skript.api.intern.Variable;

/**
 * 
 * @author Peter Güttinger
 * 
 */
public class EffSet extends Effect {
	
	static {
		Skript.addEffect(EffSet.class, "set %object% to %object%");
	}
	
	private Variable<?> setter;
	private Variable<?> setted;
	
	@Override
	public void init(final Variable<?>[] vars, final int matchedPattern, final Matcher matcher) throws ParseException {
		setted = vars[0];
		setter = vars[1];
		final Class<?> r = setted.acceptChange(ch.njol.skript.api.Changer.ChangeMode.SET);
		if (r == null) {
			throw new ParseException(setted + " can't be set");
		}
		if (r.isAssignableFrom(setter.getReturnType()))
			return;
		final Variable<?> v = setter.getConvertedVariable(r);
		if (v == null) {
			throw new ParseException(setted + " can't be set to " + setter);
		}
		setter = v;
	}
	
	@Override
	protected void execute(final Event e) {
		setted.change(e, setter, ChangeMode.SET);
	}
	
	@Override
	public String getDebugMessage(final Event e) {
		return "set " + setted.getDebugMessage(null) + " to " + setter.getDebugMessage(e);
	}
	
}
