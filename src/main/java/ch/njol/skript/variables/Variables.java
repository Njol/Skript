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

package ch.njol.skript.variables;

import java.io.Closeable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.WeakHashMap;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.regex.Pattern;

import org.bukkit.Bukkit;
import org.bukkit.configuration.serialization.ConfigurationSerializable;
import org.bukkit.configuration.serialization.ConfigurationSerialization;
import org.bukkit.event.Event;
import org.eclipse.jdt.annotation.Nullable;

import ch.njol.skript.Skript;
import ch.njol.skript.SkriptAPIException;
import ch.njol.skript.SkriptConfig;
import ch.njol.skript.classes.ClassInfo;
import ch.njol.skript.classes.ConfigurationSerializer;
import ch.njol.skript.config.Config;
import ch.njol.skript.config.Node;
import ch.njol.skript.config.SectionNode;
import ch.njol.skript.lang.Variable;
import ch.njol.skript.registrations.Classes;
import ch.njol.skript.registrations.Converters;
import ch.njol.skript.variables.DatabaseStorage.Type;
import ch.njol.util.Kleenean;
import ch.njol.yggdrasil.Yggdrasil;

/**
 * @author Peter Güttinger
 */
public abstract class Variables implements Closeable {
	private Variables() {}
	
	public final static short YGGDRASIL_VERSION = 1;
	
	public final static Yggdrasil yggdrasil = new Yggdrasil(YGGDRASIL_VERSION);
	
	private final static String ConfigurationSerializablePrefix = "ConfigurationSerializable_";
	static {
		yggdrasil.registerSingleClass(Kleenean.class, "Kleenean");
		yggdrasil.registerClassResolver(new ConfigurationSerializer<ConfigurationSerializable>() {
			{
				// used by asserts
				info = (ClassInfo<? extends ConfigurationSerializable>) Classes.getExactClassInfo(Object.class);
			}
			
			@SuppressWarnings("unchecked")
			@Override
			@Nullable
			public String getID(final Class<?> c) {
				if (ConfigurationSerializable.class.isAssignableFrom(c) && Classes.getSuperClassInfo(c) == Classes.getExactClassInfo(Object.class))
					return ConfigurationSerializablePrefix + ConfigurationSerialization.getAlias((Class<? extends ConfigurationSerializable>) c);
				return null;
			}
			
			@Override
			@Nullable
			public Class<? extends ConfigurationSerializable> getClass(final String id) {
				if (id.startsWith(ConfigurationSerializablePrefix))
					return ConfigurationSerialization.getClassByAlias(id.substring(ConfigurationSerializablePrefix.length()));
				return null;
			}
		});
	}
	
	static List<VariablesStorage> storages = new ArrayList<VariablesStorage>();
	
	public static boolean load() {
		try {
			variablesLock.writeLock().lock();
			assert variables.treeMap.isEmpty();
			assert variables.hashMap.isEmpty();
			
			final Config c = SkriptConfig.getConfig();
			if (c == null)
				throw new SkriptAPIException("Cannot load variables before the config");
			final Node databases = c.getMainNode().get("databases");
			if (databases == null || !(databases instanceof SectionNode)) {
				Skript.error("The config is missing the required 'databases' section that defines where the variables are saved");
				return false;
			}
			for (final Node node : (SectionNode) databases) {
				if (node instanceof SectionNode) {
					final SectionNode n = (SectionNode) node;
					final String type = n.getValue("type");
					if (type == null) {
						Skript.error("Missing entry 'type' in database definition");
						return false;
					}
					
					final VariablesStorage s;
					if (type.equalsIgnoreCase("csv") || type.equalsIgnoreCase("file") || type.equalsIgnoreCase("flatfile")) {
						s = new FlatFileStorage(n);
					} else if (type.equalsIgnoreCase("mysql")) {
						s = new DatabaseStorage(n, Type.MYSQL);
					} else if (type.equalsIgnoreCase("sqlite")) {
						s = new DatabaseStorage(n, Type.SQLITE);
					} else {
						if (!type.equalsIgnoreCase("disabled") && !type.equalsIgnoreCase("none")) {
							Skript.error("Invalid database type '" + type + "'");
							return false;
						}
						continue;
					}
					if (!s.load(n))
						return false;
					storages.add(s);
				} else {
					Skript.error("Invalid line in databases: databases must be defined as sections");
					return false;
				}
			}
			if (storages.isEmpty()) {
				Skript.error("No databases to store variables are defined. Please enable at least the default database, even if you don't use variables at all.");
				return false;
			}
			return true;
		} finally {
			variablesLock.writeLock().unlock();
		}
	}
	
	@SuppressWarnings("null")
	private final static Pattern variableNameSplitPattern = Pattern.compile(Pattern.quote(Variable.SEPARATOR));
	
	@SuppressWarnings("null")
	public final static String[] splitVariableName(final String name) {
		return variableNameSplitPattern.split(name);
	}
	
	private final static ReadWriteLock variablesLock = new ReentrantReadWriteLock(true);
	/**
	 * must be locked with {@link #variablesLock}.
	 */
	private final static VariablesMap variables = new VariablesMap();
	/**
	 * Not accessed concurrently
	 */
	private final static WeakHashMap<Event, VariablesMap> localVariables = new WeakHashMap<Event, VariablesMap>();
	
	/**
	 * Remember to lock with {@link #getReadLock()} and to not make any changes!
	 */
	static TreeMap<String, Object> getVariables() {
		return variables.treeMap;
	}
	
	/**
	 * Remember to lock with {@link #getReadLock()}!
	 */
	@SuppressWarnings("null")
	static Map<String, Object> getVariablesHashMap() {
		return Collections.unmodifiableMap(variables.hashMap);
	}
	
	@SuppressWarnings("null")
	static Lock getReadLock() {
		return variablesLock.readLock();
	}
	
	/**
	 * Returns the internal value of the requested variable.
	 * <p>
	 * <b>Do not modify the returned value!</b>
	 * 
	 * @param name
	 * @return an Object for a normal Variable or a Map<String, Object> for a list variable, or null if the variable is not set.
	 */
	@Nullable
	public final static Object getVariable(final String name, final @Nullable Event e, final boolean local) {
		if (local) {
			final VariablesMap map = localVariables.get(e);
			if (map == null)
				return null;
			return map.getVariable(name);
		} else {
			try {
				variablesLock.readLock().lock();
				return variables.getVariable(name);
			} finally {
				variablesLock.readLock().unlock();
			}
		}
	}
	
	/**
	 * Sets a variable.
	 * 
	 * @param name The variable's name. Can be a "list variable::*" (<tt>value</tt> must be <tt>null</tt> in this case)
	 * @param value The variable's value. Use <tt>null</tt> to delete the variable.
	 */
	public final static void setVariable(final String name, @Nullable Object value, final @Nullable Event e, final boolean local) {
		if (value != null) {
			@SuppressWarnings("null")
			final ClassInfo<?> ci = Classes.getSuperClassInfo(value.getClass());
			final Class<?> sas = ci.getSerializeAs();
			if (sas != null) {
				value = Converters.convert(value, sas);
				assert value != null : ci + ", " + sas;
			}
		}
		if (local) {
			assert e != null : name;
			VariablesMap map = localVariables.get(e);
			if (map == null)
				localVariables.put(e, map = new VariablesMap());
			map.setVariable(name, value);
		} else {
			setVariable(name, value);
		}
	}
	
	final static void setVariable(final String name, @Nullable final Object value) {
		try {
			variablesLock.writeLock().lock();
			variables.setVariable(name, value);
			saveVariableChange(name, value);
		} finally {
			variablesLock.writeLock().unlock();
		}
	}
	
	/**
	 * Doesn't lock the variables map and moves the loaded variable to the appropriate database if the config was changed.
	 * 
	 * @param name
	 * @param value
	 * @param source
	 */
	final static void variableLoaded(final String name, final @Nullable Object value, final VariablesStorage source) {
		variables.setVariable(name, value);
		
		for (final VariablesStorage s : storages) {
			if (s == source) {
				if (s.accept(name))
					break;
				else
					continue;
			}
			if (s.accept(name)) {
				s.save(serialize(name, value));
				break;
			}
		}
	}
	
	public final static SerializedVariable serialize(final String name, final @Nullable Object value) {
		assert Bukkit.isPrimaryThread();
		final SerializedVariable.Value var = serialize(value);
		return new SerializedVariable(name, var);
	}
	
	@Nullable
	public final static SerializedVariable.Value serialize(final @Nullable Object value) {
		assert Bukkit.isPrimaryThread();
		return Classes.serialize(value);
	}
	
	private final static void saveVariableChange(final String name, final @Nullable Object value) {
		queue.add(serialize(name, value));
	}
	
	final static BlockingQueue<SerializedVariable> queue = new LinkedBlockingQueue<SerializedVariable>();
	
	static volatile boolean closed = false;
	
	private final static Thread saveThread = Skript.newThread(new Runnable() {
		@Override
		public void run() {
			while (!closed) {
				try {
					final SerializedVariable v = queue.take();
					for (final VariablesStorage s : storages) {
						if (s.accept(v.name)) {
							s.save(v);
							break;
						}
					}
				} catch (final InterruptedException e) {}
			}
		}
	}, "Skript variable save thread");
	
	@Override
	public void close() {
		while (queue.size() > 0) {
			try {
				Thread.sleep(10);
			} catch (final InterruptedException e) {}
		}
		closed = true;
		saveThread.interrupt();
	}
	
	public static int numVariables() {
		try {
			variablesLock.readLock().lock();
			return variables.hashMap.size();
		} finally {
			variablesLock.readLock().unlock();
		}
	}
	
}
