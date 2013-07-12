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
 * Copyright 2011-2013 Peter Güttinger
 * 
 */

package ch.njol.skript.command;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.Serializable;
import java.util.WeakHashMap;

import org.bukkit.event.Event;

import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.ParseContext;
import ch.njol.skript.lang.SkriptParser;
import ch.njol.skript.lang.VariableString;
import ch.njol.skript.lang.util.SimpleLiteral;
import ch.njol.skript.log.RetainingLogHandler;
import ch.njol.skript.log.SkriptLogger;
import ch.njol.skript.registrations.Classes;
import ch.njol.skript.util.Utils;

/**
 * Represents an argument of a command
 * 
 * @author Peter Güttinger
 */
@SuppressWarnings("serial")
public class Argument<T> implements Serializable {
	
	private final Expression<? extends T> def;
	private final Class<T> type;
	private final boolean single;
	private final int index;
	
	private final boolean optional;
	
	private transient WeakHashMap<Event, T[]> current = new WeakHashMap<Event, T[]>();
	
	private void readObject(final ObjectInputStream in) throws ClassNotFoundException, IOException {
		in.defaultReadObject();
		current = new WeakHashMap<Event, T[]>();
	}
	
	public Argument(final Expression<? extends T> def, final Class<T> type, final boolean single, final int index, final boolean optional) {
		this.def = def;
		this.type = type;
		this.single = single;
		this.index = index;
		this.optional = optional;
	}
	
	@SuppressWarnings("unchecked")
	public static <T> Argument<T> newInstance(final Class<T> type, final String def, final int index, final boolean single, final boolean forceOptional) {
		Expression<? extends T> d = null;
		if (def != null) {
			if (def.startsWith("%") && def.endsWith("%")) {
				final RetainingLogHandler log = SkriptLogger.startRetainingLog();
				try {
					d = new SkriptParser(def.substring(1, def.length() - 1), SkriptParser.PARSE_EXPRESSIONS, ParseContext.COMMAND).parseExpression(type);
					if (d == null) {
						log.printErrors("Can't understand this expression: " + def + "");
						return null;
					}
					log.printLog();
				} finally {
					log.stop();
				}
			} else {
				final RetainingLogHandler log = SkriptLogger.startRetainingLog();
				try {
					if (type == String.class) {
						if (def.startsWith("\"") && def.endsWith("\""))
							d = (Expression<? extends T>) VariableString.newInstance(def.substring(1, def.length() - 1));
						else
							d = (Expression<? extends T>) new SimpleLiteral<String>(def, false);
					} else {
						d = SkriptParser.parseLiteral(def, type, ParseContext.DEFAULT);
					}
					if (d == null) {
						log.printErrors("'" + def + "' is not " + Classes.getSuperClassInfo(type).getName().withIndefiniteArticle());
						return null;
					}
					log.printLog();
				} finally {
					log.stop();
				}
			}
		}
		return new Argument<T>(d, type, single, index, def != null || forceOptional);
	}
	
	@Override
	public String toString() {
		return "<" + Utils.toEnglishPlural(Classes.getExactClassName(type), !single) + (def == null ? "" : " = " + def.toString()) + ">";
	}
	
	public boolean isOptional() {
		return optional;
	}
	
	public void setToDefault(final ScriptCommandEvent event) {
		if (def != null)
			current.put(event, def.getArray(event));
	}
	
	@SuppressWarnings("unchecked")
	public void set(final ScriptCommandEvent e, final Object[] o) {
		if (o == null || !(type.isAssignableFrom(o.getClass().getComponentType())))
			throw new IllegalArgumentException();
		current.put(e, (T[]) o);
	}
	
	public T[] getCurrent(final Event e) {
		return current.get(e);
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
