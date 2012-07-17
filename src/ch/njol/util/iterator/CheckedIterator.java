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

import ch.njol.util.Checker;

public class CheckedIterator<T> implements Iterator<T> {
	
	private final Iterator<T> iter;
	private final Checker<T> checker;
	private boolean returnedNext = true;
	
	private T next;
	
	public CheckedIterator(final Iterator<T> iter, final Checker<T> checker) {
		this.iter = iter;
		this.checker = checker;
	}
	
	@Override
	public boolean hasNext() {
		if (!returnedNext)
			return true;
		if (!iter.hasNext())
			return false;
		returnedNext = false;
		while (iter.hasNext()) {
			next = iter.next();
			if (checker.check(next))
				return true;
		}
		return false;
	}
	
	@Override
	public T next() {
		if (!hasNext())
			throw new NoSuchElementException();
		returnedNext = true;
		return next;
	}
	
	@Override
	public void remove() {
		iter.remove();
	}
	
}
