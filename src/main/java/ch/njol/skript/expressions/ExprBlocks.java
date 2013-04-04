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
 * Copyright 2011-2013 Peter Güttinger
 * 
 */

package ch.njol.skript.expressions;

import java.util.ArrayList;
import java.util.Iterator;

import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.event.Event;

import ch.njol.skript.Skript;
import ch.njol.skript.SkriptConfig;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Examples;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.ExpressionType;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.skript.lang.util.SimpleExpression;
import ch.njol.skript.util.BlockLineIterator;
import ch.njol.skript.util.Direction;
import ch.njol.util.Kleenean;
import ch.njol.util.iterator.ArrayIterator;
import ch.njol.util.iterator.IteratorIterable;

/**
 * @author Peter Güttinger
 */
@SuppressWarnings("serial")
@Name("Blocks")
@Description("Blocks relative to other blocks or between other blocks. Can be used to get blocks relative to other blocks or for looping.")
@Examples({"loop blocks above the player:",
		"loop blocks between the block below the player and the targeted block:",
		"set the blocks below the player, the victim and the targeted block to air"})
@Since("1.0")
public class ExprBlocks extends SimpleExpression<Block> {
	
	static {
		Skript.registerExpression(ExprBlocks.class, Block.class, ExpressionType.NORMAL,
				"[the] blocks %direction% [%locations%]",
				"[the] blocks from %location% [on] %direction%",
				"[the] blocks from %block% to %block%",
				"[the] blocks between %block% and %block%");
	}
	
	private Expression<?> from;
	private Expression<Block> end;
	private Expression<Direction> direction;
	
	@SuppressWarnings("unchecked")
	@Override
	public boolean init(final Expression<?>[] exprs, final int matchedPattern, final Kleenean isDelayed, final ParseResult parser) {
		switch (matchedPattern) {
			case 0:
				direction = (Expression<Direction>) exprs[0];
				from = exprs[1];
				break;
			case 1:
				from = exprs[0];
				direction = (Expression<Direction>) exprs[1];
				break;
			case 2:
			case 3:
				from = exprs[0];
				end = (Expression<Block>) exprs[1];
		}
		return true;
	}
	
	@Override
	public String toString(final Event e, final boolean debug) {
		if (end == null)
			return "block" + (isSingle() ? "" : "s") + " " + direction.toString(e, debug) + " " + from.toString(e, debug);
		else
			return "blocks from " + from.toString(e, debug) + " to " + end.toString(e, debug);
	}
	
	@Override
	protected Block[] get(final Event e) {
		if (direction != null && !from.isSingle()) {
			final Location[] ls = (Location[]) from.getArray(e);
			final Direction d = direction.getSingle(e);
			if (ls.length == 0 || d == null)
				return null;
			final Block[] bs = new Block[ls.length];
			for (int i = 0; i < ls.length; i++) {
				bs[i] = d.getRelative(ls[i]).getBlock();
			}
			return bs;
		}
		final ArrayList<Block> r = new ArrayList<Block>();
		for (final Block b : new IteratorIterable<Block>(iterator(e)))
			r.add(b);
		return r.toArray(new Block[r.size()]);
	}
	
	@Override
	public Iterator<Block> iterator(final Event e) {
		try {
			if (direction != null) {
				if (!from.isSingle()) {
					return new ArrayIterator<Block>(get(e));
				}
				final Object o = from.getSingle(e);
				if (o == null)
					return null;
				final Location l = o instanceof Location ? (Location) o : ((Block) o).getLocation().add(0.5, 0.5, 0.5);
				final Direction d = direction.getSingle(e);
				if (d == null)
					return null;
				return new BlockLineIterator(l, o != l ? d.getDirection((Block) o) : d.getDirection(l), SkriptConfig.maxTargetBlockDistance.value());
			} else {
				final Block b = (Block) from.getSingle(e);
				if (b == null)
					return null;
				final Block b2 = end.getSingle(e);
				if (b2 == null || b2.getWorld() != b.getWorld())
					return null;
				return new BlockLineIterator(b, b2);
			}
		} catch (IllegalStateException ex) {
			if (ex.getMessage().equals("Start block missed in BlockIterator"))
				return null;
			throw ex;
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
