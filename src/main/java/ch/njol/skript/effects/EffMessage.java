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

import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.event.Event;

import ch.njol.skript.Skript;
import ch.njol.skript.lang.Effect;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.skript.lang.util.SimpleLiteral;
import ch.njol.skript.util.Utils;

/**
 * 
 * @author Peter Güttinger
 */
public class EffMessage extends Effect {
	
	private static final long serialVersionUID = -3885693386533277658L;
	
	static {
		Skript.registerEffect(EffMessage.class, "(message|send [message]) %strings% [to %commandsenders%]", "log %strings%");
	}
	
	private Expression<String> messages;
	private Expression<CommandSender> recipients;
	
	@SuppressWarnings("unchecked")
	@Override
	public boolean init(final Expression<?>[] exprs, final int matchedPattern, final int isDelayed, final ParseResult parser) {
		if (matchedPattern == 0) {
			messages = (Expression<String>) exprs[0];
			recipients = (Expression<CommandSender>) exprs[1];
		} else {
			messages = (Expression<String>) exprs[0];
			recipients = new SimpleLiteral<CommandSender>(Bukkit.getConsoleSender(), false);
		}
		return true;
	}
	
	@Override
	protected void execute(final Event e) {
		for (String message : messages.getArray(e)) {
			message = Utils.prepareMessage(message);
			for (final CommandSender s : recipients.getArray(e)) {
				s.sendMessage(message);
			}
		}
	}
	
	@Override
	public String toString(final Event e, final boolean debug) {
		return "send " + messages.toString(e, debug) + " to " + recipients.toString(e, debug);
	}
}
