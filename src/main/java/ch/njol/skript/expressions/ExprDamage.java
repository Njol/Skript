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

import org.bukkit.event.Event;
import org.bukkit.event.entity.EntityDamageByBlockEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;

import ch.njol.skript.ScriptLoader;
import ch.njol.skript.Skript;
import ch.njol.skript.Skript.ExpressionType;
import ch.njol.skript.classes.Changer.ChangeMode;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.skript.lang.util.SimpleExpression;
import ch.njol.skript.log.ErrorQuality;
import ch.njol.skript.util.Utils;

/**
 * @author Peter Güttinger
 */
public class ExprDamage extends SimpleExpression<Float> {
	private static final long serialVersionUID = 7256625021203923955L;
	
	static {
		Skript.registerExpression(ExprDamage.class, Float.class, ExpressionType.SIMPLE, "[the] damage");
	}
	
	private int delay;
	
	@SuppressWarnings("unchecked")
	@Override
	public boolean init(final Expression<?>[] exprs, final int matchedPattern, final int isDelayed, final ParseResult parseResult) {
		if (!Utils.containsAny(ScriptLoader.currentEvents, EntityDamageEvent.class, EntityDamageByBlockEvent.class, EntityDamageByEntityEvent.class)) {
			Skript.error("'damage' can only be used in damage events", ErrorQuality.SEMANTIC_ERROR);
			return false;
		}
		delay = isDelayed;
		return true;
	}
	
	@Override
	protected Float[] get(final Event e) {
		if (!(e instanceof EntityDamageEvent))
			return null;
		return new Float[] {0.5f * ((EntityDamageEvent) e).getDamage()};
	}
	
	@SuppressWarnings("unchecked")
	@Override
	public Class<?>[] acceptChange(final ChangeMode mode) {
		if (delay != -1) {
			Skript.error("Can't change the damage anymore after the event has already passed");
			return null;
		}
		return Skript.array(Number.class);
	}
	
	@Override
	public void change(final Event e, final Object delta, final ChangeMode mode) throws UnsupportedOperationException {
		if (!(e instanceof EntityDamageEvent))
			return;
		final int d = mode == ChangeMode.CLEAR ? 0 : Math.round(2 * ((Number) delta).floatValue());
		switch (mode) {
			case SET:
			case CLEAR:
				((EntityDamageEvent) e).setDamage(d);
				break;
			case ADD:
				((EntityDamageEvent) e).setDamage(((EntityDamageEvent) e).getDamage() + d);
				break;
			case REMOVE:
				((EntityDamageEvent) e).setDamage(((EntityDamageEvent) e).getDamage() - d);
				break;
		}
	}
	
	@Override
	public boolean isSingle() {
		return true;
	}
	
	@Override
	public Class<? extends Float> getReturnType() {
		return Float.class;
	}
	
	@Override
	public boolean getAnd() {
		return false;
	}
	
	@Override
	public String toString(final Event e, final boolean debug) {
		return "the damage";
	}
	
}
