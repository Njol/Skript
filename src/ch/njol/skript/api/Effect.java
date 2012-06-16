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

package ch.njol.skript.api;

import org.bukkit.event.Event;

import ch.njol.skript.Skript;
import ch.njol.skript.api.intern.Statement;
import ch.njol.skript.lang.SkriptParser;

/**
 * An effect which is executed once execution of the trigger is at it's entry. After execution the trigger will continue with the next element in the trigger item list.
 * 
 * @author Peter Güttinger
 * @see Skript#registerEffect(Class, String...)
 */
public abstract class Effect extends Statement {
	protected Effect() {}
	
	/**
	 * executes the effect.
	 * 
	 * @param e
	 */
	protected abstract void execute(Event e);
	
	@Override
	public final boolean run(final Event e) {
		execute(e);
		return true;
	}
	
	public static Effect parse(final String s, final String defaultError) {
		return (Effect) SkriptParser.parse(s, Skript.getEffects().iterator(), false, false, defaultError);
	}
	
}
