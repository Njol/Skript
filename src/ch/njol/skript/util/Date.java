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

import ch.njol.skript.Skript;

/**
 * @author Peter Güttinger
 * 
 */
public class Date implements Comparable<Date> {
	
	public final long timestamp;
	
	public Date() {
		timestamp = System.currentTimeMillis();
	}
	
	public Date(final long timestamp) {
		this.timestamp = timestamp;
	}
	
	public Timespan difference(final Date other) {
		return new Timespan((int) Math.round(1. * Math.abs(timestamp - other.timestamp) / 50.));
	}
	
	@Override
	public int compareTo(final Date other) {
		return (int) (timestamp - other.timestamp);
	}
	
	@Override
	public String toString() {
		return Skript.getDateFormat().format(timestamp);
	}
	
}
