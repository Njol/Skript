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

package ch.njol.skript.events;

import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

import ch.njol.skript.lang.Literal;
import ch.njol.skript.lang.SkriptEvent;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.skript.lang.Trigger;

/**
 * @author Peter Güttinger
 */
public class EvtServerStart extends SkriptEvent {
	
	public static final class ServerLoadEvent extends Event {
		
		// Bukkit stuff
		private static final HandlerList handlers = new HandlerList();
		
		@Override
		public HandlerList getHandlers() {
			return handlers;
		}
		
		public static HandlerList getHandlerList() {
			return handlers;
		}
	}
	
	static {
		// TODO think about this...
//		Skript.registerEvent(EvtServerStart.class, ServerLoadEvent.class, false, "[server] (start|load)");
	}
	
	@Override
	public String toString(final Event e, final boolean debug) {
		return "server start";
	}
	
	@Override
	public boolean init(final Literal<?>[] args, final int matchedPattern, final ParseResult parser) {
		return true;
	}
	
	@Override
	public boolean check(final Event e) {
		throw new UnsupportedOperationException();
	}
	
	@Override
	public void register(final Trigger t) {
		
	}
	
	@Override
	public void unregister() {}
	
}
