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

import ch.njol.skript.api.LoopExpr;
import ch.njol.skript.config.SectionNode;

/**
 * A trigger section which represents a loop.
 * 
 * @author Peter Güttinger
 * @see LoopExpr
 */
public class Loop extends TriggerSection {
	
	private final LoopExpr<?> var;
	
	public <T> Loop(final LoopExpr<?> var, final SectionNode node) {
		super(node);
		this.var = var;
	}
	
	@Override
	public boolean run(final Event e) {
		var.startLoop(e);
		if (!var.hasNext()) {
			super.run(e, false);
			return true;
		}
		while (var.hasNext()) {
			var.next();
			super.run(e, true);
			if (isStopped())
				break;
		}
		return true;
	}
	
	@Override
	public String getDebugMessage(final Event e) {
		return "loop " + var.getLoopDebugMessage(e);
	}
	
}
