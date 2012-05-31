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
import ch.njol.skript.TriggerFileLoader;
import ch.njol.skript.config.SectionNode;

/**
 * Represents a section of a trigger, e.g. a conditional or a loop
 * 
 * @author Peter Güttinger
 * @see Conditional
 * @see Loop
 */
public abstract class TriggerSection extends TriggerItem {
	
	private final List<TriggerItem> items;
	
	private boolean stopped = false;
	
	/**
	 * how much to indent each level
	 */
	private final static String indent = "  ";
	private static String indentation = "  ";
	
	protected static void incIndentation() {
		indentation += indent;
	}
	
	protected static void decIndentation() {
		indentation = indentation.substring(indentation.length() - indent.length());
	}
	
	/**
	 * reserved for new Trigger(...)
	 */
	protected TriggerSection(final List<TriggerItem> items) {
		this.items = items;
		for (final TriggerItem item : items) {
			item.setParent(this);
		}
	}
	
	protected TriggerSection(final SectionNode node) {
		TriggerFileLoader.currentSections.add(this);
		items = TriggerFileLoader.loadItems(node);
		for (final TriggerItem item : items) {
			item.setParent(this);
		}
		TriggerFileLoader.currentSections.remove(TriggerFileLoader.currentSections.size() - 1);
	}
	
	/**
	 * Subclasses must call {@link TriggerSection#run(Event, boolean) super.run(Event, boolean)} to handle this section's items (and logging)
	 */
	@Override
	public abstract boolean run(Event e);
	
	protected void run(final Event e, final boolean run) {
		if (Skript.debug() && !(this instanceof Trigger))
			Skript.info(indentation + (run ? "" : "-") + getDebugMessage(e) + ":");
		if (!run)
			return;
		stopped = false;
		if (Skript.debug() && !(this instanceof Trigger))
			incIndentation();
		for (final TriggerItem i : items) {
			final boolean ok = i.run(e);
			if (Skript.debug() && !(i instanceof TriggerSection)) {
				if (!stopped)
					Skript.info(indentation + (ok ? "" : "-") + i.getDebugMessage(e));
				else
					Skript.info(indentation + "#" + i.getDebugMessage(e));
			}
			if (stopped || !ok)
				break;
		}
		if (Skript.debug() && !(this instanceof Trigger))
			decIndentation();
	}
	
	public void stop() {
		stopped = true;
	}
	
	public boolean isStopped() {
		return stopped;
	}
	
}
