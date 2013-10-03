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

import java.io.Closeable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.logging.Filter;
import java.util.logging.Level;
import java.util.logging.LogRecord;
import java.util.logging.Logger;

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
	
	public final static Level DEBUG = new Level("DEBUG", Level.INFO.intValue()) {
		private static final long serialVersionUID = 8959282461654206205L;
	};
	
	public final static Logger LOGGER = Bukkit.getServer() != null ? Bukkit.getLogger() : Logger.getLogger(Logger.GLOBAL_LOGGER_NAME); // cannot use Bukkit in tests
	
	private final static HandlerList handlers = new HandlerList();
	
	/**
	 * Shorthand for <tt>{@link #startLogHandler(LogHandler) startLogHandler}(new {@link RetainingLogHandler}());</tt>
	 * 
	 * @return A newly created RetainingLogHandler
	 */
	public final static RetainingLogHandler startRetainingLog() {
		return startLogHandler(new RetainingLogHandler());
	}
	
	/**
	 * Shorthand for <tt>{@link #startLogHandler(LogHandler) startLogHandler}(new {@link ParseLogHandler}());</tt>
	 * 
	 * @return A newly created ParseLogHandler
	 */
	public final static ParseLogHandler startParseLogHandler() {
		return startLogHandler(new ParseLogHandler());
	}
	
	/**
	 * Starts a log handler.
	 * <p>
	 * This should be used like this:
	 * 
	 * <pre>
	 * LogHandler log = SkriptLogger.startLogHandler(new ...LogHandler());
	 * try {
	 * 	doSomethingThatLogsMessages();
	 * 	// do something with the logged messages
	 * } finally {
	 * 	log.stop();
	 * }
	 * </pre>
	 * 
	 * @return The passed LogHandler
	 * @see #startParseLogHandler()
	 * @see #startRetainingLog()
	 * @see BlockingLogHandler
	 * @see CountingLogHandler
	 * @see ErrorDescLogHandler
	 * @see FilteringLogHandler
	 * @see RedirectingLogHandler
	 */
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
			LOGGER.severe("[Skript] " + i + " log handler" + (i == 1 ? " was" : "s were") + " not stopped properly! (at " + getCaller() + ") [if you're a server admin and you see this message please file a bug report at http://dev.bukkit.org/server-mods/skript/tickets/ if there is not already one]");
		}
	}
	
	final static boolean isStopped(final LogHandler h) {
		return !handlers.contains(h);
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
//		if (Skript.testing() && node != null && node.debug())
//			System.out.print("---> " + entry.level + ": " + entry.getMessage() + " ::" + LogEntry.findCaller());
		for (final LogHandler h : handlers) {
			if (!h.log(entry))
				return;
		}
		LOGGER.log(entry.getLevel(), "[Skript] " + entry.getMessage());
	}
	
	public static void logAll(final Collection<LogEntry> entries) {
		outer: for (final LogEntry entry : entries) {
			assert entry != null;
			for (final LogHandler h : handlers) {
				if (!h.log(entry))
					continue outer;
			}
			LOGGER.log(entry.getLevel(), "[Skript] " + entry.getMessage());
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
	
	private final static class LoggerFilter implements Filter, Closeable {
		private final Logger l;
		private final Collection<Filter> filters = new ArrayList<Filter>();
		private final Filter oldFilter;
		
		public LoggerFilter(final Logger l) {
			this.l = l;
			oldFilter = l.getFilter();
			l.setFilter(this);
		}
		
		@Override
		public boolean isLoggable(final LogRecord record) {
			if (oldFilter != null && !oldFilter.isLoggable(record))
				return false;
			for (final Filter f : filters)
				if (!f.isLoggable(record))
					return false;
			return true;
		}
		
		public final void addFilter(final Filter f) {
			assert f != null;
			if (f != null)
				filters.add(f);
		}
		
		public final boolean removeFilter(final Filter f) {
			return filters.remove(f);
		}
		
		@Override
		public void close() {
			l.setFilter(oldFilter);
		}
	}
	
	private final static LoggerFilter filter = new LoggerFilter(LOGGER);
	static {
		Skript.closeOnDisable(filter);
	}
	
	/**
	 * Adds a filter to Bukkit's log.
	 * 
	 * @param f A filter to filter log messages
	 */
	public final static void addFilter(final Filter f) {
		assert f != null;
		if (f != null)
			filter.addFilter(f);
	}
	
	public final static boolean removeFilter(final Filter f) {
		return filter.removeFilter(f);
	}
	
}
