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

package ch.njol.skript;

import java.util.logging.Level;

import org.bukkit.Bukkit;

import ch.njol.skript.config.Node;

public abstract class SkriptLogger {
	
	private static Node node = null;
	private static String prefix = "";
	private static String oldPrefix = null;
	
	static Verbosity verbosity = Verbosity.NORMAL;
	
	/**
	 * Logging should be done like this:
	 * 
	 * <pre>
	 * if (Skript.logNormal())
	 * 	Skript.info(&quot;this information is displayed on verbosity normal or higher&quot;);
	 * </pre>
	 * 
	 * @param level
	 * @param message
	 * 
	 * @see Skript#info(String)
	 * @see Skript#warning(String)
	 * @see Skript#error(String)
	 * @see Skript#logNormal()
	 * @see Skript#logHigh()
	 * @see Skript#logVeryHigh()
	 * @see Skript#logExtreme()
	 */
	static void log(final Level level, final String message) {
		if (node != null && level.intValue() >= Level.WARNING.intValue()) {
			if (node.getParent() != null)
				logDirect(level, message +
						" (" + node.getConfig().getFileName() + ", line " + node.getLine() + (node.getOrig() == null ? "" : ": '" + node.getOrig().trim() + "')"));
			else
				logDirect(level, message +
						" (" + node.getConfig().getFileName() + " [unknown line])");
		} else {
			logDirect(level, message);
		}
	}
	
	public static void expectationError(final String expectedType, final String found) {
		log(Level.SEVERE, "expecting " + expectedType + ", found: '" + found + "'");
	}
	
	public static void setNode(final Node node) {
		SkriptLogger.node = node == null || node.getParent() == null ? null : node;
	}
	
	/**
	 * Logs the message without information about the file & line the error came from.
	 * 
	 * @param level
	 * @param message
	 * 
	 * @see #log(Level, String)
	 */
	public static void logDirect(final Level level, final String message) {
		Bukkit.getLogger().log(level, "[Skript] " + prefix + message);
	}
	
	/**
	 * Sets a prefix to be applied before all messages. This will look like this:<br/>
	 * "[Skript] prefixmessage" (no space is added between the prefix and the message!)
	 * 
	 * @param prefix
	 * @return the prefix used before
	 * @see #resetPrefix()
	 */
	public final static String setPrefix(final String prefix) {
		oldPrefix = SkriptLogger.prefix;
		SkriptLogger.prefix = prefix;
		return oldPrefix;
	}
	
	/**
	 * Resets the prefix to the last value.
	 * 
	 * @throws IllegalStateException
	 */
	public static final void resetPrefix() throws IllegalStateException {
		if (oldPrefix == null)
			throw new IllegalStateException("resetPrefix was called without a setPrefix beforehand");
		prefix = oldPrefix;
		oldPrefix = null;
	}
	
	/**
	 * checks whether messages should be logged for the given verbosity.
	 * 
	 * @param minVerb minimal verbosity
	 * @return
	 */
	public static boolean log(final Verbosity minVerb) {
		return minVerb.compareTo(verbosity) <= 0;
	}
	
}
