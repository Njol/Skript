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
import java.io.FilenameFilter;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map.Entry;
import java.util.Random;
import java.util.logging.Level;
import java.util.regex.Pattern;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandMap;
import org.bukkit.command.CommandSender;
import org.bukkit.event.Event;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import org.bukkit.event.server.ServerCommandEvent;
import org.bukkit.plugin.SimplePluginManager;
import org.bukkit.plugin.java.JavaPlugin;

import ch.njol.skript.api.ClassInfo;
import ch.njol.skript.api.Comparator;
import ch.njol.skript.api.Comparator.ComparatorInfo;
import ch.njol.skript.api.Comparator.Relation;
import ch.njol.skript.api.Condition;
import ch.njol.skript.api.Converter;
import ch.njol.skript.api.Converter.ConverterInfo;
import ch.njol.skript.api.Converter.ConverterUtils;
import ch.njol.skript.api.Effect;
import ch.njol.skript.api.Getter;
import ch.njol.skript.api.InverseComparator;
import ch.njol.skript.api.LoopVar;
import ch.njol.skript.api.LoopVar.LoopInfo;
import ch.njol.skript.api.Parser;
import ch.njol.skript.api.SkriptEvent;
import ch.njol.skript.api.SkriptEvent.SkriptEventInfo;
import ch.njol.skript.api.exception.InitException;
import ch.njol.skript.api.intern.ChainedConverter;
import ch.njol.skript.api.intern.Expression.Expressions;
import ch.njol.skript.api.intern.Expression.VariableInfo;
import ch.njol.skript.api.intern.SkriptAPIException;
import ch.njol.skript.api.intern.Trigger;
import ch.njol.skript.api.intern.Variable;
import ch.njol.skript.command.CommandEvent;
import ch.njol.skript.command.SkriptCommand;
import ch.njol.skript.config.Config;
import ch.njol.skript.config.EntryNode;
import ch.njol.skript.config.Node;
import ch.njol.skript.config.SectionNode;
import ch.njol.skript.config.validate.EnumEntryValidator;
import ch.njol.skript.config.validate.SectionValidator;
import ch.njol.skript.data.BukkitEventValues;
import ch.njol.skript.data.DefaultClasses;
import ch.njol.skript.data.DefaultComparators;
import ch.njol.skript.data.DefaultConverters;
import ch.njol.skript.data.SkriptClasses;
import ch.njol.skript.data.SkriptEventValues;
import ch.njol.skript.data.SkriptTriggerItems;
import ch.njol.skript.util.ItemType;
import ch.njol.skript.util.Utils;
import ch.njol.util.Setter;
import ch.njol.util.Validate;

/**
 * <b>Skript</b> - A Bukkit plugin to modify how Minecraft behaves without having to write a single line of code.<br/>
 * <br/>
 * Use this class to extend this plugin's functionality by adding more {@link Condition conditions}, {@link Effect effects}, {@link Variable variables}, etc.<br/>
 * <br/>
 * Please also take a look at the {@link Utils} class! it will probably save you a lot of time.
 * 
 * @author Peter Güttinger
 * 
 */
public class Skript extends JavaPlugin {
	
	// ================ PLUGIN ================
	
	static Skript instance;
	
	public static Skript getInstance() {
		return instance;
	}
	
	public Skript() {
		instance = this;
		new BukkitEventValues();
		new DefaultClasses();
		new DefaultComparators();
		new DefaultConverters();
		new SkriptClasses();
		new SkriptTriggerItems();
		new SkriptEventValues();
	}
	
	private static boolean monitorMemoryUsage = false;
	private static long memoryUsed = 0;
	
	@Override
	public void onEnable() {
		// automatically added by Bukkit now
		// info("loading Skript v" + getDescription().getVersion() + "...");
		
		System.gc();
		final long startMemory = Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory();
		
		loadMainConfig();
		
		if (logNormal())
			info(" ~ created & © by Peter Güttinger aka Njol ~");
		
		monitorMemoryUsage = logHigh();
		
		if (Bukkit.getScheduler().scheduleSyncDelayedTask(this, new Runnable() {
			@Override
			public void run() {
				if (monitorMemoryUsage) {
					System.gc();
				}
				
				final long startMemory = Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory();
				
				loadTriggerFiles();
				info("Skript finished loading!");
				
				if (monitorMemoryUsage) {
					System.gc();
					final long endMemory = Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory();
					Skript.memoryUsed += endMemory - startMemory;
					info("Skript is currently using roughly " + formatMemory(memoryUsed) + " of RAM");
				}
			}
		}) == -1) {
			error("error scheduling trigger files loader task");
		}
		
		if (monitorMemoryUsage) {
			System.gc();
			final long endMemory = Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory();
			memoryUsed += endMemory - startMemory;
		}
		
	}
	
	private final static String formatMemory(final long memory) {
		double mb = 1. * memory / (1 << 20);
		mb = 1. * Math.round(1000 * mb) / 1000;
		return mb + " MiB";
	}
	
	@Override
	public void onDisable() {
		// info("disabled");
		Bukkit.getScheduler().cancelTasks(this);
	}
	
	//	@Override
	//	public boolean onCommand(final CommandSender sender, final Command command, final String label, final String[] args) {
	//		return CommandHandler.onCommand(sender, command, label, args);
	//	}
	
	// ================ CONSTANTS & OTHER ================
	
	// TODO aliases in trigger files?
	
	// TODO load triggers from all subfolders (except for those starting with '-')?
	public static final String TRIGGERFILEFOLDER = "triggers";
	
	public static final String quotesError = "Invalid use of quotes (\"). If you want to use quotes in \"quoted text\", double them: \"\".";
	
	public static final double EPSILON = 1e-20;
	
	public static final int MAXBLOCKID = 255;
	
	// TODO option? or in variable?
	public static final int TARGETBLOCKMAXDISTANCE = 100;
	
	public static final Random random = new Random();
	
	private static EventPriority priority = EventPriority.NORMAL;
	
	public static <T> T[] array(final T... array) {
		return array;
	}
	
	// ================ LISTENER FUNCTIONS ================
	
	static boolean listenerEnabled = true;
	
	public static void disableListener() {
		listenerEnabled = false;
	}
	
	public static void enableListener() {
		listenerEnabled = true;
	}
	
	// ================ CONDITIONS & EFFECTS ================
	
	static final ArrayList<VariableInfo<Boolean>> conditions = new ArrayList<VariableInfo<Boolean>>(20);
	static final ArrayList<VariableInfo<Boolean>> effects = new ArrayList<VariableInfo<Boolean>>(20);
	static final ArrayList<VariableInfo<Boolean>> topLevelExpressions = new ArrayList<VariableInfo<Boolean>>(40);
	
	/**
	 * registers a {@link Condition}.
	 * 
	 * @param condition
	 */
	public static void addCondition(final Class<? extends Condition> condition, final String... patterns) {
		final VariableInfo<Boolean> info = new VariableInfo<Boolean>(patterns, Boolean.class, condition);
		conditions.add(info);
		topLevelExpressions.add(info);
	}
	
	/**
	 * registers an {@link Effect}.
	 * 
	 * @param effect
	 */
	public static void addEffect(final Class<? extends Effect> effect, final String... patterns) {
		final VariableInfo<Boolean> info = new VariableInfo<Boolean>(patterns, Boolean.class, effect);
		effects.add(info);
		topLevelExpressions.add(info);
	}
	
	public static List<VariableInfo<Boolean>> getTopLevelExpressions() {
		return topLevelExpressions;
	}
	
	public static List<VariableInfo<Boolean>> getConditions() {
		return conditions;
	}
	
	public static List<VariableInfo<Boolean>> getEffects() {
		return effects;
	}
	
	// ================ VARIABLES ================
	
	static final ArrayList<VariableInfo<?>> variables = new ArrayList<VariableInfo<?>>(30);
	
	public static <T> void addVariable(final Class<? extends Variable<T>> c, final Class<T> returnType, final String... patterns) {
		variables.add(new VariableInfo<T>(patterns, returnType, c));
		if (log(Verbosity.VERY_HIGH))
			Skript.info("variable " + c.getSimpleName() + " added");
	}
	
	public static List<VariableInfo<?>> getVariables() {
		return variables;
	}
	
	// ================ EVENTS ================
	
	static final ArrayList<SkriptEventInfo> events = new ArrayList<SkriptEventInfo>(50);
	
	@SuppressWarnings("unchecked")
	public static void addEvent(final Class<? extends SkriptEvent> c, final Class<? extends Event> event, final String... patterns) {
		events.add(new SkriptEventInfo(patterns, c, array(event)));
	}
	
	public static void addEvent(final Class<? extends SkriptEvent> c, final Class<? extends Event>[] events, final String... patterns) {
		Skript.events.add(new SkriptEventInfo(patterns, c, events));
	}
	
	public static final ArrayList<SkriptEventInfo> getEvents() {
		return events;
	}
	
	// ================ CONVERTERS ================
	
	private static ArrayList<ConverterInfo<?, ?>> converters = new ArrayList<ConverterInfo<?, ?>>(50);
	
	/**
	 * Registers a converter.
	 * 
	 * @param from
	 * @param to
	 * @param converter
	 */
	// TODO how to manage overriding of converters?
	public static <F, T> void addConverter(final Class<F> from, final Class<T> to, final Converter<F, T> converter) {
		for (final Iterator<ConverterInfo<?, ?>> i = converters.iterator(); i.hasNext();) {
			final ConverterInfo<?, ?> conv = i.next();
			if (conv.from == from && to == conv.to) {
				i.remove();
			}
		}
		converters.add(new ConverterInfo<F, T>(from, to, converter));
		for (int i = 0; i < converters.size(); i++) {
			final ConverterInfo<?, ?> c = converters.get(i);
			if (c.from.isAssignableFrom(to)) {
				if (!converterExists(from, c.to)) {
					converters.add(createChainedConverter(from, converter, c));
					i++;
				}
			} else if (c.to.isAssignableFrom(from)) {
				if (!converterExists(c.from, to)) {
					converters.add(createChainedConverter(c, converter, to));
					i++;
				}
			}
		}
	}
	
	@SuppressWarnings("unchecked")
	private static <F, M, T> ConverterInfo<F, T> createChainedConverter(final Class<F> from, final Converter<F, M> first, final ConverterInfo<?, ?> second) {
		return new ConverterInfo<F, T>(from, (Class<T>) second.to, new ChainedConverter<F, M, T>(first, (Converter<M, T>) second.converter));
	}
	
	@SuppressWarnings("unchecked")
	private static <F, M, T> ConverterInfo<F, T> createChainedConverter(final ConverterInfo<?, ?> first, final Converter<M, T> second, final Class<T> to) {
		return new ConverterInfo<F, T>((Class<F>) first.from, to, new ChainedConverter<F, M, T>((Converter<F, M>) first.converter, second));
	}
	
	/**
	 * Converts the given value to the desired type. If you want to convert multiple values of the same type you should use {@link #getConverter(Class, Class)} to get a converter
	 * to convert the values.
	 * 
	 * @param o
	 * @param to
	 * @return The converted value or null if no converter exists or the converter returned null
	 */
	@SuppressWarnings("unchecked")
	public static <F, T> T convert(final F o, final Class<T> to) {
		for (final ConverterInfo<?, ?> c : converters) {
			if (c.from.isAssignableFrom(o.getClass()) && to.isAssignableFrom(c.to)) {
				final T t = ((Converter<F, T>) c.converter).convert(o);
				if (t != null)
					return t;
			}
		}
		return null;
	}
	
	/**
	 * Tests whether a converter between the given classes exists.
	 * 
	 * @param from
	 * @param to
	 * @return
	 */
	public final static boolean converterExists(final Class<?> from, final Class<?> to) {
		for (final ConverterInfo<?, ?> conv : converters) {
			if (conv.from.isAssignableFrom(from) && to.isAssignableFrom(conv.to))
				return true;
		}
		return false;
	}
	
	/**
	 * Gets a converter
	 * 
	 * @param from
	 * @param to
	 * @return the converter or null if none exist
	 */
	public final static <F, T> Converter<? super F, ? extends T> getConverter(final Class<F> from, final Class<T> to) {
		final ConverterInfo<? super F, ? extends T> ci = getConverterInfo(from, to);
		if (ci == null)
			return null;
		return ci.converter;
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
	
	private final static ArrayList<ClassInfo<?>> classInfos = new ArrayList<ClassInfo<?>>(50);
	
	/**
	 * Registers a class. This class will have lower proirity than any classes registered before,
	 * this means that parsing will be attempted last with this class, so if you want to e.g. parse
	 * quoted strings this won't work as string will parse before. You'd have to use {@link #addClassBefore(ClassInfo, boolean, String...)} To register the class before string.
	 * 
	 * @param info info about the class to register
	 */
	public static <T> void addClass(final ClassInfo<T> info) {
		classInfos.add(info);
	}
	
	/**
	 * Registers a class with higher priority than some other classes.
	 * 
	 * @param info info about the class to register
	 * @param addAlways whether to add this class at the end if none of the other classes are registered
	 * @param before the classes which should have lower priority than this class
	 * @return whether any of the given classes were registered
	 */
	public static <T> boolean addClassBefore(final ClassInfo<T> info, final boolean addAlways, final String... before) {
		for (int i = 0; i < classInfos.size(); i++) {
			if (Utils.contains(before, classInfos.get(i).getCodeName()) != -1) {
				classInfos.add(i, info);
				return true;
			}
		}
		if (addAlways)
			classInfos.add(info);
		return false;
	}
	
	private static ClassInfo<?> getClassInfo(final String codeName) {
		for (final ClassInfo<?> ci : classInfos) {
			if (ci.getCodeName().equals(codeName))
				return ci;
		}
		throw new RuntimeException("no class info found for " + codeName);
	}
	
	@SuppressWarnings("unchecked")
	private static <T> List<ClassInfo<? extends T>> getClassInfos(final Class<T> c) {
		final ArrayList<ClassInfo<? extends T>> infos = new ArrayList<ClassInfo<? extends T>>();
		for (final ClassInfo<?> ci : classInfos) {
			if (c.isAssignableFrom(ci.getC()))
				infos.add((ClassInfo<? extends T>) ci);
		}
		if (infos.isEmpty())
			throw new RuntimeException("no class info found for " + c.getName());
		return infos;
	}
	
	/**
	 * Gets a class by it's exact name (the name it was registered with)
	 * 
	 * @param codeName
	 * @return the class
	 * @throws RuntimeException if the class was not found
	 */
	public static Class<?> getClass(final String codeName) {
		return getClassInfo(codeName).getC();
	}
	
	/**
	 * As the name implies
	 * 
	 * @param name
	 * @return the class or null if the name was not recognized
	 */
	public static Class<?> getClassFromUserInput(final String name) {
		if (name == null)
			return null;
		for (final ClassInfo<?> ci : classInfos) {
			for (final Pattern pattern : ci.getUserInputPatterns()) {
				if (pattern.matcher(name).matches())
					return ci.getC();
			}
		}
		return null;
	}
	
	/**
	 * gets the default of a class
	 * 
	 * @param name
	 * @return the variable holding the default value or null if this class doesn't have one
	 */
	public static <T> Class<? extends Variable<?>> getDefaultVariable(final String name) {
		return getClassInfo(name).getDefaultVariable();
	}
	
	/**
	 * Gets the name this class was registered with.
	 * 
	 * @param c The class
	 * @return The name of the class or null if the given class wasn't registered.
	 */
	public final static String getExactClassName(final Class<?> c) {
		for (final ClassInfo<?> ci : classInfos) {
			if (c == ci.getC())
				return ci.getName();
		}
		return null;
	}
	
	// ======== PARSERS (part of classes) ========
	
	/**
	 * parses without trying to convert anything.
	 * 
	 * @param s
	 * @param c
	 * @return
	 */
	private static <T> T parse_simple(final String s, final Class<T> c) {
		for (final ClassInfo<? extends T> info : getClassInfos(c)) {
			if (info.getParser() == null)
				continue;
			final T t = info.getParser().parse(s);
			if (t != null)
				return t;
		}
		return null;
	}
	
	/**
	 * Parses a string to recieve an object of the desired type.<br/>
	 * Instead of repeatedly calling this with the same class argument, you should get a parse with {@link #getParser(Class)} and use it for parsing.
	 * 
	 * @param s The string to parse
	 * @param c The desired Type. The returned value will be of this type or a subclass if it.
	 * @return The parsed object.
	 */
	@SuppressWarnings("unchecked")
	public static <T> T parse(final String s, final Class<T> c) {
		T t = parse_simple(s, c);
		if (t != null)
			return t;
		for (final ConverterInfo<?, ?> conv : converters) {
			if (c.isAssignableFrom(conv.to)) {
				final Object o = parse_simple(s, conv.from);
				if (o != null) {
					t = (T) ConverterUtils.convert(conv, o);
					if (t != null)
						return t;
				}
			}
		}
		return null;
	}
	
	/**
	 * Gets a parser suitable for parsing the desired type.
	 * 
	 * @param to
	 * @return
	 */
	@SuppressWarnings("unchecked")
	public static final <T> Converter<String, ? extends T> getParser(final Class<T> to) {
		for (final ClassInfo<?> ci : classInfos) {
			if (to.isAssignableFrom(ci.getC()) && ci.getParser() != null)
				return (Parser<? extends T>) ci.getParser();
		}
		for (final ConverterInfo<?, ?> conv : converters) {
			if (to.isAssignableFrom(conv.to)) {
				for (final ClassInfo<?> ci : classInfos) {
					if (conv.from.isAssignableFrom(ci.getC()) && ci.getParser() != null)
						return ChainedConverter.newInstance(ci.getParser(), ci.getC(), (Converter<?, ? extends T>) conv.converter);
				}
			}
		}
		return null;
	}
	
	/**
	 * @param o any object or array
	 * @return String representation of the object (using a parser if found or {@link String#valueOf(Object)} otherwise).
	 * @see #addClass(String, Class, Class, Parser, String...)
	 * @see #toString(Object, boolean)
	 * @see Parser
	 */
	public static <T> String toString(final T o) {
		return toString(o, false);
	}
	
	/**
	 * @param o The object
	 * @param classname Whether to return the object's ({@link Class#getSimpleName() simple}) class name instead of {@link String#valueOf(Object)} if no parser was found
	 * @return String representation of the object
	 * @see #toString(Object)
	 */
	@SuppressWarnings("unchecked")
	public static <T> String toString(final T o, final boolean classname) {
		if (o == null)
			return "<none>";
		if (o.getClass().isArray()) {
			final StringBuilder b = new StringBuilder();
			boolean first = true;
			for (final Object i : (Object[]) o) {
				if (!first)
					b.append(", ");
				b.append(toString(i));
				first = false;
			}
			return "[" + b.toString() + "]";
		}
		for (final ClassInfo<?> ci : classInfos) {
			if (ci.getParser() != null && ci.getC().isAssignableFrom(o.getClass())) {
				final String s = ((Parser<T>) ci.getParser()).toString(o);
				if (s != null)
					return s;
			}
		}
		if (classname)
			return o.getClass().getSimpleName();
		return String.valueOf(o);
	}
	
	// ================ COMPARATORS ================
	
	private final static ArrayList<ComparatorInfo<?, ?>> comparators = new ArrayList<ComparatorInfo<?, ?>>();
	
	/**
	 * Registers a {@link Comparator}.
	 * 
	 * @param t1
	 * @param t2
	 * @param c
	 * @throws SkriptAPIException if any given class is equal to <code>Object.class</code>
	 */
	public static <T1, T2> void addComparator(final Class<T1> t1, final Class<T2> t2, final Comparator<T1, T2> c) {
		if (t1 == Object.class || t2 == Object.class)
			throw new SkriptAPIException("must not add a comparator for objects");
		comparators.add(new ComparatorInfo<T1, T2>(t1, t2, c));
	}
	
	/**
	 * Gets infos about all registered comparators. You likely want to use {@link #getComparator(Class, Class)} to get a specific comparator.
	 * 
	 * @return
	 * @see #compare(Object, Object)
	 */
	public final static List<ComparatorInfo<?, ?>> getComparators() {
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
		
		Class<E> event;
		Class<T> c;
		Getter<T, E> getter;
		
		public EventValueInfo(final Class<E> event, final Class<T> c, final Getter<T, E> getter) {
			Validate.notNull(event, c, getter);
			this.event = event;
			this.c = c;
			this.getter = getter;
		}
	}
	
	private static final ArrayList<EventValueInfo<?, ?>> eventValues = new ArrayList<Skript.EventValueInfo<?, ?>>(30);
	
	/**
	 * Registers an event value.
	 * 
	 * @param e the event type
	 * @param c the type of the default value
	 * @param g the getter to get the value
	 * @see #addEventValueBefore(Class, Class, Class, Getter)
	 */
	public static <T, E extends Event> void addEventValue(final Class<E> e, final Class<T> c, final Getter<T, E> g) {
		eventValues.add(new EventValueInfo<E, T>(e, c, g));
	}
	
	/**
	 * Registers an event value which will have higher priority than the other one given.
	 * 
	 * @param e
	 * @param next
	 * @param c
	 * @param g
	 * @see #addEventValue(Class, Class, Getter)
	 */
	public static <T, E extends Event> void addEventValueBefore(final Class<E> e, final Class<? extends Event> next, final Class<T> c, final Getter<T, E> g) {
		for (int i = 0; i < eventValues.size(); i++) {
			if (eventValues.get(i).event == next) {
				eventValues.add(i, new EventValueInfo<E, T>(e, c, g));
				return;
			}
		}
		eventValues.add(new EventValueInfo<E, T>(e, c, g));
	}
	
	/**
	 * Gets a value assiciated with the event. Returns null if the event doesn't have such a value (conversions are done to try and get the desired value).
	 * 
	 * @param e
	 * @param c
	 * @return
	 * @see #addEventValue(Class, Class, Getter)
	 * @see Converter
	 */
	@SuppressWarnings("unchecked")
	public static <T, E extends Event> T getEventValue(final E e, final Class<T> c) {
		if (e == null)
			throw new RuntimeException();
		for (final EventValueInfo<?, ?> ev : eventValues) {
			if (ev.event.isAssignableFrom(e.getClass())) {
				if (c.isAssignableFrom(ev.c)) {
					final T t = ((Getter<? extends T, ? super E>) ev.getter).get(e);
					if (t != null)
						return t;
				} else {
					final ConverterInfo<?, ? extends T> conv = getConverterInfo(ev.c, c);
					if (conv != null) {
						final T t = ConverterUtils.convert(conv, ((Getter<?, ? super E>) ev.getter).get(e));
						if (t != null)
							return t;
					}
				}
			}
		}
		return null;
	}
	
	/**
	 * Returns a getter (actually a converter) to get a value from an event.
	 * 
	 * @param e
	 * @param c
	 * @return
	 * @see #addEventValue(Class, Class, Getter)
	 * @see Getter
	 * @see Converter
	 */
	@SuppressWarnings("unchecked")
	public static final <T, E extends Event> Converter<? super E, ? extends T> getEventValueGetter(final E e, final Class<T> c) {
		for (final EventValueInfo<?, ?> ev : eventValues) {
			if (ev.event.isAssignableFrom(e.getClass())) {
				if (c.isAssignableFrom(ev.c)) {
					return (Getter<? extends T, ? super E>) ev.getter;
				} else {
					return (Converter<? super E, ? extends T>) getConvertedGetter(ev, c);
				}
			}
		}
		return null;
	}
	
	private final static <E extends Event, F, T> Converter<? super E, ? extends T> getConvertedGetter(final EventValueInfo<E, F> i, final Class<T> to) {
		final Converter<? super F, ? extends T> c = getConverter(i.c, to);
		if (c == null)
			return null;
		return new Converter<E, T>() {
			@Override
			public T convert(final E e) {
				return c.convert(i.getter.get(e));
			}
		};
	}
	
	// ================ LOOPS ================
	
	static final ArrayList<LoopInfo<?>> loops = new ArrayList<LoopVar.LoopInfo<?>>();
	
	/**
	 * Registers a loopable value.
	 * 
	 * @param c
	 * @param returnType
	 * @param patterns
	 * @see LoopVar
	 */
	public static <T> void addLoop(final Class<? extends LoopVar<T>> c, final Class<T> returnType, final String... patterns) {
		loops.add(new LoopInfo<T>(c, returnType, patterns));
	}
	
	// ================ COMMANDS ================
	
	private static final ArrayList<SkriptCommand> commands = new ArrayList<SkriptCommand>();
	
	public static CommandMap commandMap = null;
	static {
		try {
			
			if (Bukkit.getPluginManager() instanceof SimplePluginManager) {
				final Field f = SimplePluginManager.class.getDeclaredField("commandMap");
				f.setAccessible(true);
				
				commandMap = (CommandMap) f.get(Bukkit.getPluginManager());
			}
			
		} catch (final SecurityException e) {
			exception(e, "Please disable the security manager");
		} catch (final Exception e) {
			outdatedError(e);
		}
	}
	
	/**
	 * Only used internally.
	 * 
	 * @param command
	 * @throws NullPointerException if the server is not running CraftBukkit
	 */
	public static void addCommand(final SkriptCommand command) {
		commands.add(command);
		commandMap.register("/", command);
	}
	
	public static void outdatedError() {
		error("The version of Skript you're using is not compatible with " + Bukkit.getVersion());
	}
	
	public static void outdatedError(final Throwable t) {
		outdatedError();
		t.printStackTrace();
	}
	
	// ================ LOGGING ================
	
	private static int errorCount = 0;
	private static void resetErrorCount() {
		errorCount = 0;
	}
	
	public static final boolean logNormal() {
		return SkriptLogger.log(Verbosity.NORMAL);
	}
	
	public static final boolean logHigh() {
		return SkriptLogger.log(Verbosity.HIGH);
	}
	
	public static final boolean logVeryHigh() {
		return SkriptLogger.log(Verbosity.VERY_HIGH);
	}
	
	public static final boolean logExtreme() {
		return SkriptLogger.log(Verbosity.EXTREME);
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
	 * @see #addWarning(String)
	 * @see SkriptLogger#log(Level, String)
	 */
	public static void warning(final String warning) {
		SkriptLogger.log(Level.WARNING, warning);
	}
	
	private final static ArrayList<String> warnings = new ArrayList<String>();
	
	public static void addWarning(final String warning) {
		warnings.add(warning);
	}
	
	/**
	 * Error cause. Must never be null.
	 */
	private static String cause = "";
	
	public static void printErrorAndCause(final String error) {
		errorCount++;
		warnings.clear();
		SkriptLogger.log(Level.SEVERE, error + (hasErrorCause() ? " because " + cause : ""));
		clearErrorCause();
	}
	
	public static void printErrorCause(final CommandSender sender) {
		errorCount++;
		if (sender != null)
			sender.sendMessage(cause);
		else
			SkriptLogger.log(Level.SEVERE, cause);
		clearErrorCause();
	}
	
	public static void printErrorCause() {
		errorCount++;
		warnings.clear();
		SkriptLogger.log(Level.SEVERE, cause);
		clearErrorCause();
	}
	
	public static final void printWarnings() {
		for (final String w : warnings)
			SkriptLogger.log(Level.WARNING, w);
		warnings.clear();
	}
	
	/**
	 * Should not be used except in the {@link InitException} catch clause of {@link Expressions#parse(String, java.util.Iterator)}, as well as by the tigger file loader.<br/>
	 * Also prints all warnings that occurred since the last cleared or printed error.
	 */
	public static final void clearErrorCause() {
		warnings.clear();
		cause = null;
	}
	
	public static final boolean hasErrorCause() {
		return cause != null;
	}
	
	public static void setErrorCause(final String error, final boolean overwrite) {
		Validate.notNullOrEmpty(error);
		if (overwrite || !hasErrorCause())
			cause = error;
	}
	
	/**
	 * @see #setErrorCause(String, boolean)
	 * @see SkriptLogger#log(Level, String)
	 */
	public static void error(final String error) {
		errorCount++;
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
		return exception(new Exception(), info);
	}
	
	/**
	 * Used if something happens that shouldn't happen
	 * 
	 * @param cause exception that shouldn't occur
	 * @param info Description of the error and additional information
	 * @return an empty RuntimeException to throw if code execution should terminate.
	 */
	public final static RuntimeException exception(final Exception cause, final String... info) {
		SkriptLogger.setPrefix(EXCEPTION_PREFIX);
		
		SkriptLogger.logDirect(Level.SEVERE, "");
		SkriptLogger.logDirect(Level.SEVERE, "Severe Error:");
		for (final String i : info)
			SkriptLogger.logDirect(Level.SEVERE, i);
		SkriptLogger.logDirect(Level.SEVERE, "");
		SkriptLogger.logDirect(Level.SEVERE, "If you're developing an add-on for Skript this likely means you have done something wrong.");
		SkriptLogger.logDirect(Level.SEVERE, "If you're a server admin, please go to http://dev.bukkit.org/server-mods/skript/tickets/");
		SkriptLogger.logDirect(Level.SEVERE, "and check whether this error has already been reported. If not, please create a new ticket,");
		SkriptLogger.logDirect(Level.SEVERE, "copy & paste the following stacktrace into it, and please describe what you did before it happened!");

//		SkriptLogger.logDirect(Level.SEVERE, "");
//		SkriptLogger.logDirect(Level.SEVERE, "Information about the error:");
//		SkriptLogger.logDirect(Level.SEVERE, "");
//		SkriptLogger.logDirect(Level.SEVERE, "occurred at:");
//		final StackTraceElement[] st = Thread.currentThread().getStackTrace();
//		for (int i = 2; i < st.length; i++) {
//			SkriptLogger.logDirect(Level.SEVERE, "    " + st[i].toString());
//		}
		
		if (cause != null) {
			SkriptLogger.logDirect(Level.SEVERE, "");
			SkriptLogger.logDirect(Level.SEVERE, "Stacktrace:");
			SkriptLogger.logDirect(Level.SEVERE, cause.toString());
			for (final StackTraceElement e : cause.getStackTrace())
				SkriptLogger.logDirect(Level.SEVERE, "    at " + e.toString());
		}
		
		SkriptLogger.logDirect(Level.SEVERE, "");
		SkriptLogger.logDirect(Level.SEVERE, "End of Error.");
		SkriptLogger.logDirect(Level.SEVERE, "");
		
		SkriptLogger.resetPrefix();
		
		final RuntimeException r = new RuntimeException();
		r.setStackTrace(new StackTraceElement[0]);
		return r;
	}
	
	// ================ CONFIGS ================
	
	static Config mainConfig;
	static final ArrayList<Config> configs = new ArrayList<Config>();
	
	private static boolean keepConfigsLoaded = true;
	private static boolean enableEffectCommands = false;
	
	private static final void parseMainConfig() {
		
		final ArrayList<String> aliasNodes = new ArrayList<String>();
		
		final SectionValidator mainConfigStructure = new SectionValidator();
		mainConfigStructure.addNode("verbosity", new EnumEntryValidator<Verbosity>(Verbosity.class, new Setter<Verbosity>() {
			@Override
			public void set(final Verbosity v) {
				SkriptLogger.verbosity = v;
			}
		}), false);
		mainConfigStructure.addNode("plugin priority", new EnumEntryValidator<EventPriority>(EventPriority.class, new Setter<EventPriority>() {
			@Override
			public void set(final EventPriority p) {
				Skript.priority = p;
			}
		}, "lowest, low, normal, high, highest"), false);
		mainConfigStructure.addEntry("aliases", new Setter<String>() {
			@Override
			public void set(final String s) {
				for (final String n : s.split(","))
					aliasNodes.add(n.trim());
			}
		}, false);
		mainConfigStructure.addEntry("keep configs loaded", Boolean.class, new Setter<Boolean>() {
			@Override
			public void set(final Boolean b) {
				keepConfigsLoaded = b;
			}
		}, false);
		mainConfigStructure.addEntry("enable effect commands", Boolean.class, new Setter<Boolean>() {
			@Override
			public void set(final Boolean b) {
				enableEffectCommands = b;
			}
		}, false);
		mainConfigStructure.setAllowUndefinedSections(true);
		
		mainConfig.getMainNode().validate(mainConfigStructure);
		
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
						else
							Skript.printErrorAndCause("'" + ((EntryNode) a).getValue() + "' is invalid");
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
		
		if (enableEffectCommands) {
			if (commandMap == null) {
				error("you have to use CraftBukkit to use commands");
			} else {
				Bukkit.getPluginManager().registerEvents(new Listener() {
					@SuppressWarnings("unused")
					@EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
					public void onPlayerCommand(final PlayerCommandPreprocessEvent e) {
						if (Skript.handleEffectCommand(e.getPlayer(), e.getMessage().substring(1)))
							e.setCancelled(true);
					}
					
					@SuppressWarnings("unused")
					@EventHandler(priority = EventPriority.HIGHEST)
					public void onServerCommand(final ServerCommandEvent e) {
						if (Skript.handleEffectCommand(e.getSender(), e.getCommand()))
							e.setCommand("");
					}
				}, instance);
			}
		}
	}
	
	/**
	 * only called if {@link Skript#enableEffectCommands} is true
	 * 
	 * @param sender
	 * @param command
	 */
	private static boolean handleEffectCommand(final CommandSender sender, final String command) {
		if (!sender.hasPermission("skript.commands"))
			return false;
		final String x = command.split(" ", 2)[0];
		if (commandMap.getCommand(x) != null)
			return false;
		
		final Effect e = Effect.parse(command);
		if (e != null) {
			final String[] c = command.split(" ");
			sender.sendMessage(ChatColor.GRAY + ChatColor.stripColor(command));
			e.run(new CommandEvent(sender, c[0], c.length > 1 ? Arrays.copyOfRange(c, 1, c.length - 1) : new String[0]));
			return true;
		} else if (Skript.hasErrorCause()) {
			sender.sendMessage(ChatColor.GRAY + ChatColor.stripColor(command));
			Skript.printErrorCause(sender);
			return true;
		}
		return false;
	}
	
	private void loadMainConfig() {
		info("loading main config...");
		try {
			
			final File config = new File(getDataFolder(), "config.cfg");
			if (!config.exists()) {
				error("Config file 'config.cfg' does not exist!");
				return;
			}
			if (!config.canRead()) {
				error("Config file 'config.cfg' cannot be read!");
				return;
			}
			mainConfig = new Config(config, false, "=");
			parseMainConfig();
			
			if (logNormal())
				info("main config loaded successfully.");
			
		} catch (final Exception e) {
			exception(e, "error loading config");
		}
	}
	
	private void loadTriggerFiles() {
		boolean successful = true;
		resetErrorCount();
		int numFiles = 0;
		
		try {
			final File includes = new File(getDataFolder(), Skript.TRIGGERFILEFOLDER + File.separatorChar);
			for (final File f : includes.listFiles(new FilenameFilter() {
				@Override
				public boolean accept(final File dir, final String name) {
					return name.endsWith(".cfg") && !name.startsWith("-");
				}
			})) {
				final Config c = new Config(f, true, ":");
				if (keepConfigsLoaded)
					configs.add(c);
				TriggerFileLoader.load(c);
				numFiles++;
			}
		} catch (final Exception e) {
			SkriptLogger.setNode(null);
			Skript.exception(e, "could not load trigger files");
			successful = false;
		}
		
		if (successful && log(Verbosity.NORMAL))
			info("loaded " + numFiles + " trigger file" + (numFiles == 1 ? "" : "s")
					+ " with a total of " + TriggerFileLoader.loadedTriggers + " trigger" + (TriggerFileLoader.loadedTriggers == 1 ? "" : "s")
					+ " and " + TriggerFileLoader.loadedCommands + " command" + (TriggerFileLoader.loadedCommands == 1 ? "" : "s"));
		if (successful && errorCount == 0)
			info("No errors detected in any loaded trigger files");
		
		for (final Entry<Class<? extends Event>, List<Trigger>> e : SkriptEventHandler.triggers.entrySet()) {
			Bukkit.getServer().getPluginManager().registerEvent(e.getKey(), new Listener() {}, priority, SkriptEventHandler.ee, instance);
			if (log(Verbosity.EXTREME))
				config("registered Event " + e.getKey().getSimpleName());
		}
	}
	
}
