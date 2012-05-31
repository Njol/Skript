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
import ch.njol.skript.api.Changer.ChangeMode;
import ch.njol.skript.api.LoopVar;
import ch.njol.skript.api.exception.InitException;
import ch.njol.skript.api.exception.ParseException;
import ch.njol.skript.data.DefaultChangers;
import ch.njol.skript.lang.ExprParser.ParseResult;
import ch.njol.skript.lang.Variable;
import ch.njol.skript.util.BlockLineIterator;
import ch.njol.skript.util.Offset;

/**
 * @author Peter Güttinger
 * 
 */
public class LoopVarBlockLine extends LoopVar<Block> {
	
	private static final int MAXDIST = 1000;
	
	static {
		Skript.registerLoop(LoopVarBlockLine.class, Block.class,
				"blocks from %block% to %block%",
				"blocks between %block% and %block%",
				"blocks from %block% on %offset%",
				"blocks %offset% %block%");
	}
	
	private Variable<Block> start;
	private Variable<Block> end = null;
	private Variable<Offset> direction = null;
	
//	private Set<Variable<Block>> exclude = new HashSet<Variable<Block>>();
	
	@SuppressWarnings("unchecked")
	@Override
	public void init(final Variable<?>[] vars, final int matchedPattern, final ParseResult parser) throws InitException, ParseException {
		switch (matchedPattern) {
			case 0:
			case 1:
				start = (Variable<Block>) vars[0];
				end = (Variable<Block>) vars[1];
//				if (matchedPattern == 1) {
//					exclude.add(start);
//					exclude.add(end);
//				}
			break;
			case 2:
				start = (Variable<Block>) vars[0];
				direction = (Variable<Offset>) vars[1];
			break;
			case 3:
				direction = (Variable<Offset>) vars[0];
				start = (Variable<Block>) vars[1];
//				exclude.add(start);
		}
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
	
	@Override
	public Class<?> acceptChange(final ChangeMode mode) {
		return DefaultChangers.blockChanger.acceptChange(mode);
	}
	
	@Override
	public void change(final Event e, final Variable<?> delta, final ChangeMode mode) {
		DefaultChangers.blockChanger.change(e, this, delta, mode);
	}
	
}
