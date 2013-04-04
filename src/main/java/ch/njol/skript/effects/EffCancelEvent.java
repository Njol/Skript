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

package ch.njol.skript.effects;

import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;

import ch.njol.skript.ScriptLoader;
import ch.njol.skript.Skript;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Examples;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import ch.njol.skript.lang.Effect;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.skript.log.ErrorQuality;
import ch.njol.util.Kleenean;

/**
 * @author Peter Güttinger
 */
@SuppressWarnings("serial")
@Name("Cancel Event")
@Description("Cancels the event (e.g. prevent blocks from being placed, or damage being taken).")
@Examples({"on damage:",
		"	victim is a player",
		"	victim has the permission \"skript.god\"",
		"	cancel the event"})
@Since("1.0")
public class EffCancelEvent extends Effect {
	
	static {
		Skript.registerEffect(EffCancelEvent.class, "cancel [the] event");//, "uncancel event");
	}
	
	private boolean cancel;
	
	@Override
	public boolean init(final Expression<?>[] vars, final int matchedPattern, final Kleenean isDelayed, final ParseResult parser) {
		if (isDelayed == Kleenean.TRUE) {
			Skript.error("Can't cancel an event anymore after is has already passed", ErrorQuality.SEMANTIC_ERROR);
			return false;
		}
		cancel = matchedPattern == 0;
		for (final Class<? extends Event> e : ScriptLoader.currentEvents) {
			if (Cancellable.class.isAssignableFrom(e))
				return true;
		}
		Skript.error("This event can't be cancelled", ErrorQuality.SEMANTIC_ERROR);
		return false;
	}
	
	@Override
	public void execute(final Event e) {
		if (e instanceof Cancellable)
			((Cancellable) e).setCancelled(cancel);
	}
	
	@Override
	public String toString(final Event e, final boolean debug) {
		return (cancel ? "" : "un") + "cancel event";
	}
	
}
