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
import ch.njol.skript.config.OptionSection;
import ch.njol.skript.config.SectionNode;
import ch.njol.skript.localization.Language;
import ch.njol.skript.log.SkriptLogger;
import ch.njol.skript.log.Verbosity;
import ch.njol.skript.util.FileUtils;
import ch.njol.skript.util.Timespan;
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
	
	final static Option<String> version = new Option<String>("version", String.class)
			.optional(true)
			.defaultValue(null);
	
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
	
	final static Option<Boolean> checkForNewVersion = new Option<Boolean>("check for new version", Boolean.class)
			.defaultValue(false);
	final static Option<Timespan> updateCheckInterval = new Option<Timespan>("update check interval", Timespan.class)
			.defaultValue(new Timespan(12 * 60 * 60 * 1000))
			.setter(new Setter<Timespan>() {
				@Override
				public void set(final Timespan t) {
					if (t.getTicks() != 0 && Updater.checkerTask != null && !Updater.checkerTask.isAlive())
						Updater.checkerTask.setNextExecution(t.getTicks());
				}
			});
	final static Option<Boolean> automaticallyDownloadNewVersion = new Option<Boolean>("automatically download new version", Boolean.class)
			.defaultValue(false);
	
	public final static Option<Boolean> enableEffectCommands = new Option<Boolean>("enable effect commands", Boolean.class)
			.defaultValue(false);
	public final static Option<String> effectCommandToken = new Option<String>("effect command token", String.class)
			.defaultValue("!");
	public final static Option<Boolean> allowOpsToUseEffectCommands = new Option<Boolean>("allow ops to use effect commands", Boolean.class)
			.defaultValue(false);
	
	// everything handled by Variables
	public final static OptionSection databases = new OptionSection("databases");
	
	private final static Option<DateFormat> dateFormat = new Option<DateFormat>("date format", new Converter<String, DateFormat>() {
		@Override
		public DateFormat convert(final String s) {
			try {
				if (s.equalsIgnoreCase("default"))
					return null;
				return new SimpleDateFormat(s);
			} catch (final IllegalArgumentException e) {
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
	
	private final static Option<Verbosity> verbosity = new Option<Verbosity>("verbosity", new EnumParser<Verbosity>(Verbosity.class, "verbosity"))
			.defaultValue(Verbosity.NORMAL)
			.setter(new Setter<Verbosity>() {
				@Override
				public void set(final Verbosity v) {
					SkriptLogger.setVerbosity(v);
				}
			});
	
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
	
	public final static Option<Boolean> logPlayerCommands = new Option<Boolean>("log player commands", Boolean.class)
			.defaultValue(false);
	
	/**
	 * Maximum number of digits to display after the period for floats and doubles
	 */
	public final static Option<Integer> numberAccuracy = new Option<Integer>("number accuracy", Integer.class)
			.defaultValue(2);
	
	public final static Option<Integer> maxTargetBlockDistance = new Option<Integer>("maximum target block distance", Integer.class)
			.defaultValue(100);
	
	public final static Option<Boolean> caseSensitive = new Option<Boolean>("case sensitive", Boolean.class)
			.defaultValue(false);
	
	public final static Option<Boolean> disableVariableConflictWarnings = new Option<Boolean>("disable variable conflict warnings", Boolean.class)
			.defaultValue(false);
	
	public final static Option<Boolean> enableScriptCaching = new Option<Boolean>("enable script caching", Boolean.class)
			.optional(true)
			.defaultValue(false);
	
	public final static Option<Boolean> keepConfigsLoaded = new Option<Boolean>("keep configs loaded", Boolean.class)
			.optional(true)
			.defaultValue(false);
	
	/**
	 * This should only be used in special cases
	 */
	public final static Config getConfig() {
		return mainConfig;
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
				Skript.error("Config file 'config.sk' does not exist!");
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
			
			if (!Skript.getVersion().toString().equals(mainConfig.get(version.key))) {
				try {
					final Config newConfig = new Config(Skript.getInstance().getResource("config.sk"), "Skript.jar/config.sk", false, false, ":");
					
					boolean manualUpdate = false;
					
					if (mainConfig.getMainNode().get("database") != null) { // old database layout
						manualUpdate = true;
						try {
							final SectionNode oldDB = (SectionNode) mainConfig.getMainNode().get("database");
							final SectionNode dbs = (SectionNode) newConfig.getMainNode().get("databases");
							final SectionNode newDB = (SectionNode) dbs.get("database 1");
							newDB.setValues(oldDB);
							
							// was dynamically added before
							final String file = newDB.getValue("file");
							if (!file.endsWith(".db"))
								newDB.set("file", file + ".db");
							
							final SectionNode def = (SectionNode) dbs.get("default");
							def.set("backup interval", mainConfig.get("variables backup interval"));
						} catch (final Exception e) {
							Skript.exception("An error occurred while trying to update the config's database section.",
									"You'll have to update the config yourself:",
									"Open the new config.sk as well as the created backup, and move the 'database' section from the backup to the start of the 'databases' section",
									"of the new config (i.e. the line 'databases:' should be directly above 'database:'), and add a tab in front of every line that you just copied.");
						}
					}
					
					if (manualUpdate | newConfig.setValues(mainConfig)) {
						final File bu = FileUtils.backup(config);
						mainConfig = newConfig;
						mainConfig.getMainNode().set("version", Skript.getVersion().toString());
						mainConfig.save(config);
						Skript.info("Your configuration has been updated to the latest version. A backup of your old config file has been created as " + bu.getName());
					} else {
						mainConfig.getMainNode().set("version", Skript.getVersion().toString());
						mainConfig.save(config);
					}
				} catch (final IOException e) {
					Skript.error("Could not load the new config from the jar file: " + e.getLocalizedMessage());
				}
			}
			
			mainConfig.load(SkriptConfig.class);
			
//			if (!keepConfigsLoaded.value())
//				mainConfig = null;
		} catch (final Exception e) {
			Skript.exception(e, "An error occurred while loading the config");
			return false;
		}
		return true;
	}
	
}
