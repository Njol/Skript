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

package ch.njol.skript.expressions;

import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.entity.FoodLevelChangeEvent;

import ch.njol.skript.Skript;
import ch.njol.skript.classes.Changer.ChangeMode;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Examples;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import ch.njol.skript.effects.Delay;
import ch.njol.skript.expressions.base.PropertyExpression;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.ExpressionType;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.skript.util.Getter;
import ch.njol.skript.util.Utils;
import ch.njol.util.Kleenean;
import ch.njol.util.Math2;

/**
 * @author Peter Güttinger
 */
@SuppressWarnings("serial")
@Name("Food Level")
@Description("The food level of a player from 0 to 10. Has several aliases: food/hunger level/meter/bar. ")
@Examples({"set the player's food level to 10"})
@Since("1.0")
public class ExprFoodLevel extends PropertyExpression<Player, Float> {
	
	static {
		Skript.registerExpression(ExprFoodLevel.class, Float.class, ExpressionType.PROPERTY, "[the] (food|hunger)[[ ](level|meter|bar)] [of %player%]", "%player%'[s] (food|hunger)[[ ](level|meter|bar)]");
	}
	
	@SuppressWarnings("unchecked")
	@Override
	public boolean init(final Expression<?>[] vars, final int matchedPattern, final Kleenean isDelayed, final ParseResult parser) {
		setExpr((Expression<Player>) vars[0]);
		return true;
	}
	
	@Override
	public String toString(final Event e, final boolean debug) {
		return "the food level of " + getExpr().toString(e, debug);
	}
	
	@Override
	protected Float[] get(final Event e, final Player[] source) {
		if (getTime() >= 0 && getExpr().isDefault() && e instanceof FoodLevelChangeEvent && !Delay.isDelayed(e)) {
			return new Float[] {0.5f * ((FoodLevelChangeEvent) e).getFoodLevel()};
		}
		return get(source, new Getter<Float, Player>() {
			@Override
			public Float get(final Player p) {
				return 0.5f * p.getFoodLevel();
			}
		});
	}
	
	@SuppressWarnings("unchecked")
	@Override
	public Class<?>[] acceptChange(final ChangeMode mode) {
		return Utils.array(Number.class);
	}
	
	@Override
	public void change(final Event e, final Object delta, final ChangeMode mode) {
		int s = 0;
		if (mode != ChangeMode.DELETE)
			s = Math.round(((Number) delta).floatValue() * 2);
		switch (mode) {
			case SET:
			case DELETE:
				if (getTime() >= 0 && getExpr().isDefault() && e instanceof FoodLevelChangeEvent && !Delay.isDelayed(e)) {
					((FoodLevelChangeEvent) e).setFoodLevel(Math2.fit(0, s, 20));
					return;
				}
				for (final Player player : getExpr().getArray(e)) {
					player.setFoodLevel(Math2.fit(0, s, 20));
				}
				return;
			case ADD:
				if (getTime() >= 0 && getExpr().isDefault() && e instanceof FoodLevelChangeEvent && !Delay.isDelayed(e)) {
					((FoodLevelChangeEvent) e).setFoodLevel(Math2.fit(0, ((FoodLevelChangeEvent) e).getFoodLevel() + s, 20));
					return;
				}
				for (final Player player : getExpr().getArray(e)) {
					player.setFoodLevel(Math2.fit(0, player.getFoodLevel() + s, 20));
				}
				return;
			case REMOVE:
				if (getTime() >= 0 && getExpr().isDefault() && e instanceof FoodLevelChangeEvent && !Delay.isDelayed(e)) {
					((FoodLevelChangeEvent) e).setFoodLevel(Math2.fit(0, ((FoodLevelChangeEvent) e).getFoodLevel() - s, 20));
					return;
				}
				for (final Player player : getExpr().getArray(e)) {
					player.setFoodLevel(Math2.fit(0, player.getFoodLevel() - s, 20));
				}
				return;
		}
	}
	
	@Override
	public Class<Float> getReturnType() {
		return Float.class;
	}
	
	@Override
	public boolean setTime(final int time) {
		return super.setTime(time, FoodLevelChangeEvent.class, getExpr());
	}
}
