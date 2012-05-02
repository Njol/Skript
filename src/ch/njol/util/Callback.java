package ch.njol.util;

public interface Callback<R, A> {
	public R run(A arg);
}
