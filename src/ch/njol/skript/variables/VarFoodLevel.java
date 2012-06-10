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

import org.bukkit.entity.Player;
import org.bukkit.event.Event;

import ch.njol.skript.Skript;
import ch.njol.skript.api.Changer.ChangeMode;
import ch.njol.skript.api.Getter;
import ch.njol.skript.lang.ExprParser.ParseResult;
import ch.njol.skript.lang.SimpleVariable;
import ch.njol.skript.lang.Variable;

/**
 * @author Peter Güttinger
 * 
 */
public class VarFoodLevel extends SimpleVariable<Integer> {
	
	static {
		Skript.registerVariable(VarFoodLevel.class, Integer.class, "[the] food[[ ](level|meter)] [of %player%]", "%player%'[s] food[[ ](level|meter)]");
	}
	
	private Variable<Player> players;
	
	@SuppressWarnings("unchecked")
	@Override
	public boolean init(final Variable<?>[] vars, final int matchedPattern, final ParseResult parser) {
		players = (Variable<Player>) vars[0];
		return true;
	}
	
	@Override
	public String getDebugMessage(final Event e) {
		return "food level of " + players.getDebugMessage(e);
	}
	
	@Override
	protected Integer[] getAll(final Event e) {
		return players.getArray(e, Integer.class, new Getter<Integer, Player>() {
			@Override
			public Integer get(final Player p) {
				return Integer.valueOf(p.getFoodLevel());
			}
		});
	}
	
	@Override
	public Class<?> acceptChange(final ChangeMode mode) {
		return Integer.class;
	}
	
	@Override
	public void change(final Event e, final Variable<?> delta, final ChangeMode mode) {
		int s = 0;
		if (mode != ChangeMode.CLEAR)
			s = (Integer) delta.getSingle(e);
		switch (mode) {
			case SET:
			case CLEAR:
				for (final Player player : players.getArray(e)) {
					player.setFoodLevel(s);
				}
				return;
			case ADD:
				for (final Player player : players.getArray(e)) {
					player.setFoodLevel(player.getFoodLevel() + s);
				}
				return;
			case REMOVE:
				for (final Player player : players.getArray(e)) {
					player.setFoodLevel(player.getFoodLevel() - s);
				}
				return;
		}
	}
	
	@Override
	public Class<? extends Integer> getReturnType() {
		return Integer.class;
	}
	
	@Override
	public String toString() {
		return "the food level of " + players;
	}
	
	@Override
	public boolean isSingle() {
		return players.isSingle();
	}
	
}
