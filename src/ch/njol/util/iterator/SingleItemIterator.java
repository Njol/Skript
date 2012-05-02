package ch.njol.util.iterator;

import java.util.Iterator;

/**
 * @author Peter Güttinger
 * 
 */
public class SingleItemIterator<T> implements Iterator<T> {
	
	private final T item;
	private boolean returned = false;
	
	public SingleItemIterator(final T item) {
		this.item = item;
	}
	
	@Override
	public boolean hasNext() {
		return !returned;
	}
	
	@Override
	public T next() {
		returned = true;
		return item;
	}
	
	@Override
	public void remove() {}
	
}
