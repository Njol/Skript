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
import ch.njol.skript.api.Effect;
import ch.njol.skript.api.intern.VariableStringLiteral;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.skript.util.StringMode;

/**
 * 
 * @author Peter Güttinger
 * 
 */
public class EffCommand extends Effect {
	
	static {
		Skript.registerEffect(EffCommand.class,
				"[execute] command %strings% [by %-commandsenders%]",
				"[execute] %commandsenders% command %strings%",
				"(let|make) %commandsenders% execute [command] %strings%");
	}
	
	private Expression<CommandSender> senders;
	private Expression<String> commands;
	
	@SuppressWarnings("unchecked")
	@Override
	public boolean init(final Expression<?>[] vars, final int matchedPattern, final ParseResult parser) {
		if (matchedPattern > 0) {
			senders = (Expression<CommandSender>) vars[0];
			commands = (Expression<String>) vars[1];
		} else {
			commands = (Expression<String>) vars[0];
			senders = (Expression<CommandSender>) vars[1];
		}
		if (commands instanceof VariableStringLiteral) {
			((VariableStringLiteral) commands).setMode(StringMode.COMMAND);
		}
		return true;
	}
	
	@Override
	public void execute(final Event e) {
		for (String command : commands.getArray(e)) {
			if (command.startsWith("/"))
				command = command.substring(1);
			if (senders == null) {
				Bukkit.getServer().dispatchCommand(Bukkit.getConsoleSender(), command);
			} else {
				for (final CommandSender sender : senders.getArray(e)) {
					Bukkit.getServer().dispatchCommand(sender, command);
				}
			}
		}
	}
	
	@Override
	public String toString(final Event e, final boolean debug) {
		return "let " + senders.toString(e, debug) + " execute " + commands.toString(e, debug);
	}
	
}
