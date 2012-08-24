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

package ch.njol.skript.effects;

import org.bukkit.event.Event;

import ch.njol.skript.ScriptLoader;
import ch.njol.skript.Skript;
import ch.njol.skript.lang.Effect;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.Loop;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.skript.lang.TriggerItem;

/**
 * @author Peter Güttinger
 * 
 */
public class EffExit extends Effect {
	
	static {
		Skript.registerEffect(EffExit.class,
				"(exit|stop) [trigger]",
				"(exit|stop) [1] section",
				"(exit|stop) <\\d+> sections",
				"(exit|stop) all sections");
	}
	
	private int breakLevels;
	
	@Override
	public boolean init(final Expression<?>[] vars, final int matchedPattern, final int isDelayed, final ParseResult parser) {
		switch (matchedPattern) {
			case 0:
				breakLevels = ScriptLoader.currentSections.size() + 1;
			break;
			case 1:
			case 2:
				breakLevels = matchedPattern == 1 ? 1 : Integer.parseInt(parser.regexes.get(0).group());
				if (breakLevels > ScriptLoader.currentSections.size()) {
					if (ScriptLoader.currentSections.isEmpty()) {
						Skript.error("can't exit any sections as there are no sections present");
						return false;
					} else {
						Skript.error("can't exit " + breakLevels + " sections as there are only " + ScriptLoader.currentSections.size() + " sections present");
						return false;
					}
				}
			break;
			case 3:
				breakLevels = ScriptLoader.currentSections.size();
			break;
		}
		return true;
	}
	
	@Override
	protected TriggerItem walk(final Event e) {
		debug(e, false);
		int i = breakLevels - 1;
		TriggerItem n = parent;
		while (i > 0) {
			n = n.getParent();
			assert n != null;
			i--;
		}
		return n instanceof Loop ? ((Loop) n).getActualNext() : n.getNext();
	}
	
	@Override
	protected void execute(final Event e) {
		throw new UnsupportedOperationException();
	}
	
	@Override
	public String toString(final Event e, final boolean debug) {
		return "exit " + breakLevels + " sections";
	}
	
}
