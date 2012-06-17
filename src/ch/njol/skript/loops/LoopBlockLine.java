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

package ch.njol.skript.loops;

import java.util.Iterator;

import org.bukkit.block.Block;
import org.bukkit.event.Event;

import ch.njol.skript.Skript;
import ch.njol.skript.api.LoopExpr;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.skript.util.BlockLineIterator;
import ch.njol.skript.util.Offset;

/**
 * @author Peter Güttinger
 * 
 */
public class LoopBlockLine extends LoopExpr<Block> {
	
	private static final int MAXDIST = 1000;
	
	static {
		Skript.registerLoop(LoopBlockLine.class, Block.class,
				"blocks from %block% to %block%",
				"blocks between %block% and %block%",
				"blocks from %block% on %offset%",
				"blocks %offset% %block%");
	}
	
	private Expression<Block> start;
	private Expression<Block> end = null;
	private Expression<Offset> direction = null;
	
//	private Set<Expression<Block>> exclude = new HashSet<Expression<Block>>();
	
	@SuppressWarnings("unchecked")
	@Override
	public boolean init(final Expression<?>[] vars, final int matchedPattern, final ParseResult parser) {
		switch (matchedPattern) {
			case 0:
			case 1:
				start = (Expression<Block>) vars[0];
				end = (Expression<Block>) vars[1];
//				if (matchedPattern == 1) {
//					exclude.add(start);
//					exclude.add(end);
//				}
			break;
			case 2:
				start = (Expression<Block>) vars[0];
				direction = (Expression<Offset>) vars[1];
			break;
			case 3:
				direction = (Expression<Offset>) vars[0];
				start = (Expression<Block>) vars[1];
//				exclude.add(start);
		}
		return true;
	}
	
	@Override
	public String getLoopDebugMessage(final Event e) {
		if (end != null) {
			return "blocks from " + start.getDebugMessage(e) + " to " + end.getDebugMessage(e);
		} else {
			return "blocks " + direction.getDebugMessage(e) + " " + start.getDebugMessage(e);
		}
	}
	
	@Override
	protected Iterator<? extends Block> iterator(final Event e) {
		final Block b = start.getSingle(e);
		if (b == null)
			return null;
		if (direction != null) {
			final Offset o = direction.getSingle(e);
			if (o == null)
				return null;
			return new BlockLineIterator(b, o.toVector(), MAXDIST);
		} else {
			final Block b2 = end.getSingle(e);
			if (b2 == null || b2.getWorld() != b.getWorld())
				return null;
			return new BlockLineIterator(b, b2);
		}
	}
	
	@Override
	public boolean isLoopOf(final String s) {
		return s.equalsIgnoreCase("block");
	}
	
	@Override
	public String toString() {
		return "the loop-block";
	}
	
	@Override
	public Class<? extends Block> getReturnType() {
		return Block.class;
	}
	
}
