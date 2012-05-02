package ch.njol.util.iterator;

import java.util.Iterator;

public class ImprovedIterator<T> implements Iterator<T> {
	
	private final Iterator<T> iter;
	private T current = null;
	
	public ImprovedIterator(final Iterator<T> iter) {
		this.iter = iter;
	}
	
	@Override
	public boolean hasNext() {
		return iter.hasNext();
	}
	
	@Override
	public T next() {
		if (!hasNext())
			return null;
		return current = iter.next();
	}
	
	@Override
	public void remove() {
		iter.remove();
	}
	
	public T current() {
		return current;
	}
	
}
