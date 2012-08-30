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

import ch.njol.skript.config.Node;

/**
 * @author Peter Güttinger
 * 
 */
public class LogEntry {
	
	private final Level level;
	
	private final String message;
	
	private Node node = null;
	
	public LogEntry(final Level level, final String message) {
		this.level = level;
		this.message = message;
		node = SkriptLogger.getNode();
	}
	
	public LogEntry(final Level level, final String message, final Node node) {
		this(level, message);
		this.node = node;
	}
	
	public void setNode(final Node node) {
		if (node != null)
			this.node = node;
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
		return message + " (" + node.getConfig().getFileName() + ", line " + node.getLine() + (node.getOrig() == null ? "" : ": '" + node.getOrig().trim() + "')");
	}
	
}
