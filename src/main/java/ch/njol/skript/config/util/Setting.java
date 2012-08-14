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

package ch.njol.skript.config.util;

import ch.njol.skript.config.SectionNode;

/**
 * @author Peter Güttinger
 * 
 */
@SuppressWarnings("unused")
public class Setting<T> {
	
	private final String path;
	private final char pathSeparatorChar = '.';
	
	private T def = null;
	private final T value = null;
	
	private final SectionNode parent = null;
	
	public Setting(final String path) {
		this.path = path;
	}
	
	public Setting(final String path, final T def) {
		this.path = path;
		this.def = def;
	}
	
	public T getValue() {
		return value == null ? value : def;
	}
	
	public boolean isRequired() {
		return def == null;
	}
	
	public String getPath() {
		return path;
	}
	
}
