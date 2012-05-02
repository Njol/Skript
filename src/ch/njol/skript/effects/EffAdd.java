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
import ch.njol.skript.api.Effect;
import ch.njol.skript.api.exception.ParseException;
import ch.njol.skript.api.intern.Variable;

/**
 * @author Peter Güttinger
 * 
 */
public class EffAdd extends Effect {
	
	static {
		Skript.addEffect(EffAdd.class, "(add|give) %object% to %object%", "give %object% %object%");
	}
	
	private Variable<?> added;
	private Variable<?> adder;
	
	@Override
	public void init(final Variable<?>[] vars, final int matchedPattern, final Matcher matcher) throws ParseException {
		if (matchedPattern == 0) {
			adder = vars[0];
			added = vars[1];
		} else {
			adder = vars[1];
			added = vars[0];
		}
		final Class<?> r = added.acceptChange(ch.njol.skript.api.Changer.ChangeMode.ADD);
		if (r == null)
			throw new ParseException(added + " can't have something added to it");
		if (r.isAssignableFrom(adder.getReturnType()))
			return;
		final Variable<?> v = adder.getConvertedVariable(r);
		if (v == null)
			throw new ParseException(adder + " can't be added to " + added);
		adder = v;
	}
	
	@Override
	protected void execute(final Event e) {
		added.change(e, adder, ch.njol.skript.api.Changer.ChangeMode.ADD);
	}
	
	@Override
	public String getDebugMessage(final Event e) {
		return "add " + adder.getDebugMessage(e) + " to " + added.getDebugMessage(null);
	}
	
}
