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

import java.util.ArrayList;
import java.util.Iterator;
import java.util.logging.Level;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import ch.njol.skript.Skript;
import ch.njol.skript.SkriptLogger;
import ch.njol.skript.api.Debuggable;
import ch.njol.skript.api.SkriptEvent;
import ch.njol.skript.api.SkriptEvent.SkriptEventInfo;
import ch.njol.skript.api.exception.InitException;
import ch.njol.skript.api.exception.ParseException;
import ch.njol.util.Callback;
import ch.njol.util.Pair;
import ch.njol.util.StringUtils;

/**
 * Represents a general part of the syntax. Implementing classes are {@link Variable} and {@link TopLevelExpression}.
 * 
 * @author Peter Güttinger
 * 
 */
public interface Expression {
	
	public static abstract class ExpressionInfo {
		
		public final Class<? extends Expression> c;
		
		public final Pattern[] patterns;
		public final Integer[][] groups;// for Java 6 compatibility only
		public final String[][] groupNames;
		public final Boolean[][] noDefault;
		
		public ExpressionInfo(final String[] patterns, final Class<? extends Expression> c) {
			this.patterns = new Pattern[patterns.length];
			groups = new Integer[patterns.length][];
			groupNames = new String[patterns.length][];
			noDefault = new Boolean[patterns.length][];
			for (int i = 0; i < patterns.length; i++) {
				final ArrayList<Integer> groups = new ArrayList<Integer>();
				final ArrayList<String> groupNames = new ArrayList<String>();
				final ArrayList<Boolean> noDefault = new ArrayList<Boolean>();
				final String pattern = patterns[i];
				this.patterns[i] = Pattern.compile("^" + StringUtils.replaceAll(patterns[i], "%(.+?)%", new Callback<String, Matcher>() {
					private int g = 0;
					private int prevEnd = 0;
					
					@Override
					public String run(final Matcher m) {
						String s = m.group(1);
						noDefault.add(Boolean.valueOf(s.startsWith("-")));
						if (s.startsWith("-")) {
							s = s.substring(1);
						}
						g += numGroups(pattern, prevEnd, m.start() - 1) + 1;
						prevEnd = m.end();
						groups.add(Integer.valueOf(g));
						groupNames.add(s);
						return "(" + Expressions.wildcard + ")";
					}
				}) + "$", Pattern.CASE_INSENSITIVE | Pattern.UNICODE_CASE);
				this.groups[i] = groups.toArray(new Integer[0]);
				this.groupNames[i] = groupNames.toArray(new String[0]);
				this.noDefault[i] = noDefault.toArray(new Boolean[0]);
			}
			this.c = c;
		}
		
		private final static int numGroups(final String s, final int start, final int end) {
			int r = 0;
			for (int i = start; i <= end; i++) {
				if (s.charAt(i) == '(' && s.charAt(i + 1) != '?')
					r++;
			}
			return r;
		}
	}
	
	public static class VariableInfo<T> extends ExpressionInfo {
		
		public Class<T> returnType;
		
		public VariableInfo(final String[] patterns, final Class<T> returnType, final Class<? extends Expression> c) {
			super(patterns, c);
			this.returnType = returnType;
		}
	}
	
	public static final class Expressions {
		
		/**
		 * This wildcard will prevent splitting of quoted parts of a string
		 */
		public final static String wildcard = "[^\"]*?(?:\"[^\"]*?\"[^\"]*?)*?";
		public final static String stringMatcher = "\"[^\"]*?(?:\"\"[^\"]*)*?\"";
		public final static String nonStringMatcher = "[^\"]*?(?:\"\"[^\"]*)*?";
		
		public static final Expression parse(final String s, final Iterator<? extends ExpressionInfo> source) {
			while (source.hasNext()) {
				try {
					final Expression e = parse_i(s, source.next());
					if (e != null) {
						Skript.clearErrorCause();
						return e;
					}
				} catch (final ParseException e) {
					if (e.getError() != null)
						Skript.setErrorCause(e.getError(), true);
					return null;
				}
			}
			// no error message here - could still be a literal
			return null;
		}
		
		private final static Expression parse_i(final String s, final ExpressionInfo info) throws ParseException {
			for (int i = 0; i < info.patterns.length; i++) {
				final Matcher m = info.patterns[i].matcher(s);
				if (!m.matches())
					continue;
				ArrayList<Variable<?>> parts = new ArrayList<Variable<?>>(info.groups.length);
				for (int j = 0; j < info.groups[i].length; j++) {
					final String t = info.groupNames[i][j];
					final int index = info.groups[i][j];
					final String g = m.group(index);
					if (g == null) {
						try {
							if (info.noDefault[i][j]) {
								parts.add(null);
							} else {
								final Class<? extends Variable<?>> dv = Skript.getDefaultVariable(t);
								parts.add(dv == null ? null : dv.newInstance());
							}
						} catch (final InstantiationException e) {
							SkriptAPIException.instantiationException("the default variable", info.c, e);
						} catch (final IllegalAccessException e) {
							SkriptAPIException.inaccessibleConstructor(info.c, e);
						}
					} else {
						final Variable<?> p = Variable.parse(g.trim(), Skript.getClass(t));
						if (p == null) {
							parts = null;
							//some patterns match almost everything, e.g. location's "%offset% %location%".
							//thus a matching pattern doesn't mean that the input only matches the variable and nothing else.
							break;
						}
						parts.add(p);
					}
				}
				if (parts == null)
					continue;
				try {
					final Expression p = info.c.newInstance();
					try {
						p.init(parts.toArray(new Variable<?>[0]), i, m);
					} catch (final InitException e) {
						Skript.clearErrorCause();
						continue;
					} catch (final ParseException e) {
						if (Skript.logVeryHigh() && p instanceof Debuggable)
							SkriptLogger.logDirect(Level.SEVERE, "[invalid] " + ((Debuggable) p).getDebugMessage(null));
						throw e;
					}
					
					return p;
				} catch (final InstantiationException e) {
					SkriptAPIException.instantiationException("the variable", info.c, e);
				} catch (final IllegalAccessException e) {
					SkriptAPIException.inaccessibleConstructor(info.c, e);
				}
			}
			return null;
		}
		
		public static final Pair<SkriptEventInfo, SkriptEvent> parseEvent(String s) {
			s = s.replaceFirst("^on ", "");
			for (final SkriptEventInfo info : Skript.getEvents()) {
				for (int i = 0; i < info.patterns.length; i++) {
					final Matcher m = info.patterns[i].matcher(s);
					if (m.matches()) {
						final Object[][] parts = new Object[info.groups[i].length][];
						for (int j = 0; j < info.groups[i].length; j++) {
							final String t = info.groupNames[i][j];
							final int index = info.groups[i][j];
							final String g = m.group(index);
							if (g == null) {
								parts[j] = null;
							} else {
								final Variable<?> l = parseLiteral(g.trim(), Skript.getClass(t));
								if (l == null)
									return null;
								parts[j] = l.getAll(null);
							}
						}
						try {
							final SkriptEvent e = info.c.newInstance();
							e.init(parts, i, m);
							return new Pair<SkriptEventInfo, SkriptEvent>(info, e);
						} catch (final InstantiationException e) {
							SkriptAPIException.instantiationException("the skript event", info.c, e);
						} catch (final IllegalAccessException e) {
							SkriptAPIException.inaccessibleConstructor(info.c, e);
						}
					}
				}
			}
			return null;
		}
		
		public final static UnparsedLiteral parseUnparsedLiteral(final String s) {
			final ArrayList<String> parts = new ArrayList<String>();
			final Pattern p = Pattern.compile("^(" + Expressions.wildcard + ")(,\\s*|,?\\s+and\\s+|,?\\s+or\\s+)");
			final Matcher m = p.matcher(s);
			int prevEnd = 0;
			boolean and = true;
			boolean isAndSet = false;
			while (m.find()) {
				if (!m.group(2).matches(",\\s*")) {
					if (isAndSet) {
						Skript.addWarning("list has multiple 'and' or 'or', will default to 'and': " + s);
						and = true;
					} else {
						and = m.group(2).contains("and");
						isAndSet = true;
					}
				}
				parts.add(m.group(1).trim());
				prevEnd = m.end();
				m.region(m.end(), s.length());
			}
			if (!isAndSet && !parts.isEmpty()) {
				Skript.addWarning("list is missing 'and' or 'or', will default to 'and': " + s);
			}
			parts.add(s.substring(prevEnd).trim());
			return new UnparsedLiteral(parts.toArray(new String[0]), and);
		}
		
		public final static <T> Variable<? extends T> parseLiteral(final String s, final Class<T> c) {
			if (c == Object.class)
				throw new SkriptAPIException("parseLiteral(String, Class) must be called with the exact class to parse");
			final UnparsedLiteral l = parseUnparsedLiteral(s);
			if (l == null)
				return null;
			return l.getConvertedVar(c);
		}
		
		public final static <T> T[] parseData(final String s, final Class<T> c) {
			final Variable<? extends T> l = parseLiteral(s, c);
			if (l == null)
				return null;
			return l.getAll(null);
		}
		
	}
	
	/**
	 * called just after the constructor.
	 * 
	 * @param vars all %var%s included in the matching pattern in the order they appear in the pattern. If an optional value was left out it will still be included in this list
	 *            holding the default value of the desired type which usually depends on the event.
	 * @param matchedPattern the index of the pattern which matched
	 * @param matcher all info about the match. Note that any (Nth) %variable% is converted to (?&lt;variableN&gt;.*?) and is thus a group which has an index.
	 * 
	 * @throws InitException throwing this has the same effect as if no pattern matched. This is an exception, meaning it should only be thrown in exceptional cases where a regex
	 *             is not enough.
	 * @throws ParseException throw this if some part of the expression was parsed and found to be invalid, but the whole expression still matched correctly.<br/>
	 *             This will immediately print an error and it's cause which is set to the cause passed to the exception.
	 */
	public void init(Variable<?>[] vars, int matchedPattern, Matcher matcher) throws InitException, ParseException;
	
}
