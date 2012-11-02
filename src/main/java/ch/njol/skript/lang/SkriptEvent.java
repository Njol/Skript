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

import ch.njol.skript.Skript;
import ch.njol.skript.events.EvtClick;
import ch.njol.skript.lang.SkriptParser.ParseResult;

/**
 * A SkriptEvent is like a condition. It is called when any of the registered events occurs.
 * An instance of this class should then check whether the event applies
 * (e.g. the rightclick event is included in the PlayerInteractEvent which also includes lefclicks, thus the SkriptEvent {@link EvtClick} checks whether it was a rightclick or
 * not).<br/>
 * It is also needed if the event has parameters.
 * 
 * @author Peter Güttinger
 * @see Skript#registerEvent(Class, Class, String...)
 * @see Skript#registerEvent(Class, Class[], String...)
 */
public abstract class SkriptEvent implements SyntaxElement, Debuggable {
	private static final long serialVersionUID = -7442359418081996529L;
	
	public static class SkriptEventInfo<E extends SkriptEvent> extends SyntaxElementInfo<E> {
		
		public Class<? extends Event>[] events;
		
		public SkriptEventInfo(final String[] patterns, final Class<E> c, final Class<? extends Event>[] events) throws IllegalArgumentException {
			super(patterns, c);
			this.events = events;
		}
	}
	
	@Override
	public final boolean init(final ch.njol.skript.lang.Expression<?>[] vars, final int matchedPattern, final int isDelayed, final ParseResult parseResult) {
		throw new UnsupportedOperationException();
	}
	
	/**
	 * called just after the constructor
	 * 
	 * @param args
	 */
	public abstract boolean init(final Literal<?>[] args, int matchedPattern, ParseResult parser);
	
	/**
	 * Checks whether the given Event applies, e.g. the leftclick event is only part of the PlayerInteractEvent, and this checks whether the player leftclicked or not. This method
	 * will only be called for events this SkriptEvent is registered for.
	 * 
	 * @param e
	 * @return true if this is SkriptEvent is represented by the Bukkit Event or false if not
	 */
	public abstract boolean check(Event e);
	
}
