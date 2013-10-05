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

package ch.njol.skript.hooks.regions.expressions;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.NoSuchElementException;

import org.bukkit.block.Block;
import org.bukkit.event.Event;

import ch.njol.skript.Skript;
import ch.njol.skript.hooks.regions.classes.Region;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.ExpressionType;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.skript.lang.util.SimpleExpression;
import ch.njol.util.Kleenean;
import ch.njol.util.coll.iterator.ArrayIterator;
import ch.njol.util.coll.iterator.EmptyIterator;

/**
 * @author Peter Güttinger
 */
@SuppressWarnings("serial")
public class ExprBlocksInRegion extends SimpleExpression<Block> {
	static {
		Skript.registerExpression(ExprBlocksInRegion.class, Block.class, ExpressionType.COMBINED,
				"[(all|the)] blocks (in|of) [[the] region[s]] %regions%");
	}
	
	private Expression<Region> regions;
	
	@SuppressWarnings("unchecked")
	@Override
	public boolean init(final Expression<?>[] exprs, final int matchedPattern, final Kleenean isDelayed, final ParseResult parseResult) {
		regions = (Expression<Region>) exprs[0];
		return true;
	}
	
	@Override
	protected Block[] get(final Event e) {
		final Iterator<Block> iter = iterator(e);
		final ArrayList<Block> r = new ArrayList<Block>();
		while (iter.hasNext())
			r.add(iter.next());
		return r.toArray(new Block[r.size()]);
	}
	
	@Override
	public Iterator<Block> iterator(final Event e) {
		final Region[] rs = regions.getArray(e);
		if (rs.length == 0)
			return EmptyIterator.get();
		return new Iterator<Block>() {
			private final Iterator<Region> iter = new ArrayIterator<Region>(rs);
			private Iterator<Block> current = iter.next().getBlocks();
			
			@Override
			public boolean hasNext() {
				while (!current.hasNext() && iter.hasNext()) {
					current = iter.next().getBlocks();
				}
				return current.hasNext();
			}
			
			@Override
			public Block next() {
				if (!hasNext())
					throw new NoSuchElementException();
				return current.next();
			}
			
			@Override
			public void remove() {
				throw new UnsupportedOperationException();
			}
		};
	}
	
	@Override
	public boolean isSingle() {
		return false;
	}
	
	@Override
	public Class<? extends Block> getReturnType() {
		return Block.class;
	}
	
	@Override
	public String toString(final Event e, final boolean debug) {
		return "all blocks in " + regions.toString(e, debug);
	}
	
}
