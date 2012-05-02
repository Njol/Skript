package ch.njol.util.iterator;

import java.util.Iterator;
import java.util.ListIterator;

public class ListRangeIterator<T> implements Iterator<T> {
	
	private final ListIterator<T> iter;
	private final int end;
	
	public ListRangeIterator(final ListIterator<T> iter, final int start, final int end) {
		this.iter = iter;
		for (int i = 0; i < start; i++)
			iter.next();
		this.end = end;
	}
	
	@Override
	public boolean hasNext() {
		return iter.nextIndex() < end;
	}
	
	@Override
	public T next() {
		return iter.next();
	}
	
	@Override
	public void remove() {
		iter.remove();
	}
	
}
