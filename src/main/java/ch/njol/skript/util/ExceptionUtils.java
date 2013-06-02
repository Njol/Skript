package ch.njol.skript.util;

import java.io.IOException;

import ch.njol.skript.localization.Language;

public abstract class ExceptionUtils {
	private ExceptionUtils() {}
	
	private final static String IO_NODE = "io exceptions";
	
	public final static String toString(final IOException e) {
		if (Language.keyExists(IO_NODE + "." + e.getClass().getSimpleName())) {
			return Language.format(IO_NODE + "." + e.getClass().getSimpleName(), e.getLocalizedMessage());
		}
		return e.getLocalizedMessage();
	}
	
}
