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
import ch.njol.skript.lang.ExprParser.ParseResult;
import ch.njol.skript.lang.Variable;
import ch.njol.skript.util.VariableString;

/**
 * 
 * @author Peter Güttinger
 * 
 */
public class EffCommand extends Effect {
	
	static {
		Skript.registerEffect(EffCommand.class,
				"[execute] command %variablestrings% [by %commandsenders%]",
				"[execute] %commandsenders% command %variablestrings%",
				"(let|make) %commandsenders% execute [command] %variablestrings%");
	}
	
	private Variable<CommandSender> senders;
	private Variable<VariableString> commands;
	
	@SuppressWarnings("unchecked")
	@Override
	public void init(final Variable<?>[] vars, final int matchedPattern, final ParseResult parser) {
		if (matchedPattern > 0) {
			senders = (Variable<CommandSender>) vars[0];
			commands = (Variable<VariableString>) vars[1];
		} else {
			commands = (Variable<VariableString>) vars[0];
			senders = (Variable<CommandSender>) vars[1];
		}
	}
	
	@Override
	public void execute(final Event e) {
		for (final VariableString commandVS : commands.getArray(e)) {
			final String command = commandVS.get(e);
			for (final CommandSender sender : senders.getArray(e)) {
				Bukkit.getServer().dispatchCommand(sender, command);
			}
		}
	}
	
	@Override
	public String getDebugMessage(final Event e) {
		return "let " + senders.getDebugMessage(e) + " execute " + commands.getDebugMessage(e);
	}
	
}
