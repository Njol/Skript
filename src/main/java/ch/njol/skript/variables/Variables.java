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

import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.TreeMap;
import java.util.WeakHashMap;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.regex.Pattern;

import org.bukkit.event.Event;

import ch.njol.skript.lang.Variable;
import ch.njol.skript.util.Utils;
import ch.njol.util.StringUtils;

/**
 * @author Peter Güttinger
 */
public abstract class Variables {
	private Variables() {}
	
	private static DatabaseStorage database = new DatabaseStorage();
	public static FlatFileStorage file = new FlatFileStorage();
	
	public static boolean load() {
		try {
			variablesLock.writeLock().lock();
			assert variables.isEmpty();
			assert variablesHashMap.isEmpty();
			
			return database.load() && file.load();
		} finally {
			variablesLock.writeLock().unlock();
		}
	}
	
	private final static Comparator<String> variableNameComparator = new Comparator<String>() {
		@Override
		public int compare(final String s1, final String s2) {
			if (s1 == null)
				return s2 == null ? 0 : -1;
			if (s2 == null)
				return 1;
			int i = 0, j = 0;
			while (i < s1.length() && j < s2.length()) {
				final char c1 = s1.charAt(i), c2 = s2.charAt(j);
				if ('0' <= c1 && c1 <= '9' && '0' <= c2 && c2 <= '9') {
					final int i2 = StringUtils.findLastDigit(s1, i), j2 = StringUtils.findLastDigit(s2, j);
					final int n1 = Utils.parseInt(s1.substring(i, i2)), n2 = Utils.parseInt(s2.substring(j, j2));
					if (n1 > n2)
						return 1;
					if (n1 < n2)
						return -1;
					i = i2;
					j = j2;
					continue;
				} else {
					if (c1 > c2)
						return 1;
					if (c1 < c2)
						return -1;
					i++;
					j++;
				}
			}
			if (i < s1.length())
				return -1;
			if (j < s2.length())
				return 1;
			return 0;
		}
	};
	
	private final static Pattern variableNameSplitPattern = Pattern.compile(Pattern.quote(Variable.SEPARATOR));
	
	public final static String[] splitVariableName(final String name) {
		return variableNameSplitPattern.split(name);
	}
	
	private final static ReadWriteLock variablesLock = new ReentrantReadWriteLock(true);
	/**
	 * note to self: use {@link #setVariable(String, Object, VariablesStorage)} and {@link #getVariable(String)}
	 * <p>
	 * must be locked with {@link #variablesLock}.
	 */
	private final static TreeMap<String, Object> variables = new TreeMap<String, Object>(variableNameComparator);
	/**
	 * For fast access
	 * <p>
	 * must be locked with {@link #variablesLock}.
	 */
	private final static HashMap<String, Object> variablesHashMap = new HashMap<String, Object>();
	
	private final static HashMap<String, WeakHashMap<Event, Object>> localVariables = new HashMap<String, WeakHashMap<Event, Object>>();
	
	/**
	 * Remember to lock with {@link #getReadLock()} and to not make any changes!
	 * 
	 * @return
	 */
	static TreeMap<String, Object> getVariables() {
		return variables;
	}
	
	/**
	 * Remember to lock with {@link #getReadLock()}!
	 * 
	 * @return
	 */
	static Map<String, Object> getVariablesHashMap() {
		return Collections.unmodifiableMap(variablesHashMap);
	}
	
	static Lock getReadLock() {
		return variablesLock.readLock();
	}
	
	/**
	 * Sets a variable.
	 * 
	 * @param name The variable's name. Can be a "list variable::*" (<tt>value</tt> must be <tt>null</tt> in this case)
	 * @param value The variable's value. Use <tt>null</tt> to delete the variable.
	 */
	public final static void setVariable(final String name, final Object value) {
		setVariable(name, value, null);
	}
	
	@SuppressWarnings("unchecked")
	final static void setVariable(final String name, final Object value, final VariablesStorage source) {
		try {
			variablesLock.writeLock().lock();
			if (!name.endsWith("*")) {
				if (value == null)
					variablesHashMap.remove(name);
				else
					variablesHashMap.put(name, value);
			}
			final String[] split = splitVariableName(name);
			TreeMap<String, Object> parent = variables;
			for (int i = 0; i < split.length; i++) {
				final String n = split[i];
				Object current = parent.get(n);
				if (current == null) {
					if (i == split.length - 1) {
						if (value != null)
							parent.put(n, value);
						break;
					} else if (value != null) {
						parent.put(n, current = new TreeMap<String, Object>(variableNameComparator));
						parent = (TreeMap<String, Object>) current;
						continue;
					} else {
						break;
					}
				} else if (current instanceof TreeMap) {
					if (i == split.length - 1) {
						if (value == null)
							((TreeMap<String, Object>) current).remove(null);
						else
							((TreeMap<String, Object>) current).put(null, value);
						break;
					} else if (i == split.length - 2 && split[i + 1].equals("*")) {
						assert value == null;
						deleteFromHashMap(StringUtils.join(split, Variable.SEPARATOR, 0, i + 1), (TreeMap<String, Object>) current);
						final Object v = ((TreeMap<String, Object>) current).get(null);
						if (v == null)
							parent.remove(n);
						else
							parent.put(n, v);
						break;
					} else {
						parent = (TreeMap<String, Object>) current;
						continue;
					}
				} else {
					if (i == split.length - 1) {
						if (value == null)
							parent.remove(n);
						else
							parent.put(n, value);
						break;
					} else if (value != null) {
						final TreeMap<String, Object> c = new TreeMap<String, Object>(variableNameComparator);
						c.put(null, current);
						parent.put(n, c);
						parent = c;
						continue;
					} else {
						break;
					}
				}
			}
			saveVariableChange(name, value, source);
		} finally {
			variablesLock.writeLock().unlock();
		}
	}
	
	private static void deleteFromHashMap(final String parent, final TreeMap<String, Object> current) {
		for (final Entry<String, Object> e : current.entrySet()) {
			if (e.getKey() == null)
				continue;
			variablesHashMap.remove(parent + Variable.SEPARATOR + e.getKey());
			if (e.getValue() instanceof TreeMap) {
				deleteFromHashMap(parent + Variable.SEPARATOR + e.getKey(), (TreeMap<String, Object>) e.getValue());
			}
		}
	}
	
	private final static void saveVariableChange(final String name, final Object value, final VariablesStorage source) {
		if (DatabaseStorage.accept(name)) {
			if (source != database) {
				database.save(name, value);
				if (source == file)
					file.save(name, null);
			}
		} else {
			if (source != file) {
				file.save(name, value);
				if (source == database)
					database.save(name, null);
			}
		}
	}
	
	/**
	 * Returns the internal value of the requested variable.
	 * <p>
	 * <b>Do not modify the returned value!</b>
	 * 
	 * @param name
	 * @return an Object for a normal Variable or a Map<String, Object> for a list variable, or null if the variable is not set.
	 */
	@SuppressWarnings("unchecked")
	public final static Object getVariable(final String name) {
		try {
			variablesLock.readLock().lock();
			if (!name.endsWith("*")) {
				return variablesHashMap.get(name);
			} else {
				final String[] split = splitVariableName(name);
				TreeMap<String, Object> current = variables;
				for (int i = 0; i < split.length; i++) {
					final String n = split[i];
					if (n.equals("*")) {
						assert i == split.length - 1;
						return current;
					}
					final Object o = current.get(n);
					if (o == null)
						return null;
					if (o instanceof Map) {
						current = (TreeMap<String, Object>) o;
						assert i != split.length - 1;
						continue;
					} else {
						return null;
					}
				}
				return null;
			}
		} finally {
			variablesLock.readLock().unlock();
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
	
	public static int numVariables() {
		try {
			variablesLock.readLock().lock();
			return variablesHashMap.size();
		} finally {
			variablesLock.readLock().unlock();
		}
	}
	
}
