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

package ch.njol.skript.effects;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.HashMap;
import java.util.logging.Level;

import org.bukkit.Bukkit;
import org.bukkit.event.Event;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.server.PluginDisableEvent;

import ch.njol.skript.Skript;
import ch.njol.skript.SkriptConfig;
import ch.njol.skript.lang.Effect;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.util.ExceptionUtils;

/**
 * @author Peter Güttinger
 */
public class EffLog extends Effect {
	private static final long serialVersionUID = 8213269874498098037L;
	
	static {
		Skript.registerEffect(EffLog.class, "log %strings% [(to|in) [file[s]] %-strings%]");
	}
	
	private final static File logsFolder = new File(Skript.getInstance().getDataFolder(), "logs");
	static {
		logsFolder.mkdirs();
	}
	
	private final static HashMap<String, PrintWriter> writers = new HashMap<String, PrintWriter>();
	static {
		Bukkit.getPluginManager().registerEvents(new Listener() {
			@EventHandler
			public void onPluginDisable(final PluginDisableEvent e) {
				if (e.getPlugin() == Skript.getInstance()) {
					for (final PrintWriter pw : writers.values())
						pw.close();
				}
			}
		}, Skript.getInstance());
	}
	
	private Expression<String> messages;
	private Expression<String> files;
	
	@SuppressWarnings("unchecked")
	@Override
	public boolean init(final Expression<?>[] exprs, final int matchedPattern, final int isDelayed, final ParseResult parser) {
		messages = (Expression<String>) exprs[0];
		files = (Expression<String>) exprs[1];
		return true;
	}
	
	@Override
	protected void execute(final Event e) {
		for (final String message : messages.getArray(e)) {
			if (files == null) {
				Skript.info("[" + getTrigger().getScript().getName() + "] " + message);
			} else {
				for (String s : files.getArray(e)) {
					s = s.toLowerCase();
					if (!s.endsWith(".log"))
						s += ".log";
					if (s.equals("server.log")) {
						Bukkit.getLogger().log(Level.INFO, message);
						continue;
					}
					@SuppressWarnings("resource")
					PrintWriter w = writers.get(s);
					if (w == null) {
						try {
							w = new PrintWriter(new BufferedWriter(new FileWriter(new File(logsFolder, s), true)));
							writers.put(s, w);
						} catch (final IOException e1) {
							Skript.error("Cannot write to log file '" + s + "': " + ExceptionUtils.toString(e1));
							return;
						}
					}
					w.println("[" + SkriptConfig.getDateFormat().format(System.currentTimeMillis()) + "] " + message);
					w.flush();
				}
			}
		}
	}
	
	@Override
	public String toString(final Event e, final boolean debug) {
		return "log " + messages.toString(e, debug) + " to " + files.toString(e, debug);
	}
}
