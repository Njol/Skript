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

import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;

import ch.njol.skript.Skript;
import ch.njol.skript.Skript.ExpressionType;
import ch.njol.skript.classes.Changer.ChangeMode;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.skript.lang.util.SimpleExpression;

/**
 * @author Peter Güttinger
 */
public class ExprEventCancelled extends SimpleExpression<Boolean> {
	
	static {
		Skript.registerExpression(ExprEventCancelled.class, Boolean.class, ExpressionType.SIMPLE, "[is] event cancelled");
	}
	
	@Override
	protected Boolean[] get(final Event e) {
		if (!(e instanceof Cancellable))
			return null;
		return new Boolean[] {((Cancellable) e).isCancelled()};
	}
	
	@Override
	public boolean init(final Expression<?>[] vars, final int matchedPattern, final int isDelayed, final ParseResult parser) {
		return true;
	}
	
	@Override
	public Class<? extends Boolean> getReturnType() {
		return Boolean.class;
	}
	
	@Override
	public String toString(final Event e, final boolean debug) {
		return "is event cancelled";
	}
	
	@Override
	public Class<?> acceptChange(final ChangeMode mode) {
//		if (delayed) {
//			// TODO error
//			return null;
//		}
		switch (mode) {
			case CLEAR:
			case SET:
				return Boolean.class;
			default:
				return null;
		}
	}
	
	@SuppressWarnings("incomplete-switch")
	@Override
	public void change(final Event e, final Object delta, final ChangeMode mode) {
		if (!(e instanceof Cancellable))
			return;
		switch (mode) {
			case CLEAR:
				((Cancellable) e).setCancelled(false);
			break;
			case SET:
				((Cancellable) e).setCancelled((Boolean) delta);
		}
	}
	
	@Override
	public boolean isSingle() {
		return true;
	}
	
	@Override
	public boolean getAnd() {
		return true;
	}
	
}
