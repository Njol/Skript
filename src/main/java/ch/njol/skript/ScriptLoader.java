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

import java.io.File;
import java.io.FileFilter;
import java.io.FilenameFilter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;

import org.bukkit.Bukkit;
import org.bukkit.event.Event;
import org.bukkit.event.Listener;

import ch.njol.skript.SkriptLogger.SubLog;
import ch.njol.skript.classes.ClassInfo;
import ch.njol.skript.command.CommandEvent;
import ch.njol.skript.command.Commands;
import ch.njol.skript.config.Config;
import ch.njol.skript.config.EntryNode;
import ch.njol.skript.config.Node;
import ch.njol.skript.config.SectionNode;
import ch.njol.skript.config.SimpleNode;
import ch.njol.skript.effects.Delay;
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
import ch.njol.skript.util.Date;
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
	
	public static int hasDelayBefore = -1;
	
	public final static class ScriptInfo {
		public int files, triggers, commands;
		
		public ScriptInfo() {}
		
		public ScriptInfo(final int numFiles, final int numTriggers, final int numCommands) {
			files = numFiles;
			triggers = numTriggers;
			commands = numCommands;
		}
		
		public void add(final ScriptInfo other) {
			files += other.files;
			triggers += other.triggers;
			commands += other.commands;
		}
	}
	
	private static String indentation = "";
	
	final static List<Trigger> selfRegisteredTriggers = new ArrayList<Trigger>();
	
	/**
	 * As it's impossible to unregister events with Bukkit this set is used to prevent that any event will ever be registered more than once when reloading.
	 */
	private final static Set<Class<? extends Event>> registeredEvents = new HashSet<Class<? extends Event>>();
	
	static ScriptInfo loadScripts() {
		final File scriptsFolder = new File(Skript.getInstance().getDataFolder(), Skript.SCRIPTSFOLDER + File.separatorChar);
		
		final File oldFolder = new File(Skript.getInstance().getDataFolder(), "triggers" + File.separatorChar);
		if (oldFolder.isDirectory()) {
			if (!scriptsFolder.isDirectory()) {
				oldFolder.renameTo(scriptsFolder);
				Skript.info("[1.3] Renamed your 'triggers' folder to 'scripts' to match the new format");
			} else {
				Skript.error("Found both a 'triggers' and a 'scripts' folder, ignoring the 'triggers' folder");
			}
		}
		
		if (!scriptsFolder.isDirectory())
			scriptsFolder.mkdirs();
		
		int renamed = 0;
		for (final File f : scriptsFolder.listFiles(new FilenameFilter() {
			@Override
			public boolean accept(final File dir, final String name) {
				return name.endsWith(".cfg");
			}
		})) {
			final String name = f.getName().substring(0, f.getName().length() - ".cfg".length());
			final File n = new File(scriptsFolder, name + ".sk");
			if (!n.exists()) {
				f.renameTo(n);
				renamed++;
			} else {
				Skript.error("Found both an old and a new script named '" + name + "', ignoring the old one");
			}
		}
		if (renamed > 0)
			Skript.info("[1.3] Renamed " + renamed + " scripts to match the new format");
		
		final SubLog log = SkriptLogger.startSubLog(); // TODO improve this - it's only used to find out whether there were any errors or not
		final Date start = new Date();
		final ScriptInfo i = loadScripts(scriptsFolder);
		log.stop();
		log.printLog();
		if (!log.hasErrors())
			Skript.info("All scripts loaded without errors!");
		
		if (i.files == 0)
			Skript.warning("No scripts were found, maybe you should write some ;)");
		if (Skript.logNormal() && i.files > 0)
			Skript.info("loaded " + i.files + " script" + (i.files == 1 ? "" : "s")
					+ " with a total of " + i.triggers + " trigger" + (i.triggers == 1 ? "" : "s")
					+ " and " + i.commands + " command" + (i.commands == 1 ? "" : "s")
					+ " in " + start.difference(new Date()));
		
		registerBukkitEvents();
		
		return i;
	}
	
	private final static ScriptInfo loadScripts(final File directory) {
		final ScriptInfo i = new ScriptInfo();
		for (final File f : directory.listFiles(new FileFilter() {
			@Override
			public boolean accept(final File f) {
				return (f.isDirectory() || f.getName().endsWith(".sk")) && !f.getName().startsWith("-");
			}
		})) {
			if (f.isDirectory()) {
				i.add(loadScripts(f));
			} else {
				i.add(loadScript(f));
			}
		}
		return i;
	}
	
	public final static ScriptInfo loadScripts(final Collection<File> files) {
		final ScriptInfo i = new ScriptInfo();
		for (final File f : files) {
			i.add(loadScript(f));
		}
		registerBukkitEvents();
		return i;
	}
	
	final static ScriptInfo loadScript(final File f) {
		try {
			final Config c = new Config(f, true, false, ":");
			if (Skript.keepConfigsLoaded)
				Skript.configs.add(c);
			return loadScript(c);
		} catch (final IOException e) {
			Skript.error("Could not load " + f.getName() + ": " + e.getLocalizedMessage());
		} catch (final Exception e) {
			Skript.exception(e, "Could not load " + f.getName());
		} finally {
			SkriptLogger.setNode(null);
		}
		return new ScriptInfo();
	}
	
	private final static void registerBukkitEvents() {
		for (final Class<? extends Event> e : SkriptEventHandler.triggers.keySet()) {
			if (!registeredEvents.contains(e)) {
				Bukkit.getPluginManager().registerEvent(e, new Listener() {}, Skript.priority, SkriptEventHandler.ee, Skript.getInstance());
				registeredEvents.add(e);
			}
		}
	}
	
	final static ScriptInfo unloadScript(final File script) {
		final ScriptInfo info = new ScriptInfo();
		final Iterator<List<Trigger>> triggersIter = SkriptEventHandler.triggers.values().iterator();
		while (triggersIter.hasNext()) {
			final List<Trigger> ts = triggersIter.next();
			for (int i = 0; i < ts.size(); i++) {
				if (ts.get(i).getScript().equals(script)) {
					info.triggers++;
					ts.remove(i);
					i--;
					if (ts.isEmpty())
						triggersIter.remove();
				}
			}
		}
		for (int i = 0; i < ScriptLoader.selfRegisteredTriggers.size(); i++) {
			final Trigger t = ScriptLoader.selfRegisteredTriggers.get(i);
			if (t.getScript().equals(script)) {
				info.triggers++;
				t.getEvent().unregister(t);
				ScriptLoader.selfRegisteredTriggers.remove(i);
				i--;
			}
		}
		info.commands = Commands.unregisterCommands(script);
		
		return info;
	}
	
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
	
	public static ArrayList<TriggerItem> loadItems(final SectionNode node) {
		
		if (Skript.debug())
			indentation += "    ";
		
		final ArrayList<TriggerItem> items = new ArrayList<TriggerItem>();
		
		int hadDelayBeforeLastIf = -1;
		
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
				if (stmt instanceof Delay)
					hasDelayBefore = 1;
			} else if (n instanceof SectionNode) {
				if (StringUtils.startsWithIgnoreCase(n.getName(), "loop ")) {
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
					final int hadDelayBefore = hasDelayBefore;
					items.add(new Loop(loopedExpr, (SectionNode) n));
					if (hadDelayBefore != 1 && hasDelayBefore != -1)
						hasDelayBefore = 0;
				} else if (n.getName().equalsIgnoreCase("else")) {
					if (items.size() == 0 || !(items.get(items.size() - 1) instanceof Conditional)) {
						Skript.error("'else' has to be placed just after the end of a conditional section");
						continue;
					}
					if (Skript.debug())
						Skript.info(indentation + "else:");
					final int hadDelayAfterLastIf = hasDelayBefore;
					hasDelayBefore = hadDelayBeforeLastIf;
					((Conditional) items.get(items.size() - 1)).loadElseClause((SectionNode) n);
					if (hadDelayBeforeLastIf == 1) {
						hasDelayBefore = 1;
					} else if (hadDelayBeforeLastIf == -1 && hadDelayAfterLastIf == -1 && hasDelayBefore == -1) {
						hasDelayBefore = -1;
					} else {
						if (hadDelayAfterLastIf == 1 && hasDelayBefore == 1)
							hasDelayBefore = 1;
						else if (hadDelayAfterLastIf != -1 || hasDelayBefore != -1)
							hasDelayBefore = 0;
					}
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
					final int hadDelayBefore = hasDelayBefore;
					hadDelayBeforeLastIf = hadDelayBefore;
					items.add(new Conditional(cond, (SectionNode) n));
					if (hadDelayBefore != 1 && hasDelayBefore != -1)
						hasDelayBefore = 0;
				}
			}
		}
		
		for (int i = 0; i < items.size() - 1; i++)
			items.get(i).setNext(items.get(i + 1));
		
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
	private static ScriptInfo loadScript(final Config config) {
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
			
			if (event.toLowerCase().startsWith("command ")) {
				currentEvent = null;
				currentEvents = Skript.array(CommandEvent.class);
				if (Commands.loadCommand(node) != null)
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
			hasDelayBefore = -1;
			
			final Trigger trigger = new Trigger(config.getFile(), event, parsedEvent.second, loadItems(node));
			
			currentEvent = null;
			currentEvents = null;
			hasDelayBefore = -1;
			
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
		
		currentScript = null;
		
		return new ScriptInfo(1, numTriggers, numCommands);
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
		
		final Trigger t = new Trigger(null, event, parsedEvent.second, loadItems(node));
		
		currentEvent = null;
		currentEvents = null;
		
		return t;
	}
	
}
