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

package ch.njol.skript.expressions;

import java.lang.reflect.Array;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.bukkit.event.Event;

import ch.njol.skript.ScriptLoader;
import ch.njol.skript.Skript;
import ch.njol.skript.Skript.ExpressionType;
import ch.njol.skript.api.intern.Loop;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.SimpleExpression;
import ch.njol.skript.lang.SkriptParser.ParseResult;

/**
 * used to access a loop's current value.
 * 
 * @author Peter Güttinger
 * 
 */
public class ExprLoopValue extends SimpleExpression<Object> {
	
	static {
		Skript.registerExpression(ExprLoopValue.class, Object.class, ExpressionType.SIMPLE, "[the] loop-<.+>");
	}
	
	private String name;
	
	private Loop loop;
	
	private Object[] one;
	
	@Override
	public boolean init(final Expression<?>[] vars, final int matchedPattern, final ParseResult parser) {
		name = parser.expr;
		String s = parser.regexes.get(0).group();
		int i = -1;
		final Matcher m = Pattern.compile("^(.+)-(\\d+)$").matcher(s);
		if (m.matches()) {
			s = m.group(1);
			i = Integer.parseInt(m.group(2));
		}
		Class<?> c = Skript.getClassByName(s);
		if (c == null)
			c = Skript.getClassFromUserInput(s);
		for (final Loop l : ScriptLoader.currentLoops) {
			if (c != null && c.isAssignableFrom(l.getLoopedExpression().getReturnType()) || l.getLoopedExpression().isLoopOf(s)) {
				if (i > 1) {
					i--;
					continue;
				}
				if (loop != null) {
					Skript.error("there are multiple loops that match 'loop-" + s + "'");
					return false;
				}
				loop = l;
				if (i == 1)
					break;
			}
		}
		if (loop == null) {
			Skript.error("there's no loop that matches 'loop-" + s + "'");
			return false;
		}
		one = (Object[]) Array.newInstance(loop.getLoopedExpression().getReturnType(), 1);
		return true;
	}
	
	@Override
	public boolean isSingle() {
		return true;
	}
	
	@Override
	public Class<? extends Object> getReturnType() {
		return loop.getLoopedExpression().getReturnType();
	}
	
	@Override
	protected Object[] get(final Event e) {
		one[0] = loop.getCurrent();
		return one;
	}
	
	@Override
	public String toString(final Event e, final boolean debug) {
		if (e == null)
			return name;
		return Skript.getDebugMessage(loop.getCurrent());
	}
	
	@Override
	public boolean getAnd() {
		return true;
	}
	
}
