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

import ch.njol.skript.Skript;
import ch.njol.skript.TriggerFileLoader;
import ch.njol.skript.api.Effect;
import ch.njol.skript.api.exception.ParseException;
import ch.njol.skript.api.intern.TriggerSection;
import ch.njol.skript.lang.ExprParser.ParseResult;
import ch.njol.skript.lang.Variable;

/**
 * @author Peter Güttinger
 * 
 */
public class EffExit extends Effect {
	
	static {
		Skript.addEffect(EffExit.class,
				"(exit|stop) [trigger]",
				"(exit|stop) [1] section",
				"(exit|stop) <\\d+> sections",
				"(exit|stop) all sections");
	}
	
	private int breakLevels;
	
	@Override
	public void init(final Variable<?>[] vars, final int matchedPattern, final ParseResult parser) throws ParseException {
		switch (matchedPattern) {
			case 0:
				breakLevels = TriggerFileLoader.currentSections.size() + 1;
			break;
			case 1:
				breakLevels = 1;
			break;
			case 2:
				breakLevels = Integer.parseInt(parser.regexes.get(0).group());
				if (breakLevels > TriggerFileLoader.currentSections.size()) {
					if (TriggerFileLoader.currentSections.isEmpty())
						throw new ParseException("you can't exit any sections as there are no sections present");
					else
						throw new ParseException("you can't exit as there are only " + TriggerFileLoader.currentSections.size() + " sections present");
				}
			break;
			case 3:
				breakLevels = TriggerFileLoader.currentSections.size();
			break;
		}
	}
	
	@Override
	protected void execute(final Event e) {
		TriggerSection item = getParent();
		for (int i = 0; i < breakLevels && item != null; i++) {
			item.stop();
			item = item.getParent();
		}
	}
	
	@Override
	public String getDebugMessage(final Event e) {
		return "exit " + breakLevels + " sections";
	}
	
}
