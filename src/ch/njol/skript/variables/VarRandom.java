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

package ch.njol.skript.variables;

import java.util.regex.Matcher;

import org.bukkit.event.Event;

import ch.njol.skript.Skript;
import ch.njol.skript.api.intern.Variable;

/**
 * @author Peter Güttinger
 * 
 */
public class VarRandom extends Variable<Double> {
	
	static {
		Skript.addVariable(VarRandom.class, Double.class, "random number between %double% and %double%");
	}
	
	Variable<Double> lower, upper;
	
	@SuppressWarnings("unchecked")
	@Override
	public void init(final Variable<?>[] vars, final int matchedPattern, final Matcher matcher) {
		lower = (Variable<Double>) vars[0];
		upper = (Variable<Double>) vars[1];
	}
	
	@Override
	protected Double[] getAll(final Event e) {
		final double l = lower.getFirst(e);
		final double u = upper.getFirst(e);
		
		return new Double[] {l + Math.random() * (u - l)};
	}
	
	@Override
	public Class<Double> getReturnType() {
		return Double.class;
	}
	
	@Override
	public String getDebugMessage(final Event e) {
		return "random number between " + lower + " and " + upper;
	}
	
	@Override
	public String toString() {
		return "a random number between " + lower + " and " + upper;
	}
	
}
