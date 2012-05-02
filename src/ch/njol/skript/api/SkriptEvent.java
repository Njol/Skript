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

import java.util.regex.Matcher;

import org.bukkit.event.Event;

import ch.njol.skript.Skript;
import ch.njol.skript.api.intern.Expression.ExpressionInfo;
import ch.njol.skript.events.EvtRightclick;

/**
 * A SkriptEvent is like a condition. It is called when any of the registered events occurs.
 * An instance of this class should then check whether the event applies
 * (e.g. the rightclick event is included in the PlayerInteractEvent which also includes lefclicks, thus the SkriptEvent {@link EvtRightclick} checks whether it was a rightclick or
 * not).<br/>
 * It is also needed if the event has parameters.
 * 
 * @author Peter Güttinger
 * @see Skript#addEvent(Class, Class, String...)
 * @see Skript#addEvent(Class, Class[], String...)
 */
public abstract class SkriptEvent implements Debuggable {
	
	public static class SkriptEventInfo extends ExpressionInfo {
		
		public Class<? extends Event>[] events;
		public Class<? extends SkriptEvent> c;
		
		public SkriptEventInfo(final String[] patterns, final Class<? extends SkriptEvent> c, final Class<? extends Event>[] events) {
			super(patterns, null);
			this.c = c;
			this.events = events;
		}
	}
	
	/**
	 * called just after the constructor
	 * 
	 * @param args
	 * @return
	 */
	public abstract void init(final Object[][] args, int matchedPattern, Matcher matcher);
	
	/**
	 * checks whether the given Event applies, e.g. the leftclick event is only part of the PlayerInteractEvent, and this checks whether the player rightclicked or not. This method
	 * will only be called for events this SkriptEvent is registered for.
	 * 
	 * @param e
	 * @return true in most cases.
	 */
	public abstract boolean check(Event e);
	
}
