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

import org.bukkit.event.Event;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import org.bukkit.event.server.ServerCommandEvent;

import ch.njol.skript.Skript;
import ch.njol.skript.lang.Literal;
import ch.njol.skript.lang.SkriptEvent;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.util.StringUtils;

/**
 * @author Peter Güttinger
 * 
 */
@SuppressWarnings("unchecked")
public class EvtCommand extends SkriptEvent {
	private static final long serialVersionUID = 6554740820047650855L;
	
	static {
		Skript.registerEvent(EvtCommand.class, Skript.array(PlayerCommandPreprocessEvent.class, ServerCommandEvent.class), "command [%-string%]");
	}
	
	private String command;
	
	@Override
	public boolean init(final Literal<?>[] args, final int matchedPattern, final ParseResult parser) {
		if (args[0] != null) {
			command = ((Literal<String>) args[0]).getSingle();
			if (command.startsWith("/"))
				command = command.substring(1);
		}
		return true;
	}
	
	@Override
	public boolean check(final Event e) {
		if (e instanceof PlayerCommandPreprocessEvent) {
			return StringUtils.startsWithIgnoreCase(((PlayerCommandPreprocessEvent) e).getMessage(), command, 1);
		} else {
			return StringUtils.startsWithIgnoreCase(((ServerCommandEvent) e).getCommand(), command);
		}
	}
	
	@Override
	public String toString(final Event e, final boolean debug) {
		return "command" + (command == null ? "" : " " + command);
	}
	
}
