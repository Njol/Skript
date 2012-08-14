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

import java.util.Deque;
import java.util.LinkedList;
import java.util.logging.Level;

import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;

import ch.njol.skript.config.Node;
import ch.njol.util.Pair;
import ch.njol.util.iterator.IteratorIterable;

public abstract class SkriptLogger {
	
	private static Node node = null;
	
	private static Verbosity verbosity = Verbosity.NORMAL;
	
	static boolean debug;
	
	private final static Deque<SubLog> subLogs = new LinkedList<SubLog>();
	
	public static final class SubLog {
		SubLog() {}
		
		private final Deque<Pair<Level, String>> log = new LinkedList<Pair<Level, String>>();
		
		/**
		 * Print all retained errors or the given one if no errors were retained.<br>
		 * This sublog is stopped if not already done
		 * 
		 * @param def error to print if no errors were logged, can be null to not print any error if there are none
		 * @return whether there were any errors
		 */
		public final boolean printErrors(final String def) {
			stop();
			boolean hasError = false;
			for (final Pair<Level, String> e : log) {
				if (e.first == Level.SEVERE) {
					logDirect(e);
					hasError = true;
				}
			}
			if (!hasError && def != null)
				logDirect(Level.SEVERE, def);
			return hasError;
		}
		
		/**
		 * Sends all retained error messages to the given recipient<br>
		 * This sublog is stopped if not already done
		 * 
		 * @param recipient
		 * @param def error to send if no errors were logged, can be null to not print any error if there are none
		 * @return whether there were any errors to send
		 */
		public final boolean printErrors(final CommandSender recipient, final String def) {
			stop();
			boolean hasError = false;
			for (final Pair<Level, String> e : log) {
				if (e.first == Level.SEVERE) {
					recipient.sendMessage(e.second);
					hasError = true;
				}
			}
			if (!hasError && def != null)
				recipient.sendMessage(def);
			return hasError;
		}
		
		/**
		 * Prints all retained log messages<br>
		 * This sublog is stopped if not already done
		 * 
		 * @return
		 */
		public final void printLog() {
			stop();
			for (final Pair<Level, String> e : log) {
				logDirect(e);
			}
		}
		
		public boolean hasErrors() {
			for (final Pair<Level, String> e : log) {
				if (e.first == Level.SEVERE)
					return true;
			}
			return false;
		}
		
		public String getLastError() {
			for (final Pair<Level, String> e : new IteratorIterable<Pair<Level, String>>(log.descendingIterator())) {
				if (e.getKey() == Level.SEVERE)
					return e.getValue();
			}
			return null;
		}
		
		/**
		 * Clears the list if retained log messages.
		 */
		public void clear() {
			log.clear();
		}
		
		public int size() {
			return log.size();
		}
		
		public void stop() {
			SkriptLogger.stopSubLog(this);
		}
	}
	
	/**
	 * Starts a sub log. All subsequent log messages will be added to this log and not printed.<br>
	 * This should be used like this:
	 * 
	 * <pre>
	 * SubLog log = SkriptLogger.startSubLog();
	 * doSomethingThatLogsMessages();
	 * SkriptLogger.stopSubLog(log);
	 * // do something with the logged messages
	 * </pre>
	 * 
	 * @return a newly created sublog
	 */
	public final static SubLog startSubLog() {
		final SubLog subLog = new SubLog();
		subLogs.addLast(subLog);
		return subLog;
	}
	
	public final static void stopSubLog(final SubLog log) {
		if (!subLogs.contains(log))
			return;
		if (subLogs.removeLast() != log) {
			int i = 1;
			while (subLogs.removeLast() != log)
				i++;
			throw new IllegalStateException(i + " sub log(s) was/were not stopped properly!");
		}
	}
	
	static void setVerbosity(final Verbosity v) {
		verbosity = v;
		if (v.compareTo(Verbosity.DEBUG) >= 0)
			debug = true;
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
	 * 
	 * @see Skript#info(String)
	 * @see Skript#warning(String)
	 * @see Skript#error(String)
	 * @see Skript#logNormal()
	 * @see Skript#logHigh()
	 * @see Skript#logVeryHigh()
	 * @see Skript#debug()
	 */
	public static void log(final Level level, final String message) {
		if (node != null && level.intValue() >= Level.WARNING.intValue()) {
			logDirect(level, message +
					" (" + node.getConfig().getFileName() + ", line " + node.getLine() + (node.getOrig() == null ? "" : ": '" + node.getOrig().trim() + "')"));
		} else {
			logDirect(level, message);
		}
	}
	
	public static void setNode(final Node node) {
		SkriptLogger.node = node == null || node.getParent() == null ? null : node;
	}
	
	static Node getNode() {
		return node;
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
		if (!subLogs.isEmpty()) {
			subLogs.getLast().log.add(new Pair<Level, String>(level, message));
		} else {
			Bukkit.getLogger().log(level, "[Skript] " + message);
		}
	}
	
	private static void logDirect(final Pair<Level, String> message) {
		if (!subLogs.isEmpty()) {
			subLogs.getLast().log.add(message);
		} else {
			Bukkit.getLogger().log(message.first, "[Skript] " + message.second);
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
	
}
