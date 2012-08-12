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
import ch.njol.skript.Skript.ExpressionType;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.skript.lang.util.SimpleExpression;
import ch.njol.skript.util.BlockSphereIterator;
import ch.njol.util.iterator.IteratorIterable;

/**
 * @author Peter Güttinger
 * 
 */
public class ExprBlockSphere extends SimpleExpression<Block> {
	
	static {
		Skript.registerExpression(ExprBlockSphere.class, Block.class, ExpressionType.NORMAL,
				"blocks in radius %float% [around %location%]",
				"blocks around %location% in radius %float%");
	}
	
	private Expression<Float> radius;
	private Expression<Location> center;
	
	@SuppressWarnings("unchecked")
	@Override
	public boolean init(final Expression<?>[] exprs, final int matchedPattern, final boolean isDelayed, final ParseResult parser) {
		radius = (Expression<Float>) exprs[matchedPattern];
		center = (Expression<Location>) exprs[1 - matchedPattern];
		return true;
	}
	
	@Override
	public Iterator<Block> iterator(final Event e) {
		return new BlockSphereIterator(center.getSingle(e), radius.getSingle(e));
	}
	
	@Override
	protected Block[] get(final Event e) {
		final float r = radius.getSingle(e);
		final ArrayList<Block> list = new ArrayList<Block>((int) (1.1 * 4 / 3. * Math.PI * r * r * r));
		for (final Block b : new IteratorIterable<Block>(iterator(e)))
			list.add(b);
		return list.toArray(new Block[list.size()]);
	}
	
	@Override
	public Class<? extends Block> getReturnType() {
		return Block.class;
	}
	
	@Override
	public String toString(final Event e, final boolean debug) {
		return "the blocks in radius " + radius + " around " + center.toString(e, debug);
	}
	
	@Override
	public boolean canLoop() {
		return true;
	}
	
	@Override
	public boolean isLoopOf(final String s) {
		return s.equalsIgnoreCase("block");
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
