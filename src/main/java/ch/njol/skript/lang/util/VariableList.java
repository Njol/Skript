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

package ch.njol.skript.lang.util;

import java.util.HashMap;
import java.util.Iterator;

/**
 * @author Peter Güttinger
 */
public class VariableList extends HashMap<String, Object> implements Iterable<Object> {
	
	private static final long serialVersionUID = 3087232096671700971L;
	
	private int maxIndex = 1;
	
	public void add(final Object o) {
		while (containsKey("" + maxIndex))
			maxIndex++;
		put("" + maxIndex, o);
	}
	
	@Override
	public Iterator<Object> iterator() {
		return values().iterator();
	}
	
}
