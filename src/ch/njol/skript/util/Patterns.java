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

/**
 * A helper class useful when a variable/cond/eff/etc. needs to assiciate additional data with each pattern.
 * 
 * @author Peter Güttinger
 */
public class Patterns<T> {
	
	private final String[] patterns;
	private final Object[] ts;
	
	/**
	 * 
	 * @param info an array which must be like {{String, T}, ...}
	 */
	public Patterns(final Object[][] info) {
		patterns = new String[info.length];
		ts = new Object[info.length];
		for (int i = 0; i < info.length; i++) {
			if (!(info[i][0] instanceof String))
				throw new IllegalArgumentException("given array is not like {{String, T}, ...}");
			patterns[i] = (String) info[i][0];
			ts[i] = info[i][1];
		}
	}
	
	public String[] getPatterns() {
		return patterns;
	}
	
	@SuppressWarnings("unchecked")
	public T getInfo(final int matchedPattern) {
		return (T) ts[matchedPattern];
	}
	
}
