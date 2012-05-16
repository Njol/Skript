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

/**
 * @author Peter Güttinger
 * 
 */
public class StoppableIterator<T> implements Iterator<T> {
	
	private final Iterator<T> iter;
	private final Checker<T> stopper;
	private final boolean returnLast;
	private T current;
	
	private boolean stopped = false;
	private boolean calledNext = false;
	
	/**
	 * 
	 * @param iter
	 * @param stopper Called for every element. If it returns true the iteration is stopped.
	 * @param returnLast whether to return the last element, i.e. the element on which the stopper stopped, or not.
	 */
	public StoppableIterator(final Iterator<T> iter, final Checker<T> stopper, final boolean returnLast) {
		this.iter = iter;
		this.stopper = stopper;
		this.returnLast = returnLast;
		if (!returnLast && iter.hasNext())
			current = iter.next();
	}
	
	@Override
	public boolean hasNext() {
		final boolean cn = calledNext;
		calledNext = false;
		if (stopped || !iter.hasNext())
			return false;
		if (cn && !returnLast) {
			if (!iter.hasNext())
				return false;
			current = iter.next();
			if (stopper.check(current)) {
				stop();
				return false;
			}
		}
		return true;
	}
	
	@Override
	public T next() {
		if (calledNext && !hasNext())
			throw new NoSuchElementException();
		calledNext = true;
		if (!returnLast)
			return current;
		final T t = iter.next();
		if (stopper != null && stopper.check(t))
			stop();
		return t;
	}
	
	@Override
	public void remove() {
		iter.remove();
	}
	
	public void stop() {
		stopped = true;
	}
	
}
