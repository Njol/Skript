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

import ch.njol.skript.api.Debuggable;

/**
 * Represents a trigger item, i.e. a trgger section, a condition or an effect.
 * 
 * @author Peter Güttinger
 * @see TriggerSection
 * @see Trigger
 * @see TopLevelExpression
 */
public abstract class TriggerItem implements Debuggable {
	
	private TriggerSection parent;
	
	protected TriggerItem() {}
	
	protected TriggerItem(final TriggerSection parent) {
		this.parent = parent;
	}
	
	public abstract boolean run(Event e);
	
	final public void setParent(final TriggerSection parent) {
		this.parent = parent;
	}
	
	final public TriggerSection getParent() {
		return parent;
	}
	
}
