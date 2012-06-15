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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import ch.njol.skript.Skript;
import ch.njol.skript.TriggerFileLoader;
import ch.njol.skript.config.SectionNode;
import ch.njol.skript.config.validate.SectionValidator;
import ch.njol.skript.lang.SkriptParser;
import ch.njol.skript.util.Utils;
import ch.njol.util.Pair;

/**
 * @author Peter Güttinger
 * 
 */
public abstract class Commands {
	
	private final static SectionValidator commandStructure = new SectionValidator()
			.addEntry("usage", true)
			.addEntry("description", true)
			.addEntry("permission", true)
			.addEntry("permission message", true)
			.addEntry("aliases", true)
			.addSection("trigger", false);
	
	public static List<Argument<?>> currentArguments = null;
	
	public final static boolean loadCommand(final SectionNode node) {
		
		final String s = node.getName();
		
		if (Skript.commandMap == null) {
			Skript.error("CraftBukkit is required to create new commands");
			return false;
		}
		
		final String argPattern = "<([a-zA-Z -]+?)\\s*(=\\s*(" + SkriptParser.wildcard + "))?>";
		
		Matcher m = Pattern.compile("(?i)^command /?(\\S+)(,? " + argPattern + ")*$").matcher(s);
		if (!m.matches()) {
			Skript.error("invalid command layout. It should look like '/command <arg 1>, <arg 2>, ...'");
			return false;
		}
		
		final String command = m.group(1);
		
		currentArguments = new ArrayList<Argument<?>>();
		m = Pattern.compile(argPattern).matcher(s);
		boolean hadNonSingle = false;
		for (int i = 0; m.find(); i++) {
			if (hadNonSingle) {
				Skript.error("All arguments must be singular except for the last one");
				return false;
			}
			Class<?> c;
			if (m.group(1).equalsIgnoreCase("text")) {
				c = String.class;
				hadNonSingle = true;
			} else {
				c = Skript.getClassFromUserInput(m.group(1));
				final Pair<String, Boolean> p = Utils.getPlural(m.group(1));
				if (c == null)
					c = Skript.getClassFromUserInput(p.first);
				if (p.second)
					hadNonSingle = true;
				if (c == null) {
					Skript.error("unknown type '" + p.first + "'");
					return false;
				}
			}
			final Argument<?> arg = Argument.newInstance(c, m.group(3), i, !hadNonSingle);
			if (arg == null)
				return false;
			currentArguments.add(arg);
		}
		
		node.convertToEntries(0);
		
		commandStructure.validate(node);
		if (!(node.get("trigger") instanceof SectionNode))
			return false;
		
		final String desc = "/" + command + " " + Utils.join(currentArguments);
		final String usage = node.get("usage", desc);
		final String description = node.get("description", "");
		List<String> aliases = Arrays.asList(node.get("aliases", "").split("\\s*,\\s*/?"));
		if (aliases.get(0).startsWith("/"))
			aliases.set(0, aliases.get(0).substring(1));
		else if (aliases.get(0).isEmpty())
			aliases = new ArrayList<String>(0);
		final String permission = node.get("permission", null);
		final String permissionMessage = node.get("permission message", null);
		final SectionNode trigger = (SectionNode) node.get("trigger");
		
		if (trigger == null) {
			Skript.error("A command must always have a 'trigger:' section where all effects & conditions are put");
			return false;
		}
		
		if (permissionMessage != null && permission == null) {
			Skript.warning("command /" + command + " has a permission message set, but not a permission.");
		}
		
		if (Skript.debug())
			Skript.info("command " + desc + ":");
		
		Skript.registerCommand(new SkriptCommand(command, currentArguments, description, usage, aliases, permission, permissionMessage, TriggerFileLoader.loadItems(trigger)));
		if (Skript.logVeryHigh() && !Skript.debug())
			Skript.info("registered command /" + command);
		currentArguments = null;
		return true;
	}
	
}
