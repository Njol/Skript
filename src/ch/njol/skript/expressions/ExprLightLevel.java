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

import org.bukkit.block.Block;
import org.bukkit.event.Event;

import ch.njol.skript.Skript;
import ch.njol.skript.Skript.ExpressionType;
import ch.njol.skript.api.Converter;
import ch.njol.skript.expressions.base.PropertyExpression;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.skript.util.Offset;
import ch.njol.util.StringUtils;

/**
 * @author Peter Güttinger
 * 
 */
public class ExprLightLevel extends PropertyExpression<Byte> {
	
	static {
		Skript.registerExpression(ExprLightLevel.class, Byte.class, ExpressionType.PROPERTY, "[(sky|block)[ ]]light[]level (of|%offset%) %block%");
	}
	
	private Expression<Block> blocks;
	private Expression<Offset> offset;
	private final int SKY = 1, BLOCK = 2, ANY = 3;
	private int whatLight = ANY;
	
	@SuppressWarnings("unchecked")
	@Override
	public boolean init(final Expression<?>[] exprs, final int matchedPattern, final ParseResult parseResult) {
		offset = (Expression<Offset>) exprs[0];
		blocks = (Expression<Block>) exprs[1];
		if (StringUtils.startsWithIgnoreCase(parseResult.expr, "sky"))
			whatLight = SKY;
		else if (StringUtils.startsWithIgnoreCase(parseResult.expr, "block"))
			whatLight = BLOCK;
		return false;
	}
	
	@Override
	public Class<? extends Byte> getReturnType() {
		return Byte.class;
	}
	
	@Override
	public String toString(final Event e, final boolean debug) {
		return "light level " + offset.toString(e, debug) + " " + blocks.toString(e, debug);
	}
	
	@Override
	protected Byte[] get(final Event e) {
		final Offset o = offset.getSingle(e);
		if (o == null)
			return null;
		return blocks.getArray(e, Byte.class, new Converter<Block, Byte>() {
			@Override
			public Byte convert(final Block b) {
				return whatLight == ANY ? o.getRelative(b).getLightLevel() : whatLight == BLOCK ? o.getRelative(b).getLightFromBlocks() : o.getRelative(b).getLightFromSky();
			}
		});
	}
	
}
