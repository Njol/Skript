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

package ch.njol.skript.config;

import java.lang.reflect.Field;

/**
 * @author Peter Güttinger
 */
public class Section {
	
	public final String name;
	
	public Section(final String name) {
		this.name = name;
	}
	
	public final <T> T get(final String name) {
		for (final Field f : this.getClass().getDeclaredFields()) {
			f.setAccessible(true);
			if (Option.class.isAssignableFrom(f.getType())) {
				try {
					final Option<?> o = (Option<?>) f.get(this);
					if (o.key.equals(name))
						return (T) o.value();
				} catch (final IllegalArgumentException e) {
					assert false;
				} catch (final IllegalAccessException e) {
					assert false;
				}
			}
		}
		return null;
	}
	
}
