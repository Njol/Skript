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
 * Copyright 2011-2014 Peter Güttinger
 * 
 */

package ch.njol.skript.command;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.util.WeakHashMap;

import org.bukkit.event.Event;
import org.eclipse.jdt.annotation.Nullable;

import ch.njol.skript.classes.ClassInfo;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.ParseContext;
import ch.njol.skript.lang.SkriptParser;
import ch.njol.skript.lang.VariableString;
import ch.njol.skript.lang.util.SimpleLiteral;
import ch.njol.skript.log.RetainingLogHandler;
import ch.njol.skript.log.SkriptLogger;
import ch.njol.skript.util.Utils;

/**
 * Represents an argument of a command
 * <p>
 * TODO allow named arguments (stored in local variables {_name})
 * 
 * @author Peter Güttinger
 */
public class Argument<T> {
	
	@Nullable
	private final Expression<? extends T> def;
	private final ClassInfo<T> type;
	private final boolean single;
	private final int index;
	
	private final boolean optional;
	
	private transient WeakHashMap<Event, T[]> current = new WeakHashMap<Event, T[]>();
	
	private void readObject(final ObjectInputStream in) throws ClassNotFoundException, IOException {
		in.defaultReadObject();
		current = new WeakHashMap<Event, T[]>();
	}
	
	public Argument(final @Nullable Expression<? extends T> def, final ClassInfo<T> type, final boolean single, final int index, final boolean optional) {
		this.def = def;
		this.type = type;
		this.single = single;
		this.index = index;
		this.optional = optional;
	}
	
	@SuppressWarnings("unchecked")
	@Nullable
	public static <T> Argument<T> newInstance(final ClassInfo<T> type, final @Nullable String def, final int index, final boolean single, final boolean forceOptional) {
		Expression<? extends T> d = null;
		if (def != null) {
			if (def.startsWith("%") && def.endsWith("%")) {
				final RetainingLogHandler log = SkriptLogger.startRetainingLog();
				try {
					d = new SkriptParser("" + def.substring(1, def.length() - 1), SkriptParser.PARSE_EXPRESSIONS, ParseContext.COMMAND).parseExpression(type.getC());
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
					if (type.getC() == String.class) {
						if (def.startsWith("\"") && def.endsWith("\""))
							d = (Expression<? extends T>) VariableString.newInstance("" + def.substring(1, def.length() - 1));
						else
							d = (Expression<? extends T>) new SimpleLiteral<String>(def, false);
					} else {
						d = SkriptParser.parseLiteral(def, type.getC(), ParseContext.DEFAULT);
					}
					if (d == null) {
						log.printErrors("'" + def + "' is not " + type.getName().withIndefiniteArticle());
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
		final Expression<? extends T> def = this.def;
		return "<" + Utils.toEnglishPlural(type.getCodeName(), !single) + (def == null ? "" : " = " + def.toString()) + ">";
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
		if (!(type.getC().isAssignableFrom(o.getClass().getComponentType())))
			throw new IllegalArgumentException();
		current.put(e, (T[]) o);
	}
	
	@Nullable
	public T[] getCurrent(final Event e) {
		return current.get(e);
	}
	
	public Class<T> getType() {
		return type.getC();
	}
	
	public int getIndex() {
		return index;
	}
	
	public boolean isSingle() {
		return single;
	}
	
}
