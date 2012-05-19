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

package ch.njol.skript.util;

import java.util.Deque;
import java.util.LinkedList;
import java.util.logging.Level;

import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;

import ch.njol.skript.SkriptLogger;

/**
 * @author Peter Güttinger
 * 
 */
public class ErrorSession {
	
	private final Deque<String> errors = new LinkedList<String>();
	private final Deque<String> warnings = new LinkedList<String>();
	
	private int errorCount = 0;
	
	public ErrorSession() {}
	
	/**
	 * Adds a warning to this session. Does not do anything if there are any errors in this session.
	 * 
	 * @param warning
	 */
	public void warning(final String warning) {
		if (errors.isEmpty())
			warnings.add(warning);
	}
	
	/**
	 * adds an error to this session. This clears all previously added warnings and prevents further warnings from being added.
	 * 
	 * @param error
	 */
	public void error(final String error) {
		warnings.clear();
		errors.add(error);
	}
	
	/**
	 * Logs this error directly without modifying the stored errors or warnings, but increases the error count.
	 * 
	 * @param error
	 */
	public void severeError(final String error) {
		errorCount++;
		SkriptLogger.log(Level.SEVERE, error);
	}
	
	/**
	 * Prints all errors to the server log and clears all errors.
	 * 
	 * @param def
	 */
	public void printErrors(final String def) {
		if (!hasErrors())
			SkriptLogger.log(Level.SEVERE, def);
		else
			printErrors();
	}
	
	/**
	 * Sends all errors to the given command sender and clears all errors.
	 * 
	 * @param sender
	 */
	public void printErrors(final CommandSender sender) {
		errorCount += errors.size();
		for (final String error : errors) {
			if (sender != null)
				sender.sendMessage(ChatColor.DARK_RED + error);
			else
				SkriptLogger.log(Level.SEVERE, error);
		}
		errors.clear();
	}
	
	/**
	 * Prints all errors to the server log (if any) and clears all errors.
	 */
	public void printErrors() {
		errorCount += errors.size();
		for (final String error : errors) {
			SkriptLogger.log(Level.SEVERE, error);
		}
		errors.clear();
	}
	
	/**
	 * Prints all warnings to the server log and clears all warnings.
	 */
	public final void printWarnings() {
		for (final String w : warnings)
			SkriptLogger.log(Level.WARNING, w);
		warnings.clear();
	}
	
	/**
	 * clears all errors & warnings.
	 */
	public final void clearErrors() {
		warnings.clear();
		errors.clear();
	}
	
	/**
	 * 
	 * @return whether there are any errors
	 */
	public final boolean hasErrors() {
		return !errors.isEmpty();
	}
	
	/**
	 * 
	 * @return Total number of printed/sent errors since the session was created
	 */
	public int getErrorCount() {
		return errorCount;
	}
	
	public String getLastError() {
		return errors.peekLast();
	}
	
}
