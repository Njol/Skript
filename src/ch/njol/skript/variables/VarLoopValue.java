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

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.bukkit.event.Event;

import ch.njol.skript.Skript;
import ch.njol.skript.TriggerFileLoader;
import ch.njol.skript.api.LoopVar;
import ch.njol.skript.api.exception.ParseException;
import ch.njol.skript.api.intern.ConvertedVariable;
import ch.njol.skript.api.intern.Variable;
import ch.njol.skript.variables.base.VarVariable;

/**
 * used to access a loop's current value.
 * 
 * @author Peter Güttinger
 * 
 */
public class VarLoopValue extends VarVariable<Object> {
	
	static {
		Skript.addVariable(VarLoopValue.class, Object.class, "loop-(\\S+)");
	}
	
	private String name;
	
	@Override
	public void init(final Variable<?>[] vars, final int matchedPattern, final Matcher matcher) throws ParseException {
		name = matcher.group();
		String s = matcher.group(1);
		int i = 1;
		final Matcher m = Pattern.compile("^(.+)-(\\d+)$").matcher(matcher.group(1));
		if (m.matches()) {
			s = m.group(1);
			i = Integer.parseInt(m.group(2));
		}
		for (final LoopVar<?> v : TriggerFileLoader.currentLoops) {
			if (v.isLoopOf(s)) {
				if (i > 1) {
					i--;
					continue;
				}
				var = v;
				return;
			}
		}
		throw new ParseException("there's no loop that matches " + matcher.group());
	}
	
	// this is to keep "loop-xyz" as debug message and not switch back to the loopvar's debug message
	// which is intended to be used only as debug message of the loop.
	@Override
	public <R> ConvertedVariable<? extends R> getConvertedVar(final Class<R> to) {
		final Variable<?> siht = this;
		final Variable<? extends R> v = var.getConvertedVariable(to);
		if (v == null)
			return null;
		return new ConvertedVariable<R>(v, to) {
			@Override
			public String getDebugMessage(final Event e) {
				return "{" + siht.getDebugMessage(e) + "}->" + to.getName();
			}
			
			@Override
			protected R[] getAll(final Event e) {
				return v.get(e);
			}
		};
	}
	
	@Override
	public String getDebugMessage(final Event e) {
		if (e == null)
			return name;
		return var.getDebugMessage(e);
	}
	
}
