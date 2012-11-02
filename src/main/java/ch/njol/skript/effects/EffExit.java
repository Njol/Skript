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
import ch.njol.skript.log.ErrorQuality;

/**
 * @author Peter Güttinger
 */
public class EffExit extends Effect {
	
	private static final long serialVersionUID = -5403936517942163172L;
	
	static {
		Skript.registerEffect(EffExit.class,
				"(exit|stop) [trigger]",
				"(exit|stop) [(1|a|the|this)] section",
				"(exit|stop) <\\d+> sections",
				"(exit|stop) all sections");
	}
	
	private int breakLevels;
	
	@Override
	public boolean init(final Expression<?>[] exprs, final int matchedPattern, final int isDelayed, final ParseResult parser) {
		switch (matchedPattern) {
			case 0:
				breakLevels = ScriptLoader.currentSections.size() + 1;
				break;
			case 1:
			case 2:
				breakLevels = matchedPattern == 1 ? 1 : Integer.parseInt(parser.regexes.get(0).group());
				if (breakLevels > ScriptLoader.currentSections.size()) {
					if (ScriptLoader.currentSections.isEmpty())
						Skript.error("can't exit any sections as there are no sections present", ErrorQuality.SEMANTIC_ERROR);
					else
						Skript.error("can't exit " + breakLevels + " sections as there are only " + ScriptLoader.currentSections.size() + " sections present", ErrorQuality.SEMANTIC_ERROR);
					return false;
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
		TriggerItem n = parent;
		for (int i = breakLevels - 1; i > 0; i--) {
			n = n.getParent();
			assert n != null;
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
