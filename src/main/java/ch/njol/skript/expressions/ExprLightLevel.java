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
import ch.njol.skript.classes.Converter;
import ch.njol.skript.expressions.base.PropertyExpression;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.util.StringUtils;

/**
 * @author Peter Güttinger
 */
public class ExprLightLevel extends PropertyExpression<Block, Byte> {
	private static final long serialVersionUID = -5974786826590395433L;
	
	static {
		Skript.registerExpression(ExprLightLevel.class, Byte.class, ExpressionType.PROPERTY, "[(sky|sun|block)[ ]]light[ ]level [of] %block%");
	}
	
	private Expression<Block> blocks;
	private final int SKY = 1, BLOCK = 2, ANY = 3;
	private int whatLight = ANY;
	
	@SuppressWarnings("unchecked")
	@Override
	public boolean init(final Expression<?>[] exprs, final int matchedPattern, final int isDelayed, final ParseResult parseResult) {
		blocks = (Expression<Block>) exprs[0];
		if (StringUtils.startsWithIgnoreCase(parseResult.expr, "sky") || StringUtils.startsWithIgnoreCase(parseResult.expr, "sun"))
			whatLight = SKY;
		else if (StringUtils.startsWithIgnoreCase(parseResult.expr, "block"))
			whatLight = BLOCK;
		return true;
	}
	
	@Override
	public Class<Byte> getReturnType() {
		return Byte.class;
	}
	
	@Override
	public String toString(final Event e, final boolean debug) {
		return (whatLight == BLOCK ? "block " : whatLight == SKY ? "sky " : "") + "light level " + blocks.toString(e, debug);
	}
	
	@Override
	protected Byte[] get(final Event e, final Block[] source) {
		return get(source, new Converter<Block, Byte>() {
			@Override
			public Byte convert(final Block b) {
				return whatLight == ANY ? b.getLightLevel() : whatLight == BLOCK ? b.getLightFromBlocks() : b.getLightFromSky();
			}
		});
	}
	
}
