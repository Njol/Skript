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
import java.util.Iterator;

import org.bukkit.event.Event;

import ch.njol.skript.Skript;
import ch.njol.skript.SkriptAPIException;
import ch.njol.skript.classes.Changer.ChangeMode;
import ch.njol.skript.classes.ClassInfo;
import ch.njol.skript.classes.Converter;
import ch.njol.skript.classes.Converter.ConverterUtils;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.skript.lang.util.SimpleExpression;
import ch.njol.skript.util.StringMode;
import ch.njol.skript.util.Utils;
import ch.njol.skript.util.VariableString;
import ch.njol.util.Checker;
import ch.njol.util.StringUtils;
import ch.njol.util.Validate;
import ch.njol.util.iterator.ArrayIterator;
import ch.njol.util.iterator.NonNullIterator;

/**
 * @author Peter Güttinger
 */
public class ArrayVariable<T> implements Expression<T> {
	
	private final VariableString name;
	
	private final boolean local;
	
	private final Class<T> type;
	
	private final ArrayVariable<?> source;
	
	private ArrayVariable(final VariableString name, final Class<T> type, final ArrayVariable<?> source) {
		Validate.notNull(name, type);
		if (name.getMode() != StringMode.VARIABLE_NAME) // not setMode as angle brackets are not allowed in variable names
			throw new IllegalArgumentException("'name' must be a VARIABLE_NAME string");
		
		local = name.getDefaultVariableName().startsWith("*");
		
		this.name = name;
		this.type = type;
		
		this.source = source;
	}
	
	public ArrayVariable(final VariableString name, final Class<T> type) {
		this(name, type, null);
	}
	
	@Override
	public boolean init(final Expression<?>[] exprs, final int matchedPattern, final boolean isDelayed, final ParseResult parseResult) {
		throw new UnsupportedOperationException();
	}
	
	@Override
	public boolean isSingle() {
		return false;
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
		return new ArrayVariable<R>(name, to, this);
	}
	
	private Object[] get(final Event e) {
		Object val = local ? Skript.getLocalVariable(name.toString(e).toLowerCase(), e) : Skript.getVariable(name.toString(e).toLowerCase());
		if (val == null && !local)
			val = Skript.getVariable(name.getDefaultVariableName().toLowerCase());
		return val instanceof Object[] ? (Object[]) val : null;
	}
	
	private final void set(final Event e, final Object[] value) {
		if (local)
			Skript.setLocalVariable(name.toString(e).toLowerCase(), e, value);
		else
			Skript.setVariable(name.toString(e).toLowerCase(), value);
	}
	
	@Override
	public Class<?> acceptChange(final ChangeMode mode) {
		if (mode == ChangeMode.SET)
			return Object[].class;
		return Object.class;
	}
	
	@Override
	public void change(final Event e, final Object delta, final ChangeMode mode) throws UnsupportedOperationException {
		switch (mode) {
			case CLEAR:
				set(e, null);
			break;
			case SET:
				final ClassInfo<?> ci = delta == null ? null : Skript.getSuperClassInfo(delta.getClass().getComponentType());
				if (ci != null && ci.getSerializeAs() != null) {
					final Object[] newDelta = (Object[]) Array.newInstance(ci.getSerializeAs(), ((Object[]) delta).length);
					for (int i = 0; i < newDelta.length; i++) {
						newDelta[i] = Skript.convert(((Object[]) delta)[i], ci.getSerializeAs());
					}
					set(e, newDelta);
				} else {
					set(e, (Object[]) delta);
				}
			break;
			case ADD:
			case REMOVE:
				if (delta == null)
					break;
				final Object o = get(e);
				if (!(o == null || o instanceof Object[]))
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
			break;
		}
	}
	
	@SuppressWarnings("unchecked")
	@Override
	public T[] getAll(final Event e) {
		Object[] val = get(e);
		if (val == null || type == Object.class)
			return (T[]) val;
		return ConverterUtils.convert(val, type, new Converter<Object, T>() {
			@Override
			public T convert(Object o) {
				return Skript.convert(o, type);
			}
		});
	}
	
	@Override
	public T[] getArray(final Event e) {
		return getAll(e);
	}
	
	@Override
	public Iterator<T> iterator(final Event e) {
		return new ArrayIterator<T>(getArray(e));
	}
	
	@Override
	public boolean check(final Event e, final Checker<? super T> c, final Condition cond) {
		return SimpleExpression.check(getAll(e), c, cond.isNegated(), getAnd());
	}
	
	@Override
	public boolean check(final Event e, final Checker<? super T> c) {
		return SimpleExpression.check(getAll(e), c, false, getAnd());
	}
	
	@Override
	public T getSingle(final Event e) {
		throw new SkriptAPIException("Call to getSingle on a non-single expression");
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
	public boolean canLoop() {
		return true;
	}
	
	@Override
	public boolean isLoopOf(final String s) {
		return s.equalsIgnoreCase("var") || s.equalsIgnoreCase("variable");
	}
	
	@Override
	public Expression<?> getSource() {
		return source == null ? this : source;
	}
	
}
