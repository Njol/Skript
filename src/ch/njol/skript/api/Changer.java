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
import ch.njol.skript.lang.SimpleVariable;
import ch.njol.skript.lang.Variable;

/**
 * An interface to declare changeable values. All Variables implement this by default, but refuse any change if {@link #acceptChange(ChangeMode)} isn't overridden.<br/>
 * <br/>
 * Some useful Changers can be found in {@link DefaultChangers}
 * 
 * @author Peter Güttinger
 * @see DefaultChangers
 * @see SimpleVariable
 */
public interface Changer<T> {
	
	public static enum ChangeMode {
		ADD, SET, REMOVE, CLEAR;
	}
	
	/**
	 * 
	 * @param what What to change. Can contain null elements.
	 * @param delta An array of the type accepted by {@link #acceptChange(ChangeMode, Class)}. Note: if Integer.class was accepted this array's class will be Integer[], not
	 *            Object[]. Can contain null elements.
	 * @param mode
	 */
	public abstract void change(Event e, Variable<T> what, Variable<?> delta, ChangeMode mode);
	
	public abstract Class<?> acceptChange(ChangeMode mode);
	
}
