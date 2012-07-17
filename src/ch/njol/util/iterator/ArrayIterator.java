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

package ch.njol.util.iterator;

import java.util.Iterator;
import java.util.NoSuchElementException;

/**
 * A simple iterator to iterate over an array.
 * 
 * @author Peter Güttinger
 */
public class ArrayIterator<T> implements Iterator<T> {
	
	private final T[] array;
	private int index = 0;
	
	public ArrayIterator(final T[] array) {
		this.array = array;
	}
	
	public ArrayIterator(final T[] array, final int start) {
		this.array = array;
		index = start;
	}
	
	@Override
	public boolean hasNext() {
		return index < array.length;
	}
	
	@Override
	public T next() {
		if (index >= array.length)
			throw new NoSuchElementException();
		return array[index++];
	}
	
	/**
	 * not supported by arrays.
	 * 
	 * @throws UnsupportedOperationException always
	 */
	@Override
	public void remove() {
		throw new UnsupportedOperationException();
	}
	
}
