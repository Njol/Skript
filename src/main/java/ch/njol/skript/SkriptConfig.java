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

import java.io.File;
import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collection;

import org.bukkit.event.EventPriority;

import ch.njol.skript.config.Config;
import ch.njol.skript.config.validate.EnumEntryValidator;
import ch.njol.skript.config.validate.SectionValidator;
import ch.njol.skript.log.SkriptLogger;
import ch.njol.skript.log.Verbosity;
import ch.njol.skript.registrations.Classes;
import ch.njol.skript.util.Timespan;
import ch.njol.util.Setter;

/**
 * @author Peter Güttinger
 * 
 */
public abstract class SkriptConfig {
	
	private SkriptConfig() {}
	
	static Config mainConfig;
	static Collection<Config> configs = new ArrayList<Config>();
	static boolean keepConfigsLoaded = false;
	
	public static boolean enableEffectCommands = false;
	public static String effectCommandToken = "!";
	
	static boolean checkForNewVersion = false;
	static boolean automaticallyDownloadNewVersion = false;
	
	public static boolean logPlayerCommands = false;
	
	public static boolean disableVariableConflictWarnings;
	
	public static Timespan variableBackupPeriod = null;
	
	private static DateFormat dateFormat = DateFormat.getDateTimeInstance(DateFormat.SHORT, DateFormat.SHORT);
	
	public static boolean enableScriptCaching = false;
	
	public static DateFormat getDateFormat() {
		return dateFormat;
	}
	
	static boolean load() {
		try {
			final File oldConfig = new File(Skript.getInstance().getDataFolder(), "config.cfg");
			final File config = new File(Skript.getInstance().getDataFolder(), "config.sk");
			if (oldConfig.exists()) {
				if (!config.exists()) {
					oldConfig.renameTo(config);
					Skript.info("[1.3] Renamed your 'config.cfg' to 'config.sk' to match the new format");
				} else {
					Skript.error("Found both a new and an old config, ingoring the old one");
				}
			}
			if (!config.exists()) {
				Skript.error("Config file 'config.sk' does not exist! Please make sure that you downloaded the .zip file (and not the .jar) from Skript's BukkitDev page and extracted it correctly.");
				return false;
			}
			if (!config.canRead()) {
				Skript.error("Config file 'config.sk' cannot be read!");
				return false;
			}
			
			try {
				mainConfig = new Config(config, false, false, ":");
			} catch (final IOException e) {
				Skript.error("Could not load the main config: " + e.getLocalizedMessage());
				return false;
			}
			
			new SectionValidator()
					.addNode("verbosity", new EnumEntryValidator<Verbosity>(Verbosity.class, new Setter<Verbosity>() {
						@Override
						public void set(final Verbosity v) {
							SkriptLogger.setVerbosity(v);
						}
					}), false)
					.addNode("plugin priority", new EnumEntryValidator<EventPriority>(EventPriority.class, new Setter<EventPriority>() {
						@Override
						public void set(final EventPriority p) {
							Skript.defaultEventPriority = p;
						}
					}, "lowest, low, normal, high, highest"), false)
					.addEntry("keep configs loaded", Classes.getExactParser(Boolean.class), new Setter<Boolean>() {
						@Override
						public void set(final Boolean b) {
							keepConfigsLoaded = b;
						}
					}, true)
					.addEntry("enable effect commands", Classes.getExactParser(Boolean.class), new Setter<Boolean>() {
						@Override
						public void set(final Boolean b) {
							enableEffectCommands = b;
						}
					}, false)
					.addEntry("effect command token", new Setter<String>() {
						@Override
						public void set(final String s) {
							if (s.startsWith("/")) {
								Skript.error("Cannot use a token that starts with a slash because it can conflict with commands");
							} else {
								effectCommandToken = s;
							}
						}
					}, false)
					.addEntry("date format", new Setter<String>() {
						@Override
						public void set(final String s) {
							try {
								if (!s.equalsIgnoreCase("default"))
									dateFormat = new SimpleDateFormat(s);
							} catch (final IllegalArgumentException e) {
								// TODO shorten URL?
								Skript.error("'" + s + "' is not a valid date format. Please refer to http://docs.oracle.com/javase/6/docs/api/java/text/SimpleDateFormat.html for instructions on the format.");
							}
						}
					}, true)
					.addEntry("disable variable conflict warnings", Classes.getExactParser(Boolean.class), new Setter<Boolean>() {
						@Override
						public void set(final Boolean b) {
							disableVariableConflictWarnings = b;
						}
					}, true)
					.addEntry("language", new Setter<String>() {
						@Override
						public void set(final String s) {
							if (!Language.load(s)) {
								Skript.error("No language file found for '" + s + "'!");
							}
						}
					}, true)
					.addEntry("check for new version", Classes.getExactParser(Boolean.class), new Setter<Boolean>() {
						@Override
						public void set(final Boolean b) {
							checkForNewVersion = b;
						}
					}, false)
					.addEntry("automatically download new version", Classes.getExactParser(Boolean.class), new Setter<Boolean>() {
						@Override
						public void set(final Boolean b) {
							automaticallyDownloadNewVersion = b;
						}
					}, false)
					.addEntry("log player commands", Classes.getExactParser(Boolean.class), new Setter<Boolean>() {
						@Override
						public void set(final Boolean b) {
							logPlayerCommands = b;
						}
					}, true)
					.addEntry("variables backup interval", Classes.getExactParser(Timespan.class), new Setter<Timespan>() {
						@Override
						public void set(final Timespan t) {
							if (t.getTicks() != 0) {
								variableBackupPeriod = t;
								// task is scheduled when the variables are loaded
							}
						}
					}, true)
					.addEntry("enable script caching", Classes.getExactParser(Boolean.class), new Setter<Boolean>() {
						@Override
						public void set(final Boolean b) {
							enableScriptCaching = b;
						}
					}, true)
					.validate(mainConfig.getMainNode());
			
			if (!keepConfigsLoaded)
				mainConfig = null;
		} catch (final Exception e) {
			Skript.exception(e, "error loading config");
			return false;
		}
		return true;
	}
	
}
