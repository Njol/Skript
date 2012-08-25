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

import org.bukkit.event.Event;

import ch.njol.skript.config.SectionNode;

/**
 * Represents a conditional trigger section.
 * 
 * @author Peter Güttinger
 * @see TriggerSection
 * @see Condition
 */
public class Conditional extends TriggerSection {
	
	private final Condition cond;
	
	private TriggerSection elseClause = null;
	
	public Conditional(final Condition cond, final SectionNode node) {
		super(node);
		this.cond = cond;
	}
	
	@Override
	protected TriggerItem walk(final Event e) {
		if (cond.run(e)) {
			return walk(e, true);
		} else {
			debug(e, false);
			if (elseClause != null)
				return elseClause;
			return getNext();
		}
	}
	
	@Override
	public String toString(final Event e, final boolean debug) {
		return cond.toString(e, debug);
	}
	
	public void loadElseClause(final SectionNode node) {
		elseClause = new TriggerSection(node) {
			@Override
			public TriggerItem walk(final Event e) {
				return walk(e, true);
			}
			
			@Override
			public String toString(final Event e, final boolean debug) {
				return "else";
			}
		};
		elseClause.setParent(getParent());
		elseClause.setNext(getNext());
	}
	
}
