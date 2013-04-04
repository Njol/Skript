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

package ch.njol.skript.log;

import java.util.logging.Level;

import org.bukkit.command.CommandSender;

/**
 * Redirects the log to a {@link CommandSender}.
 * 
 * @author Peter Güttinger
 */
public class RedirectingLogHandler extends LogHandler {
	
	private final CommandSender recipient;
	private final String prefix;
	
	private int numErrors = 0;
	
	public RedirectingLogHandler(final CommandSender recipient, final String prefix) {
		this.recipient = recipient;
		this.prefix = prefix == null ? "" : prefix;
	}
	
	@Override
	public boolean log(final LogEntry entry) {
		recipient.sendMessage(prefix + entry.message);
		if (entry.level == Level.SEVERE)
			numErrors++;
		return false;
	}
	
	public int numErrors() {
		return numErrors;
	}
	
}
