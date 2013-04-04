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

package ch.njol.skript;

import java.io.File;
import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collection;

import org.bukkit.event.EventPriority;

import ch.njol.skript.classes.Converter;
import ch.njol.skript.config.Config;
import ch.njol.skript.config.EnumParser;
import ch.njol.skript.config.Option;
import ch.njol.skript.config.Section;
import ch.njol.skript.localization.Language;
import ch.njol.skript.log.SkriptLogger;
import ch.njol.skript.log.Verbosity;
import ch.njol.skript.util.Timespan;
import ch.njol.skript.variables.Variables;
import ch.njol.util.Setter;

/**
 * Important: don't save values from the config, a '/skript reload config/configs/all' won't work correctly otherwise!
 * 
 * @author Peter Güttinger
 */
@SuppressWarnings("unused")
public abstract class SkriptConfig {
	private SkriptConfig() {}
	
	static Config mainConfig;
	static Collection<Config> configs = new ArrayList<Config>();
	
	public final static Option<Boolean> keepConfigsLoaded = new Option<Boolean>("keep configs loaded", Boolean.class)
			.optional(true)
			.defaultValue(false);
	
	public static final Option<Boolean> enableEffectCommands = new Option<Boolean>("enable effect commands", Boolean.class)
			.defaultValue(false);
	public static final Option<String> effectCommandToken = new Option<String>("effect command token", String.class)
			.defaultValue("!");
	
	static final Option<Boolean> checkForNewVersion = new Option<Boolean>("check for new version", Boolean.class)
			.defaultValue(false);
	static final Option<Boolean> automaticallyDownloadNewVersion = new Option<Boolean>("automatically download new version", Boolean.class)
			.defaultValue(false);
	
	public static final Option<Boolean> logPlayerCommands = new Option<Boolean>("log player commands", Boolean.class)
			.defaultValue(false);
	
	public static final Option<Boolean> disableVariableConflictWarnings = new Option<Boolean>("disable variable conflict warnings", Boolean.class)
			.defaultValue(false);
	
	public static final Option<Timespan> variableBackupInterval = new Option<Timespan>("variables backup interval", Timespan.class)
			.defaultValue(new Timespan(0))
			.setter(new Setter<Timespan>() {
				@Override
				public void set(final Timespan t) {
					if (Variables.file.backupTask != null) { // initial schedule accesses this value directly
						if (t.getTicks() == 0)
							Variables.file.backupTask.cancel();
						else
							Variables.file.backupTask.setPeriod(t.getTicks());
					}
				}
			});
	
	private static final Option<DateFormat> dateFormat = new Option<DateFormat>("date format", new Converter<String, DateFormat>() {
		@Override
		public DateFormat convert(final String s) {
			try {
				if (s.equalsIgnoreCase("default"))
					return null;
				return new SimpleDateFormat(s);
			} catch (final IllegalArgumentException e) {
				// TODO shorten URL?
				Skript.error("'" + s + "' is not a valid date format. Please refer to http://docs.oracle.com/javase/6/docs/api/java/text/SimpleDateFormat.html for instructions on the format.");
			}
			return null;
		}
	}).defaultValue(DateFormat.getDateTimeInstance(DateFormat.SHORT, DateFormat.SHORT));
	
	public final static String formatDate(final long timestamp) {
		final DateFormat f = dateFormat.value();
		synchronized (f) {
			return f.format(timestamp);
		}
	}
	
	public static final Option<Boolean> enableScriptCaching = new Option<Boolean>("enable script caching", Boolean.class)
			.optional(true)
			.defaultValue(false);
	
	public final static Option<EventPriority> defaultEventPriority = new Option<EventPriority>("plugin priority", new Converter<String, EventPriority>() {
		@Override
		public EventPriority convert(final String s) {
			try {
				return EventPriority.valueOf(s.toUpperCase());
			} catch (final IllegalArgumentException e) {
				Skript.error("The plugin priority has to be one of lowest, low, normal, high, or highest.");
				return null;
			}
		}
	}).defaultValue(EventPriority.NORMAL);
	
	private final static Option<Verbosity> verbosity = new Option<Verbosity>("verbosity", new EnumParser<Verbosity>(Verbosity.class, "verbosity"))
			.defaultValue(Verbosity.NORMAL)
			.setter(new Setter<Verbosity>() {
				@Override
				public void set(final Verbosity v) {
					SkriptLogger.setVerbosity(v);
				}
			});
	
	public final static Option<String> language = new Option<String>("language", String.class)
			.optional(true)
			.setter(new Setter<String>() {
				@Override
				public void set(final String s) {
					if (!Language.load(s)) {
						Skript.error("No language file found for '" + s + "'!");
					}
				}
			});
	
	public static final Option<Integer> maxTargetBlockDistance = new Option<Integer>("maximum target block distance", Integer.class)
			.defaultValue(100);
	
	/**
	 * maximum number of digits to display after the period for floats and doubles
	 */
	public static final Option<Integer> numberAccuracy = new Option<Integer>("number accuracy", Integer.class)
			.defaultValue(2);
	
	public final static Section database = new Section("database") {
		private final Option<String> type = new Option<String>("type", String.class);
		private final Option<String> pattern = new Option<String>("pattern", String.class);
		private final Option<Boolean> monitor_changes = new Option<Boolean>("monitor changes", Boolean.class);
		private final Option<Timespan> monitor_interval = new Option<Timespan>("monitor interval", Timespan.class);
		private final Option<String> host = new Option<String>("host", String.class);
		private final Option<Integer> port = new Option<Integer>("port", Integer.class);
		private final Option<String> user = new Option<String>("user", String.class);
		private final Option<String> password = new Option<String>("password", String.class);
		private final Option<String> database = new Option<String>("database", String.class);
		private final Option<String> file = new Option<String>("file", String.class);
	};
	
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
				Skript.error("Config file 'config.sk' does not exist! Please make sure that you downloaded the .zip file (i.e. not the .jar) from Skript's BukkitDev page and extracted it correctly.");
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
			
			mainConfig.load(SkriptConfig.class);
			
			if (!keepConfigsLoaded.value())
				mainConfig = null;
		} catch (final Exception e) {
			Skript.exception(e, "An error occurred while loading the config");
			return false;
		}
		return true;
	}
	
}
