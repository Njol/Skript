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

import ch.njol.skript.Skript;
import ch.njol.skript.Skript.ExpressionType;
import ch.njol.skript.api.Changer.ChangeMode;
import ch.njol.skript.api.Getter;
import ch.njol.skript.expressions.base.PropertyExpression;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.util.Math2;

/**
 * FIXME range 0-10 (with step 0.5), not 0-20
 * 
 * @author Peter Güttinger
 * 
 */
public class ExprFoodLevel extends PropertyExpression<Float> {
	
	static {
		Skript.registerExpression(ExprFoodLevel.class, Float.class, ExpressionType.PROPERTY, "[the] food[[ ](level|meter)] [of %player%]", "%player%'[s] food[[ ](level|meter)]");
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
	protected Float[] get(final Event e) {
		if (getTime() >= 0 && players.isDefault() && e instanceof FoodLevelChangeEvent) {
			return new Float[] {0.5f * ((FoodLevelChangeEvent) e).getFoodLevel()};
		}
		return players.getArray(e, Float.class, new Getter<Float, Player>() {
			@Override
			public Float get(final Player p) {
				return 0.5f * p.getFoodLevel();
			}
		});
	}
	
	@Override
	public Class<?> acceptChange(final ChangeMode mode) {
		return Float.class;
	}
	
	@Override
	public void change(final Event e, final Object delta, final ChangeMode mode) {
		int s = 0;
		if (mode != ChangeMode.CLEAR)
			s = Math.round(((Float) delta).floatValue() * 2);
		switch (mode) {
			case SET:
			case CLEAR:
				if (getTime() >= 0 && players.isDefault() && e instanceof FoodLevelChangeEvent) {
					((FoodLevelChangeEvent) e).setFoodLevel(Math2.fit(0, s, 20));
					return;
				}
				for (final Player player : players.getArray(e)) {
					player.setFoodLevel(Math2.fit(0, s, 20));
				}
				return;
			case ADD:
				if (getTime() >= 0 && players.isDefault() && e instanceof FoodLevelChangeEvent) {
					((FoodLevelChangeEvent) e).setFoodLevel(Math2.fit(0, ((FoodLevelChangeEvent) e).getFoodLevel() + s, 20));
					return;
				}
				for (final Player player : players.getArray(e)) {
					player.setFoodLevel(Math2.fit(0, player.getFoodLevel() + s, 20));
				}
				return;
			case REMOVE:
				if (getTime() >= 0 && players.isDefault() && e instanceof FoodLevelChangeEvent) {
					((FoodLevelChangeEvent) e).setFoodLevel(Math2.fit(0, ((FoodLevelChangeEvent) e).getFoodLevel() - s, 20));
					return;
				}
				for (final Player player : players.getArray(e)) {
					player.setFoodLevel(Math2.fit(0, player.getFoodLevel() - s, 20));
				}
				return;
		}
	}
	
	@Override
	public Class<? extends Float> getReturnType() {
		return Float.class;
	}
	
	@Override
	public boolean setTime(final int time) {
		return super.setTime(time, FoodLevelChangeEvent.class, players);
	}
}
