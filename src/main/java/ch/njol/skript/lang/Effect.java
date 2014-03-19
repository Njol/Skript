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
 * Copyright 2011-2014 Peter Güttinger
 * 
 */

package ch.njol.skript.lang;

import java.util.Iterator;

import org.bukkit.event.Event;
import org.eclipse.jdt.annotation.Nullable;

import ch.njol.skript.Skript;

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
	
	@SuppressWarnings({"rawtypes", "unchecked", "null"})
	@Nullable
	public static Effect parse(final String s, final @Nullable String defaultError) {
		return (Effect) SkriptParser.parse(s, (Iterator) Skript.getEffects().iterator(), defaultError);
	}
	
}
