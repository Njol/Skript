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

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;

/**
 * @author Peter Güttinger
 */
public class ParseLogHandler extends LogHandler {
	
	private LogEntry error = null;
	private final List<LogEntry> log = new ArrayList<LogEntry>();
	
	@Override
	public boolean log(final LogEntry entry) {
		if (entry.getLevel() == Level.SEVERE) {
			if (error == null || entry.getQuality() > error.getQuality()) {
				error = entry;
			}
		} else {
			log.add(entry);
		}
		return false;
	}
	
	public void stop(final boolean error) {
		SkriptLogger.removeHandler(this);
		if (error) {
			if (this.error != null)
				SkriptLogger.log(this.error);
		} else {
			for (final LogEntry e : log)
				SkriptLogger.log(e);
		}
	}
	
	@Override
	public void onStop() {}
	
	public void error(final String error, final ErrorQuality quality) {
		log(new LogEntry(Level.SEVERE, quality, error));
	}
	
	/**
	 * Clears all log messages except for the error
	 */
	public void clear() {
		log.clear();
	}
	
	/**
	 * Prints the retained log, but no errors
	 */
	public void printLog() {
		stop();
		SkriptLogger.logAll(log);
	}
	
	public void printError() {
		printError(null);
	}
	
	/**
	 * Prints the best error or the given error if no error has been logged.
	 * 
	 * @param def Error to log if no error has been logged so far, can be null
	 */
	public void printError(final String def) {
		stop();
		if (error == null && def == null)
			return;
		if (error == null)
			SkriptLogger.log(new LogEntry(Level.SEVERE, def));
		else
			SkriptLogger.log(error);
	}
	
	public int getNumErrors() {
		return error == null ? 0 : 1;
	}
	
	public boolean hasError() {
		return error != null;
	}
	
	public LogEntry getError() {
		return error;
	}
	
}
