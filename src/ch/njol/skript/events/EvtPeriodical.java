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

package ch.njol.skript.events;

import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.event.Event;

import ch.njol.skript.Skript;
import ch.njol.skript.api.SkriptEvent;
import ch.njol.skript.lang.ExprParser.ParseResult;
import ch.njol.skript.lang.Literal;
import ch.njol.skript.util.ScheduledEvent;
import ch.njol.skript.util.Timespan;

public class EvtPeriodical extends SkriptEvent {
	
	static {
		Skript.addEvent(EvtPeriodical.class, ScheduledEvent.class, "every %timespan% [in [world[s]] %worlds%]");
	}
	
	private int period;
	
	@SuppressWarnings("unchecked")
	@Override
	public void init(final Literal<?>[] args, final int matchedPattern, final ParseResult parser) {
		period = Math.round(((Literal<Timespan>) args[0]).getSingle().getTicks());
		final World[] worlds = args[1] == null ? null : ((Literal<World>) args[1]).getArray();
		final EvtPeriodical evt = this;
		if (worlds == null) {
			Bukkit.getScheduler().scheduleSyncRepeatingTask(Skript.getInstance(), new Runnable() {
				@Override
				public void run() {
					Bukkit.getPluginManager().callEvent(new ScheduledEvent(null, evt));
				}
			}, period, period);
		} else {
			for (final World w : worlds) {
				Bukkit.getScheduler().scheduleSyncRepeatingTask(Skript.getInstance(), new Runnable() {
					@Override
					public void run() {
						Bukkit.getPluginManager().callEvent(new ScheduledEvent(w, evt));
					}
				}, period - (w.getFullTime() % period), period);
			}
		}
	}
	
	@Override
	public boolean check(final Event e) {
		return ((ScheduledEvent) e).getSkriptEvent() == this;
	}
	
	@Override
	public String getDebugMessage(final Event e) {
		return "periodical " + period;
	}
	
}
