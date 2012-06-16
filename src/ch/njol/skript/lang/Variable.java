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

import org.bukkit.event.Event;

import ch.njol.skript.Skript;
import ch.njol.skript.api.Changer.ChangeMode;
import ch.njol.skript.api.Condition;
import ch.njol.skript.api.Converter;
import ch.njol.skript.classes.ClassInfo;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.skript.util.VariableString;
import ch.njol.util.Checker;
import ch.njol.util.StringUtils;
import ch.njol.util.Validate;

/**
 * @author Peter Güttinger
 * 
 */
public class Variable<T> implements Expression<T> {
	
	private final VariableString name;
	private final Class<T> type;
	private final T[] zero, one;
	
	@SuppressWarnings("unchecked")
	public Variable(final VariableString name, final Class<T> type) {
		Validate.notNull(name, type);
		this.name = name;
		this.type = type;
		zero = (T[]) Array.newInstance(type, 0);
		one = (T[]) Array.newInstance(type, 1);
	}
	
	@Override
	public boolean init(final Expression<?>[] exprs, final int matchedPattern, final ParseResult parseResult) {
		throw new UnsupportedOperationException();
	}
	
	@Override
	public boolean isSingle() {
		return true;
	}
	
	@Override
	public Class<? extends T> getReturnType() {
		return type;
	}
	
	@Override
	public String getDebugMessage(final Event e) {
		return "{" + StringUtils.substring(name.getDebugMessage(e), 1, -1) + "}->" + type.getName();
	}
	
	@Override
	public <R> Expression<? extends R> getConvertedExpression(Class<R> to) {
		return new Variable<R>(name, to);
	}
	
	@SuppressWarnings("unchecked")
	protected T get(final Event e) {
		return type == Object.class ? (T) (Skript.variables.get(name.get(e))) : Skript.convert(Skript.variables.get(name.get(e)), type);
	}
	
	@Override
	public String toString() {
		return "{" + StringUtils.substring(name.toString(), 1, -1) + "}";
	}
	
	@Override
	public Class<?> acceptChange(final ChangeMode mode) {
		if (mode == ChangeMode.SET || mode == ChangeMode.CLEAR)
			return type;
		return null;
	}
	
	@Override
	public void change(final Event e, final Object delta, final ChangeMode mode) throws UnsupportedOperationException {
		if (mode == ChangeMode.CLEAR) {
			Skript.variables.remove(name.get(e));
		} else if (mode == ChangeMode.SET) {
			ClassInfo<?> ci = Skript.getSuperClassInfo(delta.getClass());
			if (ci != null && ci.getSerializeAs() != null) {
				Skript.variables.put(name.get(e), Skript.convert(delta, ci.getSerializeAs()));
			} else {
				Skript.variables.put(name.get(e), delta);
			}
		}
	}

	@Override
	public T getSingle(Event e) {
		return get(e);
	}

	@Override
	public T[] getArray(Event e) {
		one[0] = get(e);
		if (one[0] == null)
			return zero;
		return one;
	}

	@Override
	public <V> V getSingle(Event e, Converter<? super T, ? extends V> converter) {
		T t = get(e);
		if (t == null)
			return null;
		return converter.convert(t);
	}

	@Override
	public <V> V[] getArray(Event e, Class<V> to, Converter<? super T, ? extends V> converter) {
		return SimpleExpression.getArray(this, e, to, converter);
	}

	@Override
	public boolean check(Event e, Checker<? super T> c, Condition cond) {
		return SimpleExpression.check(getArray(e), c, cond.isNegated(), getAnd());
	}

	@Override
	public boolean check(Event e, Checker<? super T> c) {
		return SimpleExpression.check(getArray(e), c, false, getAnd());
	}

	@Override
	public void setAnd(boolean and) {}

	@Override
	public boolean getAnd() {
		return false;
	}

	@Override
	public boolean setTime(int time) {
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
	
}
