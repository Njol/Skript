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

package ch.njol.skript.lang;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Map.Entry;
import java.util.NoSuchElementException;
import java.util.TreeMap;

import org.bukkit.event.Event;

import ch.njol.skript.Skript;
import ch.njol.skript.SkriptAPIException;
import ch.njol.skript.classes.Changer;
import ch.njol.skript.classes.Changer.ChangeMode;
import ch.njol.skript.classes.Changer.ChangerUtils;
import ch.njol.skript.classes.ClassInfo;
import ch.njol.skript.classes.Comparator.Relation;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.skript.lang.util.SimpleExpression;
import ch.njol.skript.registrations.Classes;
import ch.njol.skript.registrations.Comparators;
import ch.njol.skript.registrations.Converters;
import ch.njol.skript.util.StringMode;
import ch.njol.skript.util.Utils;
import ch.njol.skript.variables.Variables;
import ch.njol.util.Checker;
import ch.njol.util.CollectionUtils;
import ch.njol.util.Kleenean;
import ch.njol.util.Pair;
import ch.njol.util.StringUtils;
import ch.njol.util.iterator.EmptyIterator;

/**
 * @author Peter Güttinger
 */
@SuppressWarnings("serial")
public class Variable<T> implements Expression<T> {
	
	public final static String SEPARATOR = "::";
	public final static String LOCAL_VARIABLE_TOKEN = "_";
	
	/**
	 * The name of this variable, excluding the local variable token, but including the list variable token '::*'.
	 */
	private final VariableString name;
	
	private final Class<T> superType;
	private final Class<? extends T>[] types;
	
	private final boolean local;
	private final boolean list;
	
	private final Variable<?> source;
	
	private Variable(final VariableString name, final Class<? extends T>[] types, final boolean local, final boolean list, final Variable<?> source) {
		assert name != null;
		assert types != null && types.length > 0;
		
		assert name.isSimple() || name.getMode() == StringMode.VARIABLE_NAME;
		
		this.local = local;
		this.list = list;
		
		this.name = name;
		
		this.types = types;
		this.superType = (Class<T>) Utils.getSuperType(types);
		
		this.source = source;
	}
	
	/**
	 * Prints errors
	 * 
	 * @param name
	 * @param type
	 * @return
	 */
	public static <T> Variable<T> newInstance(final String name, final Class<? extends T>[] types) {
		if (name.startsWith(LOCAL_VARIABLE_TOKEN) && name.contains(SEPARATOR)) {
			Skript.error("Local variables cannot be lists, i.e. must not contain the separator '" + SEPARATOR + "' (error in variable {" + name + "})");
			return null;
		} else if (name.startsWith(SEPARATOR) || name.endsWith(SEPARATOR)) {
			Skript.error("A variable's name must neither start nor end with the separator '" + SEPARATOR + "' (error in variable {" + name + "})");
			return null;
		} else if (name.contains("*") && (name.indexOf("*") != name.length() - 1 || !name.endsWith(SEPARATOR + "*"))) {
			if (name.indexOf("*") == 0)
				Skript.error("[2.0] Local variables now start with an underscore, e.g. {_local variable} (error in variable {" + name + "})");
			else
				Skript.error("A variable's name must not contain any asterisks except at the end after '" + SEPARATOR + "' to denote a list variable, e.g. {variable" + SEPARATOR + "*} (error in variable {" + name + "})");
			return null;
		} else if (name.contains(SEPARATOR + SEPARATOR)) {
			Skript.error("A variable's name must not contain the separator '" + SEPARATOR + "' multiple times in a row (error in variable {" + name + "})");
			return null;
		}
		final VariableString vs = VariableString.newInstance(name.startsWith(LOCAL_VARIABLE_TOKEN) ? name.substring(LOCAL_VARIABLE_TOKEN.length()) : name, StringMode.VARIABLE_NAME);
		if (vs == null)
			return null;
		return new Variable<T>(vs, types, name.startsWith(LOCAL_VARIABLE_TOKEN), name.endsWith(SEPARATOR + "*"), null);
	}
	
	@Override
	public boolean init(final Expression<?>[] exprs, final int matchedPattern, final Kleenean isDelayed, final ParseResult parseResult) {
		throw new UnsupportedOperationException();
	}
	
	@Override
	public boolean isSingle() {
		return !list;
	}
	
	@Override
	public Class<? extends T> getReturnType() {
		return superType;
	}
	
	@Override
	public String toString(final Event e, final boolean debug) {
		if (e != null)
			return Classes.toString(get(e));
		return "{" + (local ? "_" : "") + StringUtils.substring(name.toString(e, debug), 1, -1) + "}" + (debug ? "(as " + superType.getName() + ")" : "");
	}
	
	@Override
	public String toString() {
		return toString(null, false);
	}
	
	@SuppressWarnings("unchecked")
	@Override
	public <R> Expression<? extends R> getConvertedExpression(final Class<R> to) {
		return new Variable<R>(name, new Class[] {to}, local, list, this);
	}
	
	/**
	 * Gets the value of this variable as stored in the variables map.
	 * 
	 * @param e
	 * @return
	 */
	private Object getRaw(final Event e) {
		assert !local;
		final Object val = Variables.getVariable(name.toString(e).toLowerCase(Locale.ENGLISH));
		if (val == null)
			return Variables.getVariable(name.getDefaultVariableName().toLowerCase(Locale.ENGLISH));
		return val;
	}
	
	@SuppressWarnings("unchecked")
	private Object get(final Event e) {
		if (local)
			return Variables.getLocalVariable(name.toString(e).toLowerCase(Locale.ENGLISH), e);
		final Object val = getRaw(e);
		if (!list)
			return val;
		if (val == null)
			return Array.newInstance(types[0], 0);
		final List<Object> l = new ArrayList<Object>();
		for (final Entry<String, ?> v : ((Map<String, ?>) val).entrySet()) {
			if (v.getKey() != null && v.getValue() != null) {
				if (v.getValue() instanceof Map)
					l.add(((Map<String, ?>) v.getValue()).get(null));
				else
					l.add(v.getValue());
			}
		}
		return l.toArray((T[]) Array.newInstance(superType, l.size())); // TODO return array of one of the types (can cause CCE currently) // fixed?
	}
	
	public Iterator<Pair<String, Object>> variablesIterator(final Event e) {
		if (!list)
			throw new SkriptAPIException("");
		final String name = StringUtils.substring(this.name.toString(e), 0, -1).toLowerCase(Locale.ENGLISH);
		final Object val = Variables.getVariable(name + "*");
		if (val == null)
			return new EmptyIterator<Pair<String, Object>>();
		assert val instanceof TreeMap;
		// temporary list to prevent CMEs
		@SuppressWarnings("unchecked")
		final Iterator<String> keys = new ArrayList<String>(((Map<String, Object>) val).keySet()).iterator();
		return new Iterator<Pair<String, Object>>() {
			private String key;
			private Object next = null;
			
			@Override
			public boolean hasNext() {
				if (next != null)
					return true;
				while (keys.hasNext()) {
					key = keys.next();
					if (key != null) {
						next = Variables.getVariable(name + key);
						if (next != null && !(next instanceof TreeMap))
							return true;
					}
				}
				next = null;
				return false;
			}
			
			@Override
			public Pair<String, Object> next() {
				if (!hasNext())
					throw new NoSuchElementException();
				final Pair<String, Object> n = new Pair<String, Object>(key, next);
				next = null;
				return n;
			}
			
			@Override
			public void remove() {
				throw new UnsupportedOperationException();
			}
		};
	}
	
	@Override
	public Iterator<T> iterator(final Event e) {
		if (!list)
			throw new SkriptAPIException("");
		final String name = StringUtils.substring(this.name.toString(e), 0, -1).toLowerCase(Locale.ENGLISH);
		final Object val = Variables.getVariable(name + "*");
		if (val == null)
			return new EmptyIterator<T>();
		assert val instanceof TreeMap;
		// temporary list to prevent CMEs
		@SuppressWarnings("unchecked")
		final Iterator<String> keys = new ArrayList<String>(((Map<String, Object>) val).keySet()).iterator();
		return new Iterator<T>() {
			private String key;
			private T next = null;
			
			@Override
			public boolean hasNext() {
				if (next != null)
					return true;
				while (keys.hasNext()) {
					key = keys.next();
					if (key != null) {
						next = Converters.convert(Variables.getVariable(name + key), types);
						if (next != null && !(next instanceof TreeMap))
							return true;
					}
				}
				next = null;
				return false;
			}
			
			@Override
			public T next() {
				if (!hasNext())
					throw new NoSuchElementException();
				final T n = next;
				next = null;
				return n;
			}
			
			@Override
			public void remove() {
				throw new UnsupportedOperationException();
			}
		};
	}
	
	private T getConverted(final Event e) {
		assert !list;
		return Converters.convert(get(e), types);
	}
	
	private T[] getConvertedArray(final Event e) {
		assert list;
		return Converters.convertArray((Object[]) get(e), types, superType);
	}
	
	private final void set(final Event e, final Object value) {
		if (local)
			Variables.setLocalVariable(name.toString(e).toLowerCase(Locale.ENGLISH), e, value);
		else
			Variables.setVariable(name.toString(e).toLowerCase(Locale.ENGLISH), value);
	}
	
	private final void setIndex(final Event e, final String index, final Object value) {
		assert list;
		final String s = name.toString(e).toLowerCase(Locale.ENGLISH);
		Variables.setVariable(s.substring(0, s.length() - 1) + index.toLowerCase(Locale.ENGLISH), value);
	}
	
	@SuppressWarnings("unchecked")
	@Override
	public Class<?>[] acceptChange(final ChangeMode mode) {
		if (list)
			return CollectionUtils.array(Object[].class);
		return CollectionUtils.array(Object.class);
	}
	
	@SuppressWarnings({"unchecked", "rawtypes"})
	@Override
	public void change(final Event e, final Object delta, final ChangeMode mode) throws UnsupportedOperationException {
		switch (mode) {
			case DELETE:
				set(e, null);
				break;
			case SET:
				if (list) {
					set(e, null);
					int i = 1;
					for (final Object d : (Object[]) delta) {
						if (d instanceof Object[]) {
							for (int j = 0; j < ((Object[]) d).length; j++)
								setIndex(e, "" + i + SEPARATOR + j, ((Object[]) d)[j]);
						} else {
							setIndex(e, "" + i, d);
						}
						i++;
					}
				} else {
//					final ClassInfo<?> ci = delta == null ? null : Classes.getSuperClassInfo(delta.getClass());
//					if (ci != null && ci.getSerializeAs() != null) {
//						set(e, Converters.convert(delta, ci.getSerializeAs()));
//					} else {
					set(e, delta);
//					}
				}
				break;
			case RESET:
				final Object x = getRaw(e);
				for (final Object o : x instanceof Map ? ((Map<?, ?>) x).values() : Arrays.asList(x)) {
					final ClassInfo<?> ci = Classes.getSuperClassInfo(o.getClass());
					if (ci.getChanger() != null && ci.getChanger().acceptChange(ChangeMode.RESET) != null) {
						final Object[] one = (Object[]) Array.newInstance(o.getClass(), 1);
						one[0] = o;
						((Changer) ci.getChanger()).change(one, null, ChangeMode.RESET);
					}
				}
				break;
			case ADD:
			case REMOVE:
			case REMOVE_ALL:
				if (delta == null)
					break;
				if (list) {
					final Object[] ds = (Object[]) delta;
					final Map<String, Object> o = (Map<String, Object>) getRaw(e);
					if (mode == ChangeMode.REMOVE) {
						if (o == null)
							return;
						final ArrayList<String> rem = new ArrayList<String>(); // prevents CMEs
						for (final Object d : ds) {
							for (final Entry<String, Object> i : o.entrySet()) {
								if (Relation.EQUAL.is(Comparators.compare(i.getValue(), d))) {
									rem.add(i.getKey());
									break;
								}
							}
						}
						for (final String r : rem)
							setIndex(e, r, null);
					} else if (mode == ChangeMode.REMOVE_ALL) {
						if (o == null)
							return;
						final ArrayList<String> rem = new ArrayList<String>(); // prevents CMEs
						for (final Entry<String, Object> i : o.entrySet()) {
							for (final Object d : ds) {
								if (Relation.EQUAL.is(Comparators.compare(i.getValue(), d)))
									rem.add(i.getKey());
							}
						}
						for (final String r : rem)
							setIndex(e, r, null);
					} else { // ADD
						int i = 1;
						for (final Object d : ds) {
							if (o != null)
								while (o.containsKey("" + i))
									i++;
							setIndex(e, "" + i, d);
							i++;
						}
					}
				} else {
					final Object o = get(e);
					Number n = null;
					if ((o == null || o instanceof Number) && (delta instanceof Number || (n = Converters.convert(delta, Number.class)) != null)) {
						final int i = mode == ChangeMode.ADD ? 1 : -1;
						set(e, (o == null ? 0 : ((Number) o).doubleValue()) + i * (n != null ? n : (Number) delta).doubleValue());
					} else if (o != null) {
						final ClassInfo<?> ci = Classes.getSuperClassInfo(o.getClass());
						if (ci.getChanger() != null && ci.getChanger().acceptChange(mode) != null) {
							final Class<?>[] cs = ci.getChanger().acceptChange(mode);
							final Object[] one = (Object[]) Array.newInstance(o.getClass(), 1);
							one[0] = o;
							for (final Class<?> c : cs) {
								if (c.isInstance(delta)) {
									ChangerUtils.change(ci.getChanger(), one, delta, mode);
									return;
								} else if (c.isArray() && c.getComponentType().isInstance(delta)) {
									final Object[] deltas = (Object[]) Array.newInstance(c.getComponentType(), 1);
									deltas[0] = delta;
									ChangerUtils.change(ci.getChanger(), one, deltas, mode);
									return;
								}
							}
							for (final Class<?> c : cs) {
								if (delta instanceof Object[]) {
									if (!c.isArray())
										continue;
									final Object[] d = Converters.convertArray((Object[]) delta, c.getComponentType());
									if (d != null) {
										ChangerUtils.change(ci.getChanger(), one, d, mode);
										return;
									}
								} else {
									final Object d = Converters.convert(delta, c.isArray() ? c.getComponentType() : c);
									if (d != null) {
										if (c.isArray()) {
											final Object[] deltas = (Object[]) Array.newInstance(c.getComponentType(), 1);
											deltas[0] = d;
											ChangerUtils.change(ci.getChanger(), one, deltas, mode);
										} else {
											ChangerUtils.change(ci.getChanger(), one, d, mode);
										}
										return;
									}
								}
							}
						}
					}
				}
				break;
		}
	}
	
	@Override
	public T getSingle(final Event e) {
		if (list)
			throw new SkriptAPIException("Invalid call to getSingle");
		return getConverted(e);
	}
	
	@Override
	public T[] getArray(final Event e) {
		return getAll(e);
	}
	
	@Override
	public T[] getAll(final Event e) {
		if (list)
			return getConvertedArray(e);
		final T o = getConverted(e);
		if (o == null)
			return (T[]) Array.newInstance(superType, 0);
		final T[] one = (T[]) Array.newInstance(superType, 1);
		one[0] = o;
		return one;
	}
	
	@Override
	public boolean isLoopOf(final String s) {
		return s.equalsIgnoreCase("var") || s.equalsIgnoreCase("variable") || s.equalsIgnoreCase("value") || s.equalsIgnoreCase("index");
	}
	
	public boolean isIndexLoop(final String s) {
		return s.equalsIgnoreCase("index");
	}
	
	@Override
	public boolean check(final Event e, final Checker<? super T> c, final boolean negated) {
		return SimpleExpression.check(getAll(e), c, negated, getAnd());
	}
	
	@Override
	public boolean check(final Event e, final Checker<? super T> c) {
		return SimpleExpression.check(getAll(e), c, false, getAnd());
	}
	
	@Override
	public boolean getAnd() {
		return true;
	}
	
	@Override
	public boolean setTime(final int time) {
		return false;
	}
	
	@Override
	public int getTime() {
		return 0;
	}
	
	@Override
	public boolean isDefault() {
		return false;
	}
	
	@Override
	public Expression<?> getSource() {
		return source == null ? this : source;
	}
	
	@Override
	public Expression<? extends T> simplify() {
		return this;
	}
	
}
