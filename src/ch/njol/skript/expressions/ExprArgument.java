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
import java.util.ArrayList;

import org.bukkit.event.Event;

import ch.njol.skript.Skript;
import ch.njol.skript.Skript.ExpressionType;
import ch.njol.skript.api.intern.ConvertedExpression;
import ch.njol.skript.command.Argument;
import ch.njol.skript.command.Commands;
import ch.njol.skript.command.SkriptCommandEvent;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.SimpleExpression;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.util.StringUtils;

/**
 * 
 * @author Peter Güttinger
 * 
 */
public class ExprArgument extends SimpleExpression<Object> {
	
	static {
		Skript.registerExpression(ExprArgument.class, Object.class, ExpressionType.SIMPLE,
				"[the] last arg[ument][s]",
				"[the] arg[ument][s](-| )<(\\d+)>", "[the] <(\\d*1)st|(\\d*2)nd|(\\d*3)rd|(\\d*[4-90])th> arg[ument][s]",
				"[the] arg[ument][s]",
				"[the] <.+>( |-)arg[ument]", "[the] arg[ument]( |-)<.+>");
	}
	
	private Argument<?> arg;
	
	@Override
	public boolean init(final Expression<?>[] vars, final int matchedPattern, final ParseResult parser) {
		if (Commands.currentArguments == null) {
			Skript.error("the expression 'argument' can only be used within a command");
			return false;
		}
		if (Commands.currentArguments.size() == 0) {
			Skript.error("the command doesn't allow any arguments");
			return false;
		}
		switch (matchedPattern) {
			case 0:
				arg = Commands.currentArguments.get(Commands.currentArguments.size() - 1);
			break;
			case 1:
			case 2:
				int i = Integer.parseInt(parser.regexes.get(0).group(1));
				if (i > Commands.currentArguments.size()) {
					Skript.error("the command doesn't have a " + StringUtils.fancyOrderNumber(i) + " argument");
					return false;
				}
				arg = Commands.currentArguments.get(i - 1);
			break;
			case 3:
				if (Commands.currentArguments.size() == 1) {
					arg = Commands.currentArguments.get(0);
				} else {
					Skript.error("'argument(s)' cannot be used if the command has multiple arguments");
					return false;
				}
			break;
			case 4:
			case 5:
				final Class<?> c = Skript.getClassFromUserInput(parser.regexes.get(0).group());
				if (c == null)
					return false;
				for (final Argument<?> a : Commands.currentArguments) {
					if (!c.isAssignableFrom(a.getType()))
						continue;
					if (arg != null) {
						Skript.error("There are multiple " + Skript.getExactClassName(c) + " arguments in this command");
						return false;
					}
					arg = a;
				}
				if (arg == null) {
					Skript.error("There is no " + Skript.getExactClassName(c) + " argument in this command");
					return false;
				}
		}
		assert arg != null;
		return true;
	}
	
	@Override
	protected Object[] get(final Event e) {
		if (!(e instanceof SkriptCommandEvent))
			return null;
		return arg.getCurrent();
	}
	
	@Override
	public Class<? extends Object> getReturnType() {
		return arg.getType();
	}
	
	@Override
	public String toString(final Event e, final boolean debug) {
		if (e == null)
			return "the "+ StringUtils.fancyOrderNumber(arg.getIndex() + 1) + " argument";
		return Skript.getDebugMessage(getArray(e));
	}
	
	@Override
	public boolean isSingle() {
		return arg.isSingle();
	}
	
	@Override
	public boolean canLoop() {
		return !arg.isSingle();
	}
	
	@Override
	public boolean isLoopOf(final String s) {
		return s.equalsIgnoreCase("argument");
	}
	
	@Override
	public boolean getAnd() {
		return true;
	}
	
}
