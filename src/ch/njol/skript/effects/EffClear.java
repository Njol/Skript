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

import org.bukkit.event.Event;

import ch.njol.skript.Skript;
import ch.njol.skript.api.Changer.ChangeMode;
import ch.njol.skript.api.Effect;
import ch.njol.skript.api.exception.InitException;
import ch.njol.skript.api.exception.ParseException;
import ch.njol.skript.lang.ExprParser.ParseResult;
import ch.njol.skript.lang.SimpleLiteral;
import ch.njol.skript.lang.UnparsedLiteral;
import ch.njol.skript.lang.Variable;

/**
 * 
 * @author Peter Güttinger
 * 
 */
public class EffClear extends Effect {
	
	static {
		Skript.registerEffect(EffClear.class, "(clear|delete) %objectS%");
	}
	
	private Variable<?> cleared;
	
	@Override
	public void init(final Variable<?>[] vars, final int matchedPattern, final ParseResult parser) throws ParseException, InitException {
		cleared = vars[0];
		if (cleared instanceof UnparsedLiteral)
			throw new InitException();
		final Class<?> r = cleared.acceptChange(ChangeMode.CLEAR);
		if (r == null) {
			throw new ParseException(cleared + " can't be cleared/deleted");
		}
	}
	
	public final static class DummyVariable extends SimpleLiteral<Object> {
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
