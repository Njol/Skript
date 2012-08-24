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

package ch.njol.skript.lang;

import java.util.List;

import org.bukkit.event.Event;

import ch.njol.skript.ScriptLoader;
import ch.njol.skript.config.SectionNode;

/**
 * Represents a section of a trigger, e.g. a conditional or a loop
 * 
 * @author Peter Güttinger
 * @see Conditional
 * @see Loop
 */
public abstract class TriggerSection extends TriggerItem {
	
//	private List<TriggerItem> items;
	
	private TriggerItem first = null;
	protected TriggerItem last = null;
	
	/**
	 * reserved for new Trigger(...)
	 */
	protected TriggerSection(final List<TriggerItem> items) {
		setTriggerItems(items);
	}
	
	protected TriggerSection(final SectionNode node) {
		ScriptLoader.currentSections.add(this);
		setTriggerItems(ScriptLoader.loadItems(node));
		ScriptLoader.currentSections.remove(ScriptLoader.currentSections.size() - 1);
	}
	
	/**
	 * Important when using this constructor: set the items with {@link #setTriggerItems(List)}!
	 */
	protected TriggerSection() {}
	
	protected void setTriggerItems(final List<TriggerItem> items) {
		if (!items.isEmpty()) {
			first = items.get(0);
			last = items.get(items.size() - 1);
			last.setNext(getNext());
		}
//		this.items = items;
		for (final TriggerItem item : items) {
			item.setParent(this);
		}
	}
	
	@Override
	public void setNext(final TriggerItem next) {
		super.setNext(next);
		if (last != null)
			last.setNext(next);
	}
	
	@Override
	protected final boolean run(final Event e) {
		throw new UnsupportedOperationException();
	}
	
	@Override
	protected abstract TriggerItem walk(Event e);
	
	protected final TriggerItem walk(final Event e, final boolean run) {
		debug(e, run);
		if (run && first != null) {
			return first;
		} else {
			return getNext();
		}
	}
	
//	private final void runItems(final Event e, final int start) {
//		stopped = false;
//		if (start == 0 && Skript.debug() && !(this instanceof Trigger))
//			incIndentation();
//		for (int j = start; j < items.size(); j++) {
//			final TriggerItem i = items.get(j);
//			final boolean ok;
//			if (i instanceof EffDelay) {
//				final int d = ((EffDelay) i).getDelay(e);
//				ok = d != -1;
//				if (ok) {
//					final int newStart = j + 1;
//					Bukkit.getScheduler().scheduleSyncDelayedTask(Skript.getInstance(), new Runnable() {
//						@Override
//						public void run() {
//							TriggerSection.this.runItems(e, newStart);
//						}
//					}, d);
//					return;
//				}
//			} else {
//				if (i instanceof TriggerSection) {
//					((TriggerSection) i).run(e, j + 1);
//					return;
//				} else {
//					ok = i.run(e);
//				}
//			}
//			if (Skript.debug() && !(i instanceof TriggerSection)) {
//				if (!stopped)
//					Skript.info(indentation + (ok ? "" : "-") + i.toString(e, true));
//				else
//					Skript.info(indentation + "#" + i.toString(e, true));
//			}
//			if (!ok && stopParentOnFalseCondition)
//				getParent().stop();
//			if (stopped || !ok || i instanceof EffDelay)
//				break;
//		}
//		if (Skript.debug() && !(this instanceof Trigger))
//			decIndentation();
//	}
	
}
