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

package ch.njol.skript.expressions;

import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.entity.FoodLevelChangeEvent;

import ch.njol.skript.ScriptLoader;
import ch.njol.skript.Skript;
import ch.njol.skript.Skript.ExpressionType;
import ch.njol.skript.api.Changer.ChangeMode;
import ch.njol.skript.api.Getter;
import ch.njol.skript.expressions.base.PropertyExpression;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.skript.util.Utils;

/**
 * @author Peter Güttinger
 * 
 */
public class ExprFoodLevel extends PropertyExpression<Integer> {
	
	static {
		Skript.registerExpression(ExprFoodLevel.class, Integer.class, ExpressionType.PROPERTY, "[the] food[[ ](level|meter)] [of %player%]", "%player%'[s] food[[ ](level|meter)]");
	}
	
	private Expression<Player> players;
	
	@SuppressWarnings("unchecked")
	@Override
	public boolean init(final Expression<?>[] vars, final int matchedPattern, final ParseResult parser) {
		players = (Expression<Player>) vars[0];
		setExpr(players);
		return true;
	}
	
	@Override
	public String toString(final Event e, final boolean debug) {
		return "the food level of " + players.toString(e, debug);
	}
	
	@Override
	protected Integer[] get(final Event e) {
		if (getTime() >= 0 && players.isDefault()) {
			return new Integer[] {((FoodLevelChangeEvent) e).getFoodLevel()};
		}
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
	public void change(final Event e, final Object delta, final ChangeMode mode) {
		int s = 0;
		if (mode != ChangeMode.CLEAR)
			s = ((Integer) delta).intValue();
		switch (mode) {
			case SET:
			case CLEAR:
				if (getTime() >= 0 && players.isDefault() && e instanceof FoodLevelChangeEvent) {
					((FoodLevelChangeEvent) e).setFoodLevel(s);
					return;
				}
				for (final Player player : players.getArray(e)) {
					player.setFoodLevel(s);
				}
				return;
			case ADD:
				if (getTime() >= 0 && players.isDefault() && e instanceof FoodLevelChangeEvent) {
					((FoodLevelChangeEvent) e).setFoodLevel(((FoodLevelChangeEvent) e).getFoodLevel() + s);
					return;
				}
				for (final Player player : players.getArray(e)) {
					player.setFoodLevel(player.getFoodLevel() + s);
				}
				return;
			case REMOVE:
				if (getTime() >= 0 && players.isDefault() && e instanceof FoodLevelChangeEvent) {
					((FoodLevelChangeEvent) e).setFoodLevel(Math.max(((FoodLevelChangeEvent) e).getFoodLevel() - s, 0));
					return;
				}
				for (final Player player : players.getArray(e)) {
					player.setFoodLevel(Math.max(player.getFoodLevel() - s, 0));
				}
				return;
		}
	}
	
	@Override
	public Class<? extends Integer> getReturnType() {
		return Integer.class;
	}
	
	private int time = 0;
	
	@Override
	public boolean setTime(final int time) {
		if (Utils.contains(ScriptLoader.currentEvents, FoodLevelChangeEvent.class) && players.isDefault()) {
			this.time = time;
			return true;
		}
		return false;
	}
	
	@Override
	public int getTime() {
		return time;
	}
	
}
