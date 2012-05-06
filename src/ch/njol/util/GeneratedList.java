/*
 *  This program is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with this program.  If not, see <http://www.gnu.org/licenses/>.
 * 
 * 
 * Copyright 2011, 2012 Peter Güttinger
 * 
 */

package ch.njol.util;

import java.util.List;

/**
 * 
 * TODO: this will be the return value of a variable, but I have to think about it first
 * 
 * @author Peter Güttinger
 *
 */
public abstract class GeneratedList<T> implements List<T> {
	
	private final int size;
	
	protected GeneratedList(int size) {
		this.size = size;
	}
	
	@Override
	public final int size() {
		return size;
	}
	
	protected abstract T next();
}
