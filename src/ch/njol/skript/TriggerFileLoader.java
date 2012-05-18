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

import ch.njol.skript.api.Condition;
import ch.njol.skript.api.LoopVar;
import ch.njol.skript.api.SkriptEvent;
import ch.njol.skript.api.SkriptEvent.SkriptEventInfo;
import ch.njol.skript.api.intern.Conditional;
import ch.njol.skript.api.intern.Loop;
import ch.njol.skript.api.intern.TopLevelExpression;
import ch.njol.skript.api.intern.Trigger;
import ch.njol.skript.api.intern.TriggerItem;
import ch.njol.skript.api.intern.TriggerSection;
import ch.njol.skript.command.Commands;
import ch.njol.skript.config.Config;
import ch.njol.skript.config.EntryNode;
import ch.njol.skript.config.Node;
import ch.njol.skript.config.SectionNode;
import ch.njol.skript.config.SimpleNode;
import ch.njol.skript.lang.ExprParser;
import ch.njol.skript.util.ErrorSession;
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
	public static List<TriggerSection> currentSections = new ArrayList<TriggerSection>();
	public static List<LoopVar<?>> currentLoops = new ArrayList<LoopVar<?>>();
	public static final Map<String, ItemType> currentAliases = new HashMap<String, ItemType>();
	public static final HashMap<String, String> options = new HashMap<String, String>();
	
	static int loadedTriggers = 0, loadedCommands = 0, loadedAliases = 0;
	
	private static String indentation = "";
	
	private final static String replaceOptions(String s) {
		return StringUtils.replaceAll(s, "\\{(.+?)\\}", new Callback<String, Matcher>() {
			@Override
			public String run(Matcher m) {
				String option = options.get(m.group(1));
				if (option == null) {
					Skript.getCurrentErrorSession().severeError("undefined option "+m.group());
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
		
		final ErrorSession session = Skript.startErrorSession();
		
		for (final Node n : node) {
			SkriptLogger.setNode(n);
			if (n instanceof SimpleNode) {
				final SimpleNode e = (SimpleNode) n;
				String ex = replaceOptions(e.getName());
				if (ex == null)
					continue;
				final TopLevelExpression expr = TopLevelExpression.parse(ex);
				if (expr == null) {
					session.printErrors("can't understand this condition/effect: '" + e.getName() + "'");
					continue;
				}
				session.printWarnings();
				session.clearErrors();
				if (Skript.debug())
					Skript.info(indentation + expr.getDebugMessage(null));
				items.add(expr);
			} else if (n instanceof SectionNode) {
				if (n.getName().startsWith("loop ")) {
					String l = replaceOptions(n.getName().substring("loop ".length()));
					if (l == null)
						continue;
					final LoopVar<?> loopvar = (LoopVar<?>) ExprParser.parse(l, Skript.loops.listIterator(), false);
					if (loopvar == null) {
						session.printErrors("can't understand this loop: '" + n.getName() + "'");
						continue;
					}
					session.printWarnings();
					session.clearErrors();
					if (Skript.debug())
						Skript.info(indentation + "loop " + loopvar.getLoopDebugMessage(null) + ":");
					currentLoops.add(loopvar);
					final Loop loop = new Loop(loopvar, (SectionNode) n);
					currentLoops.remove(currentLoops.size() - 1);
					items.add(loop);
				} else if (n.getName().equalsIgnoreCase("else")) {
					if (items.size() == 0 || !(items.get(items.size() - 1) instanceof Conditional)) {
						session.severeError("'else' has to be placed just after the end of a conditional section");
						continue;
					}
					if (Skript.debug())
						Skript.info(indentation + "else:");
					((Conditional) items.get(items.size() - 1)).loadElseClause((SectionNode) n);
				} else {
					String c = n.getName();
					if (c == null)
						continue;
					final Condition cond = Condition.parse(c);
					if (cond == null) {
						session.printErrors("can't understand this condition: '" + n.getName() + "'");
						continue;
					}
					session.printWarnings();
					session.clearErrors();
					if (Skript.debug())
						Skript.info(indentation + cond.getDebugMessage(null) + ":");
					items.add(new Conditional(cond, (SectionNode) n));
				}
			}
		}
		
		SkriptLogger.setNode(node);
		Skript.stopErrorSession();
		
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
	static void load(final Config config) {
		int numTriggers = 0;
		int numCommands = 0;
		final ErrorSession session = Skript.startErrorSession();
		currentAliases.clear();
		options.clear();
		
		for (final Node cnode : config.getMainNode()) {
			session.clearErrors();
			if (!(cnode instanceof SectionNode)) {
				session.severeError("invalid line in trigger file");
				continue;
			}
			
			final SectionNode node = ((SectionNode) cnode);
			String event = node.getName();
			
			if (event.equalsIgnoreCase("aliases")) {
				node.convertToEntries(0, "=");
				for (Node n:node) {
					if (!(n instanceof EntryNode)) {
						session.severeError("invalid line in alias section");
						continue;
					}
					ItemType t = Aliases.parseAlias(((EntryNode) n).getValue());
					if (t == null) {
						session.printErrors("'"+((EntryNode) n).getValue()+"' is not an alias");
					} else {
						currentAliases.put(((EntryNode) n).getKey().toLowerCase(Locale.ENGLISH), t);
						loadedAliases++;
					}
				}
				continue;
			} else if (event.equalsIgnoreCase("options")) {
				node.convertToEntries(0);
				for (Node n:node) {
					if (!(n instanceof EntryNode)) {
						session.severeError("invalid line in options");
						continue;
					}
					options.put(((EntryNode)n).getKey(), ((EntryNode)n).getValue());
				}
				continue;
			}
			
			if (StringUtils.count(event, '"') % 2 != 0) {
				session.error(Skript.quotesError);
			}
			
			if (event.startsWith("command ")) {
				if (Commands.loadCommand(node))
					numCommands++;
				continue;
			}
			if (Skript.logVeryHigh() && !Skript.debug())
				Skript.info("loading trigger '" + event + "'");
			
			session.clearErrors();
			if (event.toLowerCase().startsWith("on "))
				event = event.substring("on ".length());
			event = replaceOptions(event);
			if (event == null)
				continue;
			final Pair<SkriptEventInfo<?>, SkriptEvent> parsedEvent = ExprParser.parseEvent(event);
			if (parsedEvent == null) {
				session.printErrors("can't understand this event: '" + node.getName() + "'");
				continue;
			}
			
			if (Skript.debug())
				Skript.info(event + " (" + parsedEvent.second.getDebugMessage(null) + "):");
			
			currentEvent = parsedEvent.second;
			final Trigger trigger = new Trigger(event, parsedEvent.second, loadItems(node));
			SkriptEventHandler.addTrigger(parsedEvent.first.events, trigger);
			numTriggers++;
		}
		
		Skript.stopErrorSession();
		
		if (Skript.logHigh())
			Skript.info("loaded " + numTriggers + " trigger" + (numTriggers == 1 ? "" : "s") + " and " + numCommands + " command" + (numCommands == 1 ? "" : "s") + (!Skript.logVeryHigh() ? " from '" + config.getFileName() + "'" : ""));
		
		loadedCommands += numCommands;
		loadedTriggers += numTriggers;
	}
	
}
