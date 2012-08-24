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

import java.io.File;
import java.lang.reflect.Constructor;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.PluginCommand;
import org.bukkit.command.SimpleCommandMap;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

import ch.njol.skript.Language;
import ch.njol.skript.Skript;
import ch.njol.skript.SkriptLogger;
import ch.njol.skript.SkriptLogger.SubLog;
import ch.njol.skript.Verbosity;
import ch.njol.skript.lang.SkriptParser;
import ch.njol.skript.lang.Trigger;
import ch.njol.skript.lang.TriggerItem;
import ch.njol.skript.lang.util.SimpleEvent;
import ch.njol.skript.util.Utils;
import ch.njol.util.Validate;

/**
 * This class is used for user-defined commands.
 * 
 * @author Peter Güttinger
 * 
 */
public class SkriptCommand implements CommandExecutor {
	
	private final String name, label;
	private final List<String> aliases;
	private List<String> activeAliases;
	private final String permission, permissionMessage;
	private final String description, usage;
	
	private final Trigger trigger;
	
	private final String pattern;
	private final List<Argument<?>> arguments;
	
	public final static int PLAYERS = 0x1, CONSOLE = 0x2, BOTH = PLAYERS | CONSOLE;
	final int executableBy;
	
	private final PluginCommand bukkitCommand;
	
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
	public SkriptCommand(final File script, final String name, final String pattern, final List<Argument<?>> arguments, final String description, final String usage, final List<String> aliases, final String permission, final String permissionMessage, final int executableBy, final List<TriggerItem> items) {
		Validate.notNull(name, pattern, arguments, description, usage, aliases, items);
		this.name = name;
		label = name.toLowerCase();
		this.permission = permission;
		this.permissionMessage = permissionMessage == null ? Language.get("command.no_permission_message") : permissionMessage;
		
		this.aliases = aliases;
		activeAliases = new ArrayList<String>(aliases);
		
		this.description = description;
		this.usage = usage;
		
		this.executableBy = executableBy;
		
		this.pattern = pattern;
		this.arguments = arguments;
		
		trigger = new Trigger(script, "command /" + name, new SimpleEvent(), items);
		
		try {
			final Constructor<PluginCommand> c = PluginCommand.class.getDeclaredConstructor(String.class, Plugin.class);
			c.setAccessible(true);
			bukkitCommand = c.newInstance(name, Skript.getInstance());
			bukkitCommand.setAliases(aliases);
			bukkitCommand.setDescription(description);
			bukkitCommand.setLabel(label);
			bukkitCommand.setPermission(permission);
			bukkitCommand.setPermissionMessage(permissionMessage);
			bukkitCommand.setUsage(usage);
			bukkitCommand.setExecutor(this);
		} catch (final Exception e) {
			throw Skript.exception(e);
		}
	}
	
	@Override
	public boolean onCommand(final CommandSender sender, final Command command, final String label, final String[] args) {
		execute(sender, label, Utils.join(args, " "));
		return true;
	}
	
	public boolean execute(final CommandSender sender, final String commandLabel, final String rest) {
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
		trigger.start(event);
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
	
	public void register(final SimpleCommandMap commandMap, final Map<String, Command> knownCommands, final Set<String> aliases) {
		synchronized (commandMap) {
			knownCommands.put(label, bukkitCommand);
			aliases.remove(label);
			final Iterator<String> as = activeAliases.iterator();
			while (as.hasNext()) {
				final String lowerAlias = as.next().toLowerCase();
				if (knownCommands.containsKey(lowerAlias)) {
					as.remove();
					continue;
				}
				knownCommands.put(lowerAlias, bukkitCommand);
				aliases.add(lowerAlias);
			}
		}
		bukkitCommand.setAliases(activeAliases);
		bukkitCommand.register(commandMap);
	}
	
	public void unregister(final SimpleCommandMap commandMap, final Map<String, Command> knownCommands, final Set<String> aliases) {
		knownCommands.remove(label);
		aliases.removeAll(activeAliases);
		activeAliases = new ArrayList<String>(aliases);
		bukkitCommand.unregister(commandMap);
		bukkitCommand.setAliases(this.aliases);
	}
	
	public String getName() {
		return name;
	}
	
	public String getLabel() {
		return label;
	}
	
	public List<String> getAliases() {
		return aliases;
	}
	
	public List<String> getActiveAliases() {
		return activeAliases;
	}
	
	public PluginCommand getBukkitCommand() {
		return bukkitCommand;
	}
	
	public File getScript() {
		return trigger.getScript();
	}
	
}
