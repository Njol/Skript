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

import ch.njol.skript.ScriptLoader;
import ch.njol.skript.Skript;
import ch.njol.skript.config.SectionNode;

/**
 * Represents a section of a trigger, e.g. a conditional or a loop
 * 
 * @author Peter Güttinger
 * @see Conditional
 * @see Loop
 */
public abstract class TriggerSection extends TriggerItem {
	
	private List<TriggerItem> items;
	
	private boolean stopped = false;
	
	private final boolean stopParentOnFalseCondition;
	
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
	protected TriggerSection(final List<TriggerItem> items, final boolean stopParentOnFalseCondition) {
		setTriggerItems(items);
		this.stopParentOnFalseCondition = stopParentOnFalseCondition;
	}
	
	protected TriggerSection(final SectionNode node, final boolean stopParentOnFalseCondition) {
		ScriptLoader.currentSections.add(this);
		setTriggerItems(ScriptLoader.loadItems(node));
		ScriptLoader.currentSections.remove(ScriptLoader.currentSections.size() - 1);
		this.stopParentOnFalseCondition = stopParentOnFalseCondition;
	}
	
	/**
	 * Important when using this constructor: set the items with {@link #setTriggerItems(List)}!
	 */
	protected TriggerSection(final boolean stopParentOnFalseCondition) {
		this.stopParentOnFalseCondition = stopParentOnFalseCondition;
	}
	
	protected void setTriggerItems(final List<TriggerItem> items) {
		this.items = items;
		for (final TriggerItem item : items) {
			item.setParent(this);
		}
	}
	
	/**
	 * Subclasses must call {@link TriggerSection#run(Event, boolean) super.run(Event, boolean)} to handle this section's items (and logging)
	 */
	@Override
	public abstract boolean run(Event e);
	
	protected void run(final Event e, final boolean run) {
		if (Skript.debug() && !(this instanceof Trigger))
			Skript.info(indentation + (run ? "" : "-") + toString(e, true) + ":");
		if (!run)
			return;
		stopped = false;
		if (Skript.debug() && !(this instanceof Trigger))
			incIndentation();
		for (final TriggerItem i : items) {
			final boolean ok = i.run(e);
			if (Skript.debug() && !(i instanceof TriggerSection)) {
				if (!stopped)
					Skript.info(indentation + (ok ? "" : "-") + i.toString(e, true));
				else
					Skript.info(indentation + "#" + i.toString(e, true));
			}
			if (!ok && stopParentOnFalseCondition)
				getParent().stop();
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
