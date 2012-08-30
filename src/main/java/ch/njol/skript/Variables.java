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

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.WeakHashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.event.Event;

import ch.njol.skript.log.SkriptLogger;
import ch.njol.skript.log.SubLog;
import ch.njol.skript.util.FileUtils;
import ch.njol.skript.util.Task;
import ch.njol.skript.util.Version;
import ch.njol.util.Callback;
import ch.njol.util.Pair;
import ch.njol.util.StringUtils;

/**
 * @author Peter Güttinger
 */
public abstract class Variables {
	
	private Variables() {}
	
	private final static String varFileName = "variables";
	private final static String varFileExt = "csv";
	
	private static Task saveTask;
	
	private final static Map<String, Object> variables = new HashMap<String, Object>();
	private static volatile boolean variablesModded = false;
	
	private final static Map<String, WeakHashMap<Event, Object>> localVariables = new HashMap<String, WeakHashMap<Event, Object>>();
	
	public final static void setVariable(final String name, final Object value) {
		synchronized (variables) {
			variablesModded = true;
			if (value == null)
				variables.remove(name);
			else
				variables.put(name, value);
		}
	}
	
	public static final Object getVariable(final String name) {
		synchronized (variables) {
			return variables.get(name);
		}
	}
	
	public final static void setLocalVariable(final String name, final Event e, final Object value) {
		WeakHashMap<Event, Object> map = localVariables.get(name);
		if (map == null)
			localVariables.put(name, map = new WeakHashMap<Event, Object>());
		map.put(e, value);
	}
	
	public final static Object getLocalVariable(final String name, final Event e) {
		final WeakHashMap<Event, Object> map = localVariables.get(name);
		if (map == null)
			return null;
		return map.get(e);
	}
	
	final static void loadVariables() {
		synchronized (variables) {
			final File oldFile = new File(Skript.getInstance().getDataFolder(), "variables.yml");//pre-1.3
			final File varFile = new File(Skript.getInstance().getDataFolder(), varFileName + "." + varFileExt);
			if (oldFile.exists()) {
				if (varFile.exists()) {
					Skript.error("Found both a new and an old variable file, ignoring the old one");
				} else {
					PrintWriter pw = null;
					try {
						pw = new PrintWriter(varFile, "UTF-8");
						final YamlConfiguration varConfig = YamlConfiguration.loadConfiguration(oldFile);
						for (final Entry<String, Object> e : varConfig.getValues(true).entrySet()) {
							if (!(e.getValue() instanceof String)) {// not an entry
								continue;
							}
							final String v = (String) e.getValue();
							final String type = v.substring(v.indexOf('<') + 1, v.indexOf('>'));
							final String value = v.substring(v.indexOf('>') + 1);
							pw.println(e.getKey() + ", " + type + ", \"" + value.replace("\"", "\"\"") + "\"");
						}
						pw.flush();
						oldFile.delete();
						Skript.info("[1.3] Converted your variables.yml to the new format and renamed it to variables.csv");
					} catch (final IOException e) {
						Skript.error("Error while vonverting the variables to the new format");
					} finally {
						if (pw != null)
							pw.close();
					}
				}
			}
			try {
				varFile.createNewFile();
			} catch (final IOException e) {
				Skript.error("Cannot create the variables file: " + e.getLocalizedMessage());
				return;
			}
			if (!varFile.canWrite()) {
				Skript.error("Cannot write to the variables file - no variables will be saved!");
			}
			if (!varFile.canRead()) {
				Skript.error("Cannot read from the variables file! Skript will try to create a backup of the file but will likely fail.");
				try {
					final File backup = FileUtils.backup(varFile);
					Skript.info("Created a backup of your variables.csv as " + backup.getName());
				} catch (final IOException e) {
					Skript.error("Failed to create a backup of your variables.csv: " + e.getMessage());
				}
				return;
			}
			
			final SubLog log = SkriptLogger.startSubLog();
			int unsuccessful = 0;
			final StringBuilder invalid = new StringBuilder();
			
			Version varVersion = Skript.getVersion();
			
			BufferedReader r = null;
			boolean ioEx = false;
			try {
				r = new BufferedReader(new InputStreamReader(new FileInputStream(varFile), "UTF-8"));
				String line = null;
				while ((line = r.readLine()) != null) {
					line = line.trim();
					if (line.isEmpty() || line.startsWith("#")) {
						if (line.startsWith("# version:")) {
							try {
								varVersion = new Version(line.substring("# version:".length()).trim());
							} catch (final IllegalArgumentException e) {}
						}
						continue;
					}
					final String[] split = splitCSV(line);
					if (split == null || split.length != 3) {
						Skript.error("invalid amount of commas in line '" + line + "'");
						if (invalid.length() != 0)
							invalid.append(", ");
						invalid.append(split == null ? "<unknown>" : split[0]);
						unsuccessful++;
						continue;
					}
					final Object d = Skript.deserialize(split[1], split[2]);
					if (d == null) {
						if (invalid.length() != 0)
							invalid.append(", ");
						invalid.append(split[0]);
						unsuccessful++;
						continue;
					}
					variables.put(split[0], d);
				}
			} catch (final IOException e) {
				ioEx = true;
			} finally {
				if (r != null) {
					try {
						r.close();
					} catch (final IOException e) {
						ioEx = true;
					}
				}
			}
			SkriptLogger.stopSubLog(log);
			if (ioEx || unsuccessful > 0) {
				if (unsuccessful > 0) {
					Skript.error(unsuccessful + " variable" + (unsuccessful == 1 ? "" : "s") + " could not be loaded!");
					Skript.error("Affected variables: " + invalid.toString());
					if (log.hasErrors()) {
						Skript.error("further information:");
						log.printErrors(null);
					}
				}
				if (ioEx) {
					Skript.error("An I/O error occurred while loading the variables");
				}
				try {
					final File backup = FileUtils.backup(varFile);
					Skript.info("Created a backup of variables.csv as " + backup.getName());
				} catch (final IOException ex) {
					Skript.error("Could not backup variables.csv: " + ex.getMessage());
				}
			}
			
			final Version v1_4 = new Version("1.4");
			
			if (v1_4.isLargerThan(varVersion)) {
				int renamed = 0;
				final Map<String, Object> toAdd = new HashMap<String, Object>();
				final Iterator<Entry<String, Object>> iter = variables.entrySet().iterator();
				while (iter.hasNext()) {
					final Entry<String, Object> e = iter.next();
					final String name = e.getKey();
					if (!name.contains("<"))
						continue;
					final String newName = StringUtils.replaceAll(name, "<(.+?):(.+?)>", new Callback<String, Matcher>() {
						private final Set<String> keepType = new HashSet<String>(Arrays.asList("entity", "offset", "time", "timespan", "timeperiod", "entitydata", "entitytype"));
						
						@Override
						public String run(final Matcher m) {
							if (keepType.contains(m.group(1)))
								return m.group(1) + ":" + m.group(2);
							return m.group(2);
						}
					});
					if (name.equals(newName))
						continue;
					iter.remove();
					toAdd.put(newName, e.getValue());
					renamed++;
				}
				variables.putAll(toAdd);
				if (renamed != 0) {
					Skript.warning("[1.4] Skript tried to fix " + renamed + " variables!");
					try {
						final File backup = FileUtils.backup(varFile);
						Skript.info("Created a backup of your old variables.csv as " + backup.getName());
					} catch (final IOException e) {
						Skript.error("Failed to create a backup of your old variables.csv: " + e.getMessage());
					}
				}
			}
			
//			if (variables.isEmpty() && varFile.length() != 0) {
//				Skript.warning("Could not load variables! Skript will try to create a backup of the file.");
//				try {
//					FileUtils.backup(varFile);
//				} catch (final IOException e) {
//					Skript.error("Could not backup the variables file: " + e.getLocalizedMessage());
//				}
//			}
		}
	}
	
	private final static Pattern csv = Pattern.compile("([^\"\n\r,]+|\"([^\"]|\"\")*\")\\s*(,\\s*|$)");
	
	private final static String[] splitCSV(final String line) {
		final Matcher m = csv.matcher(line);
		int lastEnd = 0;
		final ArrayList<String> r = new ArrayList<String>();
		while (m.find()) {
			if (lastEnd != m.start())
				return null;
			if (m.group(1).startsWith("\""))
				r.add(m.group(1).substring(1, m.group(1).length() - 1).replace("\"\"", "\""));
			else
				r.add(m.group(1));
			lastEnd = m.end();
		}
		if (lastEnd != line.length())
			return null;
		return r.toArray(new String[r.size()]);
	}
	
	final static void saveVariables() {
		synchronized (variables) {
			final File varFile = new File(Skript.getInstance().getDataFolder(), varFileName + "." + varFileExt);
			PrintWriter pw = null;
			try {
				pw = new PrintWriter(varFile, "UTF-8");
				pw.println("# Skript's variable storage");
				pw.println("# Please do not modify this file manually!");
				pw.println("#");
				pw.println("# version: " + Skript.getInstance().getDescription().getVersion());
				pw.println();
				for (final Entry<String, Object> e : variables.entrySet()) {
					if (e.getValue() == null)
						continue;
					final Pair<String, String> s = Skript.serialize(e.getValue());
					if (s == null)
						continue;
					pw.println(e.getKey() + ", " + s.first + ", " + s.second);
				}
				pw.flush();
			} catch (final IOException e) {
				Skript.error("Unable to save variables: " + e.getLocalizedMessage());
			} finally {
				if (pw != null)
					pw.close();
			}
		}
	}
	
	static void scheduleSaveTask() {
		saveTask = new Task(Skript.getInstance(), 600, 600, true) {// 30 secs
			@Override
			public void run() {
				if (variablesModded) {
					synchronized (variables) {
						saveVariables();
						variablesModded = false;
					}
				}
			}
		};
		
	}
	
	public static void cancelSaveTask() {
		saveTask.cancel();
	}
	
}
