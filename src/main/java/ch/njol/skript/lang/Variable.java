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

package ch.njol.skript.lang;

import java.lang.reflect.Array;
import java.util.Arrays;

import org.bukkit.event.Event;

import ch.njol.skript.Skript;
import ch.njol.skript.classes.Changer.ChangeMode;
import ch.njol.skript.classes.Changer.ChangerUtils;
import ch.njol.skript.classes.ClassInfo;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.skript.lang.util.SimpleExpression;
import ch.njol.skript.util.StringMode;
import ch.njol.skript.util.Utils;
import ch.njol.skript.util.VariableString;
import ch.njol.util.Checker;
import ch.njol.util.StringUtils;
import ch.njol.util.Validate;
import ch.njol.util.iterator.NonNullIterator;

/**
 * @author Peter Güttinger
 * 
 */
public class Variable<T> implements Expression<T> {
	
	private final VariableString name;
	
	private final boolean local;
	
	private final boolean isArray = false;
	
	private final Class<T> type;
	private final T[] zero, one;
	
	private final Variable<?> source;
	
	private Variable(final VariableString name, final Class<T> type, final Variable<?> source) {
		Validate.notNull(name, type);
		if (name.getMode() != StringMode.VARIABLE_NAME) // not setMode as angle brackets are not allowed in variable names
			throw new IllegalArgumentException("'name' must be a VARIABLE_NAME string");
		local = name.toString(null, false).startsWith("*");
		this.name = name;
		this.type = type;
		zero = (T[]) Array.newInstance(type, 0);
		one = (T[]) Array.newInstance(type, 1);
		
		this.source = source;
	}
	
	public Variable(final VariableString name, final Class<T> type) {
		this(name, type, null);
	}
	
	@Override
	public boolean init(final Expression<?>[] exprs, final int matchedPattern, final boolean isDelayed, final ParseResult parseResult) {
		throw new UnsupportedOperationException();
	}
	
	@Override
	public boolean isSingle() {
		return !isArray;
	}
	
	@Override
	public Class<? extends T> getReturnType() {
		return type;
	}
	
	@Override
	public String toString(final Event e, final boolean debug) {
		if (e != null)
			return Skript.toString(get(e));
		return "{" + StringUtils.substring(name.toString(e, debug), 1, -1) + "}" + (debug ? "(as " + type.getName() + ")" : "");
	}
	
	@Override
	public String toString() {
		return toString(null, false);
	}
	
	@Override
	public <R> Expression<? extends R> getConvertedExpression(final Class<R> to) {
		return new Variable<R>(name, to, this);
	}
	
	protected Object get(final Event e) {
		Object val = local ? Skript.getLocalVariable(name.toString(e).toLowerCase(), e) : Skript.getVariable(name.toString(e).toLowerCase());
		if (val == null && !local)
			val = Skript.getVariable(name.getDefaultVariableName().toLowerCase());
		return val;
	}
	
	private final void set(final Event e, final Object value) {
		if (local)
			Skript.setLocalVariable(name.toString(e).toLowerCase(), e, value);
		else
			Skript.setVariable(name.toString(e).toLowerCase(), value);
	}
	
	@Override
	public Class<?> acceptChange(final ChangeMode mode) {
		if (isArray) {
			if (mode == ChangeMode.SET)
				return Object[].class;
			return Object.class;
		}
		return Object.class;
	}
	
	@Override
	public void change(final Event e, final Object delta, final ChangeMode mode) throws UnsupportedOperationException {
		switch (mode) {
			case CLEAR:
				set(e, null);
			break;
			case SET:
				if (isArray) {
					final ClassInfo<?> ci = delta == null ? null : Skript.getSuperClassInfo(delta.getClass().getComponentType());
					if (ci != null && ci.getSerializeAs() != null) {
						final Object[] newDelta = (Object[]) Array.newInstance(ci.getSerializeAs(), ((Object[]) delta).length);
						for (int i = 0; i < newDelta.length; i++) {
							newDelta[i] = Skript.convert(((Object[]) delta)[i], ci.getSerializeAs());
						}
						set(e, newDelta);
					} else {
						set(e, delta);
					}
				} else {
					final ClassInfo<?> ci = delta == null ? null : Skript.getSuperClassInfo(delta.getClass());
					if (ci != null && ci.getSerializeAs() != null) {
						set(e, Skript.convert(delta, ci.getSerializeAs()));
					} else {
						set(e, delta);
					}
				}
			break;
			case ADD:
			case REMOVE:
				if (delta == null)
					break;
				if (isArray) {
					final Object o = get(e);
					if (!(o instanceof Object[]))
						return;
					Object[] os = (Object[]) o;
					if (mode == ChangeMode.ADD) {
						final int newLength = os == null ? 1 : os.length + 1;
						os = os == null ? new Object[1] : Arrays.copyOf(os, newLength);
						os[os.length - 1] = delta;
						set(e, os);
					} else {
						if (os == null)
							return;
						final int i = Utils.indexOf(os, delta);
						if (i == -1)
							return;
						if (os.length == 1) {
							set(e, null);
							return;
						}
						final Object[] newOs = new Object[os.length - 1];
						System.arraycopy(os, 0, newOs, 0, i);
						if (i != os.length - 1)
							System.arraycopy(os, i + 1, newOs, i, os.length - 1 - i);
						set(e, newOs);
					}
				} else {
					final Object o = get(e);
					if ((o == null || o instanceof Number) && delta instanceof Number) {
						final int i = mode == ChangeMode.ADD ? 1 : -1;
						set(e, (o == null ? 0 : ((Number) o).doubleValue()) + i * ((Number) delta).doubleValue());
					} else if (o != null) {
						final ClassInfo<?> ci = Skript.getSuperClassInfo(o.getClass());
						if (ci.getChanger() != null && ci.getChanger().acceptChange(mode) != null) {
							final Class<?> c = ci.getChanger().acceptChange(mode);
							final Object[] one = (Object[]) Array.newInstance(o.getClass(), 1);
							one[0] = o;
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
			break;
		}
	}
	
	@Override
	public T getSingle(final Event e) {
		return Skript.convert(get(e), type);
	}
	
	@Override
	public T[] getArray(final Event e) {
		return getAll(e);
	}
	
	@Override
	public T[] getAll(final Event e) {
		one[0] = Skript.convert(get(e), type);
		if (one[0] == null)
			return zero;
		return one;
	}
	
	@Override
	public boolean check(final Event e, final Checker<? super T> c, final Condition cond) {
		return SimpleExpression.check(getArray(e), c, cond.isNegated(), getAnd());
	}
	
	@Override
	public boolean check(final Event e, final Checker<? super T> c) {
		return SimpleExpression.check(getArray(e), c, false, getAnd());
	}
	
	@Override
	public boolean getAnd() {
		return false;
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
	public boolean canLoop() {
		return false;
	}
	
	@Override
	public NonNullIterator<T> iterator(final Event e) {
		return null;
	}
	
	@Override
	public boolean isLoopOf(final String s) {
		return false;
	}
	
	@Override
	public Expression<?> getSource() {
		return source == null ? this : source;
	}
	
}
