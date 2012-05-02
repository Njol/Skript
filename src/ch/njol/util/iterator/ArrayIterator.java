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
	private int current = 0;
	
	public ArrayIterator(final T[] array) {
		this.array = array;
	}
	
	@Override
	public boolean hasNext() {
		return current < array.length;
	}
	
	@Override
	public T next() {
		if (current >= array.length)
			throw new NoSuchElementException();
		return array[current++];
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
