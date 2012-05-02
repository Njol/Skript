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

import org.bukkit.event.Event;

import ch.njol.skript.api.Condition;
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
	public boolean run(final Event e) {
		final boolean b = cond.run(e);
		super.run(e, b);
		if (elseClause != null)
			elseClause.run(e, !b);
		return true;
	}
	
	@Override
	public String getDebugMessage(final Event e) {
		return cond.getDebugMessage(e);
	}
	
	public void loadElseClause(final SectionNode node) {
		elseClause = new TriggerSection(node) {
			
			@Override
			public String getDebugMessage(final Event e) {
				return "else";
			}
			
			@Override
			public boolean run(final Event e) {
				throw new RuntimeException();
			}
			
		};
		elseClause.setParent(getParent());
	}
	
}
