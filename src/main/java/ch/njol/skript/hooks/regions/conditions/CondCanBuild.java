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

package ch.njol.skript.hooks.regions.conditions;

import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;

import ch.njol.skript.Skript;
import ch.njol.skript.hooks.regions.RegionsPlugin;
import ch.njol.skript.lang.Condition;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.skript.util.Direction;
import ch.njol.util.Checker;
import ch.njol.util.Kleenean;

/**
 * @author Peter Güttinger
 */
@SuppressWarnings("serial")
public class CondCanBuild extends Condition {
	
	static {
		Skript.registerCondition(CondCanBuild.class,
				"%players% (can|(is|are) allowed to) build %directions% %locations%",
				"%players% (can('t|not)|(is|are)(n't| not) allowed to) build %directions% %locations%");
	}
	
	private Expression<Player> players;
	private Expression<Direction> directions;
	private Expression<Location> locations;
	
	@SuppressWarnings("unchecked")
	@Override
	public boolean init(final Expression<?>[] exprs, final int matchedPattern, final Kleenean isDelayed, final ParseResult parseResult) {
		players = (Expression<Player>) exprs[0];
		directions = (Expression<Direction>) exprs[1];
		locations = (Expression<Location>) exprs[2];
		setNegated(matchedPattern == 1);
		return true;
	}
	
	@Override
	public boolean check(final Event e) {
		return players.check(e, new Checker<Player>() {
			@Override
			public boolean check(final Player p) {
				return directions.check(e, new Checker<Direction>() {
					@Override
					public boolean check(final Direction d) {
						return locations.check(e, new Checker<Location>() {
							@Override
							public boolean check(final Location l) {
								return RegionsPlugin.canBuild(p, d.getRelative(l));
							}
						}, isNegated());
					}
				});
			}
		});
	}
	
	@Override
	public String toString(final Event e, final boolean debug) {
		return players.toString(e, debug) + " can build " + directions.toString() + " " + locations.toString(e, debug);
	}
	
}
