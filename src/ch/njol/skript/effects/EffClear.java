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
import ch.njol.skript.api.intern.Literal;
import ch.njol.skript.api.intern.Variable;

/**
 * 
 * @author Peter Güttinger
 * 
 */
public class EffClear extends Effect {
	
	static {
		Skript.addEffect(EffClear.class, "(clear|delete) %object%");
	}
	
	private Variable<?> cleared;
	
	@Override
	public void init(final Variable<?>[] vars, final int matchedPattern, final Matcher matcher) throws ParseException {
		cleared = vars[0];
		final Class<?> r = cleared.acceptChange(ChangeMode.CLEAR);
		if (r == null) {
			throw new ParseException(cleared + " can't be cleared/deleted");
		}
	}
	
	public final static class DummyVariable extends Literal<Object> {
		public DummyVariable() {
			super(new Object[] {null}, Object.class, true);
		}
	}
	
	@Override
	protected void execute(final Event e) {
		cleared.change(e, new DummyVariable(), ChangeMode.CLEAR);
	}
	
	@Override
	public String getDebugMessage(final Event e) {
		return "clear " + cleared.getDebugMessage(null);
	}
	
}
