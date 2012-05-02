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

import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;

import ch.njol.skript.Skript;
import ch.njol.skript.api.Changer.ChangeMode;
import ch.njol.skript.api.intern.Variable;

/**
 * @author Peter Güttinger
 * 
 */
public class VarEventCancelled extends Variable<Boolean> {
	
	static {
		Skript.addVariable(VarEventCancelled.class, Boolean.class, "event cancelled");
	}
	
	@Override
	protected Boolean[] getAll(final Event e) {
		if (!(e instanceof Cancellable))
			return null;
		return new Boolean[] {((Cancellable) e).isCancelled()};
	}
	
	@Override
	public void init(final Variable<?>[] vars, final int matchedPattern, final Matcher matcher) {}
	
	@Override
	public Class<? extends Boolean> getReturnType() {
		return Boolean.class;
	}
	
	@Override
	public String getDebugMessage(final Event e) {
		return "event cancelled";
	}
	
	@Override
	public Class<?> acceptChange(final ChangeMode mode) {
		switch (mode) {
			case CLEAR:
			case SET:
				return Boolean.class;
			default:
				return null;
		}
	}
	
	@SuppressWarnings("incomplete-switch")
	@Override
	public void change(final Event e, final Variable<?> delta, final ChangeMode mode) {
		if (!(e instanceof Cancellable))
			return;
		switch (mode) {
			case CLEAR:
				((Cancellable) e).setCancelled(false);
			break;
			case SET:
				((Cancellable) e).setCancelled((Boolean) delta.getFirst(e));
		}
	}
	
	@Override
	public String toString() {
		return "the cancelled state of the event";
	}
	
}
