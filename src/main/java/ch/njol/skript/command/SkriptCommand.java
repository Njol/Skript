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

package ch.njol.skript.command;

import java.util.Collection;
import java.util.List;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import ch.njol.skript.Skript;
import ch.njol.skript.SkriptLogger;
import ch.njol.skript.SkriptLogger.SubLog;
import ch.njol.skript.Verbosity;
import ch.njol.skript.lang.SkriptParser;
import ch.njol.skript.lang.Trigger;
import ch.njol.skript.lang.TriggerItem;
import ch.njol.skript.lang.util.SimpleEvent;
import ch.njol.util.Validate;

/**
 * This class is used for user-defined commands.
 * 
 * @author Peter Güttinger
 * 
 */
public class SkriptCommand {
	
	private final Trigger trigger;
	
	private final String pattern;
	
	private final List<Argument<?>> arguments;
	
	final String name;
	final String description;
	final String usage;
	final Collection<String> aliases;
	final String permission;
	final String permissionMessage;
	
	public final static int PLAYERS = 0x1, CONSOLE = 0x2, BOTH = PLAYERS | CONSOLE;
	final int executableBy;
	
	/**
	 * Creates a new SkriptCommand.<br/>
	 * No parameters may be null except for permission & permissionMessage.
	 * 
	 * @param name /name
	 * @param pattern
	 * @param arguments the list of Arguments this command takes
	 * @param description description to display in /help
	 * @param usage message to display if the command was used incorrectly
	 * @param aliases /alias1, /alias2, ...
	 * @param permission permission or null if none
	 * @param permissionMessage message to display if the player doesn't have the given permission
	 * @param items trigger to execute
	 */
	public SkriptCommand(final String name, final String pattern, final List<Argument<?>> arguments, final String description, final String usage, final Collection<String> aliases, final String permission, final String permissionMessage, final int executableBy, final List<TriggerItem> items) {
		Validate.notNull(name, arguments, description, usage, aliases, items);
		
		this.pattern = pattern;
		
		this.name = name;
		this.description = description;
		this.usage = usage;
		this.aliases = aliases;
		this.executableBy = executableBy;
		
		trigger = new Trigger("command /" + name, new SimpleEvent(), items);
		this.permission = permission;
		this.permissionMessage = permissionMessage == null ? "You don't have the required permission to use this command" : permissionMessage;// TODO l10n
		this.arguments = arguments;
	}
	
	public boolean execute(final CommandSender sender, @SuppressWarnings("unused") final String commandLabel, final String rest) {
		if (sender instanceof Player) {
			if ((executableBy & PLAYERS) == 0) {
				sender.sendMessage("This command can only be used by the console");
				return false;
			}
		} else {
			if ((executableBy & CONSOLE) == 0) {
				sender.sendMessage("This command can only be used by players");
				return false;
			}
		}
		
		if (permission != null && !sender.hasPermission(permission)) {
			sender.sendMessage(permissionMessage);
			return false;
		}
		
		final SkriptCommandEvent event = new SkriptCommandEvent(this, sender);
		
		final SubLog log = SkriptLogger.startSubLog();
		final boolean ok = SkriptParser.parseArguments(rest, this, event);
		SkriptLogger.stopSubLog(log);
		if (!ok) {
			log.printErrors(sender, null);
			sender.sendMessage("Correct usage: " + usage);
			return false;
		}
		
		if (Skript.log(Verbosity.VERY_HIGH))
			Skript.info("# /" + name + " " + rest);
		final long startTrigger = System.nanoTime();
		trigger.run(event);
		if (Skript.log(Verbosity.VERY_HIGH))
			Skript.info("# " + name + " took " + 1. * (System.nanoTime() - startTrigger) / 1000000. + " milliseconds");
		
		return true;
	}
	
	public void sendHelp(final CommandSender sender) {
		if (!description.isEmpty())
			sender.sendMessage(description);
		sender.sendMessage("Usage: " + usage);
	}
	
	/**
	 * Gets the arguments this command takes.
	 * 
	 * @return The internal list of arguments. Do not modify it!
	 */
	public List<Argument<?>> getArguments() {
		return arguments;
	}
	
	public String getPattern() {
		return pattern;
	}
	
	public String getName() {
		return name;
	}
	
	public Collection<String> getAliases() {
		return aliases;
	}
	
	public String getUsage() {
		return usage;
	}
	
	public String getDescription() {
		return description;
	}
}
