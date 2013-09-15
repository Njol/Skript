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

import java.io.Closeable;
import java.util.concurrent.LinkedBlockingQueue;

import ch.njol.skript.Skript;
import ch.njol.skript.registrations.Classes;
import ch.njol.util.Pair;

/**
 * @author Peter Güttinger
 */
public abstract class VariablesStorage implements Closeable {
	
	private final static int QUEUE_SIZE = 1000;
	
	private final LinkedBlockingQueue<Pair<String, Object>> changesQueue = new LinkedBlockingQueue<Pair<String, Object>>(QUEUE_SIZE);
	
	protected boolean closed = false;
	
	public final boolean load() {
		if (!load_i())
			return false;
		Skript.closeOnDisable(this);
		writeThread.start();
		return true;
	}
	
	/**
	 * Loads variables stored here.
	 * 
	 * @return Whether
	 */
	protected abstract boolean load_i();
	
	private final Thread writeThread = Skript.newThread(new Runnable() {
		@Override
		public void run() {
			while (!closed) {
				try {
					final Pair<String, Object> data = changesQueue.take();
					final Pair<String, String> data2 = data.second == null ? null : Classes.serialize(data.second);
					if (data2 != null)
						save(data.first, data2.first, data2.second);
					else
						save(data.first, null, null);
				} catch (final InterruptedException e) {}
			}
		}
	}, "Skript variable save thread (" + type() + ")");
	
	private long lastWarning = Long.MIN_VALUE;
	private final static int WARNING_INTERVAL = 10;
	
	final void save(final String name, final Object value) {
		if (!changesQueue.offer(new Pair<String, Object>(name, value))) {
			if (lastWarning < System.currentTimeMillis() - WARNING_INTERVAL * 1000) {
				Skript.warning("Cannot write variables to the " + type() + " at sufficient speed; server performance will suffer and many variables will be lost if the server crashes. (this warning will be repeated at most once every " + WARNING_INTERVAL + " seconds)");
				lastWarning = System.currentTimeMillis();
			}
			while (true) {
				try {
					changesQueue.put(new Pair<String, Object>(name, value));
					break;
				} catch (final InterruptedException e) {}
			}
		}
	}
	
	@Override
	public void close() {
		while (changesQueue.size() > 0) {
			try {
				Thread.sleep(10);
			} catch (final InterruptedException e) {}
		}
		closed = true;
		writeThread.interrupt();
	}
	
	/**
	 * Clears the queue of unsaved variables.
	 */
	protected void clearChangesQueue() {
		changesQueue.clear();
	}
	
	/**
	 * Saves a variable.
	 * 
	 * @param name
	 * @param type
	 * @param value
	 */
	protected abstract void save(String name, String type, String value);
	
	protected abstract String type();
	
}
