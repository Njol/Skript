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

import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.bukkit.event.Event;

import ch.njol.skript.Skript;
import ch.njol.skript.entity.EntityType;
import ch.njol.skript.lang.Effect;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.skript.util.Offset;

/**
 * 
 * @author Peter Güttinger
 */
public class EffSpawn extends Effect {
	
	static {
		Skript.registerEffect(EffSpawn.class,
				"spawn %entitytypes% [%offset% %locations%]",
				"spawn %integer% of %entitytypes% [%offset% %locations%]");
	}
	
	private Expression<Location> locations;
	private Expression<Offset> offsets;
	private Expression<EntityType> types;
	private Expression<Integer> amount;
	
	public static Entity lastSpawned = null;
	
	@SuppressWarnings("unchecked")
	@Override
	public boolean init(final Expression<?>[] exprs, final int matchedPattern, final int isDelayed, final ParseResult parser) {
		amount = matchedPattern == 0 ? null : (Expression<Integer>) (exprs[0]);
		types = (Expression<EntityType>) exprs[matchedPattern];
		offsets = (Expression<Offset>) exprs[1 + matchedPattern];
		locations = (Expression<Location>) exprs[2 + matchedPattern];
		return true;
	}
	
	@Override
	public void execute(final Event e) {
		if (amount != null && amount.getSingle(e) == null)
			return;
		final EntityType[] ts = types.getArray(e);
		final int a = amount == null ? 1 : amount.getSingle(e);
		for (final Location l : Offset.setOff(offsets.getArray(e), locations.getArray(e))) {
			for (final EntityType type : ts) {
				for (int i = 0; i < a * type.getAmount(); i++) {
					lastSpawned = type.data.spawn(l);
				}
			}
		}
	}
	
	@Override
	public String toString(final Event e, final boolean debug) {
		return "spawn " + types.toString(e, debug) + " " + offsets.toString(e, debug) + " " + locations.toString(e, debug);
	}
	
}
