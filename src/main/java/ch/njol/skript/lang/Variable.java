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
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.NoSuchElementException;

import org.bukkit.event.Event;

import ch.njol.skript.Skript;
import ch.njol.skript.SkriptAPIException;
import ch.njol.skript.classes.Changer.ChangeMode;
import ch.njol.skript.classes.Changer.ChangerUtils;
import ch.njol.skript.classes.ClassInfo;
import ch.njol.skript.classes.Comparator.Relation;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.skript.lang.util.SimpleExpression;
import ch.njol.skript.lang.util.VariableString;
import ch.njol.skript.registrations.Classes;
import ch.njol.skript.registrations.Comparators;
import ch.njol.skript.registrations.Converters;
import ch.njol.skript.util.StringMode;
import ch.njol.skript.util.Utils;
import ch.njol.skript.variables.Variables;
import ch.njol.util.Checker;
import ch.njol.util.Kleenean;
import ch.njol.util.StringUtils;
import ch.njol.util.iterator.EmptyIterator;

/**
 * @author Peter Güttinger
 */
@SuppressWarnings("serial")
public class Variable<T> implements Expression<T> {
	
	public final static String SEPARATOR = "::";
	public final static String LOCAL_VARIABLE_TOKEN = "_";
	
	private final VariableString name;
	
	private final Class<T> superType;
	private final Class<? extends T>[] types;
	
	private final boolean local;
	private final boolean list;
	
	private final Variable<?> source;
	
	@SuppressWarnings("unchecked")
	private Variable(final VariableString name, final Class<? extends T>[] types, final boolean local, final boolean list, final Variable<?> source) {
		assert name != null;
		assert types != null && types.length > 0;
		
		assert name.getMode() == StringMode.VARIABLE_NAME;
		
		this.local = local;
		this.list = list;
		
		this.name = name;
		
		this.types = types;
		Class<? extends T> superType = types[0];
		for (int i = 1; i < types.length; i++) {
			if (types[i].isAssignableFrom(superType))
				superType = types[i];
		}
		this.superType = (Class<T>) superType;
		
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
			Skript.error("Local variables cannot be lists, i.e. must not contain the separator '" + SEPARATOR + "' (variable: {" + name + "})");
			return null;
		} else if (name.startsWith(SEPARATOR) || name.endsWith(SEPARATOR)) {
			Skript.error("A variable's name must neither start nor end with the separator '" + SEPARATOR + "' (variable: {" + name + "})");
			return null;
		} else if (name.contains("*") && (name.indexOf("*") != name.length() - 1 || !name.endsWith(SEPARATOR + "*"))) {
			if (name.indexOf("*") == 0)
				Skript.error("[1.5] Local variables now start with an underscore: {_local variable} (variable: {" + name + "})");
			else
				Skript.error("A variable's name must not contain any asterisks except at the end after the separator '" + SEPARATOR + "' to denote a list variable: {variable" + SEPARATOR + "*} (variable: {" + name + "})");
			return null;
		} else if (name.contains(SEPARATOR + SEPARATOR)) {
			Skript.error("A variable's name must not contain the separator '" + SEPARATOR + "' multiple times in a row (variable: {" + name + "})");
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
		return "{" + StringUtils.substring(name.toString(e, debug), 1, -1) + "}" + (debug ? "(as " + superType.getName() + ")" : "");
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
		final Object val = Variables.getVariable(name.toString(e).toLowerCase());
		if (val == null)
			return Variables.getVariable(name.getDefaultVariableName().toLowerCase());
		return val;
	}
	
	@SuppressWarnings("unchecked")
	private Object get(final Event e) {
		if (local)
			return Variables.getLocalVariable(name.toString(e).toLowerCase(), e);
		final Object val = getRaw(e);
		if (!list)
			return val;
		if (val == null)
			return Array.newInstance(superType, 0);
		final List<Object> l = new ArrayList<Object>();
		for (final Entry<String, ?> v : ((Map<String, ?>) val).entrySet()) {
			if (v.getKey() != null && v.getValue() != null) {
				if (v.getValue() instanceof Map)
					l.add(((Map<String, ?>) v.getValue()).get(null));
				else
					l.add(v.getValue());
			}
		}
		return l.toArray();
	}
	
	public Iterator<Entry<String, Object>> variablesIterator(final Event e) {
		if (!list)
			throw new SkriptAPIException("");
		final Object val = getRaw(e);
		if (val == null)
			return new EmptyIterator<Entry<String, Object>>();
		@SuppressWarnings("unchecked")
		final Iterator<Entry<String, Object>> iter = ((Map<String, Object>) val).entrySet().iterator();
		return new Iterator<Entry<String, Object>>() {
			private Entry<String, Object> next = null;
			
			@Override
			public boolean hasNext() {
				if (next != null)
					return true;
				while (iter.hasNext()) {
					next = iter.next();
					if (next.getKey() != null && next.getValue() != null && !(next.getValue() instanceof Map))
						return true;
				}
				next = null;
				return false;
			}
			
			@Override
			public Entry<String, Object> next() {
				if (!hasNext())
					throw new NoSuchElementException();
				final Entry<String, Object> n = next;
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
		final Object val = getRaw(e);
		if (val == null)
			return new EmptyIterator<T>();
		@SuppressWarnings("unchecked")
		final Iterator<Entry<String, Object>> iter = ((Map<String, Object>) val).entrySet().iterator();
		return new Iterator<T>() {
			private T next = null;
			
			@Override
			public boolean hasNext() {
				if (next != null)
					return true;
				while (iter.hasNext()) {
					final Entry<String, Object> n = iter.next();
					if (n.getKey() != null && n.getValue() != null && !(n.getValue() instanceof Map)) {
						next = Converters.convert(n.getValue(), types);
						if (next != null)
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
				iter.remove();
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
			Variables.setLocalVariable(name.toString(e).toLowerCase(), e, value);
		else
			Variables.setVariable(name.toString(e).toLowerCase(), value, null);
	}
	
	private final void setIndex(final Event e, final String index, final Object value) {
		assert list;
		final String s = name.toString(e).toLowerCase();
		Variables.setVariable(s.substring(0, s.length() - 1) + index, value, null);
	}
	
	@SuppressWarnings("unchecked")
	@Override
	public Class<?>[] acceptChange(final ChangeMode mode) {
		if (list)
			return Utils.array(Object[].class);
		return Utils.array(Object.class);
	}
	
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
						setIndex(e, "" + i, d);
						i++;
					}
				} else {
					final ClassInfo<?> ci = delta == null ? null : Classes.getSuperClassInfo(delta.getClass());
					if (ci != null && ci.getSerializeAs() != null) {
						set(e, Converters.convert(delta, ci.getSerializeAs()));
					} else {
						set(e, delta);
					}
				}
				break;
			case ADD:
			case REMOVE:
				if (delta == null)
					break;
				if (list) {
					final Object[] ds = (Object[]) delta;
					@SuppressWarnings("unchecked")
					final Map<String, Object> o = (Map<String, Object>) getRaw(e);
					if (mode == ChangeMode.REMOVE) {
						if (o == null)
							return;
						for (final Entry<String, Object> i : o.entrySet()) {
							if (Relation.EQUAL.is(Comparators.compare(i.getValue(), delta))) {
								setIndex(e, i.getKey(), null);
								break;
							}
						}
					} else {
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
					if ((o == null || o instanceof Number) && delta instanceof Number) {
						final int i = mode == ChangeMode.ADD ? 1 : -1;
						set(e, (o == null ? 0 : ((Number) o).doubleValue()) + i * ((Number) delta).doubleValue());
					} else if (o != null) {
						final ClassInfo<?> ci = Classes.getSuperClassInfo(o.getClass());
						if (ci.getChanger() != null && ci.getChanger().acceptChange(mode) != null) {
							final Class<?>[] cs = ci.getChanger().acceptChange(mode);
							final Object[] one = (Object[]) Array.newInstance(o.getClass(), 1);
							one[0] = o;
							for (final Class<?> c : cs) {
								if (c.isAssignableFrom(delta.getClass())) {
									ChangerUtils.change(ci.getChanger(), one, delta, mode);
								} else if (c.isArray() && c.getComponentType().isAssignableFrom(delta.getClass())) {
									final Object[] deltas = (Object[]) Array.newInstance(c.getComponentType(), 1);
									deltas[0] = delta;
									ChangerUtils.change(ci.getChanger(), one, deltas, mode);
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
