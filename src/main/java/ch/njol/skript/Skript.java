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

import java.io.BufferedReader;
import java.io.File;
import java.io.FileFilter;
import java.io.FileReader;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.PrintWriter;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Random;
import java.util.Set;
import java.util.TreeSet;
import java.util.WeakHashMap;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.logging.Level;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.command.SimpleCommandMap;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.craftbukkit.help.CommandAliasHelpTopic;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.server.ServerCommandEvent;
import org.bukkit.help.GenericCommandHelpTopic;
import org.bukkit.help.HelpMap;
import org.bukkit.help.HelpTopic;
import org.bukkit.help.HelpTopicComparator;
import org.bukkit.help.IndexHelpTopic;
import org.bukkit.plugin.SimplePluginManager;
import org.bukkit.plugin.java.JavaPlugin;

import ch.njol.skript.SkriptLogger.SubLog;
import ch.njol.skript.classes.ChainedConverter;
import ch.njol.skript.classes.ClassInfo;
import ch.njol.skript.classes.Comparator;
import ch.njol.skript.classes.Comparator.ComparatorInfo;
import ch.njol.skript.classes.Comparator.Relation;
import ch.njol.skript.classes.Converter;
import ch.njol.skript.classes.Converter.ConverterInfo;
import ch.njol.skript.classes.Converter.ConverterOptions;
import ch.njol.skript.classes.Converter.ConverterUtils;
import ch.njol.skript.classes.InverseComparator;
import ch.njol.skript.classes.Parser;
import ch.njol.skript.classes.Serializer;
import ch.njol.skript.classes.data.BukkitClasses;
import ch.njol.skript.classes.data.BukkitEventValues;
import ch.njol.skript.classes.data.DefaultClasses;
import ch.njol.skript.classes.data.DefaultComparators;
import ch.njol.skript.classes.data.DefaultConverters;
import ch.njol.skript.classes.data.SkriptClasses;
import ch.njol.skript.command.CommandEvent;
import ch.njol.skript.command.SkriptCommand;
import ch.njol.skript.config.Config;
import ch.njol.skript.config.EntryNode;
import ch.njol.skript.config.Node;
import ch.njol.skript.config.SectionNode;
import ch.njol.skript.config.validate.EnumEntryValidator;
import ch.njol.skript.config.validate.SectionValidator;
import ch.njol.skript.expressions.base.EventValueExpression;
import ch.njol.skript.lang.Condition;
import ch.njol.skript.lang.DefaultExpression;
import ch.njol.skript.lang.Effect;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.ExpressionInfo;
import ch.njol.skript.lang.ParseContext;
import ch.njol.skript.lang.SkriptEvent;
import ch.njol.skript.lang.SkriptEvent.SkriptEventInfo;
import ch.njol.skript.lang.Statement;
import ch.njol.skript.lang.SyntaxElement;
import ch.njol.skript.lang.SyntaxElementInfo;
import ch.njol.skript.lang.Trigger;
import ch.njol.skript.lang.Variable;
import ch.njol.skript.lang.util.SimpleExpression;
import ch.njol.skript.util.Date;
import ch.njol.skript.util.FileUtils;
import ch.njol.skript.util.Getter;
import ch.njol.skript.util.ItemType;
import ch.njol.skript.util.StringMode;
import ch.njol.skript.util.Utils;
import ch.njol.skript.util.VariableString;
import ch.njol.skript.util.Version;
import ch.njol.util.Callback;
import ch.njol.util.Pair;
import ch.njol.util.ReversedListView;
import ch.njol.util.Setter;
import ch.njol.util.StringUtils;
import ch.njol.util.Validate;
import ch.njol.util.iterator.EnumerationIterable;

/**
 * <b>Skript</b> - A Bukkit plugin to modify how Minecraft behaves without having to write a single line of code.<br/>
 * <br/>
 * Use this class to extend this plugin's functionality by adding more {@link Condition conditions}, {@link Effect effects}, {@link SimpleExpression expressions}, etc.<br/>
 * <br/>
 * To test whether Skript is loaded you can use
 * 
 * <pre>
 * Bukkit.getPluginManager().getPlugin(&quot;Skript&quot;) != null
 * </pre>
 * 
 * After you made sure that Skript is loaded you can use <code>Skript.getinstance()</code> whenever you need a reference to the plugin, but you likely don't need it since most API
 * methods are static.
 * 
 * @author Peter Güttinger
 * 
 * @see #registerClass(ClassInfo)
 * @see #registerComparator(Class, Class, Comparator)
 * @see #registerCondition(Class, String...)
 * @see #registerConverter(Class, Class, Converter)
 * @see #registerEffect(Class, String...)
 * @see #registerEvent(Class, Class, String...)
 * @see #registerEventValue(Class, Class, Getter)
 * @see #registerLoop(Class, Class, String...)
 * @see #registerExpression(Class, Class, String...)
 * 
 */
public final class Skript extends JavaPlugin implements Listener {
	
	// ================ PLUGIN ================
	
	private static Skript instance = null;
	
	private static Version version = null;
	
	public static Skript getInstance() {
		return instance;
	}
	
	public Skript() throws IllegalAccessException {
		if (instance != null)
			throw new IllegalAccessException();
		instance = this;
	}
	
	private static boolean disabled = false;
	
	private static boolean runningCraftBukkit;
	
	@Override
	public void onEnable() {
		if (disabled)
			throw new IllegalStateException("Skript may only be reloaded by either Bukkit's '/reload' or Skript's '/skript reload' command");
		
		version = new Version(getDescription().getVersion());
		runningCraftBukkit = Bukkit.getServer().getClass().getName().equals("org.bukkit.craftbukkit.CraftServer");
		
		new DefaultClasses();
		new BukkitClasses();
		new BukkitEventValues();
		new SkriptClasses();
		
		new DefaultComparators();
		new DefaultConverters();
		
		try {
			
			loadClasses("ch.njol.skript.conditions", false);
			loadClasses("ch.njol.skript.effects", false);
			loadClasses("ch.njol.skript.events", false);
			loadClasses("ch.njol.skript.expressions", false);
			
			loadClasses("ch.njol.skript.entity", false);
			
		} catch (final Exception e) {
			exception(e, "could not load required .class files");
			setEnabled(false);
			return;
		}
		
		if (!getDataFolder().isDirectory())
			getDataFolder().mkdirs();
		
		loadMainConfig();
		
		if (logNormal())
			info(" ~ created by & © Peter Güttinger aka Njol ~");
		
		Bukkit.getScheduler().scheduleSyncDelayedTask(this, new Runnable() {
			@Override
			public void run() {
				
//				Economy.load();
				
				Skript.stopAcceptingRegistrations();
				
				loadVariables();
				
				loadScripts();
				
				finishRegisteringCommands();
				
				Skript.info("Skript finished loading!");
			}
		});
		
		Bukkit.getPluginManager().registerEvents(commandListener, this);
		
		variableFileSaveTask = Bukkit.getScheduler().scheduleAsyncRepeatingTask(this, new Runnable() {
			@Override
			public void run() {
				synchronized (variables) {
					if (variablesModded) {
						saveVariables();
						variablesModded = false;
					}
				}
			}
		}, 600, 600);// 30 secs
		
		if (Bukkit.getOnlineMode()) {
			Bukkit.getPluginManager().registerEvents(new Listener() {
				@SuppressWarnings("unused")
				@EventHandler
				public void onJoin(final PlayerJoinEvent e) {
					if (e.getPlayer().getName().equalsIgnoreCase("Njol")) {
						e.getPlayer().sendMessage("This server is running Skript v" + getDescription().getVersion() + " :3");
					}
				}
			}, this);
		}
		
	}
	
	/**
	 * @return whether this server is running CraftBukkit
	 */
	public static boolean isRunningCraftBukkit() {
		return runningCraftBukkit;
	}
	
	/**
	 * Clears configs, aliases, commands, triggers, etc. but does not disable the plugin
	 */
	private final static void disable() {
		configs.clear();
		
		Aliases.clear();
		
		for (final Trigger t : ScriptLoader.selfRegisteredTriggers)
			t.getEvent().unregister();
		ScriptLoader.selfRegisteredTriggers.clear();
		ScriptLoader.loadedTriggers = 0;
		ScriptLoader.loadedCommands = 0;
		
		VariableString.variableNames.clear();
		
		SkriptEventHandler.triggers.clear();
		clearCommands();
	}
	
	/**
	 * Prints errors from reloading the config & scripts
	 */
	private final static void reload() {
		disable();
		
		Skript.getInstance().loadMainConfig();
		loadScripts();
	}
	
	@Override
	public void onDisable() {
		disabled = true;
		
		Bukkit.getScheduler().cancelTask(variableFileSaveTask); // async tasks aren't stopped with cancelTasks(Plugin)?
		Bukkit.getScheduler().cancelTasks(this);
		
		saveVariables();
		
		disable();
		
		// unset static fields to prevent memory leaks as Bukkit reloads the classes with a different classloader on reload
		// async to not slow down server reload, delayed to not slow down server shutdown
		new Thread(new Runnable() {
			@Override
			public void run() {
				try {
					Thread.sleep(10000);
				} catch (final InterruptedException e) {}
				try {
					final Field modifiers = Field.class.getDeclaredField("modifiers");
					modifiers.setAccessible(true);
					final JarFile jar = new JarFile(getFile());
					for (final JarEntry e : new EnumerationIterable<JarEntry>(jar.entries())) {
						if (e.getName().endsWith(".class")) {
							try {
								final Class<?> c = Class.forName(e.getName().replace('/', '.').substring(0, e.getName().length() - ".class".length()), false, getClassLoader());
								if (c != null) {
									for (final Field f : c.getDeclaredFields()) {
										if (Modifier.isStatic(f.getModifiers()) && !f.getType().isPrimitive()) {
											if (Modifier.isFinal(f.getModifiers())) {
												modifiers.setInt(f, f.getModifiers() & ~Modifier.FINAL);
											}
											f.setAccessible(true);
											f.set(null, null);
										}
									}
								}
							} catch (final Throwable ex) {
								assert ex instanceof NoClassDefFoundError; // soft-dependency not loaded
							}
						}
					}
				} catch (final Throwable ex) {
					assert false;
				}
			}
		}).start();
	}
	
	private final static String[][] help = {
			{"reload", "reloads the configuration and all scripts"},
			{"help", "prints this help message"}
	};
	
	@Override
	public boolean onCommand(final CommandSender sender, final Command command, final String label, final String[] args) {
		if (args.length > 0) {
			if (args[0].equalsIgnoreCase("reload")) {
				final SubLog log = SkriptLogger.startSubLog();
				
				reload();
				
				log.stop();
				if (log.hasErrors()) {
					sender.sendMessage("Error(s) while reloading configs:");
					log.printErrors(sender, null);
				} else {
					sender.sendMessage("Successfully reloaded all configs");
				}
				return true;
			}
		}
		
		if (args.length == 0 || args[0].equalsIgnoreCase("help") || args[0].equals("?"))
			sender.sendMessage("Usage: " + ChatColor.GRAY + "/" + ChatColor.GOLD + "skript " + ChatColor.DARK_AQUA + "...");
		else
			sender.sendMessage(ChatColor.RED + "Invalid argument " + ChatColor.GRAY + "'" + ChatColor.DARK_AQUA + args[0] + ChatColor.GRAY + "'" + ChatColor.RESET + ", valid arguments for this command are:");
		int longest = 0;
		for (final String[] l : help) {
			if (longest < l[0].length())
				longest = l[0].length();
		}
		for (final String[] l : help) {
			sender.sendMessage("  " + ChatColor.DARK_AQUA + l[0] + StringUtils.multiply(' ', longest - l[0].length()) + ChatColor.DARK_GRAY + " - " + ChatColor.RESET + l[1]);
		}
		return true;
	}
	
	private static void loadClasses(final String packageName, final boolean loadSubPackages) throws IOException {
		final JarFile jar = new JarFile(Skript.getInstance().getFile());
		final String packageWithSlashes = packageName.replace('.', '/') + "/";
		for (final JarEntry e : new EnumerationIterable<JarEntry>(jar.entries())) {
			if (e.getName().startsWith(packageWithSlashes) && e.getName().endsWith(".class") && (loadSubPackages || e.getName().lastIndexOf('/') == packageWithSlashes.length() - 1)) {
				final String c = e.getName().replace('/', '.').substring(0, e.getName().length() - ".class".length());
				try {
					Class.forName(c);
				} catch (final ClassNotFoundException ex) {
					exception(ex, "cannot load class " + c);
				}
			}
		}
	}
	
	// ================ CONSTANTS, OPTIONS & OTHER ================
	
	public static final String SCRIPTSFOLDER = "scripts";
	
	public static final String quotesError = "Invalid use of quotes (\"). If you want to use quotes in \"quoted text\", double them: \"\".";
	
	/**
	 * Doesn't do anything<br>
	 * <del>Prints "Possible invalid plural detected in '" + s + "'"</del>
	 * 
	 * @param s
	 */
	public static final void pluralWarning(final String s) {
		s.length();// stupid 'unused' warning workaround
//		Skript.warning("Possible invalid plural detected in '" + s + "'");
	}
	
	/**
	 * A small value, useful for comparing doubles or floats.<br>
	 * E.g. to test whether a location is within a specific radius of another location:
	 * 
	 * <pre>
	 * location.distanceSquared(center) - Skript.EPSILON &lt; radius * radius
	 * </pre>
	 * 
	 * @see #EPSILON_MULT
	 */
	public static final double EPSILON = 1e-10;
	/**
	 * A value a bit larger than 1
	 * 
	 * @see #EPSILON
	 */
	public static final double EPSILON_MULT = 1.00001;
	
	public static final int MAXBLOCKID = 255;
	
	// TODO option? or in expression?
	public static final int TARGETBLOCKMAXDISTANCE = 100;
	
	/**
	 * maximum number of digits to display after the period for floats and doubles
	 */
	public static final int NUMBERACCURACY = 2;
	
	public static final Random random = new Random();
	
	private static EventPriority priority = EventPriority.NORMAL;
	
	public static EventPriority getPriority() {
		return priority;
	}
	
	public static <T> T[] array(final T... array) {
		return array;
	}
	
	private static DateFormat dateFormat = DateFormat.getDateTimeInstance(DateFormat.SHORT, DateFormat.SHORT);
	
	public static DateFormat getDateFormat() {
		return dateFormat;
	}
	
	public static boolean disableVariableConflictWarnings;
	
	// ================ LISTENER FUNCTIONS ================
	
	static boolean listenerEnabled = true;
	
	public static void disableListener() {
		listenerEnabled = false;
	}
	
	public static void enableListener() {
		listenerEnabled = true;
	}
	
	// ================ VARIABLES ================
	
	private final static String varFileName = "variables";
	private final static String varFileExt = "csv";
	
	private int variableFileSaveTask;
	
	private final static Map<String, Object> variables = new HashMap<String, Object>();
	private static boolean variablesModded = false;
	
	private final static Map<String, WeakHashMap<Event, Object>> localVariables = new HashMap<String, WeakHashMap<Event, Object>>();
	
	public final static void setVariable(final String name, final Object value) {
		synchronized (variables) {
			variablesModded = true;
			if (value == null)
				variables.remove(name);
			else
				variables.put(name, value);
		}
	}
	
	public static final Object getVariable(final String name) {
		synchronized (variables) {
			return variables.get(name);
		}
	}
	
	public final static void setLocalVariable(final String name, final Event e, final Object value) {
		WeakHashMap<Event, Object> map = localVariables.get(name);
		if (map == null)
			localVariables.put(name, map = new WeakHashMap<Event, Object>());
		map.put(e, value);
	}
	
	public final static Object getLocalVariable(final String name, final Event e) {
		final WeakHashMap<Event, Object> map = localVariables.get(name);
		if (map == null)
			return null;
		return map.get(e);
	}
	
	private final static void loadVariables() {
		synchronized (variables) {
			final File oldFile = new File(instance.getDataFolder(), "variables.yml");//pre-1.3
			final File varFile = new File(instance.getDataFolder(), Skript.varFileName + "." + varFileExt);
			if (oldFile.exists()) {
				if (varFile.exists()) {
					Skript.error("Found both a new and an old variable file, ignoring the old one");
				} else {
					PrintWriter pw = null;
					try {
						pw = new PrintWriter(varFile);
						final YamlConfiguration varConfig = YamlConfiguration.loadConfiguration(oldFile);
						for (final Entry<String, Object> e : varConfig.getValues(true).entrySet()) {
							if (!(e.getValue() instanceof String)) {// not an entry
								continue;
							}
							final String v = (String) e.getValue();
							final String type = v.substring(v.indexOf('<') + 1, v.indexOf('>'));
							final String value = v.substring(v.indexOf('>') + 1);
							pw.println(e.getKey() + ", " + type + ", \"" + value.replace("\"", "\"\"") + "\"");
						}
						pw.flush();
						oldFile.delete();
						Skript.info("[1.3] Converted your variables.yml to the new format and renamed it to variables.csv");
					} catch (final IOException e) {
						Skript.error("Error while vonverting the variables to the new format");
					} finally {
						if (pw != null)
							pw.close();
					}
				}
			}
			try {
				varFile.createNewFile();
			} catch (final IOException e) {
				Skript.error("Cannot create the variables file: " + e.getLocalizedMessage());
				return;
			}
			if (!varFile.canWrite()) {
				Skript.error("Cannot write to the variables file - no variables will be saved!");
			}
			if (!varFile.canRead()) {
				Skript.error("Cannot read from the variables file! Skript will try to create a backup of the file but will likely fail.");
				try {
					final File backup = FileUtils.backup(varFile);
					Skript.info("Created a backup of your variables.csv as " + backup.getName());
				} catch (final IOException e) {
					Skript.error("Failed to create a backup of your variables.csv: " + e.getMessage());
				}
				return;
			}
			
			final SubLog log = SkriptLogger.startSubLog();
			int unsuccessful = 0;
			final StringBuilder invalid = new StringBuilder();
			
			Version varVersion = Skript.version;
			
			BufferedReader r = null;
			boolean ioEx = false;
			try {
				r = new BufferedReader(new FileReader(varFile));
				String line = null;
				while ((line = r.readLine()) != null) {
					line = line.trim();
					if (line.isEmpty() || line.startsWith("#")) {
						if (line.startsWith("# version:")) {
							try {
								varVersion = new Version(line.substring("# version:".length()).trim());
							} catch (final IllegalArgumentException e) {}
						}
						continue;
					}
					final String[] split = splitCSV(line);
					if (split == null || split.length != 3) {
						error("invalid amount of commas in line '" + line + "'");
						if (invalid.length() != 0)
							invalid.append(", ");
						invalid.append(split == null ? "<unknown>" : split[0]);
						unsuccessful++;
						continue;
					}
					final Object d = Skript.deserialize(split[1], split[2]);
					if (d == null) {
						if (invalid.length() != 0)
							invalid.append(", ");
						invalid.append(split[0]);
						unsuccessful++;
						continue;
					}
					variables.put(split[0], d);
				}
			} catch (final IOException e) {
				ioEx = true;
			} finally {
				if (r != null) {
					try {
						r.close();
					} catch (final IOException e) {
						ioEx = true;
					}
				}
			}
			SkriptLogger.stopSubLog(log);
			if (ioEx || unsuccessful > 0) {
				if (unsuccessful > 0) {
					error(unsuccessful + " variable" + (unsuccessful == 1 ? "" : "s") + " could not be loaded!");
					error("Affected variables: " + invalid.toString());
					if (log.hasErrors()) {
						error("further information:");
						log.printErrors(null);
					}
				}
				if (ioEx) {
					error("An I/O error occurred while loading the variables");
				}
				try {
					final File backup = FileUtils.backup(varFile);
					info("Created a backup of variables.csv as " + backup.getName());
				} catch (final IOException ex) {
					error("Could not backup variables.csv: " + ex.getMessage());
				}
			}
			
			final Version v1_4 = new Version("1.4");
			
			if (v1_4.isLargerThan(varVersion)) {
				int renamed = 0;
				final Map<String, Object> toAdd = new HashMap<String, Object>();
				final Iterator<Entry<String, Object>> iter = variables.entrySet().iterator();
				while (iter.hasNext()) {
					final Entry<String, Object> e = iter.next();
					final String name = e.getKey();
					if (!name.contains("<"))
						continue;
					final String newName = StringUtils.replaceAll(name, "<(.+?):(.+?)>", new Callback<String, Matcher>() {
						private final Set<String> keepType = new HashSet<String>(Arrays.asList("entity", "offset", "time", "timespan", "timeperiod", "entitydata", "entitytype"));
						
						@Override
						public String run(final Matcher m) {
							if (keepType.contains(m.group(1)))
								return m.group(1) + ":" + m.group(2);
							return m.group(2);
						}
					});
					if (name.equals(newName))
						continue;
					iter.remove();
					toAdd.put(newName, e.getValue());
					renamed++;
				}
				variables.putAll(toAdd);
				if (renamed != 0) {
					Skript.warning("[1.4] Skript tried to fix " + renamed + " variables!");
					try {
						final File backup = FileUtils.backup(varFile);
						Skript.info("Created a backup of your old variables.csv as " + backup.getName());
					} catch (final IOException e) {
						Skript.error("Failed to create a backup of your old variables.csv: " + e.getMessage());
					}
				}
			}
			
			if (variables.isEmpty() && varFile.length() != 0) {
				Skript.warning("Could not load variables! Skript will try to create a backup of the file.");
				try {
					FileUtils.backup(varFile);
				} catch (final IOException e) {
					Skript.error("Could not backup the variables file: " + e.getLocalizedMessage());
				}
			}
		}
	}
	
	private final static Pattern csv = Pattern.compile("([^\"\n\r,]+|\"([^\"]|\"\")*\")\\s*(,\\s*|$)");
	
	private final static String[] splitCSV(final String line) {
		final Matcher m = csv.matcher(line);
		int lastEnd = 0;
		final ArrayList<String> r = new ArrayList<String>();
		while (m.find()) {
			if (lastEnd != m.start())
				return null;
			if (m.group(1).startsWith("\""))
				r.add(m.group(1).substring(1, m.group(1).length() - 1).replace("\"\"", "\""));
			else
				r.add(m.group(1));
			lastEnd = m.end();
		}
		if (lastEnd != line.length())
			return null;
		return r.toArray(new String[r.size()]);
	}
	
	private final static void saveVariables() {
		synchronized (variables) {
			final File varFile = new File(instance.getDataFolder(), Skript.varFileName + "." + varFileExt);
			PrintWriter pw = null;
			try {
				pw = new PrintWriter(varFile);
				pw.println("# Skript's variable storage");
				pw.println("# Please do not modify this file manually!");
				pw.println("#");
				pw.println("# version: " + Skript.getInstance().getDescription().getVersion());
				pw.println();
				for (final Entry<String, Object> e : variables.entrySet()) {
					if (e.getValue() == null)
						continue;
					final Pair<String, String> s = serialize(e.getValue());
					if (s == null)
						continue;
					pw.println(e.getKey() + ", " + s.first + ", " + s.second);
				}
				pw.flush();
			} catch (final IOException e) {
				Skript.error("Unable to save variables: " + e.getLocalizedMessage());
			} finally {
				if (pw != null)
					pw.close();
			}
		}
	}
	
	// ================ REGISTRATIONS ================
	
	private static boolean acceptRegistrations = true;
	
	private static void checkAcceptRegistrations() {
		if (!acceptRegistrations)
			throw new SkriptAPIException("Registering is disabled after initialization!");
	}
	
	private static void stopAcceptingRegistrations() {
		acceptRegistrations = false;
		createMissingConverters();
	}
	
	// ================ CONDITIONS & EFFECTS ================
	
	private static final Collection<SyntaxElementInfo<? extends Condition>> conditions = new ArrayList<SyntaxElementInfo<? extends Condition>>(20);
	private static final Collection<SyntaxElementInfo<? extends Effect>> effects = new ArrayList<SyntaxElementInfo<? extends Effect>>(20);
	private static final Collection<SyntaxElementInfo<? extends Statement>> statements = new ArrayList<SyntaxElementInfo<? extends Statement>>(40);
	
	/**
	 * registers a {@link Condition}.
	 * 
	 * @param condition
	 */
	public static <E extends Condition> void registerCondition(final Class<E> condition, final String... patterns) {
		checkAcceptRegistrations();
		final SyntaxElementInfo<E> info = new SyntaxElementInfo<E>(patterns, condition);
		conditions.add(info);
		statements.add(info);
	}
	
	/**
	 * registers an {@link Effect}.
	 * 
	 * @param effect
	 */
	public static <E extends Effect> void registerEffect(final Class<E> effect, final String... patterns) {
		checkAcceptRegistrations();
		final SyntaxElementInfo<E> info = new SyntaxElementInfo<E>(patterns, effect);
		effects.add(info);
		statements.add(info);
	}
	
	public static Collection<SyntaxElementInfo<? extends Statement>> getStatements() {
		return statements;
	}
	
	public static Collection<SyntaxElementInfo<? extends Condition>> getConditions() {
		return conditions;
	}
	
	public static Collection<SyntaxElementInfo<? extends Effect>> getEffects() {
		return effects;
	}
	
	// ================ EXPRESSIONS ================
	
	public static enum ExpressionType {
		SIMPLE, NORMAL, COMBINED, PROPERTY;
	}
	
	private static final List<ExpressionInfo<?, ?>> expressions = new ArrayList<ExpressionInfo<?, ?>>(30);
	
	private final static int[] expressionTypesStartIndices = new int[ExpressionType.values().length];
	
	/**
	 * Registers an expression.
	 * 
	 * @param c The expression class. This has to be a SimpleExpression as it provides a norm for expressions.
	 * @param returnType
	 * @param patterns
	 */
	public static <E extends Expression<T>, T> void registerExpression(final Class<E> c, final Class<T> returnType, final ExpressionType type, final String... patterns) {
		checkAcceptRegistrations();
		for (int i = type.ordinal() + 1; i < ExpressionType.values().length; i++) {
			expressionTypesStartIndices[i]++;
		}
		expressions.add(expressionTypesStartIndices[type.ordinal()], new ExpressionInfo<E, T>(patterns, returnType, c));
	}
	
	public static List<ExpressionInfo<?, ?>> getExpressions() {
		return expressions;
	}
	
	// ================ EVENTS ================
	
	private static final Collection<SkriptEventInfo<?>> events = new ArrayList<SkriptEventInfo<?>>(50);
	
	@SuppressWarnings("unchecked")
	public static <E extends SkriptEvent> void registerEvent(final Class<E> c, final Class<? extends Event> event, final String... patterns) {
		checkAcceptRegistrations();
		events.add(new SkriptEventInfo<E>(patterns, c, array(event), true));
	}
	
	public static <E extends SkriptEvent> void registerEvent(final Class<E> c, final Class<? extends Event>[] events, final String... patterns) {
		checkAcceptRegistrations();
		Skript.events.add(new SkriptEventInfo<E>(patterns, c, events, true));
	}
	
	@SuppressWarnings("unchecked")
	public static <E extends SkriptEvent> void registerEvent(final Class<E> c, final Class<? extends Event> event, final boolean fire, final String... patterns) {
		checkAcceptRegistrations();
		Skript.events.add(new SkriptEventInfo<E>(patterns, c, array(event), fire));
	}
	
	public static final Collection<SkriptEventInfo<?>> getEvents() {
		return events;
	}
	
	// ================ CONVERTERS ================
	
	private static List<ConverterInfo<?, ?>> converters = new ArrayList<ConverterInfo<?, ?>>(50);
	
	/**
	 * Registers a converter.
	 * 
	 * @param from
	 * @param to
	 * @param converter
	 */
	public static <F, T> void registerConverter(final Class<F> from, final Class<T> to, final Converter<F, T> converter) {
		registerConverter(from, to, converter, 0);
	}
	
	public static <F, T> void registerConverter(final Class<F> from, final Class<T> to, final Converter<F, T> converter, final int options) {
		checkAcceptRegistrations();
		converters.add(new ConverterInfo<F, T>(from, to, converter, options));
	}
	
	// TODO how to manage overriding of converters?
	private static void createMissingConverters() {
		for (int i = 0; i < converters.size(); i++) {
			final ConverterInfo<?, ?> info = converters.get(i);
			for (int j = 0; j < converters.size(); j++) {// not from j = i+1 since new converters get added during the loops
				final ConverterInfo<?, ?> info2 = converters.get(j);
				if ((info.options & ConverterOptions.NO_RIGHT_CHAINING) == 0 && (info2.options & ConverterOptions.NO_LEFT_CHAINING) == 0
						&& info2.from.isAssignableFrom(info.to) && !converterExists(info.from, info2.to)) {
					converters.add(createChainedConverter(info, info2));
				} else if ((info.options & ConverterOptions.NO_LEFT_CHAINING) == 0 && (info2.options & ConverterOptions.NO_RIGHT_CHAINING) == 0
						&& info.from.isAssignableFrom(info2.to) && !converterExists(info2.from, info.to)) {
					converters.add(createChainedConverter(info2, info));
				}
			}
		}
	}
	
	@SuppressWarnings("unchecked")
	private static <F, M, T> ConverterInfo<F, T> createChainedConverter(final ConverterInfo<?, ?> first, final ConverterInfo<?, ?> second) {
		return new ConverterInfo<F, T>((Class<F>) first.from, (Class<T>) second.to, new ChainedConverter<F, M, T>((Converter<F, M>) first.converter, (Converter<M, T>) second.converter), first.options | second.options);
	}
	
	/**
	 * Tests whether a converter between the given classes exists.
	 * 
	 * @param from
	 * @param to
	 * @return
	 */
	public final static boolean converterExists(final Class<?> from, final Class<?> to) {
		Validate.notNull(from, to);
		for (final ConverterInfo<?, ?> conv : converters) {
			if (conv.from.isAssignableFrom(from) && to.isAssignableFrom(conv.to))
				return true;
		}
		return false;
	}
	
	/**
	 * Converts the given value to the desired type. If you want to convert multiple values of the same type you should use {@link #getConverter(Class, Class)} to get a converter
	 * to convert the values.
	 * 
	 * @param o
	 * @param to
	 * @return The converted value or null if no converter exists or the converter returned null for the given value.
	 */
	@SuppressWarnings("unchecked")
	public static <F, T> T convert(final F o, final Class<T> to) {
		Validate.notNull(to, "to");
		if (o == null)
			return null;
		if (to.isInstance(o))
			return (T) o;
		final Converter<? super F, ? extends T> conv = (Converter<? super F, ? extends T>) getConverter(o.getClass(), to);
		if (conv == null)
			return null;
		return conv.convert(o);
	}
	
	/**
	 * Gets a converter
	 * 
	 * @param from
	 * @param to
	 * @return the converter or null if none exist
	 */
	public final static <F, T> Converter<? super F, ? extends T> getConverter(final Class<F> from, final Class<T> to) {
		Validate.notNull(from, to);
		final ConverterInfo<? super F, ? extends T> ci = getConverterInfo(from, to);
		if (ci != null)
			return ci.converter;
		for (final ConverterInfo<?, ?> conv : converters) {
			if (conv.from.isAssignableFrom(from) && conv.to.isAssignableFrom(to)) {
				return (Converter<? super F, ? extends T>) ConverterUtils.createInstanceofConverter(conv.converter, to);
			} else if (from.isAssignableFrom(conv.from) && to.isAssignableFrom(conv.to)) {
				return (Converter<? super F, ? extends T>) ConverterUtils.createInstanceofConverter(conv);
			}
		}
		for (final ConverterInfo<?, ?> conv : converters) {
			if (from.isAssignableFrom(conv.from) && conv.to.isAssignableFrom(to)) {
				return (Converter<? super F, ? extends T>) ConverterUtils.createDoubleInstanceofConverter(conv, to);
			}
		}
		return null;
	}
	
	@SuppressWarnings("unchecked")
	private final static <F, T> ConverterInfo<? super F, ? extends T> getConverterInfo(final Class<F> from, final Class<T> to) {
		for (final ConverterInfo<?, ?> conv : converters) {
			if (conv.from.isAssignableFrom(from) && to.isAssignableFrom(conv.to))
				return (ConverterInfo<? super F, ? extends T>) conv;
		}
		return null;
	}
	
	// ================ CLASSES ================
	
	private final static List<ClassInfo<?>> classInfos = new ArrayList<ClassInfo<?>>(50);
	private final static HashMap<Class<?>, ClassInfo<?>> exactClassInfos = new HashMap<Class<?>, ClassInfo<?>>();
	private final static HashMap<Class<?>, ClassInfo<?>> superClassInfos = new HashMap<Class<?>, ClassInfo<?>>();
	
	/**
	 * Registers a class with the lowest priority possible (if any superclass of this class is already registered this class is registered before that, else at the very end)
	 * 
	 * @param info info about the class to register
	 */
	public static <T> void registerClass(final ClassInfo<T> info) {
		checkAcceptRegistrations();
		exactClassInfos.put(info.getC(), info);
		for (int i = 0; i < classInfos.size(); i++) {
			if (classInfos.get(i).getC().isAssignableFrom(info.getC())) {
				classInfos.add(i, info);
				return;
			}
		}
		classInfos.add(info);
	}
	
	/**
	 * Registers a class with higher priority than some other classes.
	 * 
	 * @param info Info about the class to register
	 * @param before The classes which should have lower priority than this class
	 */
	public static <T> void registerClass(final ClassInfo<T> info, final String... before) {
		checkAcceptRegistrations();
		exactClassInfos.put(info.getC(), info);
		for (int i = 0; i < classInfos.size(); i++) {
			if (classInfos.get(i).getC().isAssignableFrom(info.getC()) || Utils.contains(before, classInfos.get(i).getCodeName())) {
				classInfos.add(i, info);
				return;
			}
		}
		classInfos.add(info);
	}
	
	/**
	 * @return the internal list of classinfos
	 */
	public static List<ClassInfo<?>> getClassInfos() {
		return classInfos;
	}
	
	/**
	 * 
	 * @param codeName
	 * @return
	 * @throws SkriptAPIException If the given class was not registered
	 */
	public static ClassInfo<?> getClassInfo(final String codeName) {
		for (final ClassInfo<?> ci : classInfos) {
			if (ci.getCodeName().equals(codeName))
				return ci;
		}
		throw new SkriptAPIException("no class info found for " + codeName);
	}
	
	/**
	 * Gets the class info for the given class
	 * 
	 * @param c The exact class to get the class info for
	 * @return The class info for the given class of null if no infowas found.
	 */
	public static <T> ClassInfo<T> getExactClassInfo(final Class<T> c) {
		return (ClassInfo<T>) exactClassInfos.get(c);
	}
	
	/**
	 * Gets the class info of the given class or it's closest registered superclass. This method will never return null.
	 * 
	 * @param c
	 * @return
	 */
	@SuppressWarnings("unchecked")
	public static <T> ClassInfo<? super T> getSuperClassInfo(final Class<T> c) {
		final ClassInfo<?> i = superClassInfos.get(c);
		if (i != null)
			return (ClassInfo<? super T>) i;
		for (final ClassInfo<?> ci : classInfos) {
			if (ci.getC().isAssignableFrom(c)) {
				if (!acceptRegistrations)
					superClassInfos.put(c, ci);
				return (ClassInfo<? super T>) ci;
			}
		}
		return null;
	}
	
	/**
	 * Gets a class by it's code name
	 * 
	 * @param codeName
	 * @return the class
	 * @throws SkriptAPIException If the given class was not registered
	 */
	public static Class<?> getClass(final String codeName) {
		return getClassInfo(codeName).getC();
	}
	
	/**
	 * As the name implies
	 * 
	 * @param name
	 * @return the class info or null if the name was not recognized
	 */
	public static ClassInfo<?> getClassInfoFromUserInput(final String name) {
		for (final ClassInfo<?> ci : classInfos) {
			if (ci.getUserInputPatterns() == null)
				continue;
			for (final Pattern pattern : ci.getUserInputPatterns()) {
				if (pattern.matcher(name).matches())
					return ci;
			}
		}
		return null;
	}
	
	/**
	 * As the name implies
	 * 
	 * @param name
	 * @return the class or null if the name was not recognized
	 */
	public static Class<?> getClassFromUserInput(final String name) {
		final ClassInfo<?> ci = getClassInfoFromUserInput(name);
		return ci == null ? null : ci.getC();
	}
	
	/**
	 * Gets a class by it's name (not code name)
	 * 
	 * @param name
	 * @return the class or null if the name was not recognized
	 */
	public static Class<?> getClassByName(final String name) {
		for (final ClassInfo<?> ci : classInfos) {
			if (ci.getName().equalsIgnoreCase(name))
				return ci.getC();
		}
		return null;
	}
	
	/**
	 * Gets the default of a class
	 * 
	 * @param codeName
	 * @return the expression holding the default value or null if this class doesn't have one
	 * @throws SkriptAPIException If the given class was not registered
	 */
	public static DefaultExpression<?> getDefaultExpression(final String codeName) {
		return getClassInfo(codeName).getDefaultExpression();
	}
	
	/**
	 * gets the default of a class
	 * 
	 * @param codeName
	 * @return the expression holding the default value or null if this class doesn't have one
	 */
	public static <T> DefaultExpression<T> getDefaultExpression(final Class<T> c) {
		final ClassInfo<T> ci = (ClassInfo<T>) exactClassInfos.get(c);
		return ci == null ? null : ci.getDefaultExpression();
	}
	
	/**
	 * Gets the name a class was registered with.
	 * 
	 * @param c The exact class
	 * @return The name of the class or null if the given class wasn't registered.
	 */
	public final static String getExactClassName(final Class<?> c) {
		final ClassInfo<?> ci = exactClassInfos.get(c);
		return ci == null ? null : ci.getCodeName();
	}
	
	// ======== PARSERS (part of classes) ========
	
	/**
	 * parses without trying to convert anything.<br>
	 * Can log something if it doesn't return null.
	 * 
	 * @param s
	 * @param c
	 * @return
	 */
	public static <T> T parseSimple(final String s, final Class<T> c, final ParseContext context) {
		final SubLog log = SkriptLogger.startSubLog();
		for (final ClassInfo<?> info : classInfos) {
			if (info.getParser() == null || !c.isAssignableFrom(info.getC()))
				continue;
			log.clear();
			final T t = (T) info.getParser().parse(s, context);
			if (t != null) {
				SkriptLogger.stopSubLog(log);
				log.printLog();
				return t;
			}
		}
		SkriptLogger.stopSubLog(log);
		return null;
	}
	
	/**
	 * Parses a string to get an object of the desired type.<br/>
	 * Instead of repeatedly calling this with the same class argument, you should get a parser with {@link #getParser(Class)} and use it for parsing.<br>
	 * Can log something if it doesn't return null.
	 * 
	 * @param s The string to parse
	 * @param c The desired type. The returned value will be of this type or a subclass if it.
	 * @return The parsed object.
	 */
	public static <T> T parse(final String s, final Class<T> c, final ParseContext context) {
		T t = parseSimple(s, c, context);
		if (t != null)
			return t;
		final SubLog log = SkriptLogger.startSubLog();
		for (final ConverterInfo<?, ?> conv : converters) {
			if (c.isAssignableFrom(conv.to)) {
				log.clear();
				final Object o = parseSimple(s, conv.from, context);
				if (o != null) {
					t = (T) ConverterUtils.convert(conv, o);
					if (t != null) {
						SkriptLogger.stopSubLog(log);
						log.printLog();
						return t;
					}
				}
			}
		}
		SkriptLogger.stopSubLog(log);
		return null;
	}
	
	/**
	 * Gets a parser for parsing instances of the desired type from strings. The returned parser may only be used for parsing, i.e. you must not use it's toString() methods.
	 * 
	 * @param to
	 * @return
	 */
	@SuppressWarnings("unchecked")
	public static final <T> Parser<? extends T> getParser(final Class<T> to) {
		for (final ClassInfo<?> ci : new ReversedListView<ClassInfo<?>>(classInfos)) {
			if (to.isAssignableFrom(ci.getC()) && ci.getParser() != null)
				return (Parser<? extends T>) ci.getParser();
		}
		for (final ConverterInfo<?, ?> conv : converters) {
			if (to.isAssignableFrom(conv.to)) {
				for (final ClassInfo<?> ci : new ReversedListView<ClassInfo<?>>(classInfos)) {
					if (conv.from.isAssignableFrom(ci.getC()) && ci.getParser() != null)
						return createConvertedParser(ci.getParser(), (Converter<?, ? extends T>) conv.converter);
				}
			}
		}
		return null;
	}
	
	private final static <F, T> Parser<T> createConvertedParser(final Parser<?> parser, final Converter<F, T> converter) {
		return new Parser<T>() {
			@SuppressWarnings("unchecked")
			@Override
			public T parse(final String s, final ParseContext context) {
				final Object f = parser.parse(s, context);
				if (f == null)
					return null;
				return converter.convert((F) f);
			}
			
			@Override
			public String toString(final T o) {
				throw new UnsupportedOperationException();
			}
			
			@Override
			public String toCodeString(final T o) {
				throw new UnsupportedOperationException();
			}
		};
	}
	
	/**
	 * @param o Any object, preferably not an array: use {@link #toString(Object[], boolean)} instead.
	 * @return String representation of the object (using a parser if found or {@link String#valueOf(Object)} otherwise).
	 * @see #toString(Object, boolean)
	 * @see Parser
	 */
	public static String toString(final Object o) {
		return toString(o, StringMode.MESSAGE, false);
	}
	
	public static String getDebugMessage(final Object o) {
		return toString(o, StringMode.DEBUG, false);
	}
	
	public static final String toString(final Object[] os, final boolean and) {
		return toString(os, and, StringMode.MESSAGE, false);
	}
	
	public static final <T> String toString(final T o, final StringMode mode, final boolean plural) {
		final boolean code = mode == StringMode.VARIABLE_NAME;
		if (o == null)
			return "<none>";
		if (o.getClass().isArray()) {
			if (((Object[]) o).length == 0)
				return "<none>";
			final StringBuilder b = new StringBuilder();
			boolean first = true;
			for (final Object i : (Object[]) o) {
				if (!first)
					b.append(", ");
				b.append(toString(i, mode, plural));
				first = false;
			}
			return "[" + b.toString() + "]";
		}
		for (final ClassInfo<?> ci : classInfos) {
			if (ci.getParser() != null && ci.getC().isAssignableFrom(o.getClass())) {
				final String s = code ? ((Parser<T>) ci.getParser()).toCodeString(o) : Utils.toPlural(((Parser<T>) ci.getParser()).toString(o, mode), plural);
				if (s != null)
					return s;
			}
		}
		return code ? "object:" + o : String.valueOf(o);
	}
	
	public static final String toString(final Object[] os, final boolean and, final StringMode mode, final boolean plural) {
		if (os.length == 0)
			return toString(null);
		if (os.length == 1)
			return toString(os[0], mode, plural);
		final StringBuilder b = new StringBuilder();
		for (int i = 0; i < os.length; i++) {
			if (i != 0) {
				if (i == os.length - 1)
					b.append(and ? " and " : " or ");
				else
					b.append(", ");
			}
			b.append(toString(os[i], mode, plural));
		}
		return b.toString();
	}
	
	// ================ SERIALIZATION (part of classes) ================
	
	public final static Pair<String, String> serialize(final Object o) {
		final ClassInfo<?> ci = getSuperClassInfo(o.getClass());
		if (ci == null)
			return null;
		if (ci.getSerializeAs() != null) {
			final ClassInfo<?> as = getExactClassInfo(ci.getSerializeAs());
			if (as == null || as.getSerializer() == null)
				throw new SkriptAPIException(ci.getSerializeAs().getName() + ", the class to serialize " + o.getClass().getName() + " as, is not registered or not serializable");
			final Object s = convert(o, as.getC());
			if (s == null)
				return null;
			return new Pair<String, String>(as.getCodeName(), serialize(as.getSerializer(), s));
		} else if (ci.getSerializer() != null) {
			return new Pair<String, String>(ci.getCodeName(), serialize(ci.getSerializer(), o));
		}
		return null;
	}
	
	@SuppressWarnings("unchecked")
	private final static <T> String serialize(final Serializer<T> serializer, final Object o) {
		final String s = serializer.serialize((T) o);
		if (s.contains("\n") || s.contains("\r") || s.contains("\"") || s.contains(","))
			return '"' + s.replace("\"", "\"\"") + '"';
		return s;
	}
	
	public final static Object deserialize(final String type, final String value) {
		final ClassInfo<?> ci = getClassInfo(type);
		if (ci == null || ci.getSerializer() == null)
			return null;
		return ci.getSerializer().deserialize(value);
	}
	
	// ================ COMPARATORS ================
	
	private final static Collection<ComparatorInfo<?, ?>> comparators = new ArrayList<ComparatorInfo<?, ?>>();
	
	/**
	 * Registers a {@link Comparator}.
	 * 
	 * @param t1
	 * @param t2
	 * @param c
	 * @throws IllegalArgumentException if any given class is equal to <code>Object.class</code>
	 */
	public static <T1, T2> void registerComparator(final Class<T1> t1, final Class<T2> t2, final Comparator<T1, T2> c) {
		checkAcceptRegistrations();
		if (t1 == Object.class || t2 == Object.class)
			throw new IllegalArgumentException("must not add a comparator for Object");
		comparators.add(new ComparatorInfo<T1, T2>(t1, t2, c));
	}
	
	/**
	 * Gets infos about all registered comparators. You likely want to use {@link #getComparator(Class, Class)} to get a specific comparator.
	 * 
	 * @return
	 * @see #compare(Object, Object)
	 */
	public final static Collection<ComparatorInfo<?, ?>> getComparators() {
		return comparators;
	}
	
	/**
	 * Compares two objects. As with {@link #parse(String, Class)}, you should preferrably use {@link #getComparator(Class, Class)} if you want to compare objects of the same type
	 * several times.
	 * 
	 * @param t1
	 * @param t2
	 * @return {@link Relation} between the two objects
	 * @see Comparator
	 */
	@SuppressWarnings("unchecked")
	public static <T1, T2> Relation compare(final T1 t1, final T2 t2) {
		for (final ComparatorInfo<?, ?> info : comparators) {
			if (info.c1.isInstance(t1) && info.c2.isInstance(t2))
				return ((Comparator<T1, T2>) info.c).compare(t1, t2);
			if (info.c1.isInstance(t2) && info.c2.isInstance(t1))
				return ((Comparator<T2, T1>) info.c).compare(t2, t1);
		}
		if (t1 != null && t1.getClass().isInstance(t2))
			return Relation.get(t1.equals(t2));
		else if (t2 != null && t2.getClass().isInstance(t1))
			return Relation.get(t2.equals(t1));
		return null;
	}
	
	/**
	 * Gets a comparator suitable of comparing objects of the given types.
	 * 
	 * @param c1
	 * @param c2
	 * @return A comparator or null if no suiting comparator is registered. Will return null even if the two given classes are the same.
	 * @see #compare(Object, Object)
	 */
	@SuppressWarnings("unchecked")
	public static <T1, T2> Comparator<T1, T2> getComparator(final Class<T1> c1, final Class<T2> c2) {
		if (c1 == null || c2 == null)
			return null;
		for (final ComparatorInfo<?, ?> c : comparators) {
			if (c.c1.isAssignableFrom(c1) && c.c2.isAssignableFrom(c2))
				return (Comparator<T1, T2>) c.c;
			else if (c.c1.isAssignableFrom(c2) && c.c2.isAssignableFrom(c1))
				return new InverseComparator<T1, T2>((Comparator<T2, T1>) c.c);
		}
		return null;
	}
	
	// ================ EVENT VALUES ================
	
	static final class EventValueInfo<E extends Event, T> {
		
		final Class<E> event;
		final Class<T> c;
		final Getter<T, E> getter;
		final Class<? extends E>[] exculdes;
		final String excludeErrorMessage;
		
		public EventValueInfo(final Class<E> event, final Class<T> c, final Getter<T, E> getter) {
			Validate.notNull(event, c, getter);
			this.event = event;
			this.c = c;
			this.getter = getter;
			exculdes = null;
			excludeErrorMessage = null;
		}
		
		public EventValueInfo(final Class<E> event, final Class<T> c, final Getter<T, E> getter, final String excludeErrorMessage, final Class<? extends E>[] exculdes) {
			Validate.notNull(event, c, getter);
			this.event = event;
			this.c = c;
			this.getter = getter;
			this.exculdes = exculdes;
			this.excludeErrorMessage = excludeErrorMessage;
		}
	}
	
	private static final List<EventValueInfo<?, ?>> defaultEventValues = new ArrayList<Skript.EventValueInfo<?, ?>>(30);
	private static final List<EventValueInfo<?, ?>> pastEventValues = new ArrayList<Skript.EventValueInfo<?, ?>>();
	private static final List<EventValueInfo<?, ?>> futureEventValues = new ArrayList<Skript.EventValueInfo<?, ?>>();
	
	private static final List<EventValueInfo<?, ?>> getEventValuesList(final int time) {
		if (time == -1)
			return pastEventValues;
		if (time == 0)
			return defaultEventValues;
		if (time == 1)
			return futureEventValues;
		throw new IllegalArgumentException("time must be -1, 0, or 1");
	}
	
	/**
	 * Registers an event value.
	 * 
	 * @param e the event type
	 * @param c the type of the default value
	 * @param g the getter to get the value
	 * @param time -1 if this is the value before the event, 1 if after, and 0 if it's the default or this value doesn't have distinct states.
	 *            <b>Always register a default state!</b> You can leave out one of the other states instead, e.g. only register a default and a past state. The future state will
	 *            default to the default state in this case.
	 */
	public static <T, E extends Event> void registerEventValue(final Class<E> e, final Class<T> c, final Getter<T, E> g, final int time) {
		registerEventValue(e, c, g, time, null, (Class<? extends E>[]) null);
	}
	
	/**
	 * Same as {@link #registerEventValue(Class, Class, Getter, int)}
	 * 
	 * @param e
	 * @param c
	 * @param g
	 * @param time
	 * @param excludes Subclasses of the event for which this event value should not be registered for
	 */
	public static <T, E extends Event> void registerEventValue(final Class<E> e, final Class<T> c, final Getter<T, E> g, final int time, final String excludeErrorMessage, final Class<? extends E>... excludes) {
		checkAcceptRegistrations();
		final List<EventValueInfo<?, ?>> eventValues = getEventValuesList(time);
		for (int i = 0; i < eventValues.size(); i++) {
			final EventValueInfo<?, ?> info = eventValues.get(i);
			if ((info.event.isAssignableFrom(e) && info.event != e) || (info.event == e && info.c.isAssignableFrom(c))) {
				eventValues.add(i, new EventValueInfo<E, T>(e, c, g, excludeErrorMessage, excludes));
				return;
			}
		}
		eventValues.add(new EventValueInfo<E, T>(e, c, g, excludeErrorMessage, excludes));
	}
	
	/**
	 * Gets a specific value from an event. Returns null if the event doesn't have such a value (conversions are done to try and get the desired value).<br>
	 * It is recommended to use {@link #getEventValueGetter(Class, Class)} or {@link EventValueExpression#EventValueExpression(Class)} instead of invoking this method repeatedly.
	 * 
	 * @param e
	 * @param c
	 * @param time
	 * @return
	 * @see #registerEventValue(Class, Class, Getter, int)
	 */
	public static <T, E extends Event> T getEventValue(final E e, final Class<T> c, final int time) {
		final Getter<? extends T, ? super E> g = getEventValueGetter((Class<E>) e.getClass(), c, time);
		if (g == null)
			return null;
		return g.get(e);
	}
	
	/**
	 * Returns a getter to get a value from an event.<br>
	 * Can print an error if the event value is blocked for the given event.
	 * 
	 * @param e
	 * @param c
	 * @param time
	 * @return
	 * @see #registerEventValue(Class, Class, Getter, int)
	 * @see EventValueExpression#EventValueExpression(Class)
	 */
	public static final <T, E extends Event> Getter<? extends T, ? super E> getEventValueGetter(final Class<E> e, final Class<T> c, final int time) {
		return getEventValueGetter(e, c, time, true);
	}
	
	@SuppressWarnings("unchecked")
	private static final <T, E extends Event> Getter<? extends T, ? super E> getEventValueGetter(final Class<E> e, final Class<T> c, final int time, final boolean allowDefault) {
		final List<EventValueInfo<?, ?>> eventValues = getEventValuesList(time);
		for (final EventValueInfo<?, ?> ev : eventValues) {
			if (ev.event.isAssignableFrom(e) && c.isAssignableFrom(ev.c)) {
				if (!checkExcludes(ev, e, true))
					return null;
				return (Getter<? extends T, ? super E>) ev.getter;
			}
		}
		for (final EventValueInfo<?, ?> ev : eventValues) {
			if (ev.event.isAssignableFrom(e) && ev.c.isAssignableFrom(c)) {
				if (!checkExcludes(ev, e, true))
					return null;
				return new Getter<T, E>() {
					@Override
					public T get(final E e) {
						final Object o = ((Getter<? super T, ? super E>) ev.getter).get(e);
						if (c.isInstance(o))
							return (T) o;
						return null;
					}
				};
			}
		}
		for (final EventValueInfo<?, ?> ev : eventValues) {
			if (ev.event.isAssignableFrom(e)) {
				if (!checkExcludes(ev, e, true))
					return null;
				final Getter<? extends T, ? super E> g = (Getter<? extends T, ? super E>) getConvertedGetter(ev, c);
				if (g != null)
					return g;
			}
		}
		if (allowDefault && time != 0)
			return getEventValueGetter(e, c, 0);
		return null;
	}
	
	private final static boolean checkExcludes(final EventValueInfo<?, ?> ev, final Class<? extends Event> e, final boolean printError) {
		if (ev.exculdes == null)
			return true;
		for (final Class<? extends Event> ex : ev.exculdes) {
			if (ex.isAssignableFrom(e)) {
				if (printError)
					Skript.error(ev.excludeErrorMessage);
				return false;
			}
		}
		return true;
	}
	
	private final static <E extends Event, F, T> Getter<? extends T, ? super E> getConvertedGetter(final EventValueInfo<E, F> i, final Class<T> to) {
		final Converter<? super F, ? extends T> c = getConverter(i.c, to);
		if (c == null)
			return null;
		return new Getter<T, E>() {
			@Override
			public T get(final E e) {
				final F f = i.getter.get(e);
				if (f == null)
					return null;
				return c.convert(f);
			}
		};
	}
	
	public static final boolean doesEventValueHaveTimeStates(final Class<? extends Event> e, final Class<?> c) {
		return getEventValueGetter(e, c, -1, false) != null || getEventValueGetter(e, c, 1, false) != null;
	}
	
	// ================ COMMANDS ================
	
	private static final Map<String, SkriptCommand> commands = new HashMap<String, SkriptCommand>();
	
	public static SimpleCommandMap commandMap = null;
	private static Map<String, Command> cmKnownCommands;
	private static Set<String> cmAliases;
	static {
		try {
			if (Bukkit.getPluginManager() instanceof SimplePluginManager) {
				final Field commandMapField = SimplePluginManager.class.getDeclaredField("commandMap");
				commandMapField.setAccessible(true);
				commandMap = (SimpleCommandMap) commandMapField.get(Bukkit.getPluginManager());
				
				final Field knownCommandsField = SimpleCommandMap.class.getDeclaredField("knownCommands");
				knownCommandsField.setAccessible(true);
				cmKnownCommands = (Map<String, Command>) knownCommandsField.get(commandMap);
				
				final Field aliasesField = SimpleCommandMap.class.getDeclaredField("aliases");
				aliasesField.setAccessible(true);
				cmAliases = (Set<String>) aliasesField.get(commandMap);
			}
		} catch (final SecurityException e) {
			error("Please disable the security manager");
			commandMap = null;
		} catch (final Exception e) {
			outdatedError(e);
			commandMap = null;
		}
	}
	
	/**
	 * Dispatches a command with calling command events
	 * 
	 * @param sender
	 * @param command
	 * @return
	 */
	public final static boolean dispatchCommand(final CommandSender sender, final String command) {
		if (sender instanceof Player) {
			final PlayerCommandPreprocessEvent e = new PlayerCommandPreprocessEvent((Player) sender, "/" + command);
			Bukkit.getPluginManager().callEvent(e);
			if (e.isCancelled() || !e.getMessage().startsWith("/"))
				return false;
			return Bukkit.dispatchCommand(e.getPlayer(), e.getMessage().substring(1));
		} else if (sender instanceof ConsoleCommandSender) {
			final ServerCommandEvent e = new ServerCommandEvent(sender, command);
			Bukkit.getPluginManager().callEvent(e);
			return Bukkit.dispatchCommand(e.getSender(), e.getCommand());
		} else {
			return Bukkit.dispatchCommand(sender, command);
		}
	}
	
	public static boolean commandExists(final String command) {
		final SkriptCommand c = commands.get(command);
		return c != null && c.getName().equals(command);
	}
	
	private static boolean acceptingCommandRegistrations = true;
	
	public static void registerCommand(final SkriptCommand command) {
		if (!acceptingCommandRegistrations)
			throw new SkriptAPIException("Registering commands is disabled afer all scripts have been loaded!");
		commands.put(command.getName().toLowerCase(), command);
		for (final String alias : command.getAliases()) {
			commands.put(alias.toLowerCase(), command);
		}
		if (commandMap != null) {
			command.register(commandMap, cmKnownCommands, cmAliases);
		}
	}
	
	private final static void finishRegisteringCommands() {
		acceptingCommandRegistrations = false;
		try {
			final HelpMap help = Bukkit.getServer().getHelpMap();
			final Iterator<HelpTopic> iter = help.getHelpTopics().iterator();
			while (iter.hasNext()) {
				if (iter.next().getName().equals("Skript")) {
					iter.remove();
					break;
				}
			}
			final Set<HelpTopic> topics = new TreeSet<HelpTopic>(HelpTopicComparator.helpTopicComparatorInstance());
			for (final SkriptCommand command : commands.values()) {
				final HelpTopic t = new GenericCommandHelpTopic(command.getBukkitCommand());
				help.addTopic(t);
				topics.add(t);
				for (final String alias : command.getActiveAliases()) {
					final HelpTopic at = new CommandAliasHelpTopic("/" + alias, "/" + command.getLabel(), help);
					help.addTopic(at);
					topics.add(at);
				}
			}
			help.addTopic(new IndexHelpTopic("Skript", "All commands created with Skript", null, topics, "Below is a list of all commands created with Skript:"));
		} catch (final Exception e) {
			Skript.exception(e, "Could not register the custom commands in Bukkit's help map");
		}
	}
	
	private final static void clearCommands() {
		if (commandMap != null) {
			for (final SkriptCommand c : commands.values())
				c.unregister(commandMap, cmKnownCommands, cmAliases);
		}
		commands.clear();
		acceptingCommandRegistrations = true;
	}
	
	public static void outdatedError() {
		error("Skript v" + instance.getDescription().getVersion() + " is not fully compatible with CraftBukkit " + Bukkit.getVersion() + ". Some feature(s) will be broken until you update Skript.");
	}
	
	public static void outdatedError(final Exception e) {
		outdatedError();
		e.printStackTrace();
	}
	
	private final static Listener commandListener = new Listener() {
		@SuppressWarnings("unused")
		@EventHandler(priority = EventPriority.LOW, ignoreCancelled = true)
		public void onPlayerCommand(final PlayerCommandPreprocessEvent e) {
			if (handleCommand(e.getPlayer(), e.getMessage().substring(1)))
				e.setCancelled(true);
		}
		
		@SuppressWarnings("unused")
		@EventHandler(priority = EventPriority.LOW)
		public void onServerCommand(final ServerCommandEvent e) {
			if (e.getCommand() == null || e.getCommand().isEmpty())
				return;
			if (handleCommand(e.getSender(), e.getCommand()))
				e.setCommand("");
		}
	};
	
	/**
	 * 
	 * @param sender
	 * @param command full command string without the slash
	 * @return whether to cancel the event
	 */
	private final static boolean handleCommand(final CommandSender sender, final String command) {
		final String[] cmd = command.split("\\s+", 2);
		cmd[0] = cmd[0].toLowerCase();
		if (cmd[0].endsWith("?")) {
			final SkriptCommand c = commands.get(cmd[0].substring(0, cmd[0].length() - 1));
			if (c != null) {
				c.sendHelp(sender);
				return true;
			}
		}
		final SkriptCommand c = commands.get(cmd[0]);
		if (c != null) {
			if (cmd.length == 2 && cmd[1].equals("?")) {
				c.sendHelp(sender);
				return true;
			}
			c.execute(sender, cmd[0], cmd.length == 1 ? "" : cmd[1]);
			return true;
		} else if (enableEffectCommands) {
			if (!sender.hasPermission("skript.effectcommands"))
				return false;
			if (commandMap != null && commandMap.getCommand(cmd[0]) != null)
				return false;
			
			final SubLog log = SkriptLogger.startSubLog();
			final Effect e = Effect.parse(command, null);
			SkriptLogger.stopSubLog(log);
			if (e != null) {
				sender.sendMessage(ChatColor.GRAY + "executing '" + ChatColor.stripColor(command) + "'");
				e.run(new CommandEvent(sender, "effectcommand", new String[0]));
				return true;
			} else if (log.hasErrors()) {
				sender.sendMessage(ChatColor.RED + "Error in: " + ChatColor.GRAY + ChatColor.stripColor(command));
				log.printErrors(sender, null);
				sender.sendMessage("Press the up arrow key to edit the command");
				return true;
			}
			return false;
		}
		return false;
	}
	
	// ================ LOGGING ================
	
	public static final boolean logNormal() {
		return SkriptLogger.log(Verbosity.NORMAL);
	}
	
	public static final boolean logHigh() {
		return SkriptLogger.log(Verbosity.HIGH);
	}
	
	public static final boolean logVeryHigh() {
		return SkriptLogger.log(Verbosity.VERY_HIGH);
	}
	
	public static final boolean debug() {
		return SkriptLogger.debug;
	}
	
	public static final boolean log(final Verbosity minVerb) {
		return SkriptLogger.log(minVerb);
	}
	
	/**
	 * @see SkriptLogger#log(Level, String)
	 */
	public static void config(final String info) {
		SkriptLogger.log(Level.CONFIG, info);
	}
	
	/**
	 * @see SkriptLogger#log(Level, String)
	 */
	public static void info(final String info) {
		SkriptLogger.log(Level.INFO, info);
	}
	
	/**
	 * @see SkriptLogger#log(Level, String)
	 */
	public static void warning(final String warning) {
		SkriptLogger.log(Level.WARNING, warning);
	}
	
	public static void error(final String error) {
		SkriptLogger.log(Level.SEVERE, error);
	}
	
	private final static String EXCEPTION_PREFIX = "##!! ";
	
	/**
	 * Used if something happens that shouldn't happen
	 * 
	 * @param info Description of the error and additional information
	 * @return an empty RuntimeException to throw if code execution should terminate.
	 */
	public final static RuntimeException exception(final String... info) {
		return exception(null, info);
	}
	
	/**
	 * Used if something happens that shouldn't happen
	 * 
	 * @param cause exception that shouldn't occur
	 * @param info Description of the error and additional information
	 * @return an empty RuntimeException to throw if code execution should terminate.
	 */
	public final static RuntimeException exception(Exception cause, final String... info) {
		
		logEx();
		logEx("Severe Error:");
		logEx(info);
		logEx();
		logEx("If you're developing an add-on for Skript this likely means you have done something wrong.");
		logEx("If you're a server admin, please go to http://dev.bukkit.org/server-mods/skript/tickets/");
		logEx("and create a new ticket with a meaningful title, copy & paste this whole error into it,");
		logEx("and please describe what you did before it happened and/or what you think caused the error.");
		logEx("If you feel like it's a trigger that's causing the error please post the trigger as well.");
		logEx("By following this guide fixing the error should be easy and done fast.");
		
		logEx();
		logEx("Stacktrace:");
		if (cause == null) {
			logEx("  warning: no exception given, dumping current stack trace instead");
			cause = new Exception();
		}
		logEx(cause.toString());
		for (final StackTraceElement e : cause.getStackTrace())
			logEx("    at " + e.toString());
		
		logEx();
		logEx("Version Information:");
		logEx("  Skript: " + Skript.getInstance().getDescription().getVersion());
		logEx("  Bukkit: " + Bukkit.getBukkitVersion());
		logEx("  Java: " + System.getProperty("java.version"));
		logEx();
		logEx("Running CraftBukkit: " + runningCraftBukkit);
		logEx();
		logEx("Current node: " + SkriptLogger.getNode());
		logEx();
		logEx("End of Error.");
		logEx();
		
		final RuntimeException r = new RuntimeException();
		r.setStackTrace(new StackTraceElement[0]);
		return r;
	}
	
	private final static void logEx() {
		Bukkit.getLogger().severe(EXCEPTION_PREFIX);
	}
	
	private final static void logEx(final String... lines) {
		for (final String line : lines)
			Bukkit.getLogger().severe(EXCEPTION_PREFIX + line);
	}
	
	// ================ CONFIGS ================
	
	static Config mainConfig;
	static final ArrayList<Config> configs = new ArrayList<Config>();
	
	private static boolean keepConfigsLoaded = false; //true;
	private static boolean enableEffectCommands = false;
	
	private static final void parseMainConfig() {
		
		final ArrayList<String> aliasNodes = new ArrayList<String>();
		
		new SectionValidator()
				.addNode("verbosity", new EnumEntryValidator<Verbosity>(Verbosity.class, new Setter<Verbosity>() {
					@Override
					public void set(final Verbosity v) {
						SkriptLogger.setVerbosity(v);
					}
				}), false)
				.addNode("plugin priority", new EnumEntryValidator<EventPriority>(EventPriority.class, new Setter<EventPriority>() {
					@Override
					public void set(final EventPriority p) {
						Skript.priority = p;
					}
				}, "lowest, low, normal, high, highest"), false)
				.addEntry("aliases", new Setter<String>() {
					@Override
					public void set(final String s) {
						for (final String n : s.split(","))
							aliasNodes.add(n.trim());
					}
				}, false)
				.addEntry("keep configs loaded", Boolean.class, new Setter<Boolean>() {
					@Override
					public void set(final Boolean b) {
						keepConfigsLoaded = b;
					}
				}, true)
				.addEntry("enable effect commands", Boolean.class, new Setter<Boolean>() {
					@Override
					public void set(final Boolean b) {
						enableEffectCommands = b;
					}
				}, false)
				.addEntry("date format", new Setter<String>() {
					@Override
					public void set(final String s) {
						try {
							if (!s.equalsIgnoreCase("default"))
								dateFormat = new SimpleDateFormat(s);
						} catch (final IllegalArgumentException e) {
							// TODO shorten URL?
							Skript.error("'" + s + "' is not a valid date format. Please refer to http://docs.oracle.com/javase/6/docs/api/java/text/SimpleDateFormat.html for instructions on the format.");
						}
					}
				}, true)
				.addEntry("disable variable conflict warnings", Boolean.class, new Setter<Boolean>() {
					@Override
					public void set(final Boolean b) {
						disableVariableConflictWarnings = b;
					}
				}, true)
				.setAllowUndefinedSections(true)
				.validate(mainConfig.getMainNode());
		
		for (final Node node : mainConfig.getMainNode()) {
			if (node instanceof SectionNode) {
				if (!aliasNodes.contains(node.getName())) {
					Skript.error("Invalid section '" + node.getName() + "'. If this is an alias section add it to 'aliases' so it will be loaded.");
				}
			}
		}
		
		final HashMap<String, HashMap<String, ItemType>> variations = new HashMap<String, HashMap<String, ItemType>>();
		int num = 0;
		for (final String an : aliasNodes) {
			final Node node = mainConfig.getMainNode().get(an);
			SkriptLogger.setNode(node);
			if (node == null) {
				error("alias section '" + an + "' not found!");
				continue;
			}
			if (!(node instanceof SectionNode)) {
				error("aliases have to be in sections, but '" + an + "' is not a section!");
				continue;
			}
			int i = 0;
			for (final Node n : (SectionNode) node) {
				if (n instanceof EntryNode) {
					i += Aliases.addAliases(((EntryNode) n).getKey(), ((EntryNode) n).getValue(), variations);
				} else if (n instanceof SectionNode) {
					if (!(n.getName().startsWith("{") && n.getName().endsWith("}"))) {
						Skript.error("unexpected non-variation section");
						continue;
					}
					final HashMap<String, ItemType> vs = new HashMap<String, ItemType>();
					for (final Node a : (SectionNode) n) {
						if (a instanceof SectionNode) {
							Skript.error("unexpected section");
							continue;
						}
						final ItemType t = Aliases.parseAlias(((EntryNode) a).getValue());
						if (t != null)
							vs.put(((EntryNode) a).getKey(), t);
					}
					variations.put(n.getName().substring(1, n.getName().length() - 1), vs);
				}
			}
			if (logHigh())
				info("loaded " + i + " alias" + (i == 1 ? "" : "es") + " from " + node.getName());
			num += i;
		}
		SkriptLogger.setNode(null);
		
		if (!keepConfigsLoaded)
			mainConfig = null;
		
		if (log(Verbosity.NORMAL))
			info("loaded a total of " + num + " aliases");
		
		Aliases.addMissingMaterialNames();
		
	}
	
	private void loadMainConfig() {
//		info("loading main config...");
		try {
			
			final File oldConfig = new File(getDataFolder(), "config.cfg");
			final File config = new File(getDataFolder(), "config.sk");
			if (oldConfig.exists()) {
				if (!config.exists()) {
					oldConfig.renameTo(config);
					Skript.info("[1.3] Renamed your 'config.cfg' to 'config.sk' to match the new format");
				} else {
					Skript.error("Found both a new and an old config, ingoring the old one");
				}
			}
			if (!config.exists()) {
				error("Config file 'config.sk' does not exist!");
				return;
			}
			if (!config.canRead()) {
				error("Config file 'config.sk' cannot be read!");
				return;
			}
			mainConfig = new Config(config, false, "=");
			parseMainConfig();
			
//			if (logNormal())
//				info("main config loaded successfully.");
			
		} catch (final Exception e) {
			exception(e, "error loading config");
		}
	}
	
	/**
	 * As it's impossible to unregister events with Bukkit this set is used to prevent that any event will ever be registered more than once when reloading.
	 */
	private final static Set<Class<? extends Event>> registeredEvents = new HashSet<Class<? extends Event>>();
	
	private static void loadScripts() {
		
		boolean successful = true;
		
		final File scriptsFolder = new File(instance.getDataFolder(), Skript.SCRIPTSFOLDER + File.separatorChar);
		
		final File oldFolder = new File(instance.getDataFolder(), "triggers" + File.separatorChar);
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
		
		final SubLog log = SkriptLogger.startSubLog();
		final Date start = new Date();
		
		int numFiles = 0;
		try {
			numFiles = loadScripts(scriptsFolder);
		} catch (final Exception e) {
			SkriptLogger.setNode(null);
			Skript.exception(e, "could not load trigger files");
			successful = false;
		}
		
		log.stop();
		log.printLog();
		if (!log.hasErrors())
			info("All scripts loaded without errors!");
		
		if (successful && numFiles == 0)
			warning("No scripts were found, maybe you should write some ;)");
		if (successful && logNormal() && numFiles > 0)
			info("loaded " + numFiles + " script" + (numFiles == 1 ? "" : "s")
					+ " with a total of " + ScriptLoader.loadedTriggers + " trigger" + (ScriptLoader.loadedTriggers == 1 ? "" : "s")
					+ " and " + ScriptLoader.loadedCommands + " command" + (ScriptLoader.loadedCommands == 1 ? "" : "s")
					+ " in " + start.difference(new Date()));
		
		for (final Class<? extends Event> e : SkriptEventHandler.triggers.keySet()) {
			if (!registeredEvents.contains(e)) {
				Bukkit.getPluginManager().registerEvent(e, new Listener() {}, priority, SkriptEventHandler.ee, instance);
				registeredEvents.add(e);
			}
		}
	}
	
	private final static int loadScripts(final File directory) throws IOException {
		int numFiles = 0;
		for (final File f : directory.listFiles(new FileFilter() {
			@Override
			public boolean accept(final File f) {
				return (f.isDirectory() || f.getName().endsWith(".sk")) && !f.getName().startsWith("-");
			}
		})) {
			if (f.isDirectory()) {
				numFiles += loadScripts(f);
			} else {
				final Config c = new Config(f, true, ":");
				if (keepConfigsLoaded)
					configs.add(c);
				ScriptLoader.load(c);
				numFiles++;
			}
		}
		return numFiles;
	}
	
	/**
	 * @param c
	 * @return
	 */
	public static String getSyntaxElementName(final Class<? extends SyntaxElement> c) {
		if (Condition.class.isAssignableFrom(c)) {
			return "condition";
		} else if (Effect.class.isAssignableFrom(c)) {
			return "effect";
		} else if (Variable.class.isAssignableFrom(c)) {
			return "variable";
		} else if (Expression.class.isAssignableFrom(c)) {
			return "expression";
		}
		return "syntax element";
	}
	
}
