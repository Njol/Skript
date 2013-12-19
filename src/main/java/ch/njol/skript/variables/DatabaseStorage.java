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
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.LinkedList;
import java.util.Map.Entry;
import java.util.UUID;
import java.util.concurrent.Callable;

import lib.PatPeter.SQLibrary.Database;
import lib.PatPeter.SQLibrary.DatabaseException;
import lib.PatPeter.SQLibrary.MySQL;
import lib.PatPeter.SQLibrary.SQLibrary;
import lib.PatPeter.SQLibrary.SQLite;

import org.bukkit.Bukkit;
import org.bukkit.plugin.Plugin;

import ch.njol.skript.Skript;
import ch.njol.skript.classes.ClassInfo;
import ch.njol.skript.config.SectionNode;
import ch.njol.skript.log.SkriptLogger;
import ch.njol.skript.registrations.Classes;
import ch.njol.skript.util.Task;
import ch.njol.skript.util.Timespan;
import ch.njol.util.Pair;

/**
 * @author Peter Güttinger
 */
public class DatabaseStorage extends VariablesStorage {
	
	public final static int MAX_VARIABLE_NAME_LENGTH = 500,
			MAX_CLASS_CODENAME_LENGTH = 50,
			MAX_VALUE_SIZE = 10000;
	
	private final static String TABLE_NAME = "variables21",
			OLD_TABLE_NAME = "variables";
	
	private final static String SELECT_ORDER = "name, type, value, rowid";
	
	public static enum Type {
		MYSQL("CREATE TABLE IF NOT EXISTS " + TABLE_NAME + " (" +
				"rowid        BIGINT  NOT NULL  AUTO_INCREMENT  PRIMARY KEY," +
				"name         VARCHAR(" + MAX_VARIABLE_NAME_LENGTH + ")  NOT NULL  UNIQUE," +
				"type         VARCHAR(" + MAX_CLASS_CODENAME_LENGTH + ")," +
				"value        BLOB(" + MAX_VALUE_SIZE + ")," +
				"update_guid  CHAR(36)  NOT NULL" +
				")") {
			@Override
			protected Object initialise(final DatabaseStorage s, final SectionNode n) {
				final String host = s.getValue(n, "host");
				final Integer port = s.getValue(n, "port", Integer.class);
				final String user = s.getValue(n, "user");
				final String password = s.getValue(n, "password");
				final String database = s.getValue(n, "database");
				if (host == null || port == null || user == null || password == null || database == null)
					return null;
				return new MySQL(SkriptLogger.LOGGER, "[Skript]", host, port, database, user, password);
			}
		},
		SQLITE("CREATE TABLE IF NOT EXISTS " + TABLE_NAME + " (" +
				"name         VARCHAR(" + MAX_VARIABLE_NAME_LENGTH + ")  NOT NULL  PRIMARY KEY," +
				"type         VARCHAR(" + MAX_CLASS_CODENAME_LENGTH + ")," +
				"value        BLOB(" + MAX_VALUE_SIZE + ")," +
				"update_guid  CHAR(36)  NOT NULL" +
				")") {
			@Override
			protected Object initialise(final DatabaseStorage s, final SectionNode config) {
				final String name = s.file.getName();
				assert name.endsWith(".db");
				return new SQLite(SkriptLogger.LOGGER, "[Skript]", s.file.getParent(), name.substring(0, name.length() - ".db".length()));
			}
		};
		
		final String createQuery;
		
		Type(final String cq) {
			createQuery = cq;
		}
		
		protected abstract Object initialise(DatabaseStorage s, SectionNode config);
	}
	
	private final Type type;
	
	// can't be a Database as otherwise loading this class will result in a NoClassDefFoundError if SQLibrary is not present
	private Object db;
	
	private boolean monitor = false;
	long monitor_interval;
	
	private final static String guid = UUID.randomUUID().toString();
	
	DatabaseStorage(final SectionNode n, final Type type) {
		super(n);
		this.type = type;
	}
	
	@Override
	protected boolean load_i(final SectionNode n) {
		final Plugin p = Bukkit.getPluginManager().getPlugin("SQLibrary");
		if (p == null || !(p instanceof SQLibrary)) {
			Skript.error("You need the plugin SQLibrary in order to use a database with Skript. You can download the latest version from http://dev.bukkit.org/server-mods/sqlibrary/files/");
			return false;
		}
		
		final Boolean monitor_changes = getValue(n, "monitor changes", Boolean.class);
		final Timespan monitor_interval = getValue(n, "monitor interval", Timespan.class);
		if (monitor_changes == null || monitor_interval == null)
			return false;
		monitor = monitor_changes;
		this.monitor_interval = monitor_interval.getMilliSeconds();
		
		try {
			db = type.initialise(this, n);
			if (db == null)
				return false;
		} catch (final RuntimeException e) {
			if (e instanceof DatabaseException) {// not in a catch clause to not produce a ClassNotFoundException when this class is loaded and SQLibrary is not present
				Skript.error(e.getMessage());
				return false;
			}
			throw e;
		}
		
		if (!((Database) db).open()) {
			Skript.error("Cannot connect to the database!");
			return false;
		}
		
		try {
			try {
				((Database) db).query(type.createQuery);
			} catch (final SQLException e) {
				Skript.error("Could not create the variables table: " + e.getLocalizedMessage() + ". Please create the table yourself using the following query: " + type.createQuery);
				return false;
			}
			
			writeQuery = ((Database) db).prepare("REPLACE INTO " + TABLE_NAME + " (name, type, value, update_guid) VALUES (?, ?, ?, ?)");
			
			deleteQuery = ((Database) db).prepare("DELETE FROM " + TABLE_NAME + " WHERE name = ?");
			
			monitorQuery = ((Database) db).prepare("SELECT " + SELECT_ORDER + " FROM " + TABLE_NAME + " WHERE rowid > ? AND update_guid != ?");
			monitorCleanUpQuery = ((Database) db).prepare("DELETE FROM " + TABLE_NAME + " WHERE value IS NULL AND rowid < ?");
			
			final boolean hasOldTable = ((Database) db).isTable(OLD_TABLE_NAME);
			final boolean hadNewTable = ((Database) db).isTable(TABLE_NAME);
			
			// old
			Statement old = null;
			try {
				old = ((Database) db).getConnection().createStatement();
				if (hasOldTable && !hadNewTable && old.execute("SELECT " + SELECT_ORDER + " FROM " + OLD_TABLE_NAME)) {
					ResultSet r = null;
					try {
						r = old.getResultSet();
						oldLoadVariables(r);
					} finally {
						if (r != null)
							r.close();
					}
					
				}
			} finally {
				if (old != null) {
					try {
						old.close();
					} catch (final SQLException e) {}
				}
			}
			
			// new
			Statement s = null;
			try {
				s = ((Database) db).getConnection().createStatement();
				s.execute("SELECT " + SELECT_ORDER + " FROM " + TABLE_NAME);
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
			
			// store old variables in new table and delete the old table
			if (hasOldTable) {
				if (!hadNewTable) {
					Skript.info("[2.1] Updating the database '" + name + "' to the new format...");
					try {
						Variables.getReadLock().lock();
						for (final Entry<String, Object> v : Variables.getVariablesHashMap().entrySet()) {
							if (accept(v.getKey())) {// only one database was possible, so only checking this database is correct
								final Pair<String, Pair<String, byte[]>> var = Variables.serialize(v.getKey(), v.getValue());
								save(var.first, var.second.first, var.second.second);
							}
						}
						Skript.info("Updated " + Variables.getVariablesHashMap().size() + " variables");
					} finally {
						Variables.getReadLock().unlock();
					}
				}
				Statement drop = null;
				try {
					boolean error = false;
					try {
						disconnect(); // prevents SQLITE_LOCKED error
						connect();
						drop = ((Database) db).getConnection().createStatement();
						drop.execute("DROP TABLE " + OLD_TABLE_NAME);
					} catch (final SQLException e) {
						Skript.error("There was an error deleting the old variables table, please delete it yourself: " + e.getLocalizedMessage());
						error = true;
					}
					if (!error)
						Skript.info("Successfully deleted the old variables table");
				} finally {
					if (drop != null) {
						try {
							drop.close();
						} catch (final SQLException e) {}
					}
				}
				if (!hadNewTable)
					Skript.info("Database '" + name + "' successfully updated.");
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
	
	@Override
	protected boolean requiresFile() {
		return type == Type.SQLITE;
	}
	
	@Override
	protected File getFile(String file) {
		if (!file.endsWith(".db"))
			file = file + ".db"; // required by SQLibrary
		return new File(file);
	}
	
	@Override
	protected void connect() {
		// isConnected doesn't work in SQLite
//		if (((Database) db).isConnected())
//			return;
		if (!((Database) db).open()) {
			Skript.exception("Cannot reconnect to the database '" + name + "'!"); // shouldn't ever happen as this is only used for SQLite
		}
	}
	
	@Override
	protected void disconnect() {
//		if (((Database) db).isConnected())
		((Database) db).close();
	}
	
	private PreparedStatement writeQuery, deleteQuery, monitorQuery;
	
	PreparedStatement monitorCleanUpQuery;
	
	@Override
	protected void save(final String name, final String type, final byte[] value) {
		// REMIND get the actual maximum size from the database
		if (name.length() > MAX_VARIABLE_NAME_LENGTH)
			Skript.error("The name of the variable '" + name + "' is too long to be saved in a database (length: " + name.length() + ", maximum allowed: " + MAX_VARIABLE_NAME_LENGTH + ")! It will be truncated and won't bet available under the same name again when loaded.");
		if (value != null && value.length > MAX_VALUE_SIZE)
			Skript.error("The variable '" + name + "' cannot be saved in the database as its value size (" + value.length + ") exceeds the maximum allowed size of " + MAX_VALUE_SIZE + "! An attempt to save the variable will be made nonetheless.");
		try {
			if (!monitor && type == null) {
				assert value == null;
				deleteQuery.setString(1, name);
				deleteQuery.executeUpdate();
			} else {
				int i = 1;
				writeQuery.setString(i++, name);
				writeQuery.setString(i++, type);
				writeQuery.setBytes(i++, value); // SQLite desn't support setBlob
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
		final byte[] value;
		final ClassInfo<?> ci;
		
		public VariableInfo(final String name, final byte[] value, final ClassInfo<?> ci) {
			this.name = name;
			this.value = value;
			this.ci = ci;
		}
	}
	
	final static LinkedList<VariableInfo> syncDeserializing = new LinkedList<VariableInfo>();
	
	private void loadVariables(final ResultSet r) throws SQLException {
		synchronized (syncDeserializing) {
			while (r.next()) {
				int i = 1;
				final String name = r.getString(i++);
				final String type = r.getString(i++);
				final byte[] value = r.getBytes(i++); // Blob not supported by SQLite
				lastRowID = r.getLong(i++);
				if (value == null) {
					Variables.variableLoaded(name, null, this);
				} else {
					final ClassInfo<?> c = Classes.getClassInfo(type);
					if (c == null || c.getSerializer() == null) {
						Skript.error("Cannot load the variable {" + name + "} from the database " + name + ", because the type '" + type + "' cannot be recognised or cannot be not stored in variables");
						continue;
					}
					if (c.getSerializer().mustSyncDeserialization()) {
						syncDeserializing.add(new VariableInfo(name, value, c));
					} else {
						final Object d = Classes.deserialize(c, value);
						if (d == null) {
							Skript.error("Cannot load the variable {" + name + "} from the database " + name + ", because it cannot be loaded as a " + type);
							continue;
						}
						Variables.variableLoaded(name, d, this);
					}
				}
			}
			if (!syncDeserializing.isEmpty()) {
				Task.callSync(new Callable<Void>() {
					@Override
					public Void call() throws Exception {
						synchronized (syncDeserializing) {
							for (final VariableInfo o : syncDeserializing) {
								final Object d = Classes.deserialize(o.ci, o.value);
								if (d == null) {
									Skript.error("Cannot load the variable {" + o.name + "} from the database " + name + ", because it cannot be loaded as a " + o.ci.getName());
									continue;
								}
								Variables.variableLoaded(o.name, d, DatabaseStorage.this);
							}
							syncDeserializing.clear();
							return null;
						}
					}
				});
			}
		}
	}
	
	private final static class OldVariableInfo {
		final String name;
		final String value;
		final ClassInfo<?> ci;
		
		public OldVariableInfo(final String name, final String value, final ClassInfo<?> ci) {
			this.name = name;
			this.value = value;
			this.ci = ci;
		}
	}
	
	final static LinkedList<OldVariableInfo> oldSyncDeserializing = new LinkedList<OldVariableInfo>();
	
	@Deprecated
	private void oldLoadVariables(final ResultSet r) throws SQLException {
		synchronized (oldSyncDeserializing) {
			while (r.next()) {
				int i = 1;
				final String name = r.getString(i++);
				final String type = r.getString(i++);
				final String value = r.getString(i++);
				lastRowID = r.getLong(i++);
				if (type == null) {
					Variables.variableLoaded(name, null, this);
				} else {
					final ClassInfo<?> c = Classes.getClassInfo(type);
					if (c == null || c.getSerializer() == null) {
						Skript.error("Cannot load the variable {" + name + "} from the database, because the type '" + type + "' cannot be recognised or not stored in variables");
						continue;
					}
					if (c.getSerializer().mustSyncDeserialization()) {
						oldSyncDeserializing.add(new OldVariableInfo(name, value, c));
					} else {
						final Object d = c.getSerializer().deserialize(value);
						if (d == null) {
							Skript.error("Cannot load the variable {" + name + "} from the database, because '" + value + "' cannot be parsed as a " + type);
							continue;
						}
						Variables.variableLoaded(name, d, this);
					}
				}
			}
			if (!oldSyncDeserializing.isEmpty()) {
				Task.callSync(new Callable<Void>() {
					@Override
					public Void call() throws Exception {
						synchronized (oldSyncDeserializing) {
							for (final OldVariableInfo o : oldSyncDeserializing) {
								final Object d = o.ci.getSerializer().deserialize(o.value);
								if (d == null) {
									Skript.error("Cannot load the variable {" + o.name + "} from the database, because '" + o.value + "' cannot be parsed as a " + o.ci.getCodeName());
									continue;
								}
								Variables.variableLoaded(o.name, d, DatabaseStorage.this);
							}
							oldSyncDeserializing.clear();
							return null;
						}
					}
				});
			}
		}
	}
	
	static void sqlException(final SQLException e) {
		Skript.error("database error: " + e.getLocalizedMessage());
		if (Skript.testing())
			e.printStackTrace();
	}
	
}
