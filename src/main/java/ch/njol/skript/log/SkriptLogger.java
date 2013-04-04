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

package ch.njol.skript.log;

import java.util.Collection;
import java.util.logging.Level;

import org.bukkit.Bukkit;

import ch.njol.skript.Skript;
import ch.njol.skript.config.Node;

/**
 * @author Peter Güttinger
 */
public abstract class SkriptLogger {
	
	private static Node node = null;
	
	private static Verbosity verbosity = Verbosity.NORMAL;
	
	static boolean debug;
	
	/**
	 * use addFirst and pollFirst
	 */
	private final static HandlerList handlers = new HandlerList();
	
	/**
	 * Starts retaining the log, i.e. all subsequent log messages will be added to this handler and not printed.
	 * <p>
	 * This should be used like this:
	 * 
	 * <pre>
	 * RetainingLogHandler log = SkriptLogger.startRetainingLog();
	 * doSomethingThatLogsMessages();
	 * log.stop();
	 * // do something with the logged messages
	 * </pre>
	 * 
	 * @return a newly created RetainingLogHandler
	 * @see BlockingLogHandler
	 */
	public final static RetainingLogHandler startRetainingLog() {
		final RetainingLogHandler h = new RetainingLogHandler();
		handlers.add(h);
		return h;
	}
	
	public final static ParseLogHandler startParseLogHandler() {
		final ParseLogHandler h = new ParseLogHandler();
		handlers.add(h);
		return h;
	}
	
	public final static <T extends LogHandler> T startLogHandler(final T h) {
		handlers.add(h);
		return h;
	}
	
	final static void removeHandler(final LogHandler h) {
		if (!handlers.contains(h))
			return;
		if (handlers.remove() != h) {
			int i = 1;
			while (handlers.remove() != h)
				i++;
			Bukkit.getLogger().severe("[Skript] " + i + " log handler" + (i == 1 ? " was" : "s were") + " not stopped properly! (at " + getCaller() + ") [if you're a server admin and you see this message please file a bug report at http://dev.bukkit.org/server-mods/skript/tickets/ if there is not already one]");
		}
	}
	
	final static StackTraceElement getCaller() {
		for (final StackTraceElement e : new Exception().getStackTrace()) {
			if (!e.getClassName().startsWith(SkriptLogger.class.getPackage().getName()))
				return e;
		}
		return null;
	}
	
	public static void setVerbosity(final Verbosity v) {
		verbosity = v;
		if (v.compareTo(Verbosity.DEBUG) >= 0)
			debug = true;
	}
	
	public static void setNode(final Node node) {
		SkriptLogger.node = node == null || node.getParent() == null ? null : node;
	}
	
	public static Node getNode() {
		return node;
	}
	
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
	 * @see Skript#info(String)
	 * @see Skript#warning(String)
	 * @see Skript#error(String)
	 * @see Skript#logNormal()
	 * @see Skript#logHigh()
	 * @see Skript#logVeryHigh()
	 * @see Skript#debug()
	 */
	public static void log(final Level level, final String message) {
		log(new LogEntry(level, message, node));
	}
	
	public static void log(final LogEntry entry) {
		if (entry == null)
			return;
		for (final LogHandler h : handlers) {
			if (!h.log(entry))
				return;
		}
		Bukkit.getLogger().log(entry.getLevel(), "[Skript] " + entry.getMessage());
	}
	
	public static void logAll(final Collection<LogEntry> entries) {
		outer: for (final LogEntry entry : entries) {
			assert entry != null;
			for (final LogHandler h : handlers) {
				if (!h.log(entry))
					continue outer;
			}
			Bukkit.getLogger().log(entry.getLevel(), "[Skript] " + entry.getMessage());
		}
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
	
	public static boolean debug() {
		return debug;
	}
	
//	public static int getNumErrors() {
//		int errors = numErrors;
//		for (final SubLog log : handlers)
//			errors += log.getNumErrors();
//		return errors;
//	}
//	
//	public static void error(final LogEntry error, final ErrorQuality quality) {
//		if (error.getLevel() != Level.SEVERE)
//			throw new IllegalArgumentException("Cannot error anything else than an error");
//		if (!(handlers.getLast() instanceof ParseLog))
//			throw new SkriptAPIException("Cannot log with a quality if no parsing is in progress");
//		((ParseLog) handlers.getLast()).error(error, quality);
//	}
//	
//	static void printParseLogError(final LogEntry error, final int quality) {
//		if (handlers.peekLast() instanceof ParseLog) {
//			((ParseLog) handlers.getLast()).error(error, quality);
//		} else {
//			log(error);
//		}
//	}
//	
}
