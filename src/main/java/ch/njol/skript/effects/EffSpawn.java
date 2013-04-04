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

import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.bukkit.event.Event;

import ch.njol.skript.Skript;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Examples;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import ch.njol.skript.entity.EntityType;
import ch.njol.skript.lang.Effect;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.skript.util.Direction;
import ch.njol.util.Kleenean;

/**
 * @author Peter Güttinger
 */
@SuppressWarnings("serial")
@Name("Spawn")
@Description("Spawn a creature.")
@Examples({"spawn 3 creepers at the targeted block",
		"spawn a ghast 5 meters above the player"})
@Since("1.0")
public class EffSpawn extends Effect {
	static {
		Skript.registerEffect(EffSpawn.class,
				"spawn %entitytypes% [%directions% %locations%]",
				"spawn %number% of %entitytypes% [%directions% %locations%]");
	}
	
	private Expression<Location> locations;
	private Expression<EntityType> types;
	private Expression<Number> amount;
	
	public static Entity lastSpawned = null;
	
	@SuppressWarnings("unchecked")
	@Override
	public boolean init(final Expression<?>[] exprs, final int matchedPattern, final Kleenean isDelayed, final ParseResult parser) {
		amount = matchedPattern == 0 ? null : (Expression<Number>) (exprs[0]);
		types = (Expression<EntityType>) exprs[matchedPattern];
		locations = Direction.combine((Expression<? extends Direction>) exprs[1 + matchedPattern], (Expression<? extends Location>) exprs[2 + matchedPattern]);
		return true;
	}
	
	@Override
	public void execute(final Event e) {
		if (amount != null && amount.getSingle(e) == null)
			return;
		final EntityType[] ts = types.getArray(e);
		final Number a = amount == null ? 1 : amount.getSingle(e);
		if (a == null)
			return;
		for (final Location l : locations.getArray(e)) {
			for (final EntityType type : ts) {
				for (int i = 0; i < a.doubleValue() * type.getAmount(); i++) {
					lastSpawned = type.data.spawn(l);
				}
			}
		}
	}
	
	@Override
	public String toString(final Event e, final boolean debug) {
		return "spawn " + types.toString(e, debug) + " " + locations.toString(e, debug);
	}
	
}
