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
 * Copyright 2011-2013 Peter Güttinger
 * 
 */

package ch.njol.skript.command;

import java.io.File;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.Serializable;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.PluginCommand;
import org.bukkit.command.SimpleCommandMap;
import org.bukkit.entity.Player;
import org.bukkit.help.GenericCommandHelpTopic;
import org.bukkit.help.HelpMap;
import org.bukkit.help.HelpTopic;
import org.bukkit.help.HelpTopicComparator;
import org.bukkit.help.IndexHelpTopic;
import org.bukkit.plugin.Plugin;

import ch.njol.skript.Skript;
import ch.njol.skript.command.Commands.CommandAliasHelpTopic;
import ch.njol.skript.lang.SkriptParser;
import ch.njol.skript.lang.Trigger;
import ch.njol.skript.lang.TriggerItem;
import ch.njol.skript.lang.util.SimpleEvent;
import ch.njol.skript.localization.Language;
import ch.njol.skript.localization.Message;
import ch.njol.skript.log.ParseLogHandler;
import ch.njol.skript.log.SkriptLogger;
import ch.njol.skript.log.Verbosity;
import ch.njol.skript.util.Utils;
import ch.njol.util.StringUtils;
import ch.njol.util.Validate;

/**
 * This class is used for user-defined commands.
 * 
 * @author Peter Güttinger
 */
@SuppressWarnings("serial")
public class ScriptCommand implements CommandExecutor, Serializable {
	public final static Message m_executable_by_players = new Message("commands.executable by players");
	public final static Message m_executable_by_console = new Message("commands.executable by console");
	
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
	
	private transient PluginCommand bukkitCommand;
	
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
	public ScriptCommand(final File script, final String name, final String pattern, final List<Argument<?>> arguments, final String description, final String usage, final List<String> aliases, final String permission, final String permissionMessage, final int executableBy, final List<TriggerItem> items) {
		Validate.notNull(name, pattern, arguments, description, usage, aliases, items);
		this.name = name;
		label = name.toLowerCase();
		this.permission = permission;
		this.permissionMessage = permissionMessage == null ? Language.get("commands.no permission message") : permissionMessage;
		
		this.aliases = aliases;
		activeAliases = new ArrayList<String>(aliases);
		
		this.description = Utils.replaceEnglishChatStyles(description);
		this.usage = Utils.replaceEnglishChatStyles(usage);
		
		this.executableBy = executableBy;
		
		this.pattern = pattern;
		this.arguments = arguments;
		
		trigger = new Trigger(script, "command /" + name, new SimpleEvent(), items);
		
		setupBukkitCommand();
	}
	
	private void setupBukkitCommand() {
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
			Skript.outdatedError(e);
		}
	}
	
	private void readObject(final ObjectInputStream in) throws ClassNotFoundException, IOException {
		in.defaultReadObject();
		setupBukkitCommand();
	}
	
	@Override
	public boolean onCommand(final CommandSender sender, final Command command, final String label, final String[] args) {
		execute(sender, label, StringUtils.join(args, " "));
		return true;
	}
	
	public boolean execute(final CommandSender sender, final String commandLabel, final String rest) {
		if (sender instanceof Player) {
			if ((executableBy & PLAYERS) == 0) {
				sender.sendMessage("" + m_executable_by_console);
				return false;
			}
		} else {
			if ((executableBy & CONSOLE) == 0) {
				sender.sendMessage("" + m_executable_by_players);
				return false;
			}
		}
		
		if (permission != null && !sender.hasPermission(permission)) {
			sender.sendMessage(permissionMessage);
			return false;
		}
		
		final ScriptCommandEvent event = new ScriptCommandEvent(this, sender);
		
		final ParseLogHandler log = SkriptLogger.startParseLogHandler();
		try {
			final boolean ok = SkriptParser.parseArguments(rest, this, event);
			if (!ok) {
				if (log.hasError())
					sender.sendMessage(ChatColor.DARK_RED + log.getError().getMessage());
				sender.sendMessage(Commands.m_correct_usage + " " + usage);
				return false;
			}
			log.clear();
			log.printLog();
		} finally {
			log.stop();
		}
		
		if (Skript.log(Verbosity.VERY_HIGH))
			Skript.info("# /" + name + " " + rest);
		final long startTrigger = System.nanoTime();
		
		if (!trigger.execute(event))
			sender.sendMessage(Commands.m_internal_error.toString());
		
		if (Skript.log(Verbosity.VERY_HIGH))
			Skript.info("# " + name + " took " + 1. * (System.nanoTime() - startTrigger) / 1000000. + " milliseconds");
		
		return true;
	}
	
	public void sendHelp(final CommandSender sender) {
		if (!description.isEmpty())
			sender.sendMessage(description);
		sender.sendMessage(ChatColor.GOLD + "Usage" + ChatColor.RESET + ": " + usage);
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
	
	private transient Command overriden = null;
	private transient Map<String, Command> overridenAliases;
	
	public void register(final SimpleCommandMap commandMap, final Map<String, Command> knownCommands, final Set<String> aliases) {
		synchronized (commandMap) {
			overridenAliases = new HashMap<String, Command>();
			overriden = knownCommands.put(label, bukkitCommand);
			aliases.remove(label);
			final Iterator<String> as = activeAliases.iterator();
			while (as.hasNext()) {
				final String lowerAlias = as.next().toLowerCase();
				if (knownCommands.containsKey(lowerAlias) && !aliases.contains(lowerAlias)) {
					as.remove();
					continue;
				}
				overridenAliases.put(lowerAlias, knownCommands.put(lowerAlias, bukkitCommand));
				aliases.add(lowerAlias);
			}
			bukkitCommand.setAliases(activeAliases);
			bukkitCommand.register(commandMap);
		}
	}
	
	public void unregister(final SimpleCommandMap commandMap, final Map<String, Command> knownCommands, final Set<String> aliases) {
		synchronized (commandMap) {
			knownCommands.remove(label);
			aliases.removeAll(activeAliases);
			for (final String alias : activeAliases)
				knownCommands.remove(alias);
			activeAliases = new ArrayList<String>(this.aliases);
			bukkitCommand.unregister(commandMap);
			bukkitCommand.setAliases(this.aliases);
			if (overriden != null) {
				knownCommands.put(label, overriden);
				overriden = null;
			}
			for (final Entry<String, Command> e : overridenAliases.entrySet()) {
				if (e.getValue() == null)
					continue;
				knownCommands.put(e.getKey(), e.getValue());
				aliases.add(e.getKey());
			}
			overridenAliases.clear();
		}
	}
	
	private transient Collection<HelpTopic> helps;
	
	public void registerHelp() {
		helps = new ArrayList<HelpTopic>();
		final HelpMap help = Bukkit.getHelpMap();
		final HelpTopic t = new GenericCommandHelpTopic(bukkitCommand);
		help.addTopic(t);
		helps.add(t);
		final HelpTopic aliases = help.getHelpTopic("Aliases");
		if (aliases != null && aliases instanceof IndexHelpTopic) {
			aliases.getFullText(Bukkit.getConsoleSender()); // CraftBukkit has a lazy IndexHelpTopic class (org.bukkit.craftbukkit.help.CustomIndexHelpTopic) - maybe its used for aliases as well
			try {
				final Field topics = IndexHelpTopic.class.getDeclaredField("allTopics");
				topics.setAccessible(true);
				final ArrayList<HelpTopic> as = new ArrayList<HelpTopic>((Collection<HelpTopic>) topics.get(aliases));
				for (final String alias : activeAliases) {
					final HelpTopic at = new CommandAliasHelpTopic("/" + alias, "/" + getLabel(), help);
					as.add(at);
					helps.add(at);
				}
				Collections.sort(as, HelpTopicComparator.helpTopicComparatorInstance());
				topics.set(aliases, as);
			} catch (final Exception e) {
				Skript.exception(e, "error registering aliases for /" + getName());
			}
		}
	}
	
	public void unregisterHelp() {
		Bukkit.getHelpMap().getHelpTopics().removeAll(helps);
		final HelpTopic aliases = Bukkit.getHelpMap().getHelpTopic("Aliases");
		if (aliases != null && aliases instanceof IndexHelpTopic) {
			try {
				final Field topics = IndexHelpTopic.class.getDeclaredField("allTopics");
				topics.setAccessible(true);
				final ArrayList<HelpTopic> as = new ArrayList<HelpTopic>((Collection<HelpTopic>) topics.get(aliases));
				as.removeAll(helps);
				topics.set(aliases, as);
			} catch (final Exception e) {
				Skript.exception(e, "error unregistering aliases for /" + getName());
			}
		}
		helps.clear();
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
