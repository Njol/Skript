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

import java.util.ArrayList;
import java.util.Iterator;
import java.util.regex.Matcher;

import org.bukkit.event.Event;

import ch.njol.skript.Skript;
import ch.njol.skript.SkriptAPIException;
import ch.njol.skript.classes.Changer.ChangeMode;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.skript.lang.util.SimpleExpression;
import ch.njol.skript.lang.util.VariableString;
import ch.njol.skript.util.StringMode;
import ch.njol.skript.util.Utils;
import ch.njol.util.Checker;
import ch.njol.util.iterator.ArrayIterator;

/**
 * @author Peter Güttinger
 */
public class VariableStringLiteral implements Literal<String> {
	
	private final UnparsedLiteral source;
	private final VariableString[] strings;
	private final String[] temp;
	private final boolean and;
	
	private VariableStringLiteral(final VariableString[] strings, final boolean and, final UnparsedLiteral source) {
		this.and = and;
		this.strings = strings;
		temp = new String[strings.length];
		this.source = source;
	}
	
	public static VariableStringLiteral newInstance(final UnparsedLiteral source) {
		final String s = source.getData();
		if (!s.startsWith("\"") || !s.endsWith("\""))
			return null;
		
		final ArrayList<VariableString> strings = new ArrayList<VariableString>();
		int end = 0;
		int start = 0;
		boolean and = true;
		boolean isAndSet = false;
		while (true) {
			end = s.indexOf("\"", start + 1);
			if (end == -1)
				return null;
			while (end + 1 < s.length() && s.charAt(end + 1) == '"') {
				end = s.indexOf("\"", end + 2);
				if (end == -1)
					return null;
			}
			
			final VariableString vs = VariableString.newInstance(s.substring(start + 1, end).replace("\"\"", "\""));
			if (vs == null)
				return null;
			strings.add(vs);
			
			if (end == s.length() - 1)
				break;
			
			final Matcher m = UnparsedLiteral.literalSplitPattern.matcher(s).region(end + 1, s.length());
			if (!m.lookingAt())
				return null;
			start = m.end();
			if (start >= s.length() || s.charAt(start) != '"')
				return null;
			if (!m.group().matches("\\s*,\\s*")) {
				if (isAndSet) {
					if (and != m.group().toLowerCase().contains("and")) {
						Skript.warning("list has multiple 'and' or 'or', will default to 'and'");
						and = true;
					}
				} else {
					and = m.group().toLowerCase().contains("and");
					isAndSet = true;
				}
			}
		}
		
		if (!isAndSet && strings.size() > 1) {
			Skript.warning("list is missing 'and' or 'or', will default to 'and'");
		}
		return new VariableStringLiteral(strings.toArray(new VariableString[strings.size()]), and, source);
	}
	
	@Override
	public String toString(final Event e, final boolean debug) {
		final StringBuilder b = new StringBuilder("[");
		for (int i = 0; i < strings.length; i++) {
			if (i != 0) {
				if (i != strings.length - 1)
					b.append(", ");
				else
					b.append(getAnd() ? " and " : " or ");
			}
			b.append(strings[i].toString(e, debug));
		}
		b.append("]");
		return b.toString();
	}
	
	public void setMode(final StringMode mode) {
		for (final VariableString s : strings)
			s.setMode(mode);
	}
	
	@Override
	public String[] getAll() {
		throw new SkriptAPIException("Can't use string literals like normal literals");
	}
	
	@Override
	public String[] getAll(final Event e) {
		for (int i = 0; i < strings.length; i++) {
			temp[i] = strings[i].toString(e);
		}
		return temp;
	}
	
	@Override
	public String[] getArray() {
		throw new SkriptAPIException("Can't use string literals like normal literals");
	}
	
	@Override
	public String[] getArray(final Event e) {
		if (and)
			return getAll(e);
		return new String[] {Utils.getRandom(strings).toString(e)};
	}
	
	@Override
	public String getSingle() {
		throw new SkriptAPIException("Can't use string literals like normal literals");
	}
	
	@Override
	public String getSingle(final Event e) {
		if (getAnd() && strings.length > 1)
			throw new SkriptAPIException("Call to getSingle on a non-single expression");
		return Utils.getRandom(strings).toString(e);
	}
	
	@Override
	public boolean check(final Event e, final Checker<? super String> c) {
		return SimpleExpression.check(getAll(e), c, false, getAnd());
	}
	
	@Override
	public boolean check(final Event e, final Checker<? super String> c, final Condition cond) {
		return SimpleExpression.check(getAll(e), c, cond.isNegated(), getAnd());
	}
	
	@Override
	public Class<String> getReturnType() {
		return String.class;
	}
	
	@Override
	public boolean isSingle() {
		return !and || strings.length == 1;
	}
	
	@Override
	public void change(final Event e, final Object delta, final ChangeMode mode) throws UnsupportedOperationException {
		throw new UnsupportedOperationException();
	}
	
	@Override
	public Class<?> acceptChange(final ChangeMode mode) {
		return null;
	}
	
	@Override
	public boolean getAnd() {
		return and;
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
	public Iterator<String> iterator(final Event e) {
		return new ArrayIterator<String>(getArray(e));
	}
	
	@Override
	public boolean isLoopOf(final String s) {
		return false;
	}
	
	@Override
	public boolean init(final Expression<?>[] exprs, final int matchedPattern, final int isDelayed, final ParseResult parseResult) {
		throw new UnsupportedOperationException();
	}
	
	@Override
	public <R> Literal<? extends R> getConvertedExpression(final Class<R> to) {
		return null;
	}
	
	@Override
	public Expression<?> getSource() {
		return source;
	}
	
}
