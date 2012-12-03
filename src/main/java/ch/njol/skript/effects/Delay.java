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

import java.util.Collections;
import java.util.Set;
import java.util.WeakHashMap;

import org.bukkit.Bukkit;
import org.bukkit.event.Event;

import ch.njol.skript.Skript;
import ch.njol.skript.lang.Effect;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.skript.lang.TriggerItem;
import ch.njol.skript.util.Timespan;
import ch.njol.util.Kleenean;

/**
 * @author Peter Güttinger
 */
public class Delay extends Effect {
	
	private static final long serialVersionUID = -6754842737504578386L;
	
	static {
		Skript.registerEffect(Delay.class, "(wait|halt) [for] %timespan%");
	}
	
	private Expression<Timespan> duration;
	
	@SuppressWarnings("unchecked")
	@Override
	public boolean init(final Expression<?>[] exprs, final int matchedPattern, final Kleenean isDelayed, final ParseResult parseResult) {
		duration = (Expression<Timespan>) exprs[0];
		return true;
	}
	
	@Override
	protected TriggerItem walk(final Event e) {
		debug(e, true);
		final long start = System.nanoTime();
		if (getNext() != null) {
			delayed.add(e);
			final Timespan d = duration.getSingle(e);
			if (d == null)
				return null;
			Bukkit.getScheduler().scheduleSyncDelayedTask(Skript.getInstance(), new Runnable() {
				@Override
				public void run() {
					if (Skript.debug())
						Skript.info(getIndentation() + "... continuing after " + (System.nanoTime() - start) / 1000000000. + "s");
					TriggerItem.walk(getNext(), e);
				}
			}, d.getTicks());
		}
		return null;
	}
	
	private final static Set<Event> delayed = Collections.newSetFromMap(new WeakHashMap<Event, Boolean>());
	
	public final static boolean isDelayed(final Event e) {
		return delayed.contains(e);
	}
	
	@Override
	protected void execute(final Event e) {
		throw new UnsupportedOperationException();
	}
	
	@Override
	public String toString(final Event e, final boolean debug) {
		return "wait for " + duration.toString(e, debug) + (e == null ? "" : "...");
	}
	
}
