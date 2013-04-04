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

package ch.njol.skript.effects;

import org.bukkit.event.Event;

import ch.njol.skript.Skript;
import ch.njol.skript.lang.Effect;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.SkriptParser.ParseResult;

/**
 * @author Peter Güttinger
 *
 */
public class EffPlayEffect extends Effect {
	
	static {
		Skript.registerEffect(EffPlayEffect.class, "");
	}
	
	private static enum EffectType {
		
	}
	
	@Override
	public boolean init(Expression<?>[] exprs, int matchedPattern, int isDelayed, ParseResult parseResult) {
		// TODO Auto-generated method stub
		return false;
	}
	
	@Override
	public String toString(Event e, boolean debug) {
		// TODO Auto-generated method stub
		return null;
	}
	
	@Override
	protected void execute(Event e) {
		// TODO Auto-generated method stub
		
	}
	
}
