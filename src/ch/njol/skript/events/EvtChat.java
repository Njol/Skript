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

import java.util.ArrayList;
import java.util.Collection;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

import org.bukkit.Bukkit;
import org.bukkit.event.Event;
import org.bukkit.event.EventException;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.plugin.EventExecutor;

import ch.njol.skript.Skript;
import ch.njol.skript.SkriptEventHandler;
import ch.njol.skript.lang.Literal;
import ch.njol.skript.lang.SkriptEvent;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.skript.lang.Trigger;

/**
 * @author Peter Güttinger
 * 
 */
public class EvtChat extends SkriptEvent {
	
	static {
		Skript.registerEvent(EvtChat.class, AsyncPlayerChatEvent.class, false, "chat");
	}
	
	private final static Collection<Trigger> triggers = new ArrayList<Trigger>();
	
	private static boolean registeredExecutor = false;
	private final static EventExecutor executor = new EventExecutor() {
		@Override
		public void execute(final Listener l, final Event e) throws EventException {
			if (!triggers.isEmpty()) {
				final Future<?> f = Bukkit.getScheduler().callSyncMethod(Skript.getInstance(), new Callable<Void>() {
					@Override
					public Void call() throws Exception {
						SkriptEventHandler.logEventStart(e);
						for (final Trigger t : triggers) {
							SkriptEventHandler.logTriggerStart(t);
							t.run(e);
							SkriptEventHandler.logTriggerEnd(t);
						}
						SkriptEventHandler.logEventEnd();
						return null;
					}
				});
				try {
					while (!f.isDone()) {
						try {
							f.get();
						} catch (final InterruptedException e1) {}
					}
				} catch (final ExecutionException e1) {
					Skript.exception(e1);
				}
			}
		}
	};
	
	@Override
	public boolean init(final Literal<?>[] args, final int matchedPattern, final ParseResult parser) {
		return true;
	}
	
	@Override
	public boolean check(final Event e) {
		return true;
	}
	
	@Override
	public String toString(final Event e, final boolean debug) {
		return "chat";
	}
	
	@Override
	public void register(final Trigger t) {
		triggers.add(t);
		if (!registeredExecutor) {
			Bukkit.getPluginManager().registerEvent(AsyncPlayerChatEvent.class, new Listener() {}, Skript.getPriority(), executor, Skript.getInstance(), true);
			registeredExecutor = true;
		}
	}
	
	@Override
	public void unregister() {
		triggers.clear();
	}
	
}
