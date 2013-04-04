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

package ch.njol.skript;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.Event.Result;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.server.ServerCommandEvent;
import org.bukkit.plugin.EventExecutor;

import ch.njol.skript.lang.Trigger;

/**
 * @author Peter Güttinger
 */
public abstract class SkriptEventHandler {
	private SkriptEventHandler() {}
	
	static Map<Class<? extends Event>, List<Trigger>> triggers = new HashMap<Class<? extends Event>, List<Trigger>>();
	
	final static EventExecutor ee = new EventExecutor() {
		@Override
		public void execute(final Listener l, final Event e) {
			check(e);
		}
	};
	
	private static Event last = null;
	
	static void check(final Event e) {
		if (last == e)
			return;
		last = e;
		final List<Trigger> ts = triggers.get(e.getClass());
		if (ts == null)
			return;
		
		if (Skript.logVeryHigh()) {
			boolean hasTrigger = false;
			for (final Trigger t : ts) {
				if (t.getEvent().check(e)) {
					hasTrigger = true;
					break;
				}
			}
			if (!hasTrigger)
				return;
		}
		
		logEventStart(e);
		
		if (e instanceof Cancellable && ((Cancellable) e).isCancelled() &&
				!(e instanceof PlayerInteractEvent && (((PlayerInteractEvent) e).getAction() == Action.LEFT_CLICK_AIR || ((PlayerInteractEvent) e).getAction() == Action.RIGHT_CLICK_AIR) && ((PlayerInteractEvent) e).useItemInHand() != Result.DENY)
				|| e instanceof ServerCommandEvent && (((ServerCommandEvent) e).getCommand() == null || ((ServerCommandEvent) e).getCommand().isEmpty())) {
			if (Skript.logVeryHigh())
				Skript.info(" -x- was cancelled");
			return;
		}
		
		for (final Trigger t : ts) {
			if (!t.getEvent().check(e))
				continue;
			logTriggerStart(t);
			t.execute(e);
			logTriggerEnd(t);
		}
		
		logEventEnd();
	}
	
	private static long startEvent;
	
	public static void logEventStart(final Event e) {
		if (!Skript.logVeryHigh())
			return;
		startEvent = System.nanoTime();
		Skript.info("");
		Skript.info("== " + e.getClass().getName() + " ==");
	}
	
	public static void logEventEnd() {
		if (!Skript.logVeryHigh())
			return;
		Skript.info("== took " + 1. * (System.nanoTime() - startEvent) / 1000000. + " milliseconds ==");
	}
	
	static long startTrigger;
	
	public static void logTriggerStart(final Trigger t) {
		if (!Skript.logVeryHigh())
			return;
		Skript.info("# " + t.getName());
		startTrigger = System.nanoTime();
	}
	
	public static void logTriggerEnd(final Trigger t) {
		if (!Skript.logVeryHigh())
			return;
		Skript.info("# " + t.getName() + " took " + 1. * (System.nanoTime() - startTrigger) / 1000000. + " milliseconds");
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
