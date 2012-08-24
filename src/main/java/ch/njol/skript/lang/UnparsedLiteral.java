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
import java.util.Deque;
import java.util.LinkedList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.bukkit.event.Event;

import ch.njol.skript.Skript;
import ch.njol.skript.SkriptAPIException;
import ch.njol.skript.SkriptLogger;
import ch.njol.skript.SkriptLogger.SubLog;
import ch.njol.skript.classes.Changer.ChangeMode;
import ch.njol.skript.classes.ClassInfo;
import ch.njol.skript.classes.Parser;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.skript.lang.util.SimpleLiteral;
import ch.njol.skript.util.Utils;
import ch.njol.util.Checker;
import ch.njol.util.Validate;
import ch.njol.util.iterator.NonNullIterator;

/**
 * A literal which has yet to be parsed. This is returned if %object(s)% is used within patterns and no expression matches.
 * 
 * @author Peter Güttinger
 * @see SimpleLiteral
 */
public class UnparsedLiteral implements Literal<Object> {
	
	private final String data;
	
	/**
	 * @param data non-null, non-empty & trimmed string
	 * @param and
	 */
	public UnparsedLiteral(final String data) {
		Validate.notNullOrEmpty(data, "data");
		this.data = data;
	}
	
	public String getData() {
		return data;
	}
	
	@Override
	public Class<? extends Object> getReturnType() {
		return Object.class;
	}
	
	@Override
	public <R> Literal<? extends R> getConvertedExpression(final Class<R> to) {
		return getConvertedExpression(to, ParseContext.DEFAULT);
	}
	
	@SuppressWarnings("unchecked")
	public <R> Literal<? extends R> getConvertedExpression(final Class<R> to, final ParseContext context) {
		if (to == String.class && context == ParseContext.DEFAULT) {
			return (Literal<? extends R>) VariableStringLiteral.newInstance(this);
		} else if (to == Object.class) {
			if (context == ParseContext.DEFAULT) {
				final VariableStringLiteral vsl = VariableStringLiteral.newInstance(this);
				if (vsl != null)
					return (Literal<? extends R>) vsl;
			}
			final SubLog log = SkriptLogger.startSubLog();
			for (final ClassInfo<?> ci : Skript.getClassInfos()) {
				if (ci.getParser() != null) {
					log.clear();
					final Literal<?> l = convert(ci.getClass(), ci.getParser(), context);
					if (l != null) {
						log.stop();
						log.printLog();
						return (Literal<? extends R>) l;
					}
				}
			}
			log.stop();
			return null;
		}
		final Parser<? extends R> p = Skript.getParser(to);
		if (p == null)
			return null;
		return convert(to, p, context);
	}
	
	public final static Pattern literalSplitPattern = Pattern.compile("\\s*,?\\s+(and|n?or)\\s+|\\s*,\\s*", Pattern.CASE_INSENSITIVE);
	
	private <T> Literal<T> convert(final Class<T> to, final Parser<?> parser, final ParseContext context) {
		final SubLog log = SkriptLogger.startSubLog();
		
		String last = data, lastError = null;
		
		final T r = (T) parser.parse(data, context);
		if (r != null) {
			log.stop();
			log.printLog();
			return new SimpleLiteral<T>(r, false);
		}
		lastError = log.getLastError();
		log.clear();
		
		T t = null;
		final Deque<T> ts = new LinkedList<T>();
		final Matcher m = literalSplitPattern.matcher(data);
		int end = data.length();
		int expectedEnd = -1;
		boolean and = true;
		boolean isAndSet = false;
		while (m.find()) {
			if (expectedEnd == -1)
				expectedEnd = m.start();
			t = (T) parser.parse(last = data.substring(m.end(), end), context);
			lastError = log.getLastError();
			if (t != null) {
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
				ts.addFirst(t);
				log.clear();
				end = m.start();
				m.region(0, end);
			} else {
				log.clear();
			}
		}
		if (end == expectedEnd) {
			t = (T) parser.parse(last = data.substring(0, end), context);
			lastError = log.getLastError();
			log.stop();
			if (t != null) {
				ts.addFirst(t);
				return new SimpleLiteral<T>(ts.toArray((T[]) Array.newInstance(to, ts.size())), to, and, this);
			}
		}
		log.stop();
		Skript.error(lastError != null ? lastError : "'" + last + "' is not " + Utils.a(Skript.getSuperClassInfo(to).getName()));
		return null;
	}
	
	@Override
	public String toString(final Event e, final boolean debug) {
		return "'" + data + "'";
	}
	
	@Override
	public String toString() {
		return toString(null, false);
	}
	
	@Override
	public Expression<?> getSource() {
		return this;
	}
	
	private final static SkriptAPIException invalidAccessException() {
		return new SkriptAPIException("UnparsedLiterals must be converted before use");
	}
	
	@Override
	public boolean getAnd() {
		throw invalidAccessException();
	}
	
	@Override
	public boolean isSingle() {
		throw invalidAccessException();
	}
	
	@Override
	public Object[] getAll() {
		throw invalidAccessException();
	}
	
	@Override
	public Object[] getAll(final Event e) {
		throw invalidAccessException();
	}
	
	@Override
	public Object[] getArray() {
		throw invalidAccessException();
	}
	
	@Override
	public Object[] getArray(final Event e) {
		throw invalidAccessException();
	}
	
	@Override
	public Object getSingle() {
		throw invalidAccessException();
	}
	
	@Override
	public Object getSingle(final Event e) {
		throw invalidAccessException();
	}
	
	@Override
	public boolean canLoop() {
		throw invalidAccessException();
	}
	
	@Override
	public NonNullIterator<Object> iterator(final Event e) {
		throw invalidAccessException();
	}
	
	@Override
	public void change(final Event e, final Object delta, final ChangeMode mode) throws UnsupportedOperationException {
		throw invalidAccessException();
	}
	
	@Override
	public Class<?> acceptChange(final ChangeMode mode) {
		throw invalidAccessException();
	}
	
	@Override
	public boolean check(final Event e, final Checker<? super Object> c) {
		throw invalidAccessException();
	}
	
	@Override
	public boolean check(final Event e, final Checker<? super Object> c, final Condition cond) {
		throw invalidAccessException();
	}
	
	@Override
	public boolean setTime(final int time) {
		throw invalidAccessException();
	}
	
	@Override
	public int getTime() {
		throw invalidAccessException();
	}
	
	@Override
	public boolean isDefault() {
		throw invalidAccessException();
	}
	
	@Override
	public boolean isLoopOf(final String s) {
		throw invalidAccessException();
	}
	
	@Override
	public boolean init(final Expression<?>[] exprs, final int matchedPattern, final int isDelayed, final ParseResult parseResult) {
		throw invalidAccessException();
	}
	
}
