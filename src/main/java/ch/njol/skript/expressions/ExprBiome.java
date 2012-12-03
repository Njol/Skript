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

import org.bukkit.Location;
import org.bukkit.block.Biome;
import org.bukkit.event.Event;

import ch.njol.skript.Skript;
import ch.njol.skript.classes.Converter;
import ch.njol.skript.expressions.base.PropertyExpression;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.ExpressionType;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.skript.util.Direction;
import ch.njol.util.Kleenean;

/**
 * @author Peter Güttinger
 */
public class ExprBiome extends PropertyExpression<Location, Biome> {
	private static final long serialVersionUID = 664790396177783245L;
	
	static {
		Skript.registerExpression(ExprBiome.class, Biome.class, ExpressionType.PROPERTY, "[the] biome (of|%direction%) %location%", "%location%'[s] biome");
	}
	
	@SuppressWarnings("unchecked")
	@Override
	public boolean init(final Expression<?>[] exprs, final int matchedPattern, final Kleenean isDelayed, final ParseResult parseResult) {
		setExpr(matchedPattern == 1 ? (Expression<? extends Location>) exprs[0] : Direction.combine((Expression<? extends Direction>) exprs[0], (Expression<? extends Location>) exprs[1]));
		return true;
	}
	
	@Override
	protected Biome[] get(final Event e, final Location[] source) {
		return get(source, new Converter<Location, Biome>() {
			@Override
			public Biome convert(final Location l) {
				return l.getWorld().getBiome(l.getBlockX(), l.getBlockZ());
			}
		});
	}
	
	@Override
	public Class<? extends Biome> getReturnType() {
		return Biome.class;
	}
	
	@Override
	public String toString(final Event e, final boolean debug) {
		return "the biome at " + getExpr().toString(e, debug);
	}
	
}
