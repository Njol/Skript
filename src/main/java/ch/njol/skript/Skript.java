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
import java.io.IOException;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Random;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.logging.Level;
import java.util.regex.Pattern;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.server.ServerCommandEvent;
import org.bukkit.plugin.java.JavaPlugin;

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
import ch.njol.skript.command.Commands;
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
import ch.njol.skript.lang.util.VariableString;
import ch.njol.skript.log.SkriptLogger;
import ch.njol.skript.log.SubLog;
import ch.njol.skript.log.Verbosity;
import ch.njol.skript.util.CommandHelp;
import ch.njol.skript.util.FileUtils;
import ch.njol.skript.util.Getter;
import ch.njol.skript.util.ItemType;
import ch.njol.skript.util.StringMode;
import ch.njol.skript.util.Utils;
import ch.njol.skript.util.Version;
import ch.njol.util.Pair;
import ch.njol.util.ReversedListView;
import ch.njol.util.Setter;
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
	
	public static Version getVersion() {
		return version;
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
		
		Language.loadDefault();
		
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
		
		Commands.registerListener();
		
		if (logNormal())
			info(" ~ created by & © Peter Güttinger aka Njol ~");
		
		Bukkit.getScheduler().scheduleSyncDelayedTask(this, new Runnable() {
			@Override
			public void run() {
				
//				Economy.load();
				
				Skript.stopAcceptingRegistrations();
				
				Variables.loadVariables();
				
				ScriptLoader.loadScripts();
				
				Skript.info("Skript finished loading!");
			}
		});
		
		Variables.scheduleSaveTask();
		
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
	private final static void disableScripts() {
		configs.clear();
		
		for (final Trigger t : ScriptLoader.selfRegisteredTriggers)
			t.getEvent().unregisterAll();
		ScriptLoader.selfRegisteredTriggers.clear();
		
		VariableString.variableNames.clear();
		
		SkriptEventHandler.triggers.clear();
		Commands.clearCommands();
	}
	
	/**
	 * Prints errors from reloading the config & scripts
	 */
	private final static void reload() {
		disableScripts();
		reloadMainConfig();
		ScriptLoader.loadScripts();
	}
	
	/**
	 * Prints errors
	 */
	private final static void reloadScripts() {
		disableScripts();
		ScriptLoader.loadScripts();
	}
	
	/**
	 * Prints errors
	 */
	private final static void reloadMainConfig() {
		Aliases.clear();
		Language.clear();
		Skript.getInstance().loadMainConfig();
	}
	
	@Override
	public void onDisable() {
		disabled = true;
		
		Variables.cancelSaveTask(); // async tasks aren't stopped with cancelTasks(Plugin)?
		Bukkit.getScheduler().cancelTasks(this);
		
		Variables.saveVariables();
		
		disableScripts();
		
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
	
	private final static CommandHelp skriptCommandHelp = new CommandHelp("<gray>/<gold>skript", "Skript's main command", "cyan")
			.add(new CommandHelp("reload", "Reloads the config, all scripts, everything, or a specific script", "red")
					.add("all", "Reloads the config and all scripts")
					.add("config", "Reloads the config")
					.add("scripts", "Reloads all scripts")
					.add("<script>", "Reloads a specific script")
			).add(new CommandHelp("enable", "Enables all scripts or a specific one", "red")
					.add("all", "Enables all scripts")
					.add("<script>", "Enables a specific script")
			).add(new CommandHelp("disable", "Disables all scripts or a specific one", "red")
					.add("all", "Disables all scripts")
					.add("<script>", "Disables a specific script")
			//			).add(new CommandHelp("variable", "Commands for modifying variables", "red")
//					.add("set", "Creates a new variable or changes an existing one")
//					.add("delete", "Deletes a variable")
//					.add("find", "Find variables")
			).add("help", "Prints this help message");
	
	private final static void message(final CommandSender recipient, final String message) {
		recipient.sendMessage(Utils.prepareMessage("<grey>[<gold>Skript<grey>]<reset> " + message));
	}
	
	@Override
	public boolean onCommand(final CommandSender sender, final Command command, final String label, final String[] args) {
		if (!skriptCommandHelp.test(sender, args))
			return true;
		if (args[0].equalsIgnoreCase("reload")) {
			if (args[1].equalsIgnoreCase("all")) {
				final SubLog log = SkriptLogger.startSubLog();
				reload();
				log.stop();
				if (log.hasErrors()) {
					message(sender, "Error(s) while reloading the config and all scripts:");
					log.printErrors(sender, null);
				} else {
					message(sender, "Successfully reloaded the config and all scripts");
				}
				return true;
			} else if (args[1].equalsIgnoreCase("scripts")) {
				final SubLog log = SkriptLogger.startSubLog();
				reloadScripts();
				log.stop();
				if (log.hasErrors()) {
					message(sender, "Error(s) while reloading all scripts:");
					log.printErrors(sender, null);
				} else {
					message(sender, "Successfully reloaded all scripts");
				}
			} else if (args[1].equalsIgnoreCase("config")) {
				final SubLog log = SkriptLogger.startSubLog();
				reloadMainConfig();
				log.stop();
				if (log.hasErrors()) {
					message(sender, "Error(s) while reloading the main config:");
					log.printErrors(sender, null);
				} else {
					message(sender, "Successfully reloaded the main config");
				}
			} else {
				final File f = getScriptFromArgs(sender, args, 1);
				if (f == null)
					return true;
				if (f.getName().startsWith("-")) {
					message(sender, "This script is currently disabled. Use <gray>/<gold>skript <cyan>enable <red>" + f.getName().substring(1, f.getName().length() - 3) + "<reset> to enable it.");
					return true;
				}
				ScriptLoader.unloadScript(f);
				final SubLog log = SkriptLogger.startSubLog();
				ScriptLoader.loadScript(f);
				log.stop();
				if (log.hasErrors()) {
					message(sender, "Error(s) while reloading <gold>" + f.getName() + "<reset>:");
					log.printErrors(sender, null);
				} else {
					message(sender, "Successfully reloaded <gold>" + f.getName() + "<reset>!");
				}
				return true;
			}
		} else if (args[0].equalsIgnoreCase("enable")) {
			if (args[1].equals("all")) {
				try {
					final Collection<File> files = toggleScripts(true);
					final SubLog log = SkriptLogger.startSubLog();
					ScriptLoader.loadScripts(files);
					log.stop();
					if (log.hasErrors()) {
						message(sender, "Error(s) while loading disabled scripts:");
						log.printErrors(sender, null);
					} else {
						message(sender, "Successfully loaded & enabled all previously disabled scripts!");
					}
				} catch (final IOException e) {
					message(sender, "Could not enable any scripts (some scripts might however have been renamed already): " + e.getLocalizedMessage());
				}
			} else {
				File f = getScriptFromArgs(sender, args, 1);
				if (f == null)
					return true;
				if (!f.getName().startsWith("-")) {
					message(sender, "<gold>" + f.getName() + "<reset> is already enabled!");
					return true;
				}
				
				try {
					FileUtils.move(f, new File(f.getParentFile(), f.getName().substring(1)));
				} catch (final IOException e) {
					message(sender, "Could not enable <gold>" + f.getName().substring(1) + "<reset>: " + e.getLocalizedMessage());
					return true;
				}
				f = new File(f.getParentFile(), f.getName().substring(1));
				
				final SubLog log = SkriptLogger.startSubLog();
				ScriptLoader.loadScript(f);
				log.stop();
				if (log.hasErrors()) {
					message(sender, "Error(s) while enabling <gold>" + f.getName() + "<reset>:");
					log.printErrors(sender, null);
				} else {
					message(sender, "Successfully enabled <gold>" + f.getName() + "<reset>!");
				}
				return true;
			}
		} else if (args[0].equalsIgnoreCase("disable")) {
			if (args[1].equals("all")) {
				disableScripts();
				try {
					toggleScripts(false);
					message(sender, "Successfully disabled all scripts!");
				} catch (final IOException e) {
					message(sender, "Could not rename all scripts - some scripts will be enabled again when you restart the server: " + e.getLocalizedMessage());
				}
			} else {
				final File f = getScriptFromArgs(sender, args, 1);
				if (f == null)
					return true;
				if (f.getName().startsWith("-")) {
					message(sender, "<gold>" + f.getName().substring(1) + "<reset> is already disabled!");
					return true;
				}
				
				ScriptLoader.unloadScript(f);
				
				try {
					FileUtils.move(f, new File(f.getParentFile(), "-" + f.getName()));
				} catch (final IOException e) {
					message(sender, "Could not rename <gold>" + f.getName() + "<reset>, it will be enabled again when you restart the server: " + e.getLocalizedMessage());
					return true;
				}
				message(sender, "Successfully disabled <gold>" + f.getName() + "<reset>!");
				return true;
			}
		} else if (args[0].equalsIgnoreCase("help")) {
			skriptCommandHelp.showHelp(sender);
		}
		return true;
	}
	
	private File getScriptFromArgs(final CommandSender sender, final String[] args, final int start) {
		final StringBuilder b = new StringBuilder();
		for (int i = 1; i < args.length; i++)
			b.append(" " + args[i]);
		String script = b.toString().trim();
		if (!script.endsWith(".sk"))
			script = script + ".sk";
		if (script.startsWith("-"))
			script = script.substring(1);
		File f = new File(getDataFolder(), SCRIPTSFOLDER + File.separator + script);
		if (!f.exists()) {
			f = new File(getDataFolder(), SCRIPTSFOLDER + File.separator + "-" + script);
			if (!f.exists()) {
				message(sender, "Can't find the script <grey>'<gold>" + script + "<grey>'<reset> in the scripts folder!");
				return null;
			}
		}
		return f;
	}
	
	private final static Collection<File> toggleScripts(final boolean enable) throws IOException {
		return FileUtils.renameAll(new File(Skript.getInstance().getDataFolder(), SCRIPTSFOLDER), new Converter<String, String>() {
			@Override
			public String convert(final String name) {
				if (name.startsWith("-") == enable)
					return enable ? name.substring(1) : "-" + name;
				return null;
			}
		});
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
	
	public static void outdatedError() {
		error("Skript v" + instance.getDescription().getVersion() + " is not fully compatible with CraftBukkit " + Bukkit.getVersion() + ". Some feature(s) will be broken until you update Skript.");
	}
	
	public static void outdatedError(final Exception e) {
		outdatedError();
		e.printStackTrace();
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
	
	static EventPriority defaultEventPriority = EventPriority.NORMAL;
	
	public static EventPriority getDefaultEventPriority() {
		return defaultEventPriority;
	}
	
	public static <T> T[] array(final T... array) {
		return array;
	}
	
	private static DateFormat dateFormat = DateFormat.getDateTimeInstance(DateFormat.SHORT, DateFormat.SHORT);
	
	public static DateFormat getDateFormat() {
		return dateFormat;
	}
	
	public static boolean disableVariableConflictWarnings;
	
	/**
	 * Parses a number that was validated to be an integer but might still result in a {@link NumberFormatException} when parsed with {@link Integer#parseInt(String)} due to
	 * overflow.
	 * This method will return {@link Integer#MIN_VALUE} or {@link Integer#MAX_VALUE} respectively if that happens.
	 * 
	 * @param s
	 * @return
	 */
	public final static int parseInt(final String s) {
		try {
			return Integer.parseInt(s);
		} catch (final NumberFormatException e) {
			return s.startsWith("-") ? Integer.MIN_VALUE : Integer.MAX_VALUE;
		}
	}
	
	// ================ LISTENER FUNCTIONS ================
	
	static boolean listenerEnabled = true;
	
	public static void disableListener() {
		listenerEnabled = false;
	}
	
	public static void enableListener() {
		listenerEnabled = true;
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
	public static ClassInfo<?> getClassInfoFromUserInput(String name) {
		name = name.toLowerCase();
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
		} else {
			final ServerCommandEvent e = new ServerCommandEvent(sender, command);
			Bukkit.getPluginManager().callEvent(e);
			if (e.getCommand() == null || e.getCommand().isEmpty())
				return false;
			return Bukkit.dispatchCommand(e.getSender(), e.getCommand());
		}
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
		return SkriptLogger.debug();
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
	public final static RuntimeException exception(Throwable cause, final String... info) {
		
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
	static boolean keepConfigsLoaded = false;
	
	public static boolean enableEffectCommands = false;
	public static String effectCommandToken = "!";
	
	private void loadMainConfig() {
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
				error("Config file 'config.sk' does not exist! Please make sure that you downloaded the .zip file (and not the .jar) from Skript's BukkitDev page and extracted it correctly.");
				return;
			}
			if (!config.canRead()) {
				error("Config file 'config.sk' cannot be read!");
				return;
			}
			
			mainConfig = new Config(config, false, true, "=");
			
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
							Skript.defaultEventPriority = p;
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
					.addEntry("effect command token", new Setter<String>() {
						@Override
						public void set(final String s) {
							if (s.startsWith("/")) {
								Skript.error("Cannot use a token that starts with a slash because it can conflict with commands");
							} else {
								effectCommandToken = s;
							}
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
					.addEntry("language", new Setter<String>() {
						@Override
						public void set(final String s) {
							if (!Language.load(s)) {
								Skript.error("No language file found for '" + s + "'!");
							}
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
			
		} catch (final Exception e) {
			exception(e, "error loading config");
		}
	}
	
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
