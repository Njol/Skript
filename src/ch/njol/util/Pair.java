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

import java.util.Map.Entry;

/**
 * 
 * @author Peter Güttinger
 * 
 */
public class Pair<T1, T2> implements Entry<T1, T2>, Cloneable {
	public T1 first;
	public T2 second;
	
	public Pair() {
		first = null;
		second = null;
	}
	
	public Pair(final T1 first, final T2 second) {
		this.first = first;
		this.second = second;
	}
	
	public Pair(final Entry<T1, T2> e) {
		this.first = e.getKey();
		this.second = e.getValue();
	}
	
	/**
	 * @return "first,second"
	 */
	@Override
	public String toString() {
		return "" + first + "," + second;
	}
	
	/**
	 * Checks for equality with Entries to match {@link #hashCode()}
	 */
	@Override
	public boolean equals(final Object obj) {
		if (obj == this)
			return true;
		if (!(obj instanceof Entry))
			return false;
		final Entry<?, ?> other = (Entry<?, ?>) obj;
		return (first == null ? other.getKey() == null : first.equals(other.getKey())) &&
				(second == null ? other.getValue() == null : second.equals(other.getValue()));
	}
	
	/**
	 * As defined by {@link Entry#hashCode()}
	 */
	@Override
	public int hashCode() {
		return (first == null ? 0 : first.hashCode()) ^ (second == null ? 0 : second.hashCode());
	}
	
	@Override
	public T1 getKey() {
		return first;
	}
	
	@Override
	public T2 getValue() {
		return second;
	}
	
	@Override
	public T2 setValue(final T2 value) {
		throw new UnsupportedOperationException();
	}
	
	/**
	 * @return a shallow copy of this pair
	 */
	@Override
	public Pair<T1, T2> clone() throws CloneNotSupportedException {
		return (Pair<T1, T2>) super.clone();
	}
	
}
