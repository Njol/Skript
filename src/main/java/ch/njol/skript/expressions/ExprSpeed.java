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

import ch.njol.skript.Skript;
import ch.njol.skript.classes.Changer.ChangeMode;
import ch.njol.skript.expressions.base.SimplePropertyExpression;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.util.Math2;
import ch.njol.util.Kleenean;

/**
 * @author Peter Güttinger
 * 
 */
public class ExprSpeed extends SimplePropertyExpression<Player, Float> {
	private static final long serialVersionUID = -1840963360507113110L;
	
	static {
		register(ExprSpeed.class, Float.class, "(0¦walk[ing]|1¦fl(y[ing]|ight))[( |-])speed", "players");
	}
	
	private boolean walk;
	
	@Override
	public boolean init(final Expression<?>[] exprs, final int matchedPattern, final Kleenean isDelayed, final ParseResult parseResult) {
		if (!Skript.isRunningBukkit(1, 4)) {
			Skript.error("fly and walk speed can only be used in Minecraft 1.4 and newer");
			return false;
		}
		super.init(exprs, matchedPattern, isDelayed, parseResult);
		walk = parseResult.mark == 0;
		return true;
	}
	
	@Override
	public Float convert(final Player p) {
		return walk ? p.getWalkSpeed() : p.getFlySpeed();
	}
	
	@Override
	public Class<?>[] acceptChange(final ChangeMode mode) {
		if (mode == ChangeMode.SET)
			return new Class[] {Number.class};
		return null;
	}
	
	@Override
	public void change(final Event e, final Object delta, final ChangeMode mode) throws UnsupportedOperationException {
		final float d = Math2.fit(-1, (Float) delta, 1);
		for (final Player p : getExpr().getArray(e)) {
			if (walk)
				p.setWalkSpeed(d);
			else
				p.setFlySpeed(d);
		}
	}
	
	@Override
	public Class<Float> getReturnType() {
		return Float.class;
	}
	
	@Override
	protected String getPropertyName() {
		return walk ? "walk speed" : "fly speed";
	}
	
}
