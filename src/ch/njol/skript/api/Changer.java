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

package ch.njol.skript.api;

import org.bukkit.event.Event;

import ch.njol.skript.data.DefaultChangers;
import ch.njol.skript.lang.Variable;

/**
 * An interface to declare changeable values. All Variables implement something similar like this by default, but refuse any change if {@link Variable#acceptChange(ChangeMode)} 
 * isn't overridden.<br/>
 * <br/>
 * Some useful Changers can be found in {@link DefaultChangers}
 * 
 * @author Peter Güttinger
 * @see DefaultChangers
 * @see Variable
 */
public interface Changer<T> {
	
	public static enum ChangeMode {
		ADD, SET, REMOVE, CLEAR;
	}
	
	/**
	 * 
	 * @param e
	 * @param what The variable to change
	 * @param delta A variable which returns instances of the class returned by {@link #acceptChange(ChangeMode)} for the given changemode.
	 * @param mode
	 * 
	 * @throws UnsupportedOperationException (optional) if this method was called on an unsupported ChangeMode.
	 */
	public abstract void change(Event e, Variable<T> what, Variable<?> delta, ChangeMode mode);
	
	/**
	 * test whether this changer supports the given mode, and if yes what type it expects the <code>delta</code> to be.
	 * 
	 * @param mode
	 * @return the type that {@link #change(Event, Variable, Variable, ChangeMode)} accepts as it's <code>delta</code> parameter's type param,
	 *         or null if the given mode is not supported. For {@link ChangeMode#CLEAR} this can return any non-null class instance to mark clear as supported.
	 */
	public abstract Class<?> acceptChange(ChangeMode mode);
	
}
