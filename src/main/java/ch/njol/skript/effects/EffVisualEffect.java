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
import org.bukkit.entity.Player;
import org.bukkit.event.Event;

import ch.njol.skript.Skript;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Examples;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import ch.njol.skript.lang.Effect;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.Literal;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.skript.util.Direction;
import ch.njol.skript.util.VisualEffect;
import ch.njol.util.Kleenean;

/**
 * @author Peter Güttinger
 */
@SuppressWarnings("serial")
@Name("Play Effect")
@Description({"Plays a <a href='../classes/#visualeffect'>visual effect</a> at a given location or on a given entity.",
		"Please note that some effects can only be played on entities, e..g wolf hearts or the hurt effect, and that these are always visible to all players."})
@Examples({"show wolf hearts on the clicked wolf",
		"play mob spawner flames at the targeted block to the player"})
@Since("2.1")
public class EffVisualEffect extends Effect {
	static {
		Skript.registerEffect(EffVisualEffect.class, "(play|show) %visualeffects% (on|%directions%) %entities/locations% [to %-players%]");
	}
	
	private Expression<VisualEffect> effects;
	private Expression<Direction> direction;
	private Expression<?> where;
	private Expression<Player> players;
	
	@SuppressWarnings("unchecked")
	@Override
	public boolean init(final Expression<?>[] exprs, final int matchedPattern, final Kleenean isDelayed, final ParseResult parseResult) {
		effects = (Expression<VisualEffect>) exprs[0];
		direction = (Expression<Direction>) exprs[1];
		where = exprs[2];
		players = (Expression<Player>) exprs[3];
		if (effects instanceof Literal) {
			final VisualEffect[] effs = effects.getAll(null);
			@SuppressWarnings("unused")
			boolean hasLocationEffect = false, hasEntityEffect = false;
			for (final VisualEffect e : effs) {
				if (e.isEntityEffect())
					hasEntityEffect = true;
				else
					hasLocationEffect = true;
			}
			if (!hasLocationEffect && players != null)
				Skript.warning("Entity effects are visible to all players");
			if (!hasLocationEffect && !direction.isDefault())
				Skript.warning("Entity effects are always played on an entity");
		}
		return false;
	}
	
	@Override
	protected void execute(final Event e) {
		final VisualEffect[] effs = effects.getArray(e);
		final Direction[] dirs = direction.getArray(e);
		final Object[] os = where.getArray(e);
		final Player[] ps = players == null ? null : players.getArray(e);
		for (final Direction d : dirs) {
			for (final Object o : os) {
				if (o instanceof Entity) {
					for (final VisualEffect eff : effs) {
						eff.play(ps, d.getRelative((Entity) o), (Entity) o);
					}
				} else if (o instanceof Location) {
					for (final VisualEffect eff : effs) {
						if (eff.isEntityEffect())
							continue;
						eff.play(ps, d.getRelative((Location) o), null);
					}
				} else {
					assert false;
				}
			}
		}
	}
	
	@Override
	public String toString(final Event e, final boolean debug) {
		return "play " + effects.toString(e, debug) + " " + direction.toString(e, debug) + " " + where.toString(e, debug) + (players == null ? "" : " to " + players.toString(e, debug));
	}
	
}
