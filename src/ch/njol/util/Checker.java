package ch.njol.util;

public interface Checker<T> {
	
	public boolean check(T o);
	
	public static final Checker<Object> nullChecker = new Checker<Object>() {
		@Override
		public boolean check(final Object o) {
			return o != null;
		}
	};
	
}
