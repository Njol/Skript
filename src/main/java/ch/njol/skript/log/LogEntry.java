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

package ch.njol.skript.log;

import java.util.logging.Level;

import ch.njol.skript.Skript;
import ch.njol.skript.config.Node;

/**
 * @author Peter Güttinger
 * 
 */
public class LogEntry {
	
	private final Level level;
	
	private final String message;
	
	private final Node node;
	
	private final String from;
	
	public LogEntry(final Level level, final String message) {
		this(level, message, SkriptLogger.getNode());
	}
	
	public LogEntry(final Level level, final String message, final Node node) {
		this.level = level;
		this.message = message;
		this.node = node;
		from = Skript.debug() ? findCaller() : "";
	}
	
	private static final String skriptLogPackageName = SkriptLogger.class.getPackage().getName();
	
	private static String findCaller() {
		final StackTraceElement[] es = new Exception().getStackTrace();
		for (int i = 0; i < es.length; i++) {
			if (!es[i].getClassName().startsWith(skriptLogPackageName))
				continue;
			i++;
			while (i < es.length - 1 && (es[i].getClassName().startsWith(skriptLogPackageName) || es[i].getClassName().equals(Skript.class.getName())))
				i++;
			return " (from " + es[i] + ")";
		}
		return es.length == 0 ? " (from an unknown source)" : " (from " + es[es.length - 1] + ")";
	}
	
	public Level getLevel() {
		return level;
	}
	
	public String getMessage() {
		return toString();
	}
	
	@Override
	public String toString() {
		if (node == null || level.intValue() < Level.WARNING.intValue())
			return message;
		return message + from + " (" + node.getConfig().getFileName() + ", line " + node.getLine() + (node.getOrig() == null ? "" : ": '" + node.getOrig().trim() + "')");
	}
	
}
