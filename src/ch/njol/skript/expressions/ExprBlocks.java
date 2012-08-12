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

import java.util.ArrayList;
import java.util.Iterator;

import org.bukkit.block.Block;
import org.bukkit.event.Event;

import ch.njol.skript.Skript;
import ch.njol.skript.Skript.ExpressionType;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.skript.lang.util.SimpleExpression;
import ch.njol.skript.util.BlockLineIterator;
import ch.njol.skript.util.Offset;
import ch.njol.util.StringUtils;
import ch.njol.util.iterator.IteratorIterable;

/**
 * 
 * @author Peter Güttinger
 * 
 */
public class ExprBlocks extends SimpleExpression<Block> {
	
	static {
		Skript.registerExpression(ExprBlocks.class, Block.class, ExpressionType.NORMAL,
				"[the] block[s] %offsets% [%blocks%]",
				"[the] blocks from %block% to %block%",
				"[the] blocks between %block% and %block%",
				"[the] blocks from %block% on %offset%");
	}
	
	private boolean isLoop;
	private Expression<Block> blocks;
	private Expression<Block> end;
	private Expression<Offset> offsets;
	private int matchedPattern;
	
	@SuppressWarnings("unchecked")
	@Override
	public boolean init(final Expression<?>[] vars, final int matchedPattern, final boolean isDelayed, final ParseResult parser) {
		this.matchedPattern = matchedPattern;
		switch (matchedPattern) {
			case 0:
				offsets = (Expression<Offset>) vars[0];
				blocks = (Expression<Block>) vars[1];
				isLoop = StringUtils.startsWithIgnoreCase(parser.expr, "blocks") || StringUtils.startsWithIgnoreCase(parser.expr, "the blocks");
			break;
			case 1:
			case 2:
				blocks = (Expression<Block>) vars[0];
				end = (Expression<Block>) vars[1];
				isLoop = true;
			break;
			case 3:
				blocks = (Expression<Block>) vars[0];
				offsets = (Expression<Offset>) vars[1];
				isLoop = true;
		}
		return true;
	}
	
	@Override
	public String toString(final Event e, final boolean debug) {
		if (end == null)
			return "block" + (isSingle() ? "" : "s") + " " + offsets.toString(e, debug) + " " + blocks.toString(e, debug);
		else
			return "blocks from " + blocks.toString(e, debug) + " to " + end.toString(e, debug);
	}
	
	@Override
	protected Block[] get(final Event e) {
		if (matchedPattern == 0) {
			final Block[] bs = blocks.getArray(e);
			if (offsets == null)
				return bs;
			return Offset.setOff(offsets.getArray(e), bs);
		}
		final ArrayList<Block> r = new ArrayList<Block>();
		for (final Block b : new IteratorIterable<Block>(iterator(e)))
			r.add(b);
		return r.toArray(new Block[r.size()]);
	}
	
	@Override
	public Iterator<Block> iterator(final Event e) {
		if (matchedPattern == 0 && !isLoop) {
			return super.iterator(e);
		}
		final Block b = blocks.getSingle(e);
		if (b == null)
			return null;
		if (offsets != null) {
			final Offset o = offsets.getSingle(e);
			if (o == null)
				return null;
			return new BlockLineIterator(b, o.toVector(), Skript.TARGETBLOCKMAXDISTANCE);
		} else {
			final Block b2 = end.getSingle(e);
			if (b2 == null || b2.getWorld() != b.getWorld())
				return null;
			return new BlockLineIterator(b, b2);
		}
	}
	
	@Override
	public boolean canLoop() {
		return isLoop;
	}
	
	@Override
	public Class<? extends Block> getReturnType() {
		return Block.class;
	}
	
	@Override
	public boolean isSingle() {
		return matchedPattern == 0 && blocks.isSingle() && offsets.isSingle();
	}
	
	@Override
	public boolean getAnd() {
		return matchedPattern != 0 || blocks.getAnd() && offsets.getAnd();
	}
	
}
