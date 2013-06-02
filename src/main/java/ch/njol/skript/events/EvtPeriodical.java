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

import java.io.IOException;
import java.io.ObjectInputStream;

import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.event.Event;

import ch.njol.skript.Skript;
import ch.njol.skript.SkriptEventHandler;
import ch.njol.skript.events.bukkit.ScheduledEvent;
import ch.njol.skript.events.bukkit.ScheduledWorldEvent;
import ch.njol.skript.lang.Literal;
import ch.njol.skript.lang.SelfRegisteringSkriptEvent;
import ch.njol.skript.lang.SkriptEventInfo;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.skript.lang.Trigger;
import ch.njol.skript.util.Timespan;

/**
 * @author Peter Güttinger
 */
@SuppressWarnings("serial")
public class EvtPeriodical extends SelfRegisteringSkriptEvent {
	static {
		Skript.registerEvent("*Periodical", EvtPeriodical.class, ScheduledEvent.class, "every %timespan%")
				.description(SkriptEventInfo.NO_DOC);
		Skript.registerEvent("*Periodical", EvtPeriodical.class, ScheduledWorldEvent.class, "every %timespan% in [world[s]] %worlds%")
				.description("An event that is called periodically. The event is used like 'every &lt;<a href='../classes/#timespan'>timespan</a>&gt;', e.g. 'every second' or 'every 5 minutes'.")
				.examples("every second", "every minecraft hour", "every tick # warning: lag!", "every minecraft day in \"world\"")
				.since("1.0");
	}
	
	private Timespan period;
	
	private Trigger t;
	
	private int[] taskIDs;
	
	private transient World[] worlds;
	private String[] worldNames;
	
	@SuppressWarnings("unchecked")
	@Override
	public boolean init(final Literal<?>[] args, final int matchedPattern, final ParseResult parser) {
		period = ((Literal<Timespan>) args[0]).getSingle();
		if (args.length > 1 && args[1] != null) {
			worlds = ((Literal<World>) args[1]).getArray();
			worldNames = new String[worlds.length];
			for (int i = 0; i < worlds.length; i++)
				worldNames[i] = worlds[i].getName();
		}
		return true;
	}
	
	private void execute(final World w) {
		final ScheduledEvent e = w == null ? new ScheduledEvent() : new ScheduledWorldEvent(w);
		SkriptEventHandler.logEventStart(e);
		SkriptEventHandler.logTriggerStart(t);
		t.execute(e);
		SkriptEventHandler.logTriggerEnd(t);
		SkriptEventHandler.logEventEnd();
	}
	
	private void readObject(final ObjectInputStream in) throws ClassNotFoundException, IOException {
		in.defaultReadObject();
		if (worldNames != null) {
			worlds = new World[worldNames.length];
			for (int i = 0; i < worlds.length; i++) {
				if ((worlds[i] = Bukkit.getWorld(worldNames[i])) == null)
					throw new IOException();
			}
		}
	}
	
	@Override
	public void register(final Trigger t) {
		this.t = t;
		if (worlds == null) {
			taskIDs = new int[] {Bukkit.getScheduler().scheduleSyncRepeatingTask(Skript.getInstance(), new Runnable() {
				@Override
				public void run() {
					execute(null);
				}
			}, period.getTicks(), period.getTicks())};
		} else {
			taskIDs = new int[worlds.length];
			for (int i = 0; i < worlds.length; i++) {
				final World w = worlds[i];
				taskIDs[i] = Bukkit.getScheduler().scheduleSyncRepeatingTask(Skript.getInstance(), new Runnable() {
					@Override
					public void run() {
						execute(w);
					}
				}, period.getTicks() - (w.getFullTime() % period.getTicks()), period.getTicks());
			}
		}
	}
	
	@Override
	public void unregister(final Trigger t) {
		for (final int taskID : taskIDs)
			Bukkit.getScheduler().cancelTask(taskID);
	}
	
	@Override
	public void unregisterAll() {
		for (final int taskID : taskIDs)
			Bukkit.getScheduler().cancelTask(taskID);
	}
	
	@Override
	public String toString(final Event e, final boolean debug) {
		return "every " + period;
	}
	
}
