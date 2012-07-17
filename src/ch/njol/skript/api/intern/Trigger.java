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

package ch.njol.skript.api.intern;

import java.util.List;

import org.bukkit.event.Event;

import ch.njol.skript.Skript;
import ch.njol.skript.api.SkriptEvent;

/**
 * 
 * @author Peter Güttinger
 * 
 */
public class Trigger extends TriggerSection {
	private final String name;
	private final SkriptEvent event;
	
	public Trigger(final String name, final SkriptEvent event, final List<TriggerItem> items) {
		super(items, false);
		this.name = name;
		this.event = event;
	}
	
	@Override
	public boolean run(final Event e) {
		try {
			super.run(e, true);// checked in SkriptEventHandler
		} catch (final Exception ex) {
			if (ex.getStackTrace().length != 0)// empty exceptions have already been printed
				Skript.exception(ex);
		}
		return true;
	}
	
	@Override
	public String toString(final Event e, final boolean debug) {
		throw new SkriptAPIException("a trigger's debug message should not be used");
	}
	
	public String getName() {
		return name;
	}
	
	public SkriptEvent getEvent() {
		return event;
	}
	
}
