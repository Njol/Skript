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

package ch.njol.skript.api.intern;

import org.bukkit.event.Event;

import ch.njol.skript.Skript;
import ch.njol.skript.api.Condition;
import ch.njol.skript.api.Converter;
import ch.njol.skript.lang.Literal;
import ch.njol.skript.lang.SimpleExpression;
import ch.njol.skript.lang.SimpleLiteral;
import ch.njol.skript.util.Utils;
import ch.njol.util.Checker;

/**
 * @author Peter Güttinger
 * @see SimpleLiteral
 */
public class ConvertedLiteral<F, T> extends ConvertedExpression<F, T> implements Literal<T> {
	
	private final T[] data;
	
	public ConvertedLiteral(final Literal<F> source, final T[] data, final Class<T> to) {
		super(source, to, null);
		this.data = data;
	}
	
	@Override
	public String getDebugMessage(final Event e) {
		return Skript.toString(data, getAnd());
	}
	
	@Override
	public String toString() {
		return Skript.toString(data, getAnd());
	}
	
	@Override
	public T[] getArray() {
		return data;
	}
	
	@Override
	public T[] getArray(final Event e) {
		return getArray();
	}
	
	@Override
	public <V> V[] getArray(final Event e, final Class<V> to, final Converter<? super T, ? extends V> converter) {
		return SimpleExpression.getArray(this, e, to, converter);
	}
	
	@Override
	public T getSingle() {
		if (getAnd() && data.length > 1)
			throw new SkriptAPIException("Call to getSingle on a non-single expression");
		return Utils.getRandom(data);
	}
	
	@Override
	public T getSingle(final Event e) {
		return getSingle();
	}
	
	@Override
	public <V> V getSingle(final Event e, final Converter<? super T, ? extends V> converter) {
		return converter.convert(getSingle());
	}
	
	@Override
	public boolean check(final Event e, final Checker<? super T> c) {
		return SimpleExpression.check(data, c, false, getAnd());
	}
	
	@Override
	public boolean check(final Event e, final Checker<? super T> c, final Condition cond) {
		return SimpleExpression.check(data, c, cond.isNegated(), getAnd());
	}
	
}
