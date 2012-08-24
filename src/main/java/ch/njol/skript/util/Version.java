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

import java.util.Arrays;

/**
 * @author Peter Güttinger
 * 
 */
public class Version implements Comparable<Version> {
	
	private final int[] version = new int[3];
	
	public Version(final int[] version) {
		if (version.length < 1 || version.length > 3)
			throw new IllegalArgumentException("Versions must hava a minimum of 2 and a maximum of 3 numbers (given: " + version.length + ")");
		for (int i = 0; i < version.length; i++)
			this.version[i] = version[i];
	}
	
	public Version(final String version) {
		if (!version.matches("\\d+(.\\d+){0,2}"))
			throw new IllegalArgumentException("'" + version + "' is not a valid version string");
		final String[] split = version.split("\\.");
		for (int i = 0; i < split.length; i++)
			this.version[i] = Integer.parseInt(split[i]);
	}
	
	@Override
	public boolean equals(final Object obj) {
		if (this == obj)
			return true;
		if (!(obj instanceof Version))
			return false;
		return compareTo((Version) obj) == 0;
	}
	
	@Override
	public int hashCode() {
		return Arrays.hashCode(version);
	}
	
	@Override
	public int compareTo(final Version other) {
		for (int i = 0; i < 3; i++) {
			if (version[i] > other.version[i])
				return 1;
			if (version[i] < other.version[i])
				return -1;
		}
		return 0;
	}
	
	public boolean isSmallerThan(final Version other) {
		return compareTo(other) < 0;
	}
	
	public boolean isLargerThan(final Version other) {
		return compareTo(other) > 0;
	}
	
	@Override
	public String toString() {
		return version[0] + "." + version[1] + (version[2] == 0 ? "" : "." + version[2]);
	}
	
	public final static int compare(final String v1, final String v2) {
		return new Version(v1).compareTo(new Version(v2));
	}
}
