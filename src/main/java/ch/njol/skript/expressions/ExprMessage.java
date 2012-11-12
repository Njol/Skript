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

package ch.njol.skript.expressions;

import org.bukkit.event.Event;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.event.player.PlayerChatEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;

import ch.njol.skript.ScriptLoader;
import ch.njol.skript.Skript;
import ch.njol.skript.Skript.ExpressionType;
import ch.njol.skript.classes.Changer.ChangeMode;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.skript.lang.util.SimpleExpression;
import ch.njol.skript.log.ErrorQuality;
import ch.njol.skript.util.Utils;

/**
 * @author Peter Güttinger
 */
@SuppressWarnings("deprecation")
public class ExprMessage extends SimpleExpression<String> {
	private static final long serialVersionUID = 691567046005405494L;
	
	private static enum MessageType {
		CHAT("chat", "[chat( |-)]message", null) {
			@Override
			String get(final Event e) {
				if (Skript.isRunningBukkit(1, 3))
					return ((AsyncPlayerChatEvent) e).getMessage();
				else
					return ((PlayerChatEvent) e).getMessage();
			}
			
			@Override
			void set(final Event e, final String message) {
				if (Skript.isRunningBukkit(1, 3))
					((AsyncPlayerChatEvent) e).setMessage(message);
				else
					((PlayerChatEvent) e).setMessage(message);
			}
		},
		JOIN("join", "(join|log[ ]in)( |-)message", PlayerJoinEvent.class) {
			@Override
			String get(final Event e) {
				return ((PlayerJoinEvent) e).getJoinMessage();
			}
			
			@Override
			void set(final Event e, final String message) {
				((PlayerJoinEvent) e).setJoinMessage(message);
			}
		},
		QUIT("quit", "(quit|leave|log[ ]out)( |-)message", PlayerQuitEvent.class) {
			@Override
			String get(final Event e) {
				return ((PlayerQuitEvent) e).getQuitMessage();
			}
			
			@Override
			void set(final Event e, final String message) {
				((PlayerQuitEvent) e).setQuitMessage(message);
			}
		},
		DEATH("death", "death( |-)message", PlayerDeathEvent.class) {
			@Override
			String get(final Event e) {
				return ((PlayerDeathEvent) e).getDeathMessage();
			}
			
			@Override
			void set(final Event e, final String message) {
				((PlayerDeathEvent) e).setDeathMessage(message);
			}
		};
		
		private final String name, pattern;
		private final Class<? extends Event> event;
		
		MessageType(final String name, final String pattern, final Class<? extends Event> event) {
			this.name = name;
			this.pattern = "[the] " + pattern;
			this.event = event;
		}
		
		static String[] patterns;
		static {
			patterns = new String[values().length];
			for (int i = 0; i < patterns.length; i++)
				patterns[i] = values()[i].pattern;
		}
		
		abstract String get(Event e);
		
		abstract void set(Event e, String message);
		
	}
	
	static {
		Skript.registerExpression(ExprMessage.class, String.class, ExpressionType.SIMPLE, MessageType.patterns);
	}
	
	private MessageType type;
	
	@Override
	public boolean init(final Expression<?>[] exprs, final int matchedPattern, final int isDelayed, final ParseResult parseResult) {
		type = MessageType.values()[matchedPattern];
		if (type == MessageType.CHAT) {
			if (!Utils.contains(ScriptLoader.currentEvents, Skript.isRunningBukkit(1, 3) ? AsyncPlayerChatEvent.class : PlayerChatEvent.class)) {
				Skript.error("The message can only be used in a chat event", ErrorQuality.SEMANTIC_ERROR);
				return false;
			}
		} else {
			if (!Utils.contains(ScriptLoader.currentEvents, type.event)) {
				Skript.error("The " + type.name + " message can only be used in a " + type.name + " event", ErrorQuality.SEMANTIC_ERROR);
				return false;
			}
		}
		return true;
	}
	
	@Override
	protected String[] get(final Event e) {
		if (!type.event.isInstance(e))
			return new String[0];
		return new String[] {type.get(e)};
	}
	
	@SuppressWarnings("unchecked")
	@Override
	public Class<?>[] acceptChange(final ChangeMode mode) {
		if (mode == ChangeMode.SET)
			return Skript.array(String.class);
		return null;
	}
	
	@Override
	public void change(final Event e, final Object delta, final ChangeMode mode) {
		if (!type.event.isInstance(e))
			return;
		type.set(e, (String) delta);
	}
	
	@Override
	public boolean isSingle() {
		return true;
	}
	
	@Override
	public Class<? extends String> getReturnType() {
		return String.class;
	}
	
	@Override
	public boolean getAnd() {
		return true;
	}
	
	@Override
	public String toString(final Event e, final boolean debug) {
		return "the " + type.name + " message";
	}
	
}
