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
import ch.njol.skript.api.Effect;
import ch.njol.skript.api.exception.InitException;
import ch.njol.skript.api.exception.ParseException;
import ch.njol.skript.lang.ExprParser.ParseResult;
import ch.njol.skript.lang.UnparsedLiteral;
import ch.njol.skript.lang.Variable;

/**
 * @author Peter Güttinger
 * 
 */
public class EffAdd extends Effect {
	
	static {
		Skript.registerEffect(EffAdd.class, "(add|give) %objects% to %objects%", "give %objects% %objects%");
	}
	
	private Variable<?> added;
	private Variable<?> adder;
	
	@Override
	public void init(final Variable<?>[] vars, final int matchedPattern, final ParseResult parser) throws ParseException, InitException {
		if (matchedPattern == 0) {
			adder = vars[0];
			added = vars[1];
		} else {
			adder = vars[1];
			added = vars[0];
		}
		if (added instanceof UnparsedLiteral)
			throw new InitException();
		Class<?> r = added.acceptChange(ch.njol.skript.api.Changer.ChangeMode.ADD);
		if (r == null)
			throw new ParseException(added + " can't have something added to it");
		boolean single = true;
		if (r.isArray()) {
			single = false;
			r = r.getComponentType();
		}
		if (!r.isAssignableFrom(adder.getReturnType())) {
			final Variable<?> v = adder.getConvertedVariable(r);
			if (v == null)
				throw new ParseException(adder + " can't be added to " + added);
			adder = v;
		}
		if (!adder.isSingle() && single) {
			throw new ParseException(added + " can only be set to one " + Skript.getExactClassName(r) + ", but multiple are given");
		}
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
