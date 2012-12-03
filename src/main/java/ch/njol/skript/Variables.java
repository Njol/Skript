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
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.TreeMap;
import java.util.WeakHashMap;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.bukkit.event.Event;

import ch.njol.skript.lang.Variable;
import ch.njol.skript.log.SimpleLog;
import ch.njol.skript.log.SkriptLogger;
import ch.njol.skript.registrations.Classes;
import ch.njol.skript.util.FileUtils;
import ch.njol.skript.util.Task;
import ch.njol.skript.util.Timespan;
import ch.njol.skript.util.Utils;
import ch.njol.skript.util.Version;
import ch.njol.util.StringUtils;

/**
 * @author Peter Güttinger
 */
public class Variables {
	
	private boolean loadError = false;
	
	/**
	 * note to self: use {@link #setVariable(String[], Object)} and {@link #getVariable(String[])}
	 */
	private final TreeMap<String, Object> variables = new TreeMap<String, Object>(variableNameComparator);
	
	private final static Comparator<String> variableNameComparator = new Comparator<String>() {
		@Override
		public int compare(final String s1, final String s2) {
			if (s1 == null)
				return s2 == null ? 0 : -1;
			if (s2 == null)
				return 1;
			return s1.compareTo(s2);
		}
	};
	
	private final Map<String, WeakHashMap<Event, Object>> localVariables = new HashMap<String, WeakHashMap<Event, Object>>();
	
	private File file;
	private volatile PrintWriter changesWriter;
	private final LinkedBlockingQueue<String[]> changesQueue = new LinkedBlockingQueue<String[]>();
	private final Thread changesWriterThread = new Thread(new Runnable() {
		@Override
		public void run() {
			while (true) {
				try {
					writeCSV(changesWriter, changesQueue.take());
					changesWriter.flush();
				} catch (final InterruptedException e) {}
			}
		}
	});
	
	private volatile int changes = 0;
	private final static int REQUIRED_CHANGES_FOR_RESAVE = 50;
	
	private Task saveTask;
	
	private Task backupTask = null;
	
	public Variables() {}
	
	public void startBackupTask(final Timespan t) {
		backupTask = new Task(Skript.getInstance(), t.getTicks(), t.getTicks(), true) {
			@Override
			public void run() {
				synchronized (variables) {
					closeChangesWriter();
					try {
						FileUtils.backup(file);
					} catch (final IOException e) {
						Skript.error("Automatic variables backup failed: " + e.getLocalizedMessage());
					} finally {
						setupChangesWriter();
					}
				}
			}
		};
	}
	
	@SuppressWarnings("unchecked")
	public final void setVariable(final String[] name, final Object value) {
		synchronized (variables) {
			TreeMap<String, Object> current = variables;
			for (int i = 0; i < name.length; i++) {
				final String n = name[i];
				Object o = current.get(n);
				if (o == null) {
					if (i == name.length - 1) {
						current.put(n, value);
						break;
					} else if (value != null) {
						current.put(n, o = new TreeMap<String, Object>(variableNameComparator));
						current = (TreeMap<String, Object>) o;
						continue;
					}
				} else if (o instanceof TreeMap) {
					if (i == name.length - 1) {
						((TreeMap<String, Object>) o).put(null, value);
						break;
					} else if (i == name.length - 2 && name[i + 1].equals("*")) {
						assert value == null;
						final Object v = ((TreeMap<String, Object>) o).get(null);
						current.put(n, v);
						break;
					} else {
						current = (TreeMap<String, Object>) o;
						continue;
					}
				} else {
					if (i == name.length - 1) {
						current.put(n, value);
						break;
					} else if (value != null) {
						final TreeMap<String, Object> c = new TreeMap<String, Object>(variableNameComparator);
						c.put(null, o);
						current.put(n, c);
						current = c;
						continue;
					}
				}
			}
			saveVariableChange(name, value);
		}
	}
	
	@SuppressWarnings("unchecked")
	public final Object getVariable(final String[] name) {
		synchronized (variables) {
			TreeMap<String, Object> current = variables;
			for (int i = 0; i < name.length; i++) {
				final String n = name[i];
				if (n.equals("*"))
					return Collections.unmodifiableSortedMap(current);
				final Object o = current.get(n);
				if (o == null)
					return null;
				if (o instanceof Map) {
					current = (TreeMap<String, Object>) o;
					if (i == name.length - 1)
						return current.get(null);
					continue;
				} else {
					return i == name.length - 1 ? o : null;
				}
			}
			return null;
		}
	}
	
	public final void setLocalVariable(final String name, final Event e, final Object value) {
		WeakHashMap<Event, Object> map = localVariables.get(name);
		if (map == null)
			localVariables.put(name, map = new WeakHashMap<Event, Object>());
		map.put(e, value);
	}
	
	public final Object getLocalVariable(final String name, final Event e) {
		final WeakHashMap<Event, Object> map = localVariables.get(name);
		if (map == null)
			return null;
		return map.get(e);
	}
	
	/**
	 * 
	 * @return whether all variables were loaded successfully
	 */
	final boolean loadVariables() {
		assert !Thread.holdsLock(variables);
		synchronized(variables) {
			assert variables.isEmpty();
			file = new File(Skript.getInstance().getDataFolder(), "variables.csv");
			try {
				file.createNewFile();
			} catch (final IOException e) {
				Skript.error("Cannot create the variables file: " + e.getLocalizedMessage());
				return false;
			}
			if (!file.canWrite()) {
				Skript.error("Cannot write to the variables file - no variables will be saved!");
			}
			if (!file.canRead()) {
				Skript.error("Cannot read from the variables file!");
				Skript.error("This means that no variables will be available and can also prevent new variables from being saved!");
				try {
					final File backup = FileUtils.backup(file);
					Skript.info("Created a backup of your variables.csv as " + backup.getName());
				} catch (final IOException e) {
					Skript.error("Failed to create a backup of your variables.csv: " + e.getLocalizedMessage());
					loadError = true;
				}
				return false;
			}
			
			final SimpleLog log = SkriptLogger.startSubLog();
			int unsuccessful = 0;
			final StringBuilder invalid = new StringBuilder();
			
			Version varVersion = Skript.getVersion();
			final Version v2_0_beta3 = new Version(2, 0, "beta 3");
			
			BufferedReader r = null;
			boolean ioEx = false;
			try {
				r = new BufferedReader(new InputStreamReader(new FileInputStream(file), "UTF-8"));
				String line = null;
				int lineNum = 0;
				while ((line = r.readLine()) != null) {
					lineNum++;
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
						Skript.error("invalid amount of commas in line " + lineNum + " ('" + line + "')");
						if (invalid.length() != 0)
							invalid.append(", ");
						invalid.append(split == null ? "<unknown>" : split[0]);
						unsuccessful++;
						continue;
					}
					if (split[1].equals("null")) {
						setVariable(Variable.splitVariableName(split[0]), null);
					} else {
						Object d = Classes.deserialize(split[1], split[2]);
						if (d == null) {
							if (invalid.length() != 0)
								invalid.append(", ");
							invalid.append(split[0]);
							unsuccessful++;
							continue;
						}
						if (d instanceof String && varVersion.isSmallerThan(v2_0_beta3)) {
							d = Utils.replaceChatStyles((String) d);
						}
						setVariable(Variable.splitVariableName(split[0]), d);
					}
				}
			} catch (final IOException e) {
				Skript.error(e.getLocalizedMessage());
				loadError = true;
				ioEx = true;
			} finally {
				if (r != null) {
					try {
						r.close();
					} catch (final IOException e) {}
				}
			}
			log.stop();
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
					Skript.error("This means that some to all variables could not be loaded!");
				}
				try {
					final File backup = FileUtils.backup(file);
					Skript.info("Created a backup of variables.csv as " + backup.getName());
					loadError = false;
				} catch (final IOException ex) {
					Skript.error("Could not backup variables.csv: " + ex.getMessage());
				}
			}
			
			setupChangesWriter();
			changesWriterThread.start();
			
			saveTask = new Task(Skript.getInstance(), 5 * 60 * 20, 5 * 60 * 20, true) {
				@Override
				public void run() {
					synchronized (variables) {
						if (changes >= REQUIRED_CHANGES_FOR_RESAVE) {
							saveVariables(false);
							changes = 0;
						}
					}
				}
			};
			
			if (backupTask == null && SkriptConfig.variableBackupPeriod != null)
				startBackupTask(SkriptConfig.variableBackupPeriod);
			
			return !ioEx;
		}
	}
	
	private final static Pattern csv = Pattern.compile("\\s*([^\",]+|\"([^\"]|\"\")*\")\\s*(,|$)");
	
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
	
	private final static void writeCSV(final PrintWriter pw, final String... values) {
		for (int i = 0; i < values.length; i++) {
			if (i != 0)
				pw.print(", ");
			String v = values[i];
			if (v.contains(",") || v.contains("\""))
				v = '"' + v.replace("\"", "\"\"") + '"';
			pw.print(v);
		}
		pw.println();
	}
	
	/**
	 * Must be synchronized with {@link #variables}
	 * 
	 * @param name
	 * @param value
	 */
	private final void saveVariableChange(final String[] name, final Object value) {
		assert Thread.holdsLock(variables);
		if (changesWriter != null) {// null while loading
			changes++;
			final String[] s = value == null ? null : Classes.serialize(value);
			if (s == null)
				changesQueue.add(new String[] {StringUtils.join(name, Variable.SEPARATOR), "null", "null"});
			else
				changesQueue.add(new String[] {StringUtils.join(name, Variable.SEPARATOR), s[0], s[1]});
		}
	}
	
	final void closeChangesWriter() {
		assert Thread.holdsLock(variables);
		changesQueue.clear();
		changesWriter.close();
		changesWriter = null;
	}
	
	final void setupChangesWriter() {
		assert Thread.holdsLock(variables);
		try {
			changesWriter = new PrintWriter(new OutputStreamWriter(new FileOutputStream(file, true), "UTF-8"));
		} catch (final FileNotFoundException e) {} catch (final UnsupportedEncodingException e) {}
	}
	
	public final void saveVariables(final boolean finalSave) {
		if (finalSave) {
			saveTask.cancel();
			if (backupTask != null)
				backupTask.cancel();
		}
		synchronized (variables) {
			closeChangesWriter();
			try {
				if (loadError) {
					try {
						final File backup = FileUtils.backup(file);
						Skript.info("Created a backup of your old variables.csv as " + backup.getName());
						loadError = false;
					} catch (final IOException e) {
						Skript.error("Could not backup the old variables.csv: " + e.getLocalizedMessage());
						Skript.error("No variables are saved!");
						return;
					}
				}
				final File tempFile = new File(Skript.getInstance().getDataFolder(), "variables.csv.temp");
				PrintWriter pw = null;
				try {
					pw = new PrintWriter(tempFile, "UTF-8");
					pw.println("# Skript's variable storage");
					pw.println("# Please do not modify this file manually!");
					pw.println("#");
					pw.println("# version: " + Skript.getInstance().getDescription().getVersion());
					pw.println();
					save(pw, "", variables);
					pw.println();
					pw.flush();
					pw.close();
					FileUtils.move(tempFile, file, true);
				} catch (final IOException e) {
					Skript.error("Unable to save variables: " + e.getLocalizedMessage());
				} finally {
					if (pw != null)
						pw.close();
				}
			} finally {
				if (!finalSave)
					setupChangesWriter();
			}
		}
	}
	
	/**
	 * 
	 * @param pw
	 * @param parent The parent's name with {@link Variable#SEPARATOR} at the end
	 * @param map
	 */
	private final static void save(final PrintWriter pw, final String parent, final TreeMap<String, Object> map) {
		for (final Entry<String, Object> e : map.entrySet()) {
			if (e.getValue() == null)
				continue;
			if (e.getValue() instanceof TreeMap) {
				save(pw, parent + e.getKey() + Variable.SEPARATOR, (TreeMap<String, Object>) e.getValue());
			} else {
				final String[] s = Classes.serialize(e.getValue());
				if (s != null)
					writeCSV(pw, (e.getKey() == null ? parent.substring(0, parent.length() - 2) : parent + e.getKey()), s[0], s[1]);
			}
		}
	}
	
	public int numVariables() {
		synchronized (variables) {
			return numVariables(variables);
		}
	}
	
	@SuppressWarnings("unchecked")
	private int numVariables(final TreeMap<String, Object> m) {
		int r = 0;
		for (final Entry<String, Object> e : m.entrySet()) {
			if (e instanceof TreeMap) {
				r += numVariables((TreeMap<String, Object>) e);
			} else {
				r++;
			}
		}
		return r;
	}
	
}
