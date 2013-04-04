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
 * Copyright 2011-2013 Peter Güttinger
 * 
 */

package ch.njol.skript;

import java.io.Closeable;
import java.io.IOException;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.EmptyStackException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.logging.Level;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.server.ServerCommandEvent;
import org.bukkit.plugin.java.JavaPlugin;

import ch.njol.skript.Metrics.Graph;
import ch.njol.skript.Metrics.Plotter;
import ch.njol.skript.Updater.UpdateState;
import ch.njol.skript.aliases.Aliases;
import ch.njol.skript.classes.ClassInfo;
import ch.njol.skript.classes.Comparator;
import ch.njol.skript.classes.Converter;
import ch.njol.skript.classes.data.BukkitClasses;
import ch.njol.skript.classes.data.BukkitEventValues;
import ch.njol.skript.classes.data.DefaultClasses;
import ch.njol.skript.classes.data.DefaultComparators;
import ch.njol.skript.classes.data.DefaultConverters;
import ch.njol.skript.classes.data.SkriptClasses;
import ch.njol.skript.command.Commands;
import ch.njol.skript.doc.Documentation;
import ch.njol.skript.events.EvtSkript;
import ch.njol.skript.hooks.Hook;
import ch.njol.skript.lang.Condition;
import ch.njol.skript.lang.Effect;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.ExpressionInfo;
import ch.njol.skript.lang.ExpressionType;
import ch.njol.skript.lang.SelfRegisteringSkriptEvent;
import ch.njol.skript.lang.SkriptEvent;
import ch.njol.skript.lang.SkriptEventInfo;
import ch.njol.skript.lang.Statement;
import ch.njol.skript.lang.SyntaxElementInfo;
import ch.njol.skript.lang.Trigger;
import ch.njol.skript.lang.TriggerItem;
import ch.njol.skript.lang.util.SimpleExpression;
import ch.njol.skript.lang.util.VariableString;
import ch.njol.skript.localization.Language;
import ch.njol.skript.localization.Message;
import ch.njol.skript.log.CountingLogHandler;
import ch.njol.skript.log.ErrorDescLogHandler;
import ch.njol.skript.log.ErrorQuality;
import ch.njol.skript.log.LogEntry;
import ch.njol.skript.log.LogHandler;
import ch.njol.skript.log.SkriptLogger;
import ch.njol.skript.log.Verbosity;
import ch.njol.skript.registrations.Classes;
import ch.njol.skript.registrations.Comparators;
import ch.njol.skript.registrations.Converters;
import ch.njol.skript.registrations.EventValues;
import ch.njol.skript.util.Getter;
import ch.njol.skript.util.Task;
import ch.njol.skript.util.Utils;
import ch.njol.skript.util.Version;
import ch.njol.skript.variables.Variables;
import ch.njol.util.Checker;
import ch.njol.util.Kleenean;
import ch.njol.util.StringUtils;
import ch.njol.util.iterator.CheckedIterator;
import ch.njol.util.iterator.EnumerationIterable;

/**
 * <b>Skript</b> - A Bukkit plugin to modify how Minecraft behaves without having to write a single line of code (You'll likely be writing some code though =P)
 * <p>
 * Use this class to extend this plugin's functionality by adding more {@link Condition conditions}, {@link Effect effects}, {@link SimpleExpression expressions}, etc.
 * <p>
 * If your plugin.yml contains <tt>'depend: [Skript]'</tt> then your plugin will not start at all if Skript is not present. Add <tt>'softdepend: [Skript]'</tt> to your plugin.yml
 * if you want your plugin to work even if Skript isn't present, but want to make sure that it's loaded after Skript.
 * <p>
 * If you use 'softdepend' you can test whether Skript is loaded using <tt>'Bukkit.getPluginManager().getPlugin(&quot;Skript&quot;) != null'</tt>
 * <p>
 * Once you made sure that Skript is loaded you can use <code>Skript.getInstance()</code> whenever you need a reference to the plugin, but you likely won't need it since all API
 * methods are static.
 * 
 * @author Peter Güttinger
 * @see #registerAddon(JavaPlugin)
 * @see #registerCondition(Class, String...)
 * @see #registerEffect(Class, String...)
 * @see #registerExpression(Class, Class, String...)
 * @see #registerEvent(Class, Class, String...)
 * @see EventValues#registerEventValue(Class, Class, Getter)
 * @see Classes#registerClass(ClassInfo)
 * @see Comparators#registerComparator(Class, Class, Comparator)
 * @see Converters#registerConverter(Class, Class, Converter)
 */
public final class Skript extends JavaPlugin implements Listener {
	
	private final static Message m_copyright = new Message("skript.copyright");
	public static final Message m_quotes_error = new Message("skript.quotes error");
	public static final Message m_invalid_reload = new Message("skript.invalid reload");
	
	// ================ PLUGIN ================
	
	private static Skript instance = null;
	
	private static boolean disabled = false;
	
	public static Skript getInstance() {
		return instance;
	}
	
	public Skript() throws IllegalAccessException {
		if (instance != null)
			throw new IllegalAccessException("Cannot create multiple instances of Skript!");
		instance = this;
	}
	
	private static Version version = null;
	
	public static Version getVersion() {
		return version;
	}
	
	@Override
	public void onEnable() {
		if (disabled) {
			Skript.error(m_invalid_reload.toString());
			setEnabled(false);
			return;
		}
		
		Language.loadDefault(getAddonInstance());
		getFile();
		
		version = new Version(getDescription().getVersion());
		runningCraftBukkit = Bukkit.getServer().getClass().getName().equals("org.bukkit.craftbukkit.CraftServer");
		final String bukkitV = Bukkit.getBukkitVersion();
		final Matcher m = Pattern.compile("\\d+\\.\\d+(\\.\\d+)?").matcher(bukkitV);
		if (!m.find()) {
			Skript.error("The Bukkit version '" + Bukkit.getBukkitVersion() + "' does not contain a version number which is required for Skript to enable or disable certain features. Skript will now disable itself.");
			setEnabled(false);
			return;
		}
		minecraftVersion = new Version(m.group());
		
		getCommand("skript").setExecutor(new SkriptCommand());
		
		new DefaultClasses();
		new BukkitClasses();
		new BukkitEventValues();
		new SkriptClasses();
		
		new DefaultComparators();
		new DefaultConverters();
		
		try {
			getAddonInstance().loadClasses("ch.njol.skript", "conditions", "effects", "events", "expressions", "entity");
		} catch (final Exception e) {
			exception(e, "could not load required .class files: " + e.getLocalizedMessage());
			setEnabled(false);
			return;
		}
		
		if (!getDataFolder().isDirectory())
			getDataFolder().mkdirs();
		
		SkriptConfig.load();
		
		if (SkriptConfig.checkForNewVersion.value())
			Updater.check(Bukkit.getConsoleSender(), SkriptConfig.automaticallyDownloadNewVersion.value(), true);
		
		Aliases.load();
		
		Commands.registerListener();
		
		if (logNormal())
			info(" " + m_copyright);
		
		Bukkit.getScheduler().scheduleSyncDelayedTask(this, new Runnable() {
			@Override
			public void run() {
				
				// load hooks
				try {
					final JarFile jar = new JarFile(getFile());
					try {
						for (final JarEntry e : new EnumerationIterable<JarEntry>(jar.entries())) {
							if (e.getName().startsWith("ch/njol/skript/hooks/") && e.getName().endsWith("Hook.class") && StringUtils.count(e.getName(), '/') == 5) {
								final String c = e.getName().replace('/', '.').substring(0, e.getName().length() - ".class".length());
								try {
									final Class<?> hook = Class.forName(c, true, getClassLoader());
									if (hook != null && Hook.class.isAssignableFrom(hook) && !hook.isInterface()) {
										hook.getDeclaredConstructor().setAccessible(true);
										final Hook h = (Hook) hook.getDeclaredConstructor().newInstance();
										h.load();
									}
								} catch (final ClassNotFoundException ex) {
									Skript.exception(ex, "Cannot load class " + c);
								} catch (final ExceptionInInitializerError err) {
									Skript.exception(err.getCause(), "Class " + c + " generated an exception while loading");
								}
								continue;
							}
						}
					} finally {
						try {
							jar.close();
						} catch (final IOException e) {}
					}
				} catch (final Exception e) {
					error("Error while loading plugin hooks: " + e.getLocalizedMessage());
					if (testing())
						e.printStackTrace();
				}
				
				Skript.stopAcceptingRegistrations();
				Documentation.generate();
				
				final LogHandler h = SkriptLogger.startLogHandler(new ErrorDescLogHandler() {
					@Override
					public boolean log(final LogEntry entry) {
						super.log(entry);
						logEx(entry.message); // no [Skript] prefix
						return false;
					}
					
					@Override
					protected void beforeErrors() {
						logEx();
						logEx("===!!!=== Skript variable load error ===!!!===");
						logEx("Unable to load (all) variables:");
					}
					
					@Override
					protected void afterErrors() {
						logEx();
						logEx("Skript will work properly, but old variables might not be available at all and new ones may or may not be saved until Skript is able to create a backup of the old file and/or is able to connect to the database (which requires a restart of Skript)!");
						logEx();
					}
				});
				final CountingLogHandler c = SkriptLogger.startLogHandler(new CountingLogHandler(Level.SEVERE));
				try {
					if (!Variables.load())
						if (c.getCount() == 0)
							error("(no information available)");
				} finally {
					c.stop();
					h.stop();
				}
				
				ScriptLoader.loadScripts();
				
				Skript.info("Skript finished loading!");
				
				EvtSkript.onSkriptStart();
				
				metrics = new Metrics(Skript.this);
				final Graph scriptData = metrics.createGraph("data");
				scriptData.addPlotter(new Plotter("scripts") {
					@Override
					public int getValue() {
						return ScriptLoader.loadedScripts();
					}
				});
				scriptData.addPlotter(new Plotter("triggers") {
					@Override
					public int getValue() {
						return ScriptLoader.loadedTriggers();
					}
				});
				scriptData.addPlotter(new Plotter("commands") {
					@Override
					public int getValue() {
						return ScriptLoader.loadedCommands();
					}
				});
				scriptData.addPlotter(new Plotter("variables") {
					@Override
					public int getValue() {
						return Variables.numVariables();
					}
				});
				final Graph language = metrics.createGraph("language");
				language.addPlotter(new Plotter() {
					@Override
					public int getValue() {
						return 1;
					}
					
					@Override
					public String getColumnName() {
						return Language.getName();
					}
				});
				final Graph similarPlugins = metrics.createGraph("similar plugins");
				for (final String plugin : new String[] {"VariableTriggers", "CommandHelper", "Denizen", "rTriggers", "kTriggers", "TriggerCmds", "BlockScripts", "ScriptBlock", "buscript", "BukkitScript"}) {
					similarPlugins.addPlotter(new Plotter(plugin) {
						@Override
						public int getValue() {
							return Bukkit.getPluginManager().getPlugin(plugin) != null ? 1 : 0;
						}
					});
				}
				metrics.start();
			}
		});
		
		Bukkit.getPluginManager().registerEvents(new Listener() {
			@EventHandler
			public void onJoin(final PlayerJoinEvent e) {
				if (e.getPlayer().hasPermission("skript.admin")) {
					new Task(Skript.this, 0) {
						@Override
						public void run() {
							Updater.stateLock.readLock().lock();
							try {
								if ((Updater.state == UpdateState.CHECKED_FOR_UPDATE || Updater.state == UpdateState.DOWNLOAD_ERROR) && Updater.latest.get() != null)
									info(e.getPlayer(), "" + Updater.m_update_available);
							} finally {
								Updater.stateLock.readLock().unlock();
							}
						}
					};
				}
			}
		}, this);
		
	}
	
	private static Version minecraftVersion = null;
	private static boolean runningCraftBukkit;
	
	public static Version getMinecraftVersion() {
		return minecraftVersion;
	}
	
	/**
	 * @return whether this server is running CraftBukkit
	 */
	public static boolean isRunningCraftBukkit() {
		return runningCraftBukkit;
	}
	
	/**
	 * @return whether this server is running Bukkit <tt>major.minor</tt> or higher
	 */
	public static boolean isRunningMinecraft(final int major, final int minor) {
		return minecraftVersion.compareTo(major, minor) >= 0;
	}
	
	public static boolean isRunningMinecraft(final int major, final int minor, final int revision) {
		return minecraftVersion.compareTo(major, minor, revision) >= 0;
	}
	
	private static Metrics metrics;
	
	public static Metrics getMetrics() {
		return metrics;
	}
	
	/**
	 * Clears triggers, aliases, commands, variable names, etc.
	 */
	final static void disableScripts() {
		for (final Trigger t : ScriptLoader.selfRegisteredTriggers)
			((SelfRegisteringSkriptEvent) t.getEvent()).unregisterAll();
		ScriptLoader.selfRegisteredTriggers.clear();
		
		VariableString.variableNames.clear();
		
		SkriptEventHandler.triggers.clear();
		Commands.clearCommands();
	}
	
	/**
	 * Prints errors from reloading the config & scripts
	 */
	final static void reload() {
		disableScripts();
		reloadMainConfig();
		reloadAliases();
		ScriptLoader.loadScripts();
	}
	
	/**
	 * Prints errors
	 */
	final static void reloadScripts() {
		disableScripts();
		ScriptLoader.loadScripts();
	}
	
	/**
	 * Prints errors
	 */
	final static void reloadMainConfig() {
		SkriptConfig.load();
	}
	
	/**
	 * Prints errors
	 */
	final static void reloadAliases() {
		Aliases.clear();
		Aliases.load();
	}
	
	private final static Collection<Closeable> closeOnDisable = Collections.synchronizedCollection(new ArrayList<Closeable>());
	
	/**
	 * Registers a Closeable that should be closed when this plugin is disabled.
	 * 
	 * @param closeable
	 */
	public static void closeOnDisable(final Closeable closeable) {
		closeOnDisable.add(closeable);
	}
	
	@Override
	public void onDisable() {
		if (disabled)
			return;
		disabled = true;
		
		EvtSkript.onSkriptStop(); // TODO [code style] warn user about delays in Skript stop events
		
		Bukkit.getScheduler().cancelTasks(this);
		
		disableScripts();
		
		for (final Closeable c : closeOnDisable) {
			try {
				c.close();
			} catch (final IOException e) {}
		}
		
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
					try {
						for (final JarEntry e : new EnumerationIterable<JarEntry>(jar.entries())) {
							if (e.getName().endsWith(".class")) {
								try {
									final Class<?> c = Class.forName(e.getName().replace('/', '.').substring(0, e.getName().length() - ".class".length()), false, getClassLoader());
									for (final Field f : c.getDeclaredFields()) {
										if (Modifier.isStatic(f.getModifiers()) && !f.getType().isPrimitive()) {
											if (Modifier.isFinal(f.getModifiers())) {
												modifiers.setInt(f, f.getModifiers() & ~Modifier.FINAL);
											}
											f.setAccessible(true);
											f.set(null, null);
										}
									}
								} catch (final Throwable ex) {
									assert ex instanceof NoClassDefFoundError; // soft-dependency not loaded
								}
							}
						}
					} finally {
						jar.close();
					}
				} catch (final Throwable ex) {
					assert false;
				}
			}
		}).start();
	}
	
	// ================ CONSTANTS, OPTIONS & OTHER ================
	
	public static final String SCRIPTSFOLDER = "scripts";
	
	public static void outdatedError() {
		error("Skript v" + instance.getDescription().getVersion() + " is not fully compatible with Bukkit " + Bukkit.getVersion() + ". Some feature(s) will be broken until you update Skript.");
	}
	
	public static void outdatedError(final Exception e) {
		outdatedError();
		if (testing())
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
	
	/**
	 * The maximum ID a block can have in Minecraft.
	 */
	public static final int MAXBLOCKID = 255;
	/**
	 * The maximum data value Skript supports. This is currently {@link Short#MAX_VALUE}.
	 */
	public final static int MAXDATAVALUE = Short.MAX_VALUE;
	
	public final static String toString(final double n) {
		return StringUtils.toString(n, SkriptConfig.numberAccuracy.value());
	}
	
	// ================ REGISTRATIONS ================
	
	public static boolean acceptRegistrations = true;
	
	public static void checkAcceptRegistrations() {
		if (!acceptRegistrations)
			throw new SkriptAPIException("Registering is disabled after initialization!");
	}
	
	private static void stopAcceptingRegistrations() {
		acceptRegistrations = false;
		Converters.createMissingConverters();
		
		Classes.sortClassInfos();
		if (debug()) {
			final StringBuilder b = new StringBuilder();
			for (final ClassInfo<?> ci : Classes.getClassInfos()) {
				if (b.length() != 0)
					b.append(", ");
				b.append(ci.getCodeName());
			}
			Skript.info("All registered classes in order: " + b.toString());
		}
	}
	
	// ================ ADDONS ================
	
	private final static HashMap<String, SkriptAddon> addons = new HashMap<String, SkriptAddon>();
	
	/**
	 * Registers an addon to Skript. This is currently not required for addons to work, but the returned {@link SkriptAddon} provides useful methods for registering syntax elements
	 * and adding new strings to Skript's localization system (e.g. the required "types.[type]" strings for registered classes).
	 * 
	 * @param p The plugin
	 */
	public static SkriptAddon registerAddon(final JavaPlugin p) {
		checkAcceptRegistrations();
		if (addons.containsKey(p.getName()))
			throw new IllegalArgumentException("The plugin " + p.getName() + " is already registered");
		final SkriptAddon addon = new SkriptAddon(p);
		addons.put(p.getName(), addon);
		return addon;
	}
	
	public static SkriptAddon getAddon(final JavaPlugin p) {
		return addons.get(p.getName());
	}
	
	public static SkriptAddon getAddon(final String name) {
		return addons.get(name);
	}
	
	public static Collection<SkriptAddon> getAddons() {
		return Collections.unmodifiableCollection(addons.values());
	}
	
	private static SkriptAddon addon;
	
	/**
	 * @return A {@link SkriptAddon} representing Skript.
	 */
	public static SkriptAddon getAddonInstance() {
		if (addon == null) {
			final SkriptAddon a = new SkriptAddon(Skript.getInstance())
					.setLanguageFileDirectory("lang");
			addon = a; // Not set directly to make FindBugs shut up =P
		}
		return addon;
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
	public static <E extends Condition> void registerCondition(final Class<E> condition, final String... patterns) throws IllegalArgumentException {
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
	public static <E extends Effect> void registerEffect(final Class<E> effect, final String... patterns) throws IllegalArgumentException {
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
	
	private static final List<ExpressionInfo<?, ?>> expressions = new ArrayList<ExpressionInfo<?, ?>>(30);
	
	private final static int[] expressionTypesStartIndices = new int[ExpressionType.values().length];
	
	/**
	 * Registers an expression.
	 * 
	 * @param c The expression class.
	 * @param returnType
	 * @param patterns
	 */
	public static <E extends Expression<T>, T> void registerExpression(final Class<E> c, final Class<T> returnType, final ExpressionType type, final String... patterns) throws IllegalArgumentException {
		checkAcceptRegistrations();
		final ExpressionInfo<?, ?> info = new ExpressionInfo<E, T>(patterns, returnType, c);
		for (int i = type.ordinal() + 1; i < ExpressionType.values().length; i++) {
			expressionTypesStartIndices[i]++;
		}
		expressions.add(expressionTypesStartIndices[type.ordinal()], info);
	}
	
	public static Iterator<ExpressionInfo<?, ?>> getExpressions() {
		return expressions.iterator();
	}
	
	public static Iterator<ExpressionInfo<?, ?>> getExpressions(final Class<?>... returnTypes) {
		return new CheckedIterator<ExpressionInfo<?, ?>>(expressions.iterator(), new Checker<ExpressionInfo<?, ?>>() {
			@Override
			public boolean check(final ExpressionInfo<?, ?> i) {
				if (i.returnType == Object.class)
					return true;
				for (final Class<?> returnType : returnTypes) {
					if (Converters.converterExists(i.returnType, returnType))
						return true;
				}
				return false;
			}
		});
	}
	
	// ================ EVENTS ================
	
	private static final Collection<SkriptEventInfo<?>> events = new ArrayList<SkriptEventInfo<?>>(50);
	
	@SuppressWarnings("unchecked")
	public static <E extends SkriptEvent> SkriptEventInfo<E> registerEvent(final String name, final Class<E> c, final Class<? extends Event> event, final String... patterns) throws IllegalArgumentException {
		checkAcceptRegistrations();
		final SkriptEventInfo<E> r = new SkriptEventInfo<E>(name, patterns, c, Utils.array(event));
		events.add(r);
		return r;
	}
	
	public static <E extends SkriptEvent> SkriptEventInfo<E> registerEvent(final String name, final Class<E> c, final Class<? extends Event>[] events, final String... patterns) throws IllegalArgumentException {
		checkAcceptRegistrations();
		final SkriptEventInfo<E> r = new SkriptEventInfo<E>(name, patterns, c, events);
		Skript.events.add(r);
		return r;
	}
	
	public static final Collection<SkriptEventInfo<?>> getEvents() {
		return events;
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
		try {
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
		} catch (final Exception ex) {
			ex.printStackTrace(); // just like Bukkit
			return false;
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
	
	public static final boolean testing() {
		return debug() || Skript.class.desiredAssertionStatus();
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
	
	/**
	 * @see SkriptLogger#log(Level, String)
	 */
	public static void error(final String error) {
		SkriptLogger.log(Level.SEVERE, error);
	}
	
	/**
	 * Use this in {@link Expression#init(Expression[], int, Kleenean, ch.njol.skript.lang.SkriptParser.ParseResult)} (and other methods that are called during the parsing) to log
	 * errors with a specific {@link ErrorQuality}.
	 * 
	 * @param error
	 * @param quality
	 */
	public static void error(final String error, final ErrorQuality quality) {
		SkriptLogger.log(new LogEntry(Level.SEVERE, quality, error));
	}
	
	private final static String EXCEPTION_PREFIX = "#!#! ";
	
	/**
	 * Used if something happens that shouldn't happen
	 * 
	 * @param info Description of the error and additional information
	 * @return an EmptyStackException to throw if code execution should terminate.
	 */
	public final static EmptyStackException exception(final String... info) {
		return exception(null, info);
	}
	
	public final static EmptyStackException exception(final Throwable cause, final String... info) {
		return exception(cause, null, info);
	}
	
	/**
	 * Used if something happens that shouldn't happen
	 * 
	 * @param cause exception that shouldn't occur
	 * @param info Description of the error and additional information
	 * @return an EmptyStackException to throw if code execution should terminate.
	 */
	public final static EmptyStackException exception(Throwable cause, final TriggerItem item, final String... info) {
		
		logEx();
		logEx("[Skript] Severe Error:");
		logEx(info);
		logEx();
		logEx("If you're developping an add-on for Skript this likely means that you have done something wrong.");
		logEx("If you're a server admin however please go to http://dev.bukkit.org/server-mods/skript/tickets/");
		logEx("and check whether this error has already been reported.");
		logEx("If not please create a new ticket with a meaningful title, copy & paste this whole error into it,");
		logEx("and describe what you did before it happened and/or what you think caused the error.");
		logEx("If you think that it's a trigger that's causing the error please post the trigger as well.");
		logEx("By following this guide fixing the error should be easy and done fast.");
		
		logEx();
		logEx("Stacktrace:");
		if (cause == null || cause.getStackTrace().length == 0) {
			logEx("  warning: no/empty exception given, dumping current stack trace instead");
			cause = new Exception(cause);
		}
		logEx(cause.toString());
		for (final StackTraceElement e : cause.getStackTrace())
			logEx("    at " + e.toString());
		
		logEx();
		logEx("Version Information:");
		logEx("  Skript: " + getVersion());
		logEx("  Bukkit: " + Bukkit.getBukkitVersion());
		logEx("  Minecraft: " + getMinecraftVersion());
		logEx("  Java: " + System.getProperty("java.version"));
		logEx();
		logEx("Running CraftBukkit: " + runningCraftBukkit);
		logEx();
		logEx("Current node: " + SkriptLogger.getNode());
		logEx("Current item: " + (item == null ? "null" : item.toString(null, true)));
		logEx();
		logEx("Current thread: " + Thread.currentThread().getName());
		logEx();
		logEx("End of Error.");
		logEx();
		
		return new EmptyStackException();
	}
	
	private final static void logEx() {
		Bukkit.getLogger().severe(EXCEPTION_PREFIX);
	}
	
	private final static void logEx(final String... lines) {
		for (final String line : lines)
			Bukkit.getLogger().severe(EXCEPTION_PREFIX + line);
	}
	
	public final static String SKRIPT_PREFIX = ChatColor.GRAY + "[" + ChatColor.GOLD + "Skript" + ChatColor.GRAY + "]" + ChatColor.RESET + " ";
	
	public static void info(final CommandSender sender, final String info) {
		sender.sendMessage(SKRIPT_PREFIX + Utils.replaceEnglishChatStyles(info));
	}
	
	/**
	 * @param message
	 * @param permission
	 * @see #adminBroadcast(String)
	 */
	public static void broadcast(final String message, final String permission) {
		Bukkit.broadcast(SKRIPT_PREFIX + Utils.replaceEnglishChatStyles(message), permission);
	}
	
	public static void adminBroadcast(final String message) {
		Bukkit.broadcast(SKRIPT_PREFIX + Utils.replaceEnglishChatStyles(message), "skript.admin");
	}
	
	/**
	 * Similar to {@link #info(CommandSender, String)} but no [Skript] prefix is added.
	 * 
	 * @param sender
	 * @param info
	 */
	public static void message(final CommandSender sender, final String info) {
		sender.sendMessage(Utils.replaceEnglishChatStyles(info));
	}
	
	public static void error(final CommandSender sender, final String error) {
		sender.sendMessage(SKRIPT_PREFIX + ChatColor.DARK_RED + Utils.replaceEnglishChatStyles(error));
	}
	
}
