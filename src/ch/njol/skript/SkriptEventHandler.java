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

package ch.njol.skript;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.plugin.EventExecutor;

import ch.njol.skript.api.intern.Trigger;

/**
 * @author Peter Güttinger
 * 
 */
abstract class SkriptEventHandler {
	private SkriptEventHandler() {}
	
	static Map<Class<? extends Event>, List<Trigger>> triggers = new HashMap<Class<? extends Event>, List<Trigger>>();
	
	public final static EventExecutor ee = new EventExecutor() {
		@Override
		public void execute(final Listener l, final Event e) {
			check(e);
		}
	};
	
	static void check(final Event e) {
		if (!Skript.listenerEnabled)
			return;
		final List<Trigger> ts = triggers.get(e.getClass());
		if (ts == null)
			return;
		final long startEvent = System.nanoTime();
		if (Skript.log(Verbosity.VERY_HIGH)) {
			Skript.info("");
			Skript.info("== " + e.getClass().getName() + " ==");
		}
		if (e instanceof Cancellable && !(e instanceof PlayerInteractEvent && ((PlayerInteractEvent) e).getAction() == Action.RIGHT_CLICK_AIR) && ((Cancellable) e).isCancelled()) {
			if (Skript.log(Verbosity.VERY_HIGH))
				Skript.info(" -x- was cancelled");
			return;
		}
		for (final Trigger t : ts) {
			if (!t.getEvent().check(e))
				continue;
			if (Skript.log(Verbosity.VERY_HIGH))
				Skript.info("# " + t.getName());
			final long startTrigger = System.nanoTime();
			t.run(e);
			if (Skript.log(Verbosity.VERY_HIGH))
				Skript.info("# " + t.getName() + " took " + 1. * (System.nanoTime() - startTrigger) / 1000000. + " milliseconds");
		}
		// in case it got forgotten somewhere (you must not rely on this, as you will disable Skript's listener for all events triggered by any effects/conditions following yours!)
		Skript.enableListener();
		
		if (Skript.log(Verbosity.VERY_HIGH))
			Skript.info("== took " + 1. * (System.nanoTime() - startEvent) / 1000000. + " milliseconds ==");
	}
	
	static void addTrigger(final Class<? extends Event>[] events, final Trigger trigger) {
		for (final Class<? extends Event> e : events) {
			List<Trigger> ts = triggers.get(e);
			if (ts == null)
				triggers.put(e, ts = new ArrayList<Trigger>());
			ts.add(trigger);
		}
	}
	
}
