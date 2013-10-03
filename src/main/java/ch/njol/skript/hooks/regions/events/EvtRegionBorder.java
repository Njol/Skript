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

package ch.njol.skript.hooks.regions.events;

import java.util.ArrayList;
import java.util.Collection;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.event.Event;
import org.bukkit.event.EventException;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.plugin.EventExecutor;

import ch.njol.skript.Skript;
import ch.njol.skript.SkriptConfig;
import ch.njol.skript.hooks.regions.RegionsPlugin;
import ch.njol.skript.hooks.regions.classes.Region;
import ch.njol.skript.lang.Literal;
import ch.njol.skript.lang.SelfRegisteringSkriptEvent;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.skript.lang.Trigger;
import ch.njol.util.Checker;

/**
 * @author Peter Güttinger
 */
@SuppressWarnings("serial")
public class EvtRegionBorder extends SelfRegisteringSkriptEvent {
	static {
		Skript.registerEvent("Region Enter/Leave", EvtRegionBorder.class, RegionBorderEvent.class,
				"(0¦enter[ing]|1¦leav(e|ing)|1¦exit[ing]) [of] (a region|[[the] region[s]] %-regions%)");
	}
	
	private boolean enter;
	private Literal<Region> regions;
	
	@SuppressWarnings("unchecked")
	@Override
	public boolean init(final Literal<?>[] args, final int matchedPattern, final ParseResult parseResult) {
		enter = parseResult.mark == 0;
		regions = (Literal<Region>) args[0];
		return true;
	}
	
	@Override
	public String toString(final Event e, final boolean debug) {
		return (enter ? "enter" : "leave") + " of " + regions.toString(e, debug);
	}
	
	private final static Collection<Trigger> triggers = new ArrayList<Trigger>();
	
	@Override
	public void register(final Trigger t) {
		triggers.add(t);
		register();
	}
	
	@Override
	public void unregister(final Trigger t) {
		triggers.remove(t);
	}
	
	@Override
	public void unregisterAll() {
		triggers.clear();
	}
	
	private boolean applies(final Event e) {
		assert e instanceof RegionBorderEvent;
		if (enter != ((RegionBorderEvent) e).enter)
			return false;
		if (regions == null)
			return true;
		final Region re = ((RegionBorderEvent) e).region;
		return regions.check(e, new Checker<Region>() {
			@Override
			public boolean check(final Region r) {
				return r.equals(re);
			}
		});
	}
	
	private final static void callEvent(final Region r, final boolean enter) {
		final RegionBorderEvent e = new RegionBorderEvent(r, enter);
		for (final Trigger t : triggers) {
			if (((EvtRegionBorder) t.getEvent()).applies(e))
				t.execute(e);
		}
	}
	
	private final static EventExecutor ee = new EventExecutor() {
		@Override
		public void execute(final Listener listener, final Event event) throws EventException {
			final PlayerMoveEvent e = (PlayerMoveEvent) event; // even WorldGuard doesn't have events, and this method supports all region plugins for sure.
			final Location to = e.getTo(), from = e.getFrom();
			if (to.equals(from))
				return;
			final Collection<? extends Region> oldRs = RegionsPlugin.getRegionsAt(from), newRs = RegionsPlugin.getRegionsAt(to);
			for (final Region r : oldRs) {
				if (!newRs.contains(r))
					callEvent(r, false);
			}
			for (final Region r : newRs) {
				if (!oldRs.contains(r))
					callEvent(r, true);
			}
		}
	};
	
	private static boolean registered = false;
	
	private final static void register() {
		if (registered)
			return;
		Bukkit.getPluginManager().registerEvent(PlayerMoveEvent.class, new Listener() {}, SkriptConfig.defaultEventPriority.value(), ee, Skript.getInstance(), true);
		registered = true;
	}
	
}
