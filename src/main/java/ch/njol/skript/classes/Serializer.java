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

package ch.njol.skript.classes;

/**
 * @author Peter Güttinger
 */
public interface Serializer<T> {
	
	/**
	 * Serializes an object to a string.
	 * <p>
	 * This method must be thread-safe and never return null.
	 * 
	 * @param o
	 * @return
	 */
	public String serialize(T o);
	
	/**
	 * Deserializes an object from a string returned by this serializer or an earlier version thereof.
	 * <p>
	 * This method should only return null if the input is invalid (i.e. not produced by {@link #serialize(Object)} or an older version of that method)
	 * <p>
	 * This method must only be called from Bukkit's main thread if {@link #mustSyncDeserialization()} returned true.
	 * 
	 * @param s
	 * @return The deserialized object or null if the input is invalid. An error message may be logged to specify the cause.
	 */
	public T deserialize(String s);
	
	/**
	 * @return Whether deserialization must be done on Bukkit's main thread
	 */
	public boolean mustSyncDeserialization();
	
}
