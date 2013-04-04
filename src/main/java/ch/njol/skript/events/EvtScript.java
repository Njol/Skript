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

import ch.njol.skript.Skript;
import ch.njol.skript.events.bukkit.ScriptEvent;
import ch.njol.skript.lang.Literal;
import ch.njol.skript.lang.SelfRegisteringSkriptEvent;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.skript.lang.Trigger;

/**
 * Currently only "on script load/init" which is called once when the script is loaded.
 * 
 * @author Peter Güttinger
 */
@SuppressWarnings("serial")
public class EvtScript extends SelfRegisteringSkriptEvent {
	static {
		Skript.registerEvent("Script Load", EvtScript.class, ScriptEvent.class, "[script] (load|init)")
				.description("Called directly after the trigger is loaded.")
				.examples("")
				.since("2.0");
	}
	
	@Override
	public boolean init(final Literal<?>[] args, final int matchedPattern, final ParseResult parser) {
		return true;
	}
	
	@Override
	public String toString(final Event e, final boolean debug) {
		return "script init";
	}
	
	@Override
	public void register(final Trigger t) {
		t.execute(new ScriptEvent());
	}
	
	@Override
	public void unregister(final Trigger t) {}
	
	@Override
	public void unregisterAll() {}
	
}
