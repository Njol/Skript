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

package ch.njol.skript.command;

import ch.njol.skript.Skript;
import ch.njol.skript.SkriptLogger;
import ch.njol.skript.SkriptLogger.SubLog;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.ParseContext;
import ch.njol.skript.lang.SkriptParser;
import ch.njol.skript.util.Utils;

/**
 * Represents an argument of a command
 * 
 * @author Peter Güttinger
 * 
 */
public class Argument<T> {
	
	private final Expression<? extends T> def;
	private final Class<T> type;
	private final boolean single;
	private final int index;
	
	private T[] current;
	
	public Argument(final Expression<? extends T> def, final Class<T> type, final boolean single, final int index) {
		this.def = def;
		this.type = type;
		this.single = single;
		this.index = index;
	}
	
	public static <T> Argument<T> newInstance(final Class<T> type, final String def, final int index, final boolean single) {
		Expression<? extends T> d = null;
		if (def != null) {
			if (def.startsWith("%") && def.endsWith("%")) {
				final Expression<?> e = (Expression<?>) SkriptParser.parse(def.substring(1, def.length() - 1), Skript.getExpressions().iterator(), false, true, "Can't understand the expression '" + def + "'");
				if (e == null)
					return null;
				final SubLog log = SkriptLogger.startSubLog();
				d = e.getConvertedExpression(type);
				SkriptLogger.stopSubLog(log);
				if (d == null) {
					log.printErrors("'" + def + "' is not " + Utils.a(Skript.getExactClassName(type)));
					return null;
				}
			} else {
				final SubLog log = SkriptLogger.startSubLog();
				d = SkriptParser.parseLiteral(def, type, ParseContext.DEFAULT);
				SkriptLogger.stopSubLog(log);
				if (d == null) {
					log.printErrors("'" + def + "' is not " + Utils.a(Skript.getExactClassName(type)));
					return null;
				}
			}
		}
		return new Argument<T>(d, type, single, index);
	}
	
	@Override
	public String toString() {
		return "<" + Skript.getExactClassName(type) + (single ? "" : "s") + (def == null ? "" : " = " + def.toString()) + ">";
	}
	
	public boolean isOptional() {
		return def != null;
	}
	
	public void setToDefault(final SkriptCommandEvent event) {
		assert def != null;
		current = def.getArray(event);
	}
	
	@SuppressWarnings("unchecked")
	public void set(final Object[] o) {
		if (o == null || !(type.isAssignableFrom(o.getClass().getComponentType())))
			throw new IllegalArgumentException();
		current = (T[]) o;
	}
	
	public T[] getCurrent() {
		return current;
	}
	
	public Class<T> getType() {
		return type;
	}
	
	public int getIndex() {
		return index;
	}
	
	public boolean isSingle() {
		return single;
	}
	
}
