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

import ch.njol.skript.api.Condition;
import ch.njol.skript.api.Converter;
import ch.njol.skript.lang.SimpleExpression;
import ch.njol.skript.lang.UnparsedLiteral;
import ch.njol.skript.util.Utils;
import ch.njol.skript.util.VariableString;
import ch.njol.util.Checker;

/**
 * @author Peter Güttinger
 * 
 */
public class VariableStringLiteral extends ConvertedLiteral<Object, String> {
	
	private final VariableString[] strings;
	private final String[] temp;
	
	private VariableStringLiteral(final UnparsedLiteral source, final VariableString[] strings) {
		super(source, null, String.class);
		this.strings = strings;
		temp = new String[strings.length];
	}
	
	public static VariableStringLiteral newInstance(final UnparsedLiteral source) {
		for (final String s : source.getData()) {
			if (!s.startsWith("\"") && !s.endsWith("\""))
				return null;
		}
		final VariableString[] strings = VariableString.makeStringsFromQuoted(source.getData());
		if (strings == null)
			return null;
		return new VariableStringLiteral(source, strings);
	}
	
	@Override
	public String getDebugMessage(final Event e) {
		return "[" + Utils.join(strings, e, getAnd()) + "]";
	}
	
	@Override
	public String[] getArray() {
		for (int i = 0; i < strings.length; i++) {
			if (!strings[i].isSimple())
				return null;
		}
		return ((UnparsedLiteral) source).getData();
	}
	
	@Override
	public String[] getArray(final Event e) {
		for (int i = 0; i < strings.length; i++) {
			temp[i] = strings[i].get(e);
		}
		return temp;
	}
	
	@Override
	public <V> V[] getArray(final Event e, final Class<V> to, final Converter<? super String, ? extends V> converter) {
		return SimpleExpression.getArray(this, e, to, converter);
	}
	
	@Override
	public String getSingle() {
		if (getAnd() && strings.length > 1)
			throw new SkriptAPIException("Call to getSingle on a non-single expression");
		return Utils.getRandom(((UnparsedLiteral) source).getData());
	}
	
	@Override
	public String getSingle(final Event e) {
		if (getAnd() && strings.length > 1)
			throw new SkriptAPIException("Call to getSingle on a non-single expression");
		return Utils.getRandom(strings).get(e);
	}
	
	@Override
	public <V> V getSingle(final Event e, final Converter<? super String, ? extends V> converter) {
		return converter.convert(getSingle(e));
	}
	
	@Override
	public boolean check(final Event e, final Checker<? super String> c) {
		return SimpleExpression.check(getArray(e), c, false, getAnd());
	}
	
	@Override
	public boolean check(final Event e, final Checker<? super String> c, final Condition cond) {
		return SimpleExpression.check(getArray(e), c, cond.isNegated(), getAnd());
	}
	
	@Override
	public String toString() {
		return "[" + Utils.join(strings, null, getAnd()) + "]";
	}
	
	@Override
	public Class<String> getReturnType() {
		return String.class;
	}
	
}
