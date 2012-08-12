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

package ch.njol.skript.lang;

import org.bukkit.event.Event;

/**
 * Used for debugging.
 * 
 * @author Peter Güttinger
 */
public interface Debuggable {
	
	/**
	 * @param e The event to get information to. This is always null if debug = false.
	 * @param debug If true this should print more information, if false this should print what is shown to the end user
	 * @return string representation of this object
	 */
	public String toString(Event e, boolean debug);
	
	/**
	 * Should return {@link #toString(Event, boolean) toString(null, false)}
	 */
	@Override
	public String toString();
	
}
