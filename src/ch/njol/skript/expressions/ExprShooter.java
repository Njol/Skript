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

import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Projectile;
import org.bukkit.event.Event;

import ch.njol.skript.Skript;
import ch.njol.skript.Skript.ExpressionType;
import ch.njol.skript.api.Converter;
import ch.njol.skript.expressions.base.PropertyExpression;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.SkriptParser.ParseResult;

/**
 * @author Peter Güttinger
 * 
 */
public class ExprShooter extends PropertyExpression<LivingEntity> {
	
	static {
		Skript.registerExpression(ExprShooter.class, LivingEntity.class, ExpressionType.SIMPLE, "[the] shooter [of %projectile%]");
	}
	
	@Override
	public boolean init(final Expression<?>[] exprs, final int matchedPattern, final ParseResult parseResult) {
		setExpr(exprs[0]);
		return true;
	}
	
	@Override
	protected LivingEntity[] get(final Event e) {
		return getExpr().getArray(e, LivingEntity.class, new Converter<Object, LivingEntity>() {
			@Override
			public LivingEntity convert(final Object o) {
				return ((Projectile) o).getShooter();
			}
		});
	}
	
	@Override
	public Class<? extends LivingEntity> getReturnType() {
		return LivingEntity.class;
	}
	
	@Override
	public String toString(final Event e, final boolean debug) {
		return "the shooter" + (getExpr().isDefault() ? "" : " of " + getExpr().toString(e, debug));
	}
	
}
