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
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;

import org.bukkit.event.Event;

import ch.njol.skript.SkriptLogger.SubLog;
import ch.njol.skript.classes.ClassInfo;
import ch.njol.skript.command.CommandEvent;
import ch.njol.skript.command.Commands;
import ch.njol.skript.config.Config;
import ch.njol.skript.config.EntryNode;
import ch.njol.skript.config.Node;
import ch.njol.skript.config.SectionNode;
import ch.njol.skript.config.SimpleNode;
import ch.njol.skript.effects.EffDelay;
import ch.njol.skript.lang.Condition;
import ch.njol.skript.lang.Conditional;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.Loop;
import ch.njol.skript.lang.ParseContext;
import ch.njol.skript.lang.SkriptEvent;
import ch.njol.skript.lang.SkriptEvent.SkriptEventInfo;
import ch.njol.skript.lang.SkriptParser;
import ch.njol.skript.lang.Statement;
import ch.njol.skript.lang.Trigger;
import ch.njol.skript.lang.TriggerItem;
import ch.njol.skript.lang.TriggerSection;
import ch.njol.skript.util.ItemType;
import ch.njol.util.Callback;
import ch.njol.util.Pair;
import ch.njol.util.StringUtils;

/**
 * 
 * @author Peter Güttinger
 * 
 */
final public class ScriptLoader {
	private ScriptLoader() {}
	
	public static Config currentScript = null;
	
	public static SkriptEvent currentEvent = null;
	public static Class<? extends Event>[] currentEvents = null;
	
	public static List<TriggerSection> currentSections = new ArrayList<TriggerSection>();
	public static List<Loop> currentLoops = new ArrayList<Loop>();
	public static final Map<String, ItemType> currentAliases = new HashMap<String, ItemType>();
	public static final HashMap<String, String> currentOptions = new HashMap<String, String>();
	
	static int loadedTriggers = 0, loadedCommands = 0;
	
	private static String indentation = "";
	
	final static Collection<Trigger> selfRegisteredTriggers = new ArrayList<Trigger>();
	
	private final static String replaceOptions(final String s) {
		return StringUtils.replaceAll(s, "\\{@(.+?)\\}", new Callback<String, Matcher>() {
			@Override
			public String run(final Matcher m) {
				final String option = currentOptions.get(m.group(1));
				if (option == null) {
					Skript.error("undefined option " + m.group());
					return null;
				}
				return option;
			}
		});
	}
	
	public static boolean hasDelayBefore = false;
	
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
				final Statement stmt = Statement.parse(ex, "can't understand this condition/effect: '" + ex + "'");
				if (stmt == null)
					continue;
				if (Skript.debug())
					Skript.info(indentation + stmt.toString(null, true));
				items.add(stmt);
				if (stmt instanceof EffDelay)
					hasDelayBefore = true;
			} else if (n instanceof SectionNode) {
				if (n.getName().startsWith("loop ")) {
					final String l = replaceOptions(n.getName().substring("loop ".length()));
					if (l == null)
						continue;
					final Expression<?> loopedExpr = (Expression<?>) SkriptParser.parse(l, Skript.getExpressions().iterator(), false, false, "can't understand this expression: '" + n.getName() + "'");
					if (loopedExpr == null)
						continue;
					if (!loopedExpr.canLoop()) {
						Skript.error("Can't loop " + loopedExpr);
						continue;
					}
					if (Skript.debug())
						Skript.info(indentation + "loop " + loopedExpr.toString(null, true) + ":");
					final Loop loop = new Loop(loopedExpr, (SectionNode) n);
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
						Skript.info(indentation + cond.toString(null, true) + ":");
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
	 */
	@SuppressWarnings("unchecked")
	static void load(final Config config) {
		int numTriggers = 0;
		int numCommands = 0;
		
		currentAliases.clear();
		currentOptions.clear();
		
		currentScript = config;
		
		for (final Node cnode : config.getMainNode()) {
			if (!(cnode instanceof SectionNode)) {
				Skript.error("invalid line - all code has to be put into triggers");
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
					currentAliases.put(((EntryNode) n).getKey().toLowerCase(), t);
				}
				continue;
			} else if (event.equalsIgnoreCase("options")) {
				node.convertToEntries(0);
				for (final Node n : node) {
					if (!(n instanceof EntryNode)) {
						Skript.error("invalid line in options");
						continue;
					}
					currentOptions.put(((EntryNode) n).getKey(), ((EntryNode) n).getValue());
				}
				continue;
			} else if (event.equalsIgnoreCase("variables")) {
				node.convertToEntries(0, "=");
				for (final Node n : node) {
					if (!(n instanceof EntryNode)) {
						Skript.error("invalid line in variables");
						continue;
					}
					String name = ((EntryNode) n).getKey();
					if (name.startsWith("{") && name.endsWith("}"))
						name = name.substring(1, name.length() - 1);
					final String var = name;
					name = StringUtils.replaceAll(name, "%(.+)?%", new Callback<String, Matcher>() {
						@Override
						public String run(final Matcher m) {
							if (m.group(1).contains("{") || m.group(1).contains("}") || m.group(1).contains("%")) {
								Skript.error("'" + var + "' is not a valid name for a default variable");
								return null;
							}
							final ClassInfo<?> ci = Skript.getClassInfoFromUserInput(m.group(1));
							if (ci == null) {
								Skript.error("Can't understand the type '" + m.group(1) + "'");
								return null;
							}
							return "<" + ci.getCodeName() + ">";
						}
					});
					if (name == null || name.contains("%")) {
						continue;
					}
					if (Skript.getVariable(name) != null)
						continue;
					final SubLog log = SkriptLogger.startSubLog();
					Object o = Skript.parseSimple(((EntryNode) n).getValue(), Object.class, ParseContext.CONFIG);
					SkriptLogger.stopSubLog(log);
					if (o == null) {
						log.printErrors("Can't understand the value '" + ((EntryNode) n).getValue() + "'");
						continue;
					}
					final ClassInfo<?> ci = Skript.getSuperClassInfo(o.getClass());
					if (ci.getSerializeAs() != null) {
						final ClassInfo<?> as = Skript.getSuperClassInfo(ci.getSerializeAs());
						if (as == null) {
							Skript.exception("Missing class info for " + ci.getSerializeAs().getName() + ", the class to serialize " + ci.getC().getName() + " as");
							continue;
						}
						o = Skript.convert(o, as.getC());
						if (o == null) {
							Skript.error("Can't save '" + ((EntryNode) n).getValue() + "' in a variable");
							continue;
						}
					}
					Skript.setVariable(name, o);
				}
				continue;
			}
			
			if (StringUtils.count(event, '"') % 2 != 0) {
				Skript.error(Skript.quotesError);
				continue;
			}
			
			hasDelayBefore = false;
			
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
				Skript.info(event + " (" + parsedEvent.second.toString(null, true) + "):");
			
			currentEvent = parsedEvent.second;
			currentEvents = parsedEvent.first.events;
			
			final Trigger trigger = new Trigger(event, parsedEvent.second, loadItems(node));
			
			currentEvent = null;
			currentEvents = null;
			
			if (parsedEvent.first.fire) {
				SkriptEventHandler.addTrigger(parsedEvent.first.events, trigger);
			} else {
				parsedEvent.second.register(trigger);
				selfRegisteredTriggers.add(trigger);
			}
			
			numTriggers++;
		}
		
		if (Skript.logHigh())
			Skript.info("loaded " + numTriggers + " trigger" + (numTriggers == 1 ? "" : "s") + " and " + numCommands + " command" + (numCommands == 1 ? "" : "s") + " from '" + config.getFileName() + "'");
		
		loadedCommands += numCommands;
		loadedTriggers += numTriggers;
		
		currentScript = null;
		
	}
	
	/**
	 * For unit testing
	 * 
	 * @param node
	 * @return
	 */
	static Trigger loadTrigger(final SectionNode node) {
		String event = node.getName();
		if (event.toLowerCase().startsWith("on "))
			event = event.substring("on ".length());
		
		final Pair<SkriptEventInfo<?>, SkriptEvent> parsedEvent = SkriptParser.parseEvent(event, "can't understand this event: '" + node.getName() + "'");
		
		currentEvent = parsedEvent.second;
		currentEvents = parsedEvent.first.events;
		
		final Trigger t = new Trigger(event, parsedEvent.second, loadItems(node));
		
		currentEvent = null;
		currentEvents = null;
		
		return t;
	}
	
}
