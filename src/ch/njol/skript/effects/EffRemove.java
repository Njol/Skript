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
import ch.njol.skript.api.Testable;
import ch.njol.skript.api.exception.ParseException;
import ch.njol.skript.lang.ExprParser.ParseResult;
import ch.njol.skript.lang.Variable;

/**
 * 
 * @author Peter Güttinger
 * 
 */
public class EffRemove extends Effect implements Testable {
	
	static {
		Skript.addEffect(EffRemove.class, "remove %objects% from %objects%");
	}
	
	private Variable<?> removed;
	private Variable<?> remover;
	
	@Override
	public void init(final Variable<?>[] vars, final int matchedPattern, final ParseResult parser) throws ParseException {
		remover = vars[0];
		removed = vars[1];
		Class<?> r = removed.acceptChange(ChangeMode.REMOVE);
		if (r == null) {
			throw new ParseException(removed + " can't have something 'removed' from it");
		}
		boolean single = true;
		if (r.isArray()) {
			single = false;
			r = r.getComponentType();
		}
		if (!r.isAssignableFrom(remover.getReturnType())) {
			final Variable<?> v = remover.getConvertedVariable(r);
			if (v == null) {
				throw new ParseException(remover + " can't be removed from " + removed);
			}
			remover = v;
		}
		if (!remover.isSingle() && single) {
			throw new ParseException("only one " + Skript.getExactClassName(r) + " can be removed from " + removed + ", but multiple are given");
		}
	}
	
	@Override
	protected void execute(final Event e) {
		removed.change(e, remover, ChangeMode.REMOVE);
	}
	
	@Override
	public String getDebugMessage(final Event e) {
		return "remove " + remover.getDebugMessage(e) + " from " + removed.getDebugMessage(null);
	}
	
	@Override
	public boolean test(final Event e) {
		// FIXME test changers
		return false;
	}
	
}
