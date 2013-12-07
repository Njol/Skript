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

import org.bukkit.event.Event;

import ch.njol.skript.Skript;
import ch.njol.skript.SkriptConfig;
import ch.njol.skript.config.Node;
import ch.njol.skript.config.SectionNode;
import ch.njol.skript.lang.Variable;
import ch.njol.skript.variables.DatabaseStorage.Type;
import ch.njol.util.Kleenean;
import ch.njol.util.Pair;
import ch.njol.yggdrasil.Yggdrasil;

/**
 * @author Peter Güttinger
 */
public abstract class Variables implements Closeable {
	private Variables() {}
	
	public final static Yggdrasil yggdrasil = new Yggdrasil();
	static {
		yggdrasil.registerSingleClass(Kleenean.class, "Kleenean");
	}
	
	static List<VariablesStorage> storages = new ArrayList<VariablesStorage>();
	
	@SuppressWarnings("resource")
	public static boolean load() {
		try {
			variablesLock.writeLock().lock();
			assert variables.treeMap.isEmpty();
			assert variables.hashMap.isEmpty();
			
			final Node databases = SkriptConfig.getConfig().getMainNode().get("databases");
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
	
	private final static Pattern variableNameSplitPattern = Pattern.compile(Pattern.quote(Variable.SEPARATOR));
	
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
	static Map<String, Object> getVariablesHashMap() {
		return Collections.unmodifiableMap(variables.hashMap);
	}
	
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
	public final static Object getVariable(final String name, final Event e, final boolean local) {
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
	public final static void setVariable(final String name, final Object value, final Event e, final boolean local) {
		if (local) {
			VariablesMap map = localVariables.get(e);
			if (map == null)
				localVariables.put(e, map = new VariablesMap());
			map.setVariable(name, value);
		} else {
			setVariable(name, value);
		}
	}
	
	final static void setVariable(final String name, final Object value) {
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
	final static void variableLoaded(final String name, final Object value, final VariablesStorage source) {
		variables.setVariable(name, value);
		
		for (final VariablesStorage s : storages) {
			if (s == source)
				continue;
			if (s.accept(name)) {
				s.save(new Pair<String, Object>(name, value));
				break;
			}
		}
	}
	
	private final static void saveVariableChange(final String name, final Object value) {
		queue.add(new Pair<String, Object>(name, value));
	}
	
	final static BlockingQueue<Pair<String, Object>> queue = new LinkedBlockingQueue<Pair<String, Object>>();
	
	static volatile boolean closed = false;
	
	private final static Thread saveThread = Skript.newThread(new Runnable() {
		@Override
		public void run() {
			while (!closed) {
				try {
					final Pair<String, Object> v = queue.take();
					for (final VariablesStorage s : storages) {
						if (s.accept(v.first)) {
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
