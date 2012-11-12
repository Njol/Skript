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

package ch.njol.skript.localization;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map.Entry;
import java.util.regex.Pattern;

import ch.njol.skript.Skript;
import ch.njol.skript.config.Config;

/**
 * @author Peter Güttinger
 */
public class Language {
	
	private static String name = "english";
	
	private static HashMap<String, String> english = new HashMap<String, String>();
	private static HashMap<String, String> englishPlurals = new HashMap<String, String>();
	
	private static HashMap<String, String> localized;
	private static HashMap<String, String> localizedPlurals;
	
	private static boolean useLocal = false;
	
	public static String getName() {
		return name;
	}
	
	private final static String get_i(final String key) {
		if (useLocal && localized != null) {
			final String s = localized.get(key);
			if (s != null)
				return s;
		}
		return english.get(key);
	}
	
	/**
	 * Gets a string from the language file with the given key, or the english variant if the string is missing from the chosen language's file, or the key itself if the key is
	 * invalid.
	 * 
	 * @param key
	 * @return
	 */
	public static String get(final String key) {
		final String s = get_i(key.toLowerCase(Locale.ENGLISH));
		if (s == null)
			return key.toLowerCase(Locale.ENGLISH);
		return s;
	}
	
	/**
	 * Gets the plural of a word, or the singular if no plural is defined for the given key, or the key itself if it's invalid.
	 * 
	 * @param key
	 * @return
	 */
	public static String getPlural(final String key) {
		if (useLocal && localized != null) {
			String s = localizedPlurals.get(key);
			if (s != null)
				return s;
			s = localized.get(key);
			if (s != null)
				return s;
		}
		String s = englishPlurals.get(key);
		if (s != null)
			return s;
		s = english.get(key);
		if (s != null)
			return s;
		return key;
	}
	
	/**
	 * Gets either the plural or singular version of a word, i.e. returns {@link #getPlural(String)} if <tt>plural</tt> is <tt>true</tt> and {@link #get(String)} otherwise.
	 * 
	 * @param key
	 * @param plural
	 * @return
	 */
	public static String get(final String key, final boolean plural) {
		return plural ? getPlural(key) : get(key);
	}
	
	/**
	 * Gets a string and uses it as format in {@link String#format(String, Object...)}.
	 * 
	 * @param key
	 * @param args The arguments to pass to {@link String#format(String, Object...)}
	 * @return The formatted string
	 */
	public static String format(String key, final Object... args) {
		key = key.toLowerCase(Locale.ENGLISH);
		final String value = get_i(key);
		if (value == null)
			return key;
		try {
			return String.format(value, args);
		} catch (final Exception e) {
			return key;
		}
	}
	
	/**
	 * Gets a localized string surrounded by spaces, or a space if the string is empty
	 * 
	 * @param key
	 * @return The spaced string or " "+key+" " if the entry is missing from the file.
	 */
	public static String getSpaced(final String key) {
		final String s = get(key.toLowerCase(Locale.ENGLISH));
		if (s.isEmpty())
			return " ";
		return " " + s + " ";
	}
	
	private final static Pattern split = Pattern.compile("\\s*,\\s*");
	
	/**
	 * Gets a list of strings.
	 * 
	 * @param key
	 * @return a non-null String array with at least one element
	 */
	public static String[] getList(final String key) {
		final String s = get_i(key.toLowerCase(Locale.ENGLISH));
		if (s == null)
			return new String[] {key.toLowerCase(Locale.ENGLISH)};
		return split.split(s);
	}
	
	private final static InputStream getInputStream(final String name) {
		final File f = new File(Skript.getInstance().getDataFolder(), "lang" + File.separator + name + ".lang");
		try {
			if (f.exists())
				return new FileInputStream(f);
		} catch (final FileNotFoundException e) {
			assert false;
		}
		return Skript.getInstance().getResource("lang/" + name + ".lang");
	}
	
	public static void loadDefault() {
		final InputStream din = getInputStream("english");
		if (din == null)
			throw new IllegalStateException("Skript.jar is missing the required english.lang file!");
		try {
			english = new Config(din, "english.lang", false, false, ":").toMap(".");
		} catch (final Exception e) {
			throw Skript.exception(e, "Could not load the default language file!");
		} finally {
			try {
				din.close();
			} catch (final IOException e) {}
		}
		makePlurals(english, englishPlurals);
		for (final LanguageChangeListener l : listeners)
			l.onLanguageChange();
	}
	
	public static boolean load(String name) {
		name = name.toLowerCase();
		final InputStream in = getInputStream(name);
		if (in == null)
			return false;
		try {
			localized = new Config(in, name + ".lang", false, false, ":").toMap(".");
		} catch (final IOException e) {
			Skript.exception(e, "Could not load the language file '" + name + ".lang': " + e.getLocalizedMessage());
			return false;
		} finally {
			try {
				in.close();
			} catch (final IOException e) {}
		}
		localizedPlurals = new HashMap<String, String>();
		makePlurals(localized, localizedPlurals);
		Language.name = name;
		if (useLocal) {
			for (final LanguageChangeListener l : listeners)
				l.onLanguageChange();
		}
		return true;
	}
	
	private static final void makePlurals(final HashMap<String, String> lang, final HashMap<String, String> plurals) {
		for (final Entry<String, String> e : lang.entrySet()) {
			final String s = e.getValue();
			final int c = s.indexOf("¦");
			if (c == -1)
				continue;
			final int c2 = s.indexOf("¦", c + 1);
			if (c2 == -1) {
				e.setValue(s.substring(0, c));
			} else {
				e.setValue(s.substring(0, c) + s.substring(c + 1, c2));
			}
			plurals.put(e.getKey(), s.substring(0, c) + s.substring((c2 == -1 ? c : c2) + 1));
		}
	}
	
	private final static Collection<LanguageChangeListener> listeners = new ArrayList<LanguageChangeListener>();
	
	public static interface LanguageChangeListener {
		public void onLanguageChange();
	}
	
	public static void addListener(final LanguageChangeListener l) {
		listeners.add(l);
		if (english != null)
			l.onLanguageChange();
	}
	
//	public static void removeListener(final LanguageChangeListener l) {
//		listeners.remove(l);
//	}
	
	public static void setUseLocal(final boolean b) {
		if (useLocal == b)
			return;
		useLocal = b;
		for (final LanguageChangeListener l : listeners)
			l.onLanguageChange();
	}
	
}
