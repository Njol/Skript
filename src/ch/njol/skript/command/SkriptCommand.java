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

import ch.njol.skript.Skript;
import ch.njol.skript.Verbosity;
import ch.njol.skript.api.SimpleEvent;
import ch.njol.skript.api.intern.Trigger;
import ch.njol.skript.api.intern.TriggerItem;
import ch.njol.skript.util.Utils;
import ch.njol.util.Validate;

/**
 * This class is used for user-defined commands.
 * 
 * @author Peter Güttinger
 * 
 */
public class SkriptCommand {
	
	private final Trigger trigger;
	
	private final List<Argument<?>> arguments;
	
	final String name;
	final String description;
	final String usageMessage;
	final Collection<String> aliases;
	final String permission;
	final String permissionMessage;
	
	/**
	 * Creates a new SkriptCommand.<br/>
	 * No parameters may be null except for permission & permissionMessage.
	 * 
	 * @param name /name
	 * @param arguments the list of Arguments this command takes
	 * @param description description to display in /help
	 * @param usageMessage message to display if the command was used incorrectly
	 * @param aliases /alias1, /alias2, ...
	 * @param permission permission or null if none
	 * @param permissionMessage message to display if the player doesn't have the given permission
	 * @param items trigger to execute
	 */
	public SkriptCommand(final String name, final List<Argument<?>> arguments, final String description, final String usageMessage, final Collection<String> aliases, final String permission, final String permissionMessage, final List<TriggerItem> items) {
		Validate.notNull(name, arguments, description, usageMessage, aliases, items);
		
		this.name = name;
		this.description = description;
		this.usageMessage = usageMessage;
		this.aliases = aliases;
		
		trigger = new Trigger("command /" + name, new SimpleEvent(), items);
		this.permission = permission;
		this.permissionMessage = permissionMessage == null ? "You don't have the required permission to use this command" : permissionMessage;// TODO l10n
		this.arguments = arguments;
	}
	
	public boolean execute(final CommandSender sender, @SuppressWarnings("unused") final String commandLabel, final String rest) {
		if (permission != null && !sender.hasPermission(permission)) {
			sender.sendMessage(permissionMessage);
			return false;
		}
		String[] args = rest.split("\\s*,\\s*");
		if (args.length == 1 && args[0].isEmpty())
			args = new String[0];
		final SkriptCommandEvent event = new SkriptCommandEvent(this, sender, args);
		for (int i = 0; i < arguments.size(); i++) {
			final Argument<?> a = arguments.get(i);
			if (i < args.length) {
				if (a.isSingle()) {
					if (!a.parse(args[i], sender)) {
//						sender.sendMessage("correct usage: " + usageMessage);
						return false;
					}
				} else {
					if (!a.parse(args, i, sender)) {
//						sender.sendMessage("correct usage: " + usageMessage);
						return false;
					}
				}
			} else {
				a.setToDefault(event);
			}
		}
		
		if (Skript.log(Verbosity.VERY_HIGH))
			Skript.info("# /" + name + " " + Utils.join(args, ", "));
		final long startTrigger = System.nanoTime();
		trigger.run(event);
		if (Skript.log(Verbosity.VERY_HIGH))
			Skript.info("# " + name + " took " + 1. * (System.nanoTime() - startTrigger) / 1000000. + " milliseconds");
		
		return true;
	}
	
	/**
	 * Gets the argumetns this command takes.
	 * 
	 * @return The internal list of arguments. Do not modify it!
	 */
	public List<Argument<?>> getArguments() {
		return arguments;
	}
	
	public String getName() {
		return name;
	}
	
	public Collection<String> getAliases() {
		return aliases;
	}
	
}
