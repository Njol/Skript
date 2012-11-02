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

import org.bukkit.entity.Creature;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.event.Event;

import ch.njol.skript.Skript;
import ch.njol.skript.Skript.ExpressionType;
import ch.njol.skript.classes.Changer.ChangeMode;
import ch.njol.skript.classes.Converter;
import ch.njol.skript.entity.EntityData;
import ch.njol.skript.expressions.base.PropertyExpression;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.skript.registrations.Classes;
import ch.njol.skript.util.Utils;

/**
 * @author Peter Güttinger
 */
public class ExprTarget extends PropertyExpression<LivingEntity, Entity> {
	private static final long serialVersionUID = -2553996050059853101L;
	
	static {
		Skript.registerExpression(ExprTarget.class, Entity.class, ExpressionType.NORMAL, "[the] target[[ed] <.+>] [of %livingentities%]", "%livingentities%'[s] target[[ed] <.+>]");
	}
	
	private EntityData<?> type;
	
	@SuppressWarnings("unchecked")
	@Override
	public boolean init(final Expression<?>[] vars, final int matchedPattern, final int isDelayed, final ParseResult parser) {
		type = parser.regexes.isEmpty() ? EntityData.fromClass(Entity.class) : EntityData.parseWithoutAnOrAny(parser.regexes.get(0).group());
		if (type == null)
			return false;
		setExpr((Expression<? extends LivingEntity>) vars[0]);
		return true;
	}
	
	@Override
	protected Entity[] get(final Event evt, final LivingEntity[] source) {
		return get(source, new Converter<LivingEntity, Entity>() {
			@Override
			public Entity convert(final LivingEntity e) {
				return Utils.getTarget(e, type);
			}
		});
	}
	
	@Override
	public Class<? extends Entity> getReturnType() {
		return type.getType();
	}
	
	@Override
	public String toString(final Event e, final boolean debug) {
		if (e == null)
			return "the targeted " + type + " of " + getExpr().toString(e, debug);
		return Classes.getDebugMessage(getAll(e));
	}
	
	@SuppressWarnings("unchecked")
	@Override
	public Class<?>[] acceptChange(final ChangeMode mode) {
		if (mode == ChangeMode.CLEAR || mode == ChangeMode.SET)
			return Skript.array(LivingEntity.class);
		return null;
	}
	
	@Override
	public void change(final Event e, final Object delta, final ChangeMode mode) {
		final LivingEntity target = (LivingEntity) delta;
		for (final LivingEntity entity : getExpr().getArray(e)) {
			if (!(entity instanceof Creature))
				continue;
			((Creature) entity).setTarget(target);
		}
	}
	
}
