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

package ch.njol.skript.util;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Map.Entry;

import org.bukkit.command.CommandSender;

/**
 * @author Peter Güttinger
 * 
 */
public class CommandHelp {
	
	private String command;
	private final String description;
	private final String argsColor;
	
	private final Map<String, Object> arguments = new LinkedHashMap<String, Object>();
	
	private Object wildcardArg = null;
	
	public CommandHelp(final String command, final String description, final String argsColor) {
		this.command = command;
		this.description = description;
		this.argsColor = argsColor;
	}
	
	public CommandHelp add(String argument, final String description) {
		if (argument.startsWith("<") && argument.endsWith(">")) {
			argument = "<gray><<" + argsColor + ">" + argument.substring(1, argument.length() - 1) + "<gray>>";
			wildcardArg = description;
		}
		arguments.put(argument, description);
		return this;
	}
	
	public CommandHelp add(final CommandHelp help) {
		arguments.put(help.command, help);
		help.command = command + " <" + argsColor + ">" + help.command;
		return this;
	}
	
	private final static void message(final CommandSender recipient, final String message) {
		recipient.sendMessage(Utils.prepareMessage(message));
	}
	
	public boolean test(final CommandSender sender, final String[] args) {
		return test(sender, args, 0);
	}
	
	private boolean test(final CommandSender sender, final String[] args, final int index) {
		if (index >= args.length) {
			showHelp(sender);
			return false;
		}
		final Object help = arguments.get(args[index].toLowerCase());
		if (help == null && wildcardArg == null) {
			showHelp(sender, "Invalid argument <gray>'<" + argsColor + ">" + args[index] + "<gray>'<reset>. Correct usage:");
			return false;
		}
		if (help instanceof CommandHelp)
			return ((CommandHelp) help).test(sender, args, index + 1);
		return true;
	}
	
	public void showHelp(final CommandSender sender) {
		showHelp(sender, "Usage:");
	}
	
	private void showHelp(final CommandSender sender, final String pre) {
		message(sender, pre + " " + command + " <" + argsColor + ">...");
		for (final Entry<String, Object> e : arguments.entrySet()) {
			message(sender, "  <" + argsColor + ">" + e.getKey() + " <gray>-<reset> " + e.getValue());
		}
	}
	
	@Override
	public String toString() {
		return description;
	}
	
}
