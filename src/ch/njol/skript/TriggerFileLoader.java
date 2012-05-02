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

import ch.njol.skript.api.Condition;
import ch.njol.skript.api.LoopVar;
import ch.njol.skript.api.SkriptEvent;
import ch.njol.skript.api.SkriptEvent.SkriptEventInfo;
import ch.njol.skript.api.intern.Conditional;
import ch.njol.skript.api.intern.Expression.Expressions;
import ch.njol.skript.api.intern.Loop;
import ch.njol.skript.api.intern.TopLevelExpression;
import ch.njol.skript.api.intern.Trigger;
import ch.njol.skript.api.intern.TriggerItem;
import ch.njol.skript.api.intern.TriggerSection;
import ch.njol.skript.api.intern.Variable;
import ch.njol.skript.command.Commands;
import ch.njol.skript.config.Config;
import ch.njol.skript.config.Node;
import ch.njol.skript.config.SectionNode;
import ch.njol.skript.config.SimpleNode;
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
	public static ArrayList<TriggerSection> currentSections = new ArrayList<TriggerSection>();
	public static ArrayList<LoopVar<?>> currentLoops = new ArrayList<LoopVar<?>>();
	
	static int loadedTriggers = 0, loadedCommands = 0;
	
	private static String indentation = "";
	
	public static ArrayList<TriggerItem> loadItems(final SectionNode node) {
		if (Skript.logExtreme())
			indentation += "    ";
		final ArrayList<TriggerItem> items = new ArrayList<TriggerItem>();
		for (final Node n : node) {
			if (n.isVoid())
				continue;
			SkriptLogger.setNode(n);
			if (n instanceof SimpleNode) {
				final SimpleNode e = (SimpleNode) n;
				
				final TopLevelExpression expr = TopLevelExpression.parse(e.getName());
				if (expr == null) {
					Skript.printErrorAndCause("can't understand this condition/effect: '" + e.getName() + "'");
					continue;
				}
				Skript.printWarnings();
				Skript.clearErrorCause();
				if (Skript.logExtreme())
					Skript.info(indentation + expr.getDebugMessage(null));
				items.add(expr);
			} else if (n instanceof SectionNode) {
				if (n.getName().startsWith("loop ")) {
					final LoopVar<?> loopvar = (LoopVar<?>) Variable.parseNoLiteral(n.getName().substring("loop ".length()), Skript.loops.listIterator());
					if (loopvar == null) {
						Skript.printErrorAndCause("can't understand this loop: '" + n.getName() + "'");
						continue;
					}
					Skript.printWarnings();
					Skript.clearErrorCause();
					if (Skript.logExtreme())
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
					if (Skript.logExtreme())
						Skript.info(indentation + "else:");
					((Conditional) items.get(items.size() - 1)).loadElseClause((SectionNode) n);
				} else {
					final Condition cond = Condition.parse(n.getName());
					if (cond == null) {
						Skript.printErrorAndCause("can't understand this condition: '" + n.getName() + "'");
						continue;
					}
					Skript.printWarnings();
					Skript.clearErrorCause();
					if (Skript.logExtreme())
						Skript.info(indentation + cond.getDebugMessage(null) + ":");
					final Conditional c = new Conditional(cond, (SectionNode) n);
					items.add(c);
				}
			}
		}
		SkriptLogger.setNode(node);
		if (Skript.logExtreme())
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
		for (final Node cnode : config.getMainNode()) {
			if (cnode.isVoid())
				continue;
			if (!(cnode instanceof SectionNode)) {
				Skript.error("invalid line");
				continue;
			}
			
			final SectionNode node = ((SectionNode) cnode);
			final String s = node.getName();
			
			if (StringUtils.count(s, '"') % 2 != 0) {
				Skript.error(Skript.quotesError);
			}
			
			if (s.startsWith("command ")) {
				if (Commands.loadCommand(node))
					numCommands++;
				continue;
			}
			if (Skript.logVeryHigh() && !Skript.logExtreme())
				Skript.info("loading trigger '" + s + "'");
			
			final Pair<SkriptEventInfo, SkriptEvent> event = Expressions.parseEvent(s);
			if (event == null) {
				Skript.printErrorAndCause("can't understand this event: '" + s + "'");
				continue;
			}
			
			else if (Skript.logExtreme())
				Skript.info(s + " (" + event.second.getDebugMessage(null) + "):");
			
			currentEvent = event.second;
			final Trigger trigger = new Trigger(s, event.second, loadItems(node));
			SkriptEventHandler.addTrigger(event.first.events, trigger);
			numTriggers++;
		}
		
		if (Skript.logHigh())
			Skript.info("loaded " + numTriggers + " trigger" + (numTriggers == 1 ? "" : "s") + " and " + numCommands + " command" + (numCommands == 1 ? "" : "s") + (!Skript.logVeryHigh() ? " from '" + config.getFileName() + "'" : ""));
		
		loadedCommands += numCommands;
		loadedTriggers += numTriggers;
	}
	
}
