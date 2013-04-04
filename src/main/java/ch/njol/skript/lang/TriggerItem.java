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

package ch.njol.skript.lang;

import java.io.Serializable;

import org.bukkit.event.Event;

import ch.njol.skript.Skript;
import ch.njol.util.StringUtils;

/**
 * Represents a trigger item, i.e. a trigger section, a condition or an effect.
 * 
 * @author Peter Güttinger
 * @see TriggerSection
 * @see Trigger
 * @see Statement
 */
@SuppressWarnings("serial")
public abstract class TriggerItem implements Debuggable, Serializable {
	
	protected TriggerSection parent = null;
	private TriggerItem next = null;
	
	protected TriggerItem() {}
	
	protected TriggerItem(final TriggerSection parent) {
		this.parent = parent;
	}
	
	/**
	 * Executes this item and returns the next item to run.
	 * <p>
	 * Overriding classes must call {@link #debug(Event, boolean)}. If this method is overridden, {@link #run(Event)} is not used anymore and can be ignored.
	 * 
	 * @param e
	 * @return The next item to run or null to stop execution
	 */
	protected TriggerItem walk(final Event e) {
		if (run(e)) {
			debug(e, true);
			return next;
		} else {
			debug(e, false);
			return parent == null ? null : parent.getNext();
		}
	}
	
	/**
	 * Executes this item.
	 * 
	 * @param e
	 * @return Whether the next item should be run or this ite's parent
	 */
	protected abstract boolean run(Event e);
	
	public final static void walk(final TriggerItem start, final Event e) {
		assert start != null && e != null;
		TriggerItem i = start;
		try {
			while (i != null)
				i = i.walk(e);
		} catch (final StackOverflowError err) {
			Skript.adminBroadcast("<red>The script '<gold>" + start.getTrigger().getScript().getName() + "<red>' infinitely repeated itself!");
			if (Skript.debug())
				err.printStackTrace();
		} catch (final Exception ex) {
			if (ex.getStackTrace().length != 0)// empty exceptions have already been printed
				Skript.exception(ex, i);
		}
	}
	
	/**
	 * how much to indent each level
	 */
	private final static String indent = "  ";
	
	private String indentation = null;
	
	protected String getIndentation() {
		if (indentation == null) {
			int level = 0;
			TriggerItem i = this;
			while ((i = i.parent) != null)
				level++;
			indentation = StringUtils.multiply(indent, level);
		}
		return indentation;
	}
	
	protected final void debug(final Event e, final boolean run) {
		if (!Skript.debug())
			return;
		Skript.info(getIndentation() + (run ? "" : "-") + toString(e, true));
	}
	
	@Override
	public final String toString() {
		return toString(null, false);
	}
	
	public void setParent(final TriggerSection parent) {
		this.parent = parent;
	}
	
	public final TriggerSection getParent() {
		return parent;
	}
	
	public final Trigger getTrigger() {
		TriggerItem i = this;
		while (i != null && !(i instanceof Trigger))
			i = i.getParent();
		if (i == null)
			throw new IllegalStateException("TriggerItem without a Trigger detected!");
		return (Trigger) i;
	}
	
	public void setNext(final TriggerItem next) {
		this.next = next;
	}
	
	public TriggerItem getNext() {
		return next;
	}
	
}
