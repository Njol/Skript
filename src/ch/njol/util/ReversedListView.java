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

package ch.njol.util;

import java.lang.reflect.Array;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;

import ch.njol.util.iterator.ReversedListIterator;

/**
 * @author Peter Güttinger
 * 
 */
public class ReversedListView<T> implements List<T> {
	
	private final List<T> list;
	
	public ReversedListView(final List<T> list) {
		Validate.notNull(list, "list");
		this.list = list;
	}
	
	@Override
	public int size() {
		return list.size();
	}
	
	@Override
	public boolean isEmpty() {
		return list.isEmpty();
	}
	
	@Override
	public boolean contains(final Object o) {
		return list.contains(o);
	}
	
	@Override
	public Iterator<T> iterator() {
		return new ReversedListIterator<T>(list);
	}
	
	@Override
	public ListIterator<T> listIterator() {
		return new ReversedListIterator<T>(list);
	}
	
	@Override
	public ListIterator<T> listIterator(final int index) {
		return new ReversedListIterator<T>(list, index);
	}
	
	@Override
	public Object[] toArray() {
		final Object[] r = new Object[size()];
		int i = 0;
		for (final Object o : this)
			r[i++] = o;
		return r;
	}
	
	@SuppressWarnings("unchecked")
	@Override
	public <R> R[] toArray(final R[] a) {
		final R[] t = a.length >= size() ? a : (R[]) Array.newInstance(a.getClass().getComponentType(), size());
		int i = 0;
		for (final T o : this)
			t[i++] = (R) o;
		if (t.length > size())
			t[size()] = null;
		return t;
	}
	
	@Override
	public boolean add(final T e) {
		list.add(0, e);
		return true;
	}
	
	@Override
	public boolean remove(final Object o) {
		final int i = list.lastIndexOf(o);
		if (i != -1)
			list.remove(i);
		return i != -1;
	}
	
	@Override
	public boolean containsAll(final Collection<?> c) {
		return list.containsAll(c);
	}
	
	@Override
	public boolean addAll(final Collection<? extends T> c) {
		return list.addAll(0, c);
	}
	
	@Override
	public boolean addAll(final int index, final Collection<? extends T> c) {
		return list.addAll(size() - index, c);
	}
	
	@Override
	public boolean removeAll(final Collection<?> c) {
		return list.removeAll(c);
	}
	
	@Override
	public boolean retainAll(final Collection<?> c) {
		return list.retainAll(c);
	}
	
	@Override
	public void clear() {
		list.clear();
	}
	
	@Override
	public T get(final int index) {
		return list.get(size() - index - 1);
	}
	
	@Override
	public T set(final int index, final T element) {
		return list.set(size() - index - 1, element);
	}
	
	@Override
	public void add(final int index, final T element) {
		list.add(size() - index, element);
	}
	
	@Override
	public T remove(final int index) {
		return list.remove(size() - index - 1);
	}
	
	@Override
	public int indexOf(final Object o) {
		return size() - list.lastIndexOf(o) - 1;
	}
	
	@Override
	public int lastIndexOf(final Object o) {
		return size() - list.indexOf(o) - 1;
	}
	
	@Override
	public List<T> subList(final int fromIndex, final int toIndex) {
		return new ReversedListView<T>(list.subList(size() - toIndex, size() - fromIndex));
	}
	
}
