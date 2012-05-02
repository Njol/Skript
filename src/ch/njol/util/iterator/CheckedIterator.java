package ch.njol.util.iterator;

import java.util.Iterator;
import java.util.NoSuchElementException;

import ch.njol.util.Checker;

public class CheckedIterator<T> implements Iterator<T> {
	
	private final Iterator<T> iter;
	private final Checker<T> checker;
	private boolean calledNext = false;
	
	private T current;
	
	public CheckedIterator(final Iterator<T> iter, final Checker<T> checker) {
		this.iter = iter;
		this.checker = checker;
	}
	
	/**
	 * Advances the iteration no matter whether next() was called.
	 */
	@Override
	public boolean hasNext() {
		calledNext = false;
		while (iter.hasNext()) {
			current = iter.next();
			if (checker.check(current))
				return true;
		}
		return false;
	}
	
	@Override
	public T next() {
		if (calledNext && !hasNext())
			throw new NoSuchElementException();
		calledNext = true;
		return current;
	}
	
	@Override
	public void remove() {
		iter.remove();
	}
	
}
