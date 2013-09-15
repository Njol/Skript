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

import java.io.File;
import java.io.IOException;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.LinkedList;
import java.util.UUID;
import java.util.concurrent.Callable;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

import lib.PatPeter.SQLibrary.Database;
import lib.PatPeter.SQLibrary.MySQL;
import lib.PatPeter.SQLibrary.SQLibrary;
import lib.PatPeter.SQLibrary.SQLite;

import org.bukkit.Bukkit;
import org.bukkit.plugin.Plugin;

import ch.njol.skript.Skript;
import ch.njol.skript.SkriptConfig;
import ch.njol.skript.classes.ClassInfo;
import ch.njol.skript.registrations.Classes;
import ch.njol.skript.util.Task;
import ch.njol.skript.util.Timespan;

/**
 * @author Peter Güttinger
 */
public class DatabaseStorage extends VariablesStorage {
	
	private final static String MISSINGINFORMATION = "The config is missing relevant information on the database";
	
	// can't be a Database or loading this class will result in a NoClassDefFoundError if SQLibrary is not present
	private Object db;
	// TODO pattern for each table & patterns for different .csv files
	private static Pattern variablePattern;
	
	private boolean monitor = false;
	private long monitor_interval;
	
	public final static boolean accept(final String name) {
		return variablePattern == null ? false : variablePattern.matcher(name).matches();
	}
	
	private final static String guid = UUID.randomUUID().toString();
	
	@Override
	protected boolean load_i() {
		
		final String type = SkriptConfig.database.get("type");
		if (type == null) {
			Skript.error(MISSINGINFORMATION);
			return false;
		}
		if (type.equalsIgnoreCase("none"))
			return true;
		
		final Plugin p = Bukkit.getPluginManager().getPlugin("SQLibrary");
		if (p == null || !(p instanceof SQLibrary)) {
			Skript.error("You need the plugin SQLibrary in order to use a database with Skript. You can download the latest version from http://dev.bukkit.org/server-mods/sqlibrary/files/");
			return false;
		}
		
		final String pattern = SkriptConfig.database.get("pattern");
		if (pattern == null) {
			Skript.error(MISSINGINFORMATION);
			return false;
		}
		try {
			variablePattern = Pattern.compile(pattern);
		} catch (final PatternSyntaxException e) {
			Skript.error("Invalid pattern '" + pattern + "': " + e.getLocalizedMessage());
			return false;
		}
		
		final Boolean monitor_changes = SkriptConfig.database.get("monitor changes");
		final Timespan monitor_interval = SkriptConfig.database.get("monitor interval");
		if (monitor_changes == null || monitor_interval == null) {
			Skript.error(MISSINGINFORMATION);
			return false;
		}
		monitor = monitor_changes;
		this.monitor_interval = monitor_interval.getMilliSeconds();
		
		if (type.equalsIgnoreCase("mysql")) {
			final String host = SkriptConfig.database.get("host");
			final Integer port = SkriptConfig.database.get("port");
			final String user = SkriptConfig.database.get("user");
			final String password = SkriptConfig.database.get("password");
			final String database = SkriptConfig.database.get("database");
			if (host == null || port == null || user == null || password == null || database == null) {
				Skript.error(MISSINGINFORMATION);
				return false;
			}
			db = new MySQL(Bukkit.getLogger(), "[Skript]", host, port, database, user, password);
		} else if (type.equalsIgnoreCase("sqlite")) {
			String file = (String) SkriptConfig.database.get("file");
			if (file == null) {
				Skript.error(MISSINGINFORMATION);
				return false;
			}
			if (!file.endsWith(".db"))
				file = file + ".db";
			File f = new File(file);
			if (f.exists() && !f.isFile()) {
				Skript.error("The database file must be an actual file, not a directory.");
				return false;
			} else {
				try {
					f.createNewFile();
				} catch (final IOException e) {
					Skript.error("Cannot create the database file: " + e.getLocalizedMessage());
					return false;
				}
			}
			f = f.getAbsoluteFile();
			final String name = f.getName();
			db = new SQLite(Bukkit.getLogger(), "[Skript]", f.getParent(), name.substring(0, name.length() - ".db".length()));
		} else {
			Skript.error("Invalid database type '" + type + "', only 'MySQL', 'SQLite' and 'none' are allowed.");
			return false;
		}
		
		if (!((Database) db).open()) {
			Skript.error("Cannot connect to the database!");
			return false;
		}
		
		try {
			if (db instanceof SQLite) {
				((Database) db).query("CREATE TABLE IF NOT EXISTS variables (" +
						"name         VARCHAR(500)   NOT NULL  PRIMARY KEY," +
						"type         VARCHAR(50)    ," +
						"value        VARCHAR(5000)  ," +
						"update_guid  CHAR(36)       NOT NULL" +
						")");
			} else {
				((Database) db).query("CREATE TABLE IF NOT EXISTS variables (" +
						"rowid        BIGINT         NOT NULL  AUTO_INCREMENT PRIMARY KEY," +
						"name         VARCHAR(500)   NOT NULL  UNIQUE," +
						"type         VARCHAR(50)    ," +
						"value        VARCHAR(5000)  ," +
						"update_guid  CHAR(36)       NOT NULL" +
						")");
			}
			
			writeQuery = ((Database) db).prepare("REPLACE INTO variables (name, type, value, update_guid) VALUES (?, ?, ?, ?)");
			
			deleteQuery = ((Database) db).prepare("DELETE FROM variables WHERE name = ?");
			
			monitorQuery = ((Database) db).prepare("SELECT name, type, value, rowid FROM variables WHERE rowid > ? AND update_guid != ?");
			monitorCleanUpQuery = ((Database) db).prepare("DELETE FROM variables WHERE type IS NULL AND rowid < ?");
			
			Statement s = null;
			try {
				s = ((Database) db).getConnection().createStatement();
				
				s.execute("SELECT name, type, value, rowid from variables");
				ResultSet r = null;
				try {
					r = s.getResultSet();
					loadVariables(r);
				} finally {
					if (r != null)
						r.close();
				}
			} finally {
				if (s != null) {
					try {
						s.close();
					} catch (final SQLException e) {}
				}
			}
		} catch (final SQLException e) {
			sqlException(e);
			return false;
		}
		
		if (monitor) {
			Skript.newThread(new Runnable() {
				@Override
				public void run() {
					long lastWarning = Long.MIN_VALUE;
					final int WARING_INTERVAL = 10;
					
					while (!closed) {
						final long target = System.currentTimeMillis() + DatabaseStorage.this.monitor_interval;
						checkDatabase();
						final long now = System.currentTimeMillis();
						if (target < now && lastWarning < now - WARING_INTERVAL * 1000) {
							Skript.warning("Cannot load variables from the database fast enough (loading took " + ((now - target + DatabaseStorage.this.monitor_interval) / 1000.) + "s, monitor interval = " + (DatabaseStorage.this.monitor_interval / 1000.) + "s). " +
									"Please increase your monitor interval or reduce usage of variables. " +
									"(this warning will be repeated at most once every " + WARING_INTERVAL + " seconds)");
							lastWarning = now;
						}
						while (System.currentTimeMillis() < target) {
							try {
								Thread.sleep(target - System.currentTimeMillis());
							} catch (final InterruptedException e) {}
						}
					}
				}
			}, "Skript database monitor thread").start();
		}
		
		return true;
	}
	
	private PreparedStatement writeQuery, deleteQuery, monitorQuery, monitorCleanUpQuery;
	
	@Override
	protected void save(final String name, final String type, final String value) {
		try {
			if (!monitor && type == null) {
				assert value == null;
				deleteQuery.setString(1, name);
				deleteQuery.executeUpdate();
			} else {
				int i = 1;
				writeQuery.setString(i++, name);
				writeQuery.setString(i++, type);
				writeQuery.setString(i++, value);
				writeQuery.setString(i++, guid);
				writeQuery.executeUpdate();
			}
		} catch (final SQLException e) {
			sqlException(e);
		}
	};
	
	@Override
	public void close() {
		super.close();
		if (db != null)
			((Database) db).close();
		db = null;
	}
	
	private long lastRowID = -1;
	
	protected void checkDatabase() {
		if (db == null)
			return;
		try {
			final long lastRowID = this.lastRowID;
			monitorQuery.setLong(1, lastRowID);
			monitorQuery.setString(2, guid);
			monitorQuery.execute();
			ResultSet r = null;
			try {
				r = monitorQuery.getResultSet();
				loadVariables(r);
			} finally {
				if (r != null)
					r.close();
			}
			
			new Task(Skript.getInstance(), (long) Math.ceil(1. * monitor_interval / 50), true) {
				@Override
				public void run() {
					try {
						monitorCleanUpQuery.setLong(1, lastRowID);
						monitorCleanUpQuery.executeUpdate();
					} catch (final SQLException e) {
						sqlException(e);
					}
				}
			};
		} catch (final SQLException e) {
			sqlException(e);
		}
	}
	
	private final static class VariableInfo {
		final String name;
		final String value;
		final ClassInfo<?> ci;
		
		public VariableInfo(final String name, final String value, final ClassInfo<?> ci) {
			this.name = name;
			this.value = value;
			this.ci = ci;
		}
	}
	
	private final static LinkedList<VariableInfo> syncDeserializing = new LinkedList<VariableInfo>();
	
	private void loadVariables(final ResultSet r) throws SQLException {
		synchronized (syncDeserializing) {
			while (r.next()) {
				int i = 1;
				final String name = r.getString(i++);
				final String type = r.getString(i++);
				final String value = r.getString(i++);
				lastRowID = r.getLong(i++);
				if (type == null) {
					Variables.setVariable(name, null, this);
				} else {
					final ClassInfo<?> c = Classes.getClassInfo(type);
					if (c == null || c.getSerializer() == null) {
						Skript.error("Cannot load the variable {" + name + "} from the database, because the type '" + type + "' cannot be recognized or not stored in variables");
						continue;
					}
					if (c.getSerializer().mustSyncDeserialization()) {
						syncDeserializing.add(new VariableInfo(name, value, c));
					} else {
						final Object d = c.getSerializer().deserialize(value);
						if (d == null) {
							Skript.error("Cannot load the variable {" + name + "} from the database, because '" + value + "' cannot be parsed as a " + type);
							continue;
						}
						Variables.setVariable(name, d, this);
					}
				}
			}
			if (!syncDeserializing.isEmpty()) {
				Task.callSync(new Callable<Void>() {
					@Override
					public Void call() throws Exception {
						synchronized (syncDeserializing) {
							for (final VariableInfo o : syncDeserializing) {
								final Object d = o.ci.getSerializer().deserialize(o.value);
								if (d == null) {
									Skript.error("Cannot load the variable {" + o.name + "} from the database, because '" + o.value + "' cannot be parsed as a " + o.ci.getCodeName());
									continue;
								}
								Variables.setVariable(o.name, d, DatabaseStorage.this);
							}
							syncDeserializing.clear();
							return null;
						}
					}
				});
			}
		}
	}
	
	private static void sqlException(final SQLException e) {
		Skript.error("database error: " + e.getLocalizedMessage());
		if (Skript.testing())
			e.printStackTrace();
	}
	
	@Override
	protected String type() {
		return "database";
	}
	
}
