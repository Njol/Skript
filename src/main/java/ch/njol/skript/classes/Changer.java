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
 * Copyright 2011-2013 Peter Güttinger
 * 
 */

package ch.njol.skript.classes;

import org.bukkit.event.Event;

import ch.njol.skript.classes.data.DefaultChangers;
import ch.njol.skript.lang.Expression;

/**
 * An interface to declare changeable values. All Expressions implement something similar like this by default, but refuse any change if {@link Expression#acceptChange(ChangeMode)}
 * isn't overridden.<br/>
 * <br/>
 * Some useful Changers can be found in {@link DefaultChangers}
 * 
 * @author Peter Güttinger
 * @see DefaultChangers
 * @see Expression
 */
public interface Changer<T, V> {
	
	public static enum ChangeMode {
		ADD, SET, REMOVE, REMOVE_ALL, DELETE, RESET;
	}
	
	/**
	 * Test whether this changer supports the given mode, and if yes what type it expects the <code>delta</code> to be.
	 * <p>
	 * Unlike {@link Expression#acceptChange(ChangeMode)} this method must not print errors
	 * 
	 * @param mode
	 * @return An array of types that {@link #change(Event, Expression, Expression, ChangeMode)} accepts as it's <code>delta</code> parameter's type param,
	 *         or null if the given mode is not supported. For {@link ChangeMode#DELETE} and {@link ChangeMode#RESET} this can return any non-null array to mark them as supported.
	 */
	public abstract Class<? extends V>[] acceptChange(ChangeMode mode);
	
	/**
	 * @param e The objects to change
	 * @param delta An instance of one of the the classes returned by {@link #acceptChange(ChangeMode)} for the given change mode (null for {@link ChangeMode#DELETE} and
	 *            {@link ChangeMode#RESET})
	 * @param mode
	 * @throws UnsupportedOperationException (optional) if this method was called on an unsupported ChangeMode.
	 */
	public abstract void change(T[] what, V delta, ChangeMode mode);
	
	public static abstract class ChangerUtils {
		
		@SuppressWarnings("unchecked")
		public static final <T, V> void change(final Changer<T, V> changer, final Object[] what, final Object delta, final ChangeMode mode) {
			changer.change((T[]) what, (V) delta, mode);
		}
		
	}
	
}
