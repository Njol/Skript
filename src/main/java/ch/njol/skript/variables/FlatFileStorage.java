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
import java.util.Collection;
import java.util.Map.Entry;
import java.util.TreeMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import ch.njol.skript.Skript;
import ch.njol.skript.config.SectionNode;
import ch.njol.skript.lang.Variable;
import ch.njol.skript.log.LogEntry;
import ch.njol.skript.log.RetainingLogHandler;
import ch.njol.skript.log.SkriptLogger;
import ch.njol.skript.registrations.Classes;
import ch.njol.skript.util.ExceptionUtils;
import ch.njol.skript.util.FileUtils;
import ch.njol.skript.util.Task;
import ch.njol.skript.util.Utils;
import ch.njol.skript.util.Version;
import ch.njol.util.Pair;

/**
 * TODO use a database (SQLite) instead and only load a limited amount of variables into RAM - e.g. 2 GB (configurable). If more variables are available they will be loaded when
 * accessed. (rem: print a warning when Skript starts)
 * rem: store null variables to prevent looking up the same variables over and over again
 * 
 * @author Peter Güttinger
 */
public class FlatFileStorage extends VariablesStorage {
	
	private volatile PrintWriter changesWriter;
	
	final AtomicInteger changes = new AtomicInteger(0);
	private final int REQUIRED_CHANGES_FOR_RESAVE = 1000;
	
	private Task saveTask;
	
	private boolean loadError = false;
	
	protected FlatFileStorage(final SectionNode n) {
		super(n);
	}
	
	@SuppressWarnings("deprecation")
	@Override
	protected boolean load_i(final SectionNode n) {
		IOException ioEx = null;
		int unsuccessful = 0;
		final StringBuilder invalid = new StringBuilder();
		
		Version varVersion = Skript.getVersion(); // will be set later
		
		final Version v2_0_beta3 = new Version(2, 0, "beta 3");
		boolean update2_0_beta3 = false;
		final Version v2_1 = new Version(2, 1);
		boolean update2_1 = false;
		
		final RetainingLogHandler log = SkriptLogger.startRetainingLog();
		Collection<LogEntry> errors = null;
		try {
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
								update2_0_beta3 = varVersion.isSmallerThan(v2_0_beta3);
								update2_1 = varVersion.isSmallerThan(v2_1);
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
						Variables.variableLoaded(split[0], null, this);
					} else {
						Object d;
						if (update2_1)
							d = Classes.deserialize(split[1], split[2]);
						else
							d = Classes.deserialize(split[1], decode(split[2]));
						if (d == null) {
							if (invalid.length() != 0)
								invalid.append(", ");
							invalid.append(split[0]);
							unsuccessful++;
							continue;
						}
						if (d instanceof String && update2_0_beta3) {
							d = Utils.replaceChatStyles((String) d);
						}
						Variables.variableLoaded(split[0], d, this);
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
			errors = log.getErrors();
			log.clear();
			log.printLog();
			log.stop();
		}
		
		if (ioEx != null || unsuccessful > 0 || update2_1) {
			if (unsuccessful > 0) {
				Skript.error(unsuccessful + " variable" + (unsuccessful == 1 ? "" : "s") + " could not be loaded!");
				Skript.error("Affected variables: " + invalid.toString());
				if (!errors.isEmpty()) {
					Skript.error("further information:");
					SkriptLogger.logAll(errors);
				}
			}
			if (ioEx != null) {
				Skript.error("An I/O error occurred while loading the variables: " + ExceptionUtils.toString(ioEx));
				Skript.error("This means that some to all variables could not be loaded!");
			}
			try {
				if (update2_1) {
					Skript.info("[2.1] updating " + file.getName() + " to the new format...");
				}
				final File bu = FileUtils.backup(file);
				Skript.info("Created a backup of " + file.getName() + " as " + bu.getName());
				loadError = false;
			} catch (final IOException ex) {
				Skript.error("Could not backup " + file.getName() + ": " + ex.getMessage());
			}
		}
		
		if (update2_1) {
			saveVariables(false);
			Skript.info(file.getName() + " successfully updated.");
		}
		
		synchronized (connectionLock) { // only synchronised because of the assertion in connect()
			connect();
		}
		
		saveTask = new Task(Skript.getInstance(), 5 * 60 * 20, 5 * 60 * 20, true) {
			@Override
			public void run() {
				if (changes.get() >= REQUIRED_CHANGES_FOR_RESAVE) {
					try {
						Variables.getReadLock().lock();
						saveVariables(false);
						changes.set(0);
					} finally {
						Variables.getReadLock().unlock();
					}
				}
			}
		};
		
		return ioEx == null;
	}
	
	@Override
	protected boolean requiresFile() {
		return true;
	}
	
	@Override
	protected File getFile(final String file) {
		return new File(file);
	}
	
	final static String encode(final byte[] data) {
		final char[] r = new char[data.length * 2];
		for (int i = 0; i < data.length; i++) {
			r[2 * i] = Character.toUpperCase(Character.forDigit((data[i] & 0xF0) >>> 4, 16));
			r[2 * i + 1] = Character.toUpperCase(Character.forDigit(data[i] & 0xF, 16));
		}
		return new String(r);
	}
	
	final static byte[] decode(final String hex) {
		final byte[] r = new byte[hex.length() / 2];
		for (int i = 0; i < r.length; i++) {
			r[i] = (byte) ((Character.digit(hex.charAt(2 * i), 16) << 4) + Character.digit(hex.charAt(2 * i + 1), 16));
		}
		return r;
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
	protected void save(final String name, final String type, final byte[] value) {
		synchronized (connectionLock) {
			while (changesWriter == null) {
				try {
					connectionLock.wait();
				} catch (final InterruptedException e) {}
			}
			writeCSV(changesWriter, name, type, new String(encode(value)));
			changesWriter.flush();
			changes.incrementAndGet();
		}
	}
	
	private final static void writeCSV(final PrintWriter pw, final String... values) {
		assert values.length == 3; // name, type, value
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
	
	@Override
	protected final void disconnect() {
		assert Thread.holdsLock(connectionLock);
		clearChangesQueue();
		if (changesWriter != null) {
			changesWriter.close();
			changesWriter = null;
		}
	}
	
	@Override
	protected final void connect() {
		assert Thread.holdsLock(connectionLock);
		if (changesWriter != null)
			return;
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
		saveVariables(true); // also closes the writer
	}
	
	/**
	 * Completely rewrites the while file
	 * 
	 * @param finalSave whether this is the last save in this session or not.
	 */
	public final void saveVariables(final boolean finalSave) {
		if (finalSave) {
			saveTask.cancel();
			if (backupTask != null)
				backupTask.cancel();
		}
		synchronized (connectionLock) {
			try {
				Variables.getReadLock().lock();
				disconnect();
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
					connect();
					connectionLock.notifyAll();
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
	private final void save(final PrintWriter pw, final String parent, final TreeMap<String, Object> map) {
		outer: for (final Entry<String, Object> e : map.entrySet()) {
			if (e.getValue() == null)
				continue;
			if (e.getValue() instanceof TreeMap) {
				save(pw, parent + e.getKey() + Variable.SEPARATOR, (TreeMap<String, Object>) e.getValue());
			} else {
				final String name = (e.getKey() == null ? parent.substring(0, parent.length() - Variable.SEPARATOR.length()) : parent + e.getKey());
				for (final VariablesStorage s : Variables.storages) {
					if (s != this && s.accept(name))
						continue outer;
				}
				final Pair<String, byte[]> value = Classes.serialize(e.getValue());
				if (value != null)
					writeCSV(pw, name, value.first, new String(encode(value.second)));
			}
		}
	}
	
}
