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

package ch.njol.skript;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.regex.Matcher;

import org.bukkit.event.Event;

import ch.njol.skript.api.Condition;
import ch.njol.skript.api.LoopExpr;
import ch.njol.skript.api.SkriptEvent;
import ch.njol.skript.api.SkriptEvent.SkriptEventInfo;
import ch.njol.skript.api.intern.Conditional;
import ch.njol.skript.api.intern.Loop;
import ch.njol.skript.api.intern.Statement;
import ch.njol.skript.api.intern.Trigger;
import ch.njol.skript.api.intern.TriggerItem;
import ch.njol.skript.api.intern.TriggerSection;
import ch.njol.skript.command.CommandEvent;
import ch.njol.skript.command.Commands;
import ch.njol.skript.config.Config;
import ch.njol.skript.config.EntryNode;
import ch.njol.skript.config.Node;
import ch.njol.skript.config.SectionNode;
import ch.njol.skript.config.SimpleNode;
import ch.njol.skript.lang.SkriptParser;
import ch.njol.skript.util.ItemType;
import ch.njol.util.Callback;
import ch.njol.util.Pair;
import ch.njol.util.StringUtils;

/**
 * 
 * @author Peter Güttinger
 * 
 */
final public class TriggerFileLoader {
	private TriggerFileLoader() {}
	
	public static SkriptEvent currentEvent = null;
	public static Class<? extends Event>[] currentEvents = null;
	
	public static List<TriggerSection> currentSections = new ArrayList<TriggerSection>();
	public static List<LoopExpr<?>> currentLoops = new ArrayList<LoopExpr<?>>();
	public static final Map<String, ItemType> currentAliases = new HashMap<String, ItemType>();
	public static final HashMap<String, String> options = new HashMap<String, String>();
	
	static int loadedTriggers = 0, loadedCommands = 0;
	
	private static String indentation = "";
	
	private final static String replaceOptions(final String s) {
		return StringUtils.replaceAll(s, "\\{@(.+?)\\}", new Callback<String, Matcher>() {
			@Override
			public String run(final Matcher m) {
				final String option = options.get(m.group(1));
				if (option == null) {
					Skript.error("undefined option " + m.group());
					return null;
				}
				return option;
			}
		});
	}
	
	public static ArrayList<TriggerItem> loadItems(final SectionNode node) {
		
		if (Skript.debug())
			indentation += "    ";
		
		final ArrayList<TriggerItem> items = new ArrayList<TriggerItem>();
		
		for (final Node n : node) {
			SkriptLogger.setNode(n);
			if (n instanceof SimpleNode) {
				final SimpleNode e = (SimpleNode) n;
				final String ex = replaceOptions(e.getName());
				if (ex == null)
					continue;
				final Statement expr = Statement.parse(ex, "can't understand this condition/effect: '" + ex + "'");
				if (expr == null) {
					continue;
				}
				if (Skript.debug())
					Skript.info(indentation + expr.getDebugMessage(null));
				items.add(expr);
			} else if (n instanceof SectionNode) {
				if (n.getName().startsWith("loop ")) {
					final String l = replaceOptions(n.getName().substring("loop ".length()));
					if (l == null)
						continue;
					final LoopExpr<?> loopvar = (LoopExpr<?>) SkriptParser.parse(l, Skript.loops.listIterator(), false, false, "can't understand this loop: '" + n.getName() + "'");
					if (loopvar == null) {
						continue;
					}
					if (Skript.debug())
						Skript.info(indentation + "loop " + loopvar.getLoopDebugMessage(null) + ":");
					currentLoops.add(loopvar);
					final Loop loop = new Loop(loopvar, (SectionNode) n);
					currentLoops.remove(currentLoops.size() - 1);
					items.add(loop);
				} else if (n.getName().equalsIgnoreCase("else")) {
					if (items.size() == 0 || !(items.get(items.size() - 1) instanceof Conditional)) {
						Skript.error("'else' has to be placed just after the end of a conditional section");
						continue;
					}
					if (Skript.debug())
						Skript.info(indentation + "else:");
					((Conditional) items.get(items.size() - 1)).loadElseClause((SectionNode) n);
				} else {
					String name = n.getName();
					if (name.startsWith("if "))
						name = name.substring(3);
					final Condition cond = Condition.parse(name, "can't understand this condition: '" + name + "'");
					if (cond == null) {
						continue;
					}
					if (Skript.debug())
						Skript.info(indentation + cond.getDebugMessage(null) + ":");
					items.add(new Conditional(cond, (SectionNode) n));
				}
			}
		}
		
		SkriptLogger.setNode(node);
		
		if (Skript.debug())
			indentation = indentation.substring(0, indentation.length() - 4);
		
		return items;
	}
	
	/**
	 * Loads triggers and commands from a config.
	 * 
	 * @param config Config to load from
	 * @return how many triggers and commands were loaded (in this order)
	 */
	@SuppressWarnings("unchecked")
	static void load(final Config config) {
		int numTriggers = 0;
		int numCommands = 0;
		
		currentAliases.clear();
		options.clear();
		
		for (final Node cnode : config.getMainNode()) {
			if (!(cnode instanceof SectionNode)) {
				Skript.error("invalid line in trigger file");
				continue;
			}
			
			final SectionNode node = ((SectionNode) cnode);
			String event = node.getName();
			
			if (event.equalsIgnoreCase("aliases")) {
				node.convertToEntries(0, "=");
				for (final Node n : node) {
					if (!(n instanceof EntryNode)) {
						Skript.error("invalid line in alias section");
						continue;
					}
					final ItemType t = Aliases.parseAlias(((EntryNode) n).getValue());
					if (t == null)
						continue;
					currentAliases.put(((EntryNode) n).getKey().toLowerCase(Locale.ENGLISH), t);
				}
				continue;
			} else if (event.equalsIgnoreCase("options")) {
				node.convertToEntries(0);
				for (final Node n : node) {
					if (!(n instanceof EntryNode)) {
						Skript.error("invalid line in options");
						continue;
					}
					options.put(((EntryNode) n).getKey(), ((EntryNode) n).getValue());
				}
				continue;
			}
			
			if (StringUtils.count(event, '"') % 2 != 0) {
				Skript.error(Skript.quotesError);
				continue;
			}
			
			if (event.toLowerCase().startsWith("command ")) {
				currentEvent = null;
				currentEvents = Skript.array(CommandEvent.class);
				if (Commands.loadCommand(node))
					numCommands++;
				continue;
			}
			if (Skript.logVeryHigh() && !Skript.debug())
				Skript.info("loading trigger '" + event + "'");
			
			if (event.toLowerCase().startsWith("on "))
				event = event.substring("on ".length());
			event = replaceOptions(event);
			if (event == null)
				continue;
			final Pair<SkriptEventInfo<?>, SkriptEvent> parsedEvent = SkriptParser.parseEvent(event, "can't understand this event: '" + node.getName() + "'");
			if (parsedEvent == null) {
				continue;
			}
			
			if (Skript.debug())
				Skript.info(event + " (" + parsedEvent.second.getDebugMessage(null) + "):");
			
			currentEvent = parsedEvent.second;
			currentEvents = parsedEvent.first.events;
			
			final Trigger trigger = new Trigger(event, parsedEvent.second, loadItems(node));
			
			SkriptEventHandler.addTrigger(parsedEvent.first.events, trigger);
			
			numTriggers++;
		}
		
		if (Skript.logHigh())
			Skript.info("loaded " + numTriggers + " trigger" + (numTriggers == 1 ? "" : "s") + " and " + numCommands + " command" + (numCommands == 1 ? "" : "s") + (!Skript.logVeryHigh() ? " from '" + config.getFileName() + "'" : ""));
		
		loadedCommands += numCommands;
		loadedTriggers += numTriggers;
	}
	
}
