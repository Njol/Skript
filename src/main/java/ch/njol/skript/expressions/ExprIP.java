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
import ch.njol.skript.classes.Converter;
import ch.njol.skript.expressions.base.PropertyExpression;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.ExpressionType;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.util.Kleenean;

/**
 * @author Peter Güttinger
 */
public class ExprIP extends PropertyExpression<Player, String> {
	private static final long serialVersionUID = 2670595902919005648L;
	
	static {
		Skript.registerExpression(ExprIP.class, String.class, ExpressionType.PROPERTY, "IP[s][( |-)address[es]] of %players%", "%players%'[s] IP[s][( |-)address[es]]");
	}
	
	@SuppressWarnings("unchecked")
	@Override
	public boolean init(final Expression<?>[] exprs, final int matchedPattern, final Kleenean isDelayed, final ParseResult parseResult) {
		setExpr((Expression<? extends Player>) exprs[0]);
		return true;
	}
	
	@Override
	protected String[] get(final Event e, final Player[] source) {
		return get(source, new Converter<Player, String>() {
			@Override
			public String convert(final Player p) {
				return p.getAddress().getAddress().getHostAddress();
			}
		});
	}
	
	@Override
	public Class<? extends String> getReturnType() {
		return String.class;
	}
	
	@Override
	public String toString(final Event e, final boolean debug) {
		return "IP address" + (getExpr().isSingle() ? "" : "es") + " of " + getExpr().toString(e, debug);
	}
	
}
