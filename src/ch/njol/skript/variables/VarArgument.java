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

package ch.njol.skript.variables;

import java.util.ArrayList;

import org.bukkit.event.Event;

import ch.njol.skript.Skript;
import ch.njol.skript.api.exception.ParseException;
import ch.njol.skript.api.intern.ConvertedVariable;
import ch.njol.skript.command.Argument;
import ch.njol.skript.command.Commands;
import ch.njol.skript.command.SkriptCommandEvent;
import ch.njol.skript.lang.ExprParser.ParseResult;
import ch.njol.skript.lang.SimpleVariable;
import ch.njol.skript.lang.Variable;
import ch.njol.util.StringUtils;

/**
 * 
 * @author Peter Güttinger
 * 
 */
public class VarArgument extends SimpleVariable<Object> {
	
	static {
		Skript.addVariable(VarArgument.class, Object.class, "last arg[ument]", "arg[ument](-| )<(\\d+)>", "<(\\d*1)st|(\\d*2)nd|(\\d*3)rd|(\\d*[4-90])th> arg[ument]", "arg[ument]s");
	}
	
	private Class<?> type = Object.class;
	private Argument<?> arg;
	private int a = -1;
	
	@Override
	public void init(final Variable<?>[] vars, final int matchedPattern, final ParseResult parser) throws ParseException {
		if (Commands.currentArguments == null) {
			throw new ParseException("the variable 'argument' can only be used within a command");
		}
		if (Commands.currentArguments.size() == 0) {
			throw new ParseException("the command doesn't allow any arguments");
		}
		switch (matchedPattern) {
			case 0:
				a = Commands.currentArguments.size();
				arg = Commands.currentArguments.get(a - 1);
				type = arg.getType();
			break;
			case 1:
			case 2:
				a = Integer.parseInt(parser.regexes.get(0).group(1));
				if (Commands.currentArguments.size() <= a - 1) {
					throw new ParseException("the command doesn't have a " + StringUtils.fancyOrderNumber(a) + " argument");
				}
				arg = Commands.currentArguments.get(a - 1);
				type = arg.getType();
			break;
			case 3:
				if (Commands.currentArguments.size() == 1) {
					arg = Commands.currentArguments.get(0);
					type = arg.getType();
				} else {
					throw new ParseException("'arguments' cannot be used if the command has multiple arguments");
				}
		}
	}
	
	@Override
	protected Object[] getAll(final Event e) {
		if (!(e instanceof SkriptCommandEvent))
			return null;
		if (arg == null) {
			final ArrayList<Object> r = new ArrayList<Object>(((SkriptCommandEvent) e).getSkriptCommand().getArguments().size());
			for (final Argument<?> a : ((SkriptCommandEvent) e).getSkriptCommand().getArguments()) {
				for (final Object o : a.getCurrent())
					r.add(o);
			}
			return r.toArray();
		}
		return arg.getCurrent();
	}
	
	@Override
	public <R> ConvertedVariable<? extends R> getConvertedVar(final Class<R> to) {
		if (arg != null) {
			return super.getConvertedVar(to);
		}
		return null;
	}
	
	@Override
	public Class<? extends Object> getReturnType() {
		return type;
	}
	
	@Override
	public String getDebugMessage(final Event e) {
		if (e == null)
			return a == -1 ? "arguments" : StringUtils.fancyOrderNumber(a) + " argument";
		return Skript.getDebugMessage(getSingle(e));
	}
	
	@Override
	public String toString() {
		if (a == -1)
			return "the arguments";
		return "the " + StringUtils.fancyOrderNumber(a) + " argument";
	}
	
	@Override
	public boolean isSingle() {
		return arg.isSingle();
	}
	
}
