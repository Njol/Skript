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

package ch.njol.skript.variables;

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
import java.util.Map.Entry;
import java.util.SortedMap;
import java.util.TreeMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import ch.njol.skript.Skript;
import ch.njol.skript.SkriptConfig;
import ch.njol.skript.lang.Variable;
import ch.njol.skript.log.RetainingLogHandler;
import ch.njol.skript.log.SkriptLogger;
import ch.njol.skript.registrations.Classes;
import ch.njol.skript.util.ExceptionUtils;
import ch.njol.skript.util.FileUtils;
import ch.njol.skript.util.Task;
import ch.njol.skript.util.Timespan;
import ch.njol.skript.util.Utils;
import ch.njol.skript.util.Version;
import ch.njol.util.Pair;

/**
 * TODO use a database (SQLite) instead and only load a limited amount of variables into RAM
 * rem: store null variables to prevent looking up the same variables over and over again
 * 
 * @author Peter Güttinger
 */
public class FlatFileStorage extends VariablesStorage {
	
	private File file;
	private volatile PrintWriter changesWriter;
	private final Object changesWriterLock = new Object();
	
	private volatile int changes = 0;
	private final int REQUIRED_CHANGES_FOR_RESAVE = 50;
	
	private Task saveTask;
	
	public Task backupTask = null;
	
	private boolean loadError = false;
	
	public void startBackupTask(final Timespan t) {
		backupTask = new Task(Skript.getInstance(), t.getTicks(), t.getTicks(), true) {
			@Override
			public void run() {
				synchronized (changesWriterLock) {
					try {
						Variables.getReadLock().lock();
						closeChangesWriter();
						try {
							FileUtils.backup(file);
						} catch (final IOException e) {
							Skript.error("Automatic variables backup failed: " + e.getLocalizedMessage());
						} finally {
							setupChangesWriter();
							changesWriterLock.notifyAll();
						}
					} finally {
						Variables.getReadLock().unlock();
					}
				}
			}
		};
	}
	
	@Override
	protected boolean load_i() {
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
				Skript.error("A backup of your variables.csv was created as " + backup.getName());
			} catch (final IOException e) {
				Skript.error("Failed to create a backup of your variables.csv: " + e.getLocalizedMessage());
				loadError = true;
			}
			return false;
		}
		
		IOException ioEx = null;
		int unsuccessful = 0;
		final StringBuilder invalid = new StringBuilder();
		final RetainingLogHandler log = SkriptLogger.startRetainingLog();
		try {
			Version varVersion = Skript.getVersion();
			final Version v2_0_beta3 = new Version(2, 0, "beta 3");
			
			BufferedReader r = null;
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
						Variables.setVariable(split[0], null, this);
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
						Variables.setVariable(split[0], d, this);
					}
				}
			} catch (final IOException e) {
				loadError = true;
				ioEx = e;
			} finally {
				if (r != null) {
					try {
						r.close();
					} catch (final IOException e) {}
				}
			}
		} finally {
			log.stop();
		}
		if (ioEx != null || unsuccessful > 0) {
			if (unsuccessful > 0) {
				Skript.error(unsuccessful + " variable" + (unsuccessful == 1 ? "" : "s") + " could not be loaded!");
				Skript.error("Affected variables: " + invalid.toString());
				if (log.hasErrors()) {
					Skript.error("further information:");
					log.printErrors(null);
				}
			}
			if (ioEx != null) {
				Skript.error("An I/O error occurred while loading the variables: " + ExceptionUtils.toString(ioEx));
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
		
		saveTask = new Task(Skript.getInstance(), 5 * 60 * 20, 5 * 60 * 20, true) {
			@Override
			public void run() {
				if (changes >= REQUIRED_CHANGES_FOR_RESAVE) {
					try {
						Variables.getReadLock().lock();
						saveVariables(false);
						changes = 0;
					} finally {
						Variables.getReadLock().unlock();
					}
				}
			}
		};
		
		if (backupTask == null && SkriptConfig.variableBackupInterval.value() != null)
			startBackupTask(SkriptConfig.variableBackupInterval.value());
		
		return ioEx == null;
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
	
	@Override
	protected void save(final String name, final String type, final String value) {
		synchronized (changesWriterLock) {
			while (changesWriter == null) {
				try {
					changesWriterLock.wait();
				} catch (final InterruptedException e) {}
			}
		}
		writeCSV(changesWriter, name, "" + type, "" + value);
		changesWriter.flush();
		final int c = changes; // FindBugs workaround - 'changes' is only changed here and on a full save and only acts as a hint to not save if not many modifications were done, so lost increments do not matter.
		changes = c + 1;
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
	
	final void closeChangesWriter() {
		assert Thread.holdsLock(changesWriterLock);
		clearChangesQueue();
		if (changesWriter != null) {
			changesWriter.close();
			changesWriter = null;
		}
	}
	
	private final void setupChangesWriter() {
		try {
			changesWriter = new PrintWriter(new OutputStreamWriter(new FileOutputStream(file, true), "UTF-8"));
		} catch (final FileNotFoundException e) {
			Skript.exception(e);
		} catch (final UnsupportedEncodingException e) {
			Skript.exception(e);
		}
	}
	
	@Override
	public void close() {
		clearChangesQueue();
		super.close();
		saveVariables(true);
	}
	
	public final void saveVariables(final boolean finalSave) {
		if (finalSave) {
			saveTask.cancel();
			if (backupTask != null)
				backupTask.cancel();
		}
		synchronized (changesWriterLock) {
			try {
				Variables.getReadLock().lock();
				closeChangesWriter();
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
					pw.println("# === Skript's variable storage ===");
					pw.println("# Please do not modify this file manually!");
					pw.println("#");
					pw.println("# version: " + Skript.getInstance().getDescription().getVersion());
					pw.println();
					save(pw, "", Variables.getVariables());
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
				Variables.getReadLock().unlock();
				if (!finalSave) {
					setupChangesWriter();
					changesWriterLock.notifyAll();
				}
			}
		}
	}
	
	/**
	 * Saves the variables.
	 * <p>
	 * This method uses the sorted variables map to save the variables in order.
	 * 
	 * @param pw
	 * @param parent The parent's name with {@link Variable#SEPARATOR} at the end
	 * @param map
	 */
	private final void save(final PrintWriter pw, final String parent, final SortedMap<String, Object> map) {
		for (final Entry<String, Object> e : map.entrySet()) {
			if (e.getValue() == null)
				continue;
			if (e.getValue() instanceof TreeMap) {
				save(pw, parent + e.getKey() + Variable.SEPARATOR, (TreeMap<String, Object>) e.getValue());
			} else {
				final String name = (e.getKey() == null ? parent.substring(0, parent.length() - Variable.SEPARATOR.length()) : parent + e.getKey());
				if (!DatabaseStorage.accept(name)) {
					final Pair<String, String> s = Classes.serialize(e.getValue());
					if (s != null)
						writeCSV(pw, name, s.first, s.second);
				}
			}
		}
	}
	
	@Override
	protected String type() {
		return "file";
	}
	
}
