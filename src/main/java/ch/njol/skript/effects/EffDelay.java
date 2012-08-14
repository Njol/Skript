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

import org.bukkit.event.Event;

import ch.njol.skript.Skript;
import ch.njol.skript.lang.Effect;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.skript.util.Timespan;

/**
 * @author Peter Güttinger
 * 
 */
public class EffDelay extends Effect {
	
	static {
		Skript.registerEffect(EffDelay.class, "(wait|halt) [for] %timespan%");
	}
	
	private Expression<Timespan> duration;
	
	@SuppressWarnings("unchecked")
	@Override
	public boolean init(final Expression<?>[] exprs, final int matchedPattern, final boolean isDelayed, final ParseResult parseResult) {
		duration = (Expression<Timespan>) exprs[0];
		return true;
	}
	
	/**
	 * Gets this delay's delay in ticks for the given event
	 * 
	 * @param e
	 * @return the delay or -1 if not applicable
	 */
	public int getDelay(final Event e) {
		final Timespan d = duration.getSingle(e);
		return d == null ? -1 : d.getTicks();
	}
	
	@Override
	public String toString(final Event e, final boolean debug) {
		return "wait for " + duration.toString(e, debug);
	}
	
	@Override
	protected void execute(final Event e) {
		throw new UnsupportedOperationException();
	}
	
}
