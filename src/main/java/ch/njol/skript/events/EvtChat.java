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
import java.util.concurrent.CancellationException;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

import org.bukkit.Bukkit;
import org.bukkit.event.Event;
import org.bukkit.event.EventException;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.event.player.PlayerChatEvent;
import org.bukkit.plugin.EventExecutor;

import ch.njol.skript.Skript;
import ch.njol.skript.SkriptEventHandler;
import ch.njol.skript.events.util.PlayerChatEventHandler;
import ch.njol.skript.lang.Literal;
import ch.njol.skript.lang.SelfRegisteringSkriptEvent;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.skript.lang.Trigger;

/**
 * @author Peter Güttinger
 */
@SuppressWarnings("deprecation")
public class EvtChat extends SelfRegisteringSkriptEvent {
	private static final long serialVersionUID = -605839769417043132L;
	
	static {
		if (Skript.isRunningBukkit1_3())
			Skript.registerEvent(EvtChat.class, AsyncPlayerChatEvent.class, "chat");
		else
			Skript.registerEvent(EvtChat.class, PlayerChatEvent.class, "chat");
	}
	
	private final static Collection<Trigger> triggers = new ArrayList<Trigger>();
	
	private static boolean registeredExecutor = false;
	private final static EventExecutor executor = new EventExecutor() {
		
		private final void execute(final Event e) {
			SkriptEventHandler.logEventStart(e);
			for (final Trigger t : triggers) {
				SkriptEventHandler.logTriggerStart(t);
				t.start(e);
				SkriptEventHandler.logTriggerEnd(t);
			}
			SkriptEventHandler.logEventEnd();
		}
		
		@Override
		public void execute(final Listener l, final Event e) throws EventException {
			if (!triggers.isEmpty()) {
				if (!Skript.isRunningBukkit1_3() || !e.isAsynchronous()) {
					execute(e);
					return;
				}
				final Future<Void> f = Bukkit.getScheduler().callSyncMethod(Skript.getInstance(), new Callable<Void>() {
					@Override
					public Void call() throws Exception {
						execute(e);
						return null;
					}
				});
				try {
					while (true) {
						try {
							f.get();
							break;
						} catch (final InterruptedException e1) {}
					}
				} catch (final ExecutionException ex) {
					Skript.exception(ex);
				} catch (final CancellationException ex) {} catch (final ThreadDeath err) {}// server shutting down
			}
		}
	};
	
	@Override
	public boolean init(final Literal<?>[] args, final int matchedPattern, final ParseResult parser) {
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
			PlayerChatEventHandler.registerChatEvent(Skript.getDefaultEventPriority(), executor, true);
			registeredExecutor = true;
		}
	}
	
	@Override
	public void unregister(final Trigger t) {
		triggers.remove(t);
	}
	
	@Override
	public void unregisterAll() {
		triggers.clear();
	}
	
}
