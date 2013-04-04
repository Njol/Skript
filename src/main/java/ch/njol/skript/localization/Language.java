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
 * Copyright 2011-2013 Peter Güttinger
 * 
 */

package ch.njol.skript.localization;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.regex.Pattern;

import org.bukkit.plugin.Plugin;

import ch.njol.skript.Skript;
import ch.njol.skript.SkriptAddon;
import ch.njol.skript.config.Config;
import ch.njol.skript.util.Version;
import ch.njol.util.ExceptionUtils;
import ch.njol.util.Pair;
import ch.njol.util.StringUtils;

/**
 * @author Peter Güttinger
 */
public class Language {
	
	private static String name = "english";
	
	static HashMap<String, String> english = new HashMap<String, String>();
	/**
	 * Must never be null but set to {@link #english} instead.
	 */
	static HashMap<String, String> localized = english;
	private static boolean useLocal = false;
	
	private static HashMap<Plugin, Version> langVersion = new HashMap<Plugin, Version>();
	
	public static String getName() {
		return name;
	}
	
	private final static String get_i(final String key) {
		if (useLocal) {
			final String s = localized.get(key);
			if (s != null)
				return s;
		}
		return english.get(key);
	}
	
	/**
	 * Gets a string from the language file with the given key, or the english variant if the string is missing from the chosen language's file, or the key itself if the key does
	 * not exist.
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
	 * Equal to {@link #get(String)}, but returns null instead of the key if the key cannot be found.
	 * 
	 * @param key
	 * @return
	 */
	public static String get_(final String key) {
		return get_i(key.toLowerCase(Locale.ENGLISH));
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
	
	/**
	 * @param key
	 * @return Whether the given key exists in the <b>english</b> language file.
	 */
	public static boolean keyExists(final String key) {
		return english.containsKey(key.toLowerCase(Locale.ENGLISH));
	}
	
	public static void loadDefault(final SkriptAddon addon) {
		if (addon.getLanguageFileDirectory() == null)
			return;
		final InputStream din = addon.plugin.getResource(addon.getLanguageFileDirectory() + "/english.lang");
		if (din == null)
			throw new IllegalStateException(addon.getFile().getName() + " is missing the required english.lang file!");
		HashMap<String, String> en;
		try {
			en = new Config(din, "english.lang", false, false, ":").toMap(".");
		} catch (final Exception e) {
			throw Skript.exception(e, "Could not load " + addon + "'s default language file!");
		} finally {
			try {
				din.close();
			} catch (final IOException e) {}
		}
		langVersion.put(addon.plugin, new Version(en.get("version")));
		en.remove("version");
		english.putAll(en);
		for (final LanguageChangeListener l : listeners)
			l.onLanguageChange();
	}
	
	public static boolean load(String name) {
		name = name.toLowerCase();
		localized = new HashMap<String, String>();
		boolean exists = load(Skript.getAddonInstance(), name);
		for (final SkriptAddon addon : Skript.getAddons())
			exists |= load(addon, name);
		if (!exists) {
			localized = english;
			Language.name = "english";
			return false;
		}
		Language.name = name;
		validateLocalized();
		if (useLocal) {
			for (final LanguageChangeListener l : listeners)
				l.onLanguageChange();
		}
		return true;
	}
	
	private static boolean load(final SkriptAddon addon, final String name) {
		if (addon.getLanguageFileDirectory() == null)
			return false;
		final HashMap<String, String> l = load(addon.plugin.getResource(addon.getLanguageFileDirectory() + "/" + name + ".lang"), name);
		final File f = new File(addon.plugin.getDataFolder(), addon.getLanguageFileDirectory() + File.separator + name + ".lang");
		try {
			if (f.exists())
				l.putAll(load(new FileInputStream(f), name));
		} catch (final FileNotFoundException e) {
			assert false;
		}
		if (l.isEmpty())
			return false;
		if (!l.containsKey("version")) {
			Skript.error(addon + "'s language file " + name + ".lang does not provide a version number!");
		} else {
			try {
				final Version v = new Version(l.get("version"));
				if (v.isSmallerThan(langVersion.get(addon.plugin)))
					Skript.warning(addon + "'s language file " + name + ".lang is outdated, some messages will be english.");
			} catch (final IllegalArgumentException e) {
				Skript.error("Illegal version syntax in " + addon + "'s language file " + name + ".lang: " + e.getLocalizedMessage());
			}
		}
		l.remove("version");
		localized.putAll(l);
		return true;
	}
	
	private static HashMap<String, String> load(final InputStream in, final String name) {
		if (in == null)
			return new HashMap<String, String>();
		try {
			return new Config(in, name + ".lang", false, false, ":").toMap(".");
		} catch (final IOException e) {
			Skript.exception(e, "Could not load the language file '" + name + ".lang': " + ExceptionUtils.toString(e));
			return new HashMap<String, String>();
		} finally {
			try {
				in.close();
			} catch (final IOException e) {}
		}
	}
	
	private static void validateLocalized() {
		HashSet<String> s = new HashSet<String>(english.keySet());
		s.removeAll(localized.keySet());
		if (!s.isEmpty() && Skript.logNormal())
			Skript.error("The following messages have not been translated to " + name + ": " + StringUtils.join(s));
		s = new HashSet<String>(localized.keySet());
		s.removeAll(english.keySet());
		if (!s.isEmpty() && Skript.logHigh())
			Skript.warning("The localized language file(s) have superfluous entries: " + StringUtils.join(s));
	}
	
	private final static List<LanguageChangeListener> listeners = new ArrayList<LanguageChangeListener>();
	
	public static enum LanguageListenerPriority {
		EARLIEST, NORMAL;
	}
	
	private final static int[] priorityStartIndices = new int[LanguageListenerPriority.values().length];
	
	/**
	 * Registers a listener. The listener will immediately be called if a language has already been loaded.
	 * 
	 * @param l
	 */
	public static void addListener(final LanguageChangeListener l) {
		addListener(l, LanguageListenerPriority.NORMAL);
	}
	
	static void addListener(final LanguageChangeListener l, final LanguageListenerPriority priority) {
		assert priority != null;
		listeners.add(priorityStartIndices[priority.ordinal()], l);
		for (int i = priority.ordinal() + 1; i < LanguageListenerPriority.values().length; i++)
			priorityStartIndices[i]++;
		if (english != null)
			l.onLanguageChange();
	}
	
//	public static void removeListener(final LanguageChangeListener l) {
//		listeners.remove(l);
//	}
	
	// some general words - down here so that 'listeners' is initialized
	public final static Message m_and = new Message("and");
	public final static Message m_or = new Message("or");
	
	public static void setUseLocal(final boolean b) {
		if (useLocal == b)
			return;
		useLocal = b;
		for (final LanguageChangeListener l : listeners)
			l.onLanguageChange();
	}
	
	/**
	 * @param s String with ¦ plural markers
	 * @return (singular, plural)
	 */
	public static Pair<String, String> getPlural(final String s) {
		final int c = s.indexOf("¦");
		if (c == -1) // common
			return new Pair<String, String>(s, s);
		final int c2 = s.indexOf("¦", c + 1);
		if (c2 == -1) { // common¦plural
			return new Pair<String, String>(s.substring(0, c), s.substring(0, c) + s.substring(c + 1));
		} else {
			final int c3 = s.indexOf("¦", c2 + 1);
			if (c3 == -1) { // common¦singular¦plural
				return new Pair<String, String>(s.substring(0, c) + s.substring(c + 1, c2), s.substring(0, c) + s.substring(c2 + 1));
			} else { // common¦singular¦plural¦common
				return new Pair<String, String>(s.substring(0, c) + s.substring(c + 1, c2) + s.substring(c3 + 1), s.substring(0, c) + s.substring(c2 + 1, c3) + s.substring(c3 + 1));
			}
		}
	}
	
}
