package ch.njol.util;

import java.util.Map.Entry;

/**
 * 
 * @author Peter Güttinger
 * 
 */
public class Pair<T1, T2> implements Entry<T1, T2> {
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
	
	@Override
	public boolean equals(final Object obj) {
		if (obj == this)
			return true;
		if (!(obj instanceof Pair))
			return false;
		final Pair<?, ?> other = (Pair<?, ?>) obj;
		return (first == null ? other.first == null : first.equals(other.first)) &&
				(second == null ? other.second == null : second.equals(other.second));
	}
	
	@Override
	public int hashCode() {
		return first.hashCode() ^ ~second.hashCode();
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
		final T2 s = second;
		second = value;
		return s;
	}
}
