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

import org.apache.commons.lang.Validate;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.help.HelpMap;
import org.bukkit.help.HelpTopic;

import ch.njol.skript.ScriptLoader;
import ch.njol.skript.Skript;
import ch.njol.skript.classes.ClassInfo;
import ch.njol.skript.config.SectionNode;
import ch.njol.skript.config.validate.SectionValidator;
import ch.njol.skript.lang.ParseContext;
import ch.njol.skript.lang.SkriptParser;
import ch.njol.skript.util.Utils;
import ch.njol.util.Pair;
import ch.njol.util.StringUtils;

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
			.addEntry("executable by", true)
			.addSection("trigger", false);
	
	public static List<Argument<?>> currentArguments = null;
	
	private final static String escape = Pattern.quote("(|)<>%\\");
	
	private final static String escape(final String s) {
		return s.replaceAll("([" + escape + "])", "\\\\$1");
	}
	
	private final static String unescape(final String s) {
		return s.replaceAll("\\\\([" + escape + "])", "$1");
	}
	
	public final static boolean loadCommand(final SectionNode node) {
		
		final String s = node.getName();
		
		int level = 0;
		for (int i = 0; i < s.length(); i++) {
			if (s.charAt(i) == '[') {
				level++;
			} else if (s.charAt(i) == ']') {
				if (level == 0) {
					Skript.error("Invalid placement of [optional brackets]");
					return false;
				}
				level--;
			}
		}
		if (level > 0) {
			Skript.error("Invalid amount of [optional brackets]");
			return false;
		}
		
		Matcher m = Pattern.compile("(?i)^command /?(\\S+)(\\s+(.+))?$").matcher(s);
		final boolean a = m.matches();
		assert a;
		
		final String command = m.group(1);
		if (Skript.commandExists(command)) {
			Skript.error("A command with the name /" + command + " is already defined");
			return false;
		}
		
		final String arguments = m.group(3) == null ? "" : m.group(3);
		final StringBuilder pattern = new StringBuilder();
		
		currentArguments = new ArrayList<Argument<?>>();
		m = Pattern.compile("<([a-zA-Z -]+?)\\s*(=\\s*(" + SkriptParser.wildcard + "))?>").matcher(arguments);
		int lastEnd = 0;
		int optionals = 0;
		for (int i = 0; m.find(); i++) {
			pattern.append(escape(arguments.substring(lastEnd, m.start())));
			optionals += StringUtils.count(arguments, '[', lastEnd, m.start());
			optionals -= StringUtils.count(arguments, ']', lastEnd, m.start());
			
			lastEnd = m.end();
			
			ClassInfo<?> c;
			c = Skript.getClassInfoFromUserInput(m.group(1));
			final Pair<String, Boolean> p = Utils.getPlural(m.group(1));
			if (c == null)
				c = Skript.getClassInfoFromUserInput(p.first);
			if (c == null) {
				Skript.error("unknown type '" + m.group(1) + "'");
				return false;
			}
			if (c.getParser() == null || !c.getParser().canParse(ParseContext.COMMAND)) {
				Skript.error("can't use " + m.group(1) + " as argument of a command");
				return false;
			}
			
			final Argument<?> arg = Argument.newInstance(c.getC(), m.group(3), i, !p.second, optionals > 0);
			if (arg == null)
				return false;
			currentArguments.add(arg);
			
			if (arg.isOptional() && optionals == 0) {
				pattern.append('[');
				optionals++;
			}
			pattern.append("%" + (arg.isOptional() ? "-" : "") + Utils.toPlural(c.getCodeName(), p.second) + "%");
		}
		
		pattern.append(escape(arguments.substring(lastEnd)));
		optionals += StringUtils.count(arguments, '[', lastEnd);
		optionals -= StringUtils.count(arguments, ']', lastEnd);
		for (int i = 0; i < optionals; i++)
			pattern.append("]");
		
		node.convertToEntries(0);
		commandStructure.validate(node);
		if (!(node.get("trigger") instanceof SectionNode))
			return false;
		
		final String desc = "/" + command + " " + unescape(pattern.toString().replaceAll("%-?(.+?)%", "<$1>"));
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
		final String[] by = node.get("executable by", "console,players").split("\\s*,\\s*|\\s+(and|or)\\s+");
		int executableBy = 0;
		for (final String b : by) {
			if (b.equalsIgnoreCase("console") || b.equalsIgnoreCase("the console")) {
				executableBy |= SkriptCommand.CONSOLE;
			} else if (b.equalsIgnoreCase("players") || b.equalsIgnoreCase("player")) {
				executableBy |= SkriptCommand.PLAYERS;
			} else {
				Skript.error("'executable by' should be either be 'players', 'console' or both, but found '" + b + "'");
			}
		}
		
		if (permissionMessage != null && permission == null) {
			Skript.warning("command /" + command + " has a permission message set, but not a permission");
		}
		
		if (Skript.debug())
			Skript.info("command " + desc + ":");
		
		Skript.registerCommand(new SkriptCommand(command, pattern.toString().replaceAll("[<>]", "\\\\$0"), currentArguments, description, usage, aliases, permission, permissionMessage, executableBy, ScriptLoader.loadItems(trigger)));
		if (Skript.logVeryHigh() && !Skript.debug())
			Skript.info("registered command " + desc);
		currentArguments = null;
		return true;
	}
	
	/**
	 * copied from CraftBukkit
	 */
	public final static class CommandAliasHelpTopic extends HelpTopic {
		
		private final String aliasFor;
		private final HelpMap helpMap;
		
		public CommandAliasHelpTopic(final String alias, final String aliasFor, final HelpMap helpMap) {
			this.aliasFor = aliasFor.startsWith("/") ? aliasFor : "/" + aliasFor;
			this.helpMap = helpMap;
			name = alias.startsWith("/") ? alias : "/" + alias;
			Validate.isTrue(!name.equals(this.aliasFor), "Command " + name + " cannot be alias for itself");
			shortText = ChatColor.YELLOW + "Alias for " + ChatColor.WHITE + this.aliasFor;
		}
		
		@Override
		public String getFullText(final CommandSender forWho) {
			final StringBuilder sb = new StringBuilder(shortText);
			final HelpTopic aliasForTopic = helpMap.getHelpTopic(aliasFor);
			if (aliasForTopic != null) {
				sb.append("\n");
				sb.append(aliasForTopic.getFullText(forWho));
			}
			return sb.toString();
		}
		
		@Override
		public boolean canSee(final CommandSender commandSender) {
			if (amendedPermission == null) {
				final HelpTopic aliasForTopic = helpMap.getHelpTopic(aliasFor);
				if (aliasForTopic != null) {
					return aliasForTopic.canSee(commandSender);
				} else {
					return false;
				}
			} else {
				return commandSender.hasPermission(amendedPermission);
			}
		}
	}
	
}
