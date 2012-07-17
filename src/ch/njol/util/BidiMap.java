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

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

/**
 * @author Peter Güttinger
 * 
 */
public class BidiMap<T1, T2> extends HashMap<T1, T2> {
	
	private static final long serialVersionUID = -9011678701069901061L;
	
	private final BidiMap<T2, T1> other;
	
	private BidiMap(final BidiMap<T2, T1> other) {
		this.other = other;
	}
	
	public BidiMap() {
		other = new BidiMap<T2, T1>(this);
	}
	
	public BidiMap(final Map<? extends T1, ? extends T2> values) {
		other = new BidiMap<T2, T1>(this);
		putAll(values);
	}
	
	public BidiMap<T2, T1> getReverseView() {
		return other;
	}
	
	public T1 getKey(final Object value) {
		return other.get(value);
	}
	
	public T2 getValue(final Object key) {
		return get(key);
	}
	
	private T2 putDirect(final T1 key, final T2 value) {
		return super.put(key, value);
	}
	
	@Override
	public T2 put(final T1 key, final T2 value) {
		if (key == null || value == null)
			throw new NullPointerException("Can't store null in a BidiMap");
		
		final T2 oldValue = putDirect(key, value);
		if (oldValue != null && oldValue.equals(value)) {
			other.putDirect(value, key);
			return oldValue;
		}
		if (oldValue != null)
			other.removeDirect(oldValue);
		
		final T1 oldKey = other.putDirect(value, key);
		if (oldKey != null)
			this.removeDirect(oldKey);
		
		return oldValue;
	}
	
	@Override
	public void putAll(final Map<? extends T1, ? extends T2> m) {
		for (final Entry<? extends T1, ? extends T2> e : m.entrySet()) {
			put(e.getKey(), e.getValue());
		}
	}
	
	private T2 removeDirect(final Object key) {
		return super.remove(key);
	}
	
	@Override
	public T2 remove(final Object key) {
		final T2 oldValue = removeDirect(key);
		if (oldValue != null)
			other.removeDirect(oldValue);
		return oldValue;
	}
	
	private void clearDirect() {
		super.clear();
	}
	
	@Override
	public void clear() {
		this.clearDirect();
		other.clearDirect();
	}
	
	@Override
	public boolean containsValue(final Object value) {
		return other.containsKey(value);
	}
	
	@Override
	public Collection<T2> values() {
		return other.keySet();
	}
	
	public Set<T2> valueSet() {
		return other.keySet();
	}
	
	@Override
	public BidiMap<T1, T2> clone() {
		return new BidiMap<T1, T2>(this);
	}
	
}
