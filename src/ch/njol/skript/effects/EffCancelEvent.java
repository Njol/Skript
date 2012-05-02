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

package ch.njol.skript.effects;

import java.util.regex.Matcher;

import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;

import ch.njol.skript.Skript;
import ch.njol.skript.api.Effect;
import ch.njol.skript.api.intern.Variable;

/**
 * 
 * @author Peter Güttinger
 * 
 */
public class EffCancelEvent extends Effect {
	
	static {
		Skript.addEffect(EffCancelEvent.class, "cancel event", "uncancel event");
	}
	
	private boolean cancel;
	
	@Override
	public void init(final Variable<?>[] vars, final int matchedPattern, final Matcher matcher) {
		cancel = matchedPattern == 0;
	}
	
	@Override
	public void execute(final Event e) {
		if (e instanceof Cancellable)
			((Cancellable) e).setCancelled(cancel);
	}
	
	@Override
	public String getDebugMessage(final Event e) {
		return (cancel ? "" : "un") + "cancel event";
	}
	
}
