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

import java.lang.reflect.Array;

import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;

import ch.njol.skript.Skript;
import ch.njol.skript.SkriptLogger;
import ch.njol.skript.SkriptLogger.SubLog;
import ch.njol.skript.api.Converter;
import ch.njol.skript.util.Utils;
import ch.njol.skript.util.VariableString;

/**
 * Represents an argument of a command
 * 
 * @author Peter Güttinger
 * 
 */
public class Argument<T> {
	
	private final VariableString def;
	private final T defT;
	private final Class<T> type;
	private final Converter<String, ? extends T> conv;
	private final boolean single;
	private final int index;
	
	private T[] current;
	private final T[] singleTArray;
	
	@SuppressWarnings("unchecked")
	public Argument(final VariableString def, final T defT, final Class<T> type, final Converter<String, ? extends T> conv, final boolean single, final int index) {
		this.def = def;
		this.defT = defT;
		this.type = type;
		this.conv = conv;
		this.single = single;
		this.index = index;
		singleTArray = (T[]) Array.newInstance(type, 1);
	}
	
	@SuppressWarnings("unchecked")
	public static <T> Argument<T> newInstance(final Class<T> type, final String def, final int index, final boolean single) {
		Converter<String, ? extends T> conv = null;
		if (type != String.class)
			conv = Skript.getParser(type);
		VariableString d = null;
		if (def != null) {
			d = VariableString.newInstance(def);
			if (d == null)
				return null;
		}
		T defT = null;
		if (def != null && d.isSimple()) {
			if (type == String.class)
				defT = (T) def;
			else
				defT = conv.convert(def);
			if (defT == null) {
				Skript.error("'" + def + "' is not " + Utils.a(Skript.getExactClassName(type)));
				return null;
			}
		}
		return new Argument<T>(d, defT, type, conv, single, index);
	}
	
	@Override
	public String toString() {
		return "<" + Skript.getExactClassName(type) + (single ? "" : "s") + (def == null ? "" : " = " + (defT != null ? Skript.toString(defT) : Skript.toString(def))) + ">";
	}
	
	public void setToDefault(final SkriptCommandEvent event) {
		if (defT != null) {
			current = singleTArray;
			current[0] = defT;
			return;
		}
		parse(def.get(event), Bukkit.getConsoleSender());
	}
	
	@SuppressWarnings("unchecked")
	public boolean parse(final String s, final CommandSender sender) {
		current = singleTArray;
		if (type == String.class) {
			current[0] = (T) s;
			return true;
		}
		SubLog log = SkriptLogger.startSubLog();
		current[0] = conv.convert(s);
		SkriptLogger.stopSubLog(log);
		if (current[0] == null)
			log.printErrors(sender, "'" + s + "' is not " + Utils.a(Skript.getExactClassName(type)));
		return current[0] != null;
	}
	
	@SuppressWarnings("unchecked")
	public boolean parse(final String[] args, final int start, final CommandSender sender) {
		if (type == String.class) {
			current = singleTArray;
			final StringBuilder b = new StringBuilder(args[start]);
			for (int i = start + 1; i < args.length; i++)
				b.append(", ").append(args[i]);
			current[0] = (T) b.toString();
			return true;
		}
		current = (T[]) Array.newInstance(type, args.length - start);
		for (int i = 0; i < args.length - start; i++) {
			if ((current[i] = conv.convert(args[start + i])) == null) {
				sender.sendMessage("'" + args[start + i] + "' is not " + Utils.a(Skript.getExactClassName(type)));
				return false;
			}
		}
		return true;
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
