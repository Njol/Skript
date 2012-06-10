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

import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.event.Event;

import ch.njol.skript.Skript;
import ch.njol.skript.api.Changer.ChangeMode;
import ch.njol.skript.api.LoopVar;
import ch.njol.skript.data.DefaultChangers;
import ch.njol.skript.lang.ExprParser.ParseResult;
import ch.njol.skript.lang.Variable;
import ch.njol.skript.util.BlockSphereIterator;

/**
 * @author Peter Güttinger
 * 
 */
public class LoopVarBlockSphere extends LoopVar<Block> {
	
	static {
		Skript.registerLoop(LoopVarBlockSphere.class, Block.class, "blocks in radius %float% [around %location%]");
	}
	
	private Variable<Float> radius;
	private Variable<Location> center;
	
	@SuppressWarnings("unchecked")
	@Override
	public boolean init(final Variable<?>[] vars, final int matchedPattern, final ParseResult parser) {
		radius = (Variable<Float>) vars[0];
		center = (Variable<Location>) vars[1];
		return true;
	}
	
	@Override
	protected Iterator<Block> iterator(final Event e) {
		return new BlockSphereIterator(center.getSingle(e), radius.getSingle(e));
	}
	
	@Override
	public Class<?> acceptChange(final ChangeMode mode) {
		return DefaultChangers.blockChanger.acceptChange(mode);
	}
	
	@Override
	public void change(final Event e, final Variable<?> delta, final ChangeMode mode) {
		DefaultChangers.blockChanger.change(e, this, delta, mode);
	}
	
	@Override
	public Class<? extends Block> getReturnType() {
		return Block.class;
	}
	
	@Override
	public String getLoopDebugMessage(final Event e) {
		return "blocks in radius " + radius.getDebugMessage(e) + " around " + center.getDebugMessage(e);
	}
	
	@Override
	public String toString() {
		return "the loop-block";
	}
	
	@Override
	public boolean isLoopOf(final String s) {
		return s.equalsIgnoreCase("block");
	}
	
}
