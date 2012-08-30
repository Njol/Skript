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

package ch.njol.skript;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Collection;
import java.util.HashSet;
import java.util.Properties;

/**
 * @author Peter Güttinger
 */
public class Language {
	
	private static String name = "english";
	
	private static Properties defaults = new Properties();
	private static Properties strings = new Properties(defaults);
	
	public static String getName() {
		return name;
	}
	
	public static String get(final String key) {
		return strings.getProperty(key.toLowerCase());
	}
	
	public static String getDefault(final String key) {
		return defaults.getProperty(key.toLowerCase());
	}
	
	public static String format(final String key, final Object... args) {
		return String.format(get(key), args);
	}
	
	/**
	 * Gets a localized string surrounded by spaces, or a space if the string is empty
	 * 
	 * @param key
	 * @return
	 */
	public static String getSpaced(final String key) {
		final String s = strings.getProperty(key.toLowerCase());
		if (s.isEmpty())
			return " ";
		return " " + s + " ";
	}
	
	static void loadDefault() {
		final InputStream din = Skript.getInstance().getResource("lang/english.lang");
		if (din == null)
			throw new IllegalStateException("Skript.jar is missing the required english.lang file!");
		try {
			defaults.load(new InputStreamReader(din, "UTF-8"));
		} catch (final IOException e) {
			throw Skript.exception(e, "Could not load the default language file!");
		}
		for (final LanguageChangeListener l : listeners)
			l.onLanguageChange();
	}
	
	static boolean load(final String name) {
		final InputStream in = Skript.getInstance().getResource("lang/" + name + ".lang");
		if (in == null)
			return false;
		try {
			strings.load(new InputStreamReader(in, "UTF-8"));
		} catch (final IOException e) {
			throw Skript.exception(e, "Could not load the language file!");
		}
		Language.name = name;
		for (final LanguageChangeListener l : listeners)
			l.onLanguageChange();
		return true;
	}
	
	static void clear() {
		strings.clear();
	}
	
	private final static Collection<LanguageChangeListener> listeners = new HashSet<LanguageChangeListener>();
	
	public static interface LanguageChangeListener {
		public void onLanguageChange();
	}
	
	public static void addListener(final LanguageChangeListener l) {
		listeners.add(l);
		if (!defaults.isEmpty())
			l.onLanguageChange();
	}
	
	public static void removeListener(final LanguageChangeListener l) {
		listeners.remove(l);
	}
	
}
