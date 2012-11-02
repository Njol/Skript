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
 * 
 */
public class ParseLog implements SubLog {
	
	private LogEntry error = null;
	private int quality = 0;
	private final List<LogEntry> log = new ArrayList<LogEntry>();
	
	@Override
	public void log(final LogEntry entry) {
		if (entry.getLevel() == Level.SEVERE)
			error(entry, ErrorQuality.SEMANTIC_ERROR);
		else
			log.add(entry);
	}
	
	public void error(final LogEntry error, final ErrorQuality quality) {
		if (this.quality < quality.quality()) {
			this.quality = quality.quality();
			this.error = error;
		}
	}
	
	void error(final LogEntry error, final int quality) {
		if (this.quality < quality) {
			this.quality = quality;
			this.error = error;
		}
	}
	
	/**
	 * Clears all log messages except for the error
	 */
	@Override
	public void clear() {
		log.clear();
	}
	
	@Override
	public void stop() {
		SkriptLogger.stopSubLog(this);
	}
	
	/**
	 * Prints the retained log, but no errors
	 */
	@Override
	public void printLog() {
		stop();
		SkriptLogger.logAll(log);
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
			SkriptLogger.printParseLogError(new LogEntry(Level.SEVERE, def), quality);
		else
			SkriptLogger.printParseLogError(error, quality);
	}
	
	@Override
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
