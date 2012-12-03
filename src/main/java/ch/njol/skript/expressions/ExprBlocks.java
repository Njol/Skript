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

import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.event.Event;

import ch.njol.skript.Skript;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.ExpressionType;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.skript.lang.util.SimpleExpression;
import ch.njol.skript.util.BlockLineIterator;
import ch.njol.skript.util.Direction;
import ch.njol.util.Kleenean;
import ch.njol.util.iterator.IteratorIterable;

/**
 * 
 * @author Peter Güttinger
 */
public class ExprBlocks extends SimpleExpression<Block> {
	private static final long serialVersionUID = -5840214530628307924L;
	
	static {
		Skript.registerExpression(ExprBlocks.class, Block.class, ExpressionType.NORMAL,
				"[the] blocks %directions% [%locations%]",
				"[the] blocks from %location% [on] %directions%",
				"[the] blocks from %block% to %block%",
				"[the] blocks between %block% and %block%");
	}
	
	private Expression<?> from;
	private Expression<Block> end;
	private Expression<Direction> directions;
	
	@SuppressWarnings("unchecked")
	@Override
	public boolean init(final Expression<?>[] exprs, final int matchedPattern, final Kleenean isDelayed, final ParseResult parser) {
		switch (matchedPattern) {
			case 0:
				directions = (Expression<Direction>) exprs[0];
				from = exprs[1];
				break;
			case 1:
				from = exprs[1];
				directions = (Expression<Direction>) exprs[1];
				break;
			case 2:
			case 3:
				from = exprs[1];
				end = (Expression<Block>) exprs[1];
		}
		return true;
	}
	
	@Override
	public String toString(final Event e, final boolean debug) {
		if (end == null)
			return "block" + (isSingle() ? "" : "s") + " " + directions.toString(e, debug) + " " + from.toString(e, debug);
		else
			return "blocks from " + from.toString(e, debug) + " to " + end.toString(e, debug);
	}
	
	@Override
	protected Block[] get(final Event e) {
		final ArrayList<Block> r = new ArrayList<Block>();
		for (final Block b : new IteratorIterable<Block>(iterator(e)))
			r.add(b);
		return r.toArray(new Block[r.size()]);
	}
	
	@Override
	public Iterator<Block> iterator(final Event e) {
		if (directions != null) {
			Object o = from.getSingle(e);
			if (o == null)
				return null;
			final Location l = o instanceof Location ? (Location) o : ((Block) o).getLocation().add(0.5,0.5,0.5);
			final Direction d = directions.getSingle(e);
			if (d == null)
				return null;
			return new BlockLineIterator(l, o != l ? d.getDirection((Block) o) : d.getDirection(l), Skript.TARGETBLOCKMAXDISTANCE);
		} else {
			final Block b = (Block) from.getSingle(e);
			if (b == null)
				return null;
			final Block b2 = end.getSingle(e);
			if (b2 == null || b2.getWorld() != b.getWorld())
				return null;
			return new BlockLineIterator(b, b2);
		}
	}
	
	@Override
	public Class<? extends Block> getReturnType() {
		return Block.class;
	}
	
	@Override
	public boolean isSingle() {
		return false;
	}
	
	@Override
	public boolean getAnd() {
		return true;
	}
	
}
