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

import java.util.logging.Filter;

import ch.njol.skript.Skript;
import ch.njol.util.LoggerFilter;

/**
 * @author Peter Güttinger
 */
public class BukkitLoggerFilter {
	
	private final static LoggerFilter filter = new LoggerFilter(SkriptLogger.LOGGER);
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
