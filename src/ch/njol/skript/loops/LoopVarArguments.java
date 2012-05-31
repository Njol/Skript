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

package ch.njol.skript.loops;

import java.util.Iterator;

import org.bukkit.event.Event;

import ch.njol.skript.Skript;
import ch.njol.skript.api.LoopVar;
import ch.njol.skript.api.exception.ParseException;
import ch.njol.skript.api.intern.SkriptAPIException;
import ch.njol.skript.command.Argument;
import ch.njol.skript.command.Commands;
import ch.njol.skript.lang.ExprParser.ParseResult;
import ch.njol.skript.lang.Variable;
import ch.njol.skript.util.Container;
import ch.njol.skript.util.Container.ContainerType;
import ch.njol.util.StringUtils;
import ch.njol.util.iterator.ArrayIterator;

/**
 * @author Peter Güttinger
 * 
 */
public class LoopVarArguments extends LoopVar<Object> {
	
	static {
		Skript.registerLoop(LoopVarArguments.class, Object.class, "last argument", "argument(-| )<(\\d+)>", "<(?:(\\d*1)st|(\\d*2)nd|(\\d*3)rd|(\\d*[4-90])th)> argument", "arguments");
	}
	
	private Argument<?> arg;
	private Class<?> type;
	private boolean isContainer = false;
	
	@Override
	public void init(final Variable<?>[] vars, final int matchedPattern, final ParseResult parser) throws ParseException {
		if (Commands.currentArguments == null)
			throw new ParseException("you can't loop through any arguments outside of a command");
		switch (matchedPattern) {
			case 0:
				arg = Commands.currentArguments.get(Commands.currentArguments.size() - 1);
			break;
			case 1:
			case 2:
				final int a = Integer.parseInt(parser.regexes.get(0).group(1));
				if (a - 1 >= Commands.currentArguments.size())
					throw new ParseException("the command doesn't have a " + StringUtils.fancyOrderNumber(a) + " argument");
				arg = Commands.currentArguments.get(a - 1);
			break;
			case 3:
				if (Commands.currentArguments.size() != 1)
					throw new ParseException("it's not possible to loop through multiple arguments (yet)");
				arg = Commands.currentArguments.get(0);
		}
		type = arg.getType();
		if (Container.class.isAssignableFrom(type)) {
			if (type.getAnnotation(ContainerType.class) == null)
				throw new SkriptAPIException("Missing annotation @ContainerType in container " + type.getName());
			isContainer = true;
			type = type.getAnnotation(ContainerType.class).value();
		}
	}
	
	@Override
	protected Iterator<?> iterator(final Event e) {
		if (arg.getCurrent().length == 0)
			return null;
		if (!isContainer) {
			return new ArrayIterator<Object>(arg.getCurrent());
		}
		final Object[] os = arg.getCurrent();
		return new Iterator<Object>() {
			
			private int i = 0;
			
			private Iterator<?> current = ((Container<?>) os[i]).containerIterator();
			
			@Override
			public boolean hasNext() {
				while (i < os.length && !current.hasNext())
					current = ((Container<?>) os[++i]).containerIterator();
				return i < os.length || current.hasNext();
			}
			
			@Override
			public Object next() {
				return current.next();
			}
			
			@Override
			public void remove() {}
			
		};
	}
	
	@Override
	public Class<? extends Object> getReturnType() {
		return type;
	}
	
	@Override
	public String getLoopDebugMessage(final Event e) {
		return "argument " + (arg.getIndex() + 1);
	}
	
	@Override
	public String toString() {
		return "the loop-argument";
	}
	
	@Override
	public boolean isLoopOf(final String s) {
		if (s.equalsIgnoreCase("argument"))
			return true;
		final Class<?> c = Skript.getClassFromUserInput(s);
		return c == type || c == arg.getType();
	}
	
}
