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

package ch.njol.skript.variables.base;

import java.lang.reflect.Array;
import java.util.regex.Matcher;

import org.bukkit.event.Event;

import ch.njol.skript.Skript;
import ch.njol.skript.api.Changer;
import ch.njol.skript.api.Changer.ChangeMode;
import ch.njol.skript.api.Parser;
import ch.njol.skript.api.intern.Variable;

/**
 * A useful class for creating default variables. It simply returns the event value of the given type.
 * 
 * @author Peter Güttinger
 * @see Skript#addClass(String, Class, Class, Parser, String...)
 */
public abstract class EventValueVariable<T> extends Variable<T> {
	
	private final Class<T> c;
	private final Changer<T> changer;
	
	public EventValueVariable(final Class<T> c) {
		this.c = c;
		changer = null;
	}
	
	public EventValueVariable(final Class<T> c, final Changer<T> changer) {
		this.c = c;
		this.changer = changer;
	}
	
	@SuppressWarnings("unchecked")
	@Override
	protected final T[] getAll(final Event e) {
		final T[] t = (T[]) Array.newInstance(c, 1);
		t[0] = Skript.getEventValue(e, c);
		return t;
	}
	
	@Override
	public void init(final Variable<?>[] vars, final int matchedPattern, final Matcher matcher) {}
	
	@Override
	public Class<T> getReturnType() {
		return c;
	}
	
	@Override
	public String getDebugMessage(final Event e) {
		if (e == null)
			return "event-" + c.getName();
		return Skript.toString(Skript.getEventValue(e, c));
	}
	
	@Override
	public Class<?> acceptChange(final ChangeMode mode) {
		if (changer == null)
			return null;
		return changer.acceptChange(mode);
	}
	
	@Override
	public void change(final Event e, final Variable<?> delta, final ChangeMode mode) {
		if (changer == null)
			throw new UnsupportedOperationException();
		changer.change(e, this, delta, mode);
	}
	
	@Override
	public String toString() {
		return "event-" + c.getSimpleName();
	}
}
