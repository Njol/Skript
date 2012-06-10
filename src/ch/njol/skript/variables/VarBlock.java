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

package ch.njol.skript.variables;

import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockState;
import org.bukkit.event.Event;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockFadeEvent;
import org.bukkit.event.block.BlockFormEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.block.BlockSpreadEvent;

import ch.njol.skript.Skript;
import ch.njol.skript.api.Changer.ChangeMode;
import ch.njol.skript.data.DefaultChangers;
import ch.njol.skript.lang.ExprParser.ParseResult;
import ch.njol.skript.lang.SimpleVariable;
import ch.njol.skript.lang.Variable;
import ch.njol.skript.util.BlockStateBlock;
import ch.njol.skript.util.Offset;

/**
 * 
 * @author Peter Güttinger
 * 
 */
public class VarBlock extends SimpleVariable<Block> {
	
	static {
		Skript.registerVariable(VarBlock.class, Block.class, "[the] block[[s] %-offsets% [%blocks%]]");
	}
	
	private Variable<Offset> offsets;
	private Variable<Block> blocks;
	
	@SuppressWarnings("unchecked")
	@Override
	public boolean init(final Variable<?>[] vars, final int matchedPattern, final ParseResult parser) {
		offsets = (Variable<Offset>) vars[0];
		blocks = (Variable<Block>) vars[1];
		return true;
	}
	
	@Override
	public String getDebugMessage(final Event e) {
		if (offsets == null)
			return blocks.getDebugMessage(e);
		return "block" + (isSingle() ? "" : "s") + " " + offsets.getDebugMessage(e) + " " + blocks.getDebugMessage(e);
	}
	
	@Override
	protected Block[] getAll(final Event e) {
		Block b = null;
		if (blocks.isDefault()) {
			if (e instanceof BlockBreakEvent && getTime() == 1) {
				if (((BlockBreakEvent) e).getBlock().getType() == Material.ICE) {
					final BlockState s = ((BlockBreakEvent) e).getBlock().getState();
					s.setType(Material.STATIONARY_WATER);
					b = new BlockStateBlock(s);
				}
			} else if (e instanceof BlockPlaceEvent && getTime() == -1) {
				b = new BlockStateBlock(((BlockPlaceEvent) e).getBlockReplacedState());
			} else if (e instanceof BlockFadeEvent && getTime() == 1) {
				b = new BlockStateBlock(((BlockFadeEvent) e).getNewState());
			} else if (e instanceof BlockFormEvent && !(e instanceof BlockSpreadEvent) && getTime() >= 0) { // BlockSpreadEvent is a subclass of BlockFormEvent which modifies *another* block
				b = new BlockStateBlock(((BlockFormEvent) e).getNewState());
			}
		}
		final Block[] bs = b != null ? new Block[] {b} : blocks.getArray(e);
		if (offsets == null)
			return bs;
		return Offset.setOff(offsets.getArray(e), bs);
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
	public void change(final Event e, final Variable<?> delta, final ChangeMode mode) throws UnsupportedOperationException {
		DefaultChangers.blockChanger.change(e, this, delta, mode);
	}
	
	@Override
	public String toString() {
		if (offsets == null)
			return "the block";
		return "the block" + (isSingle() ? "" : "s") + " " + offsets + " " + blocks;
	}
	
	@Override
	public boolean isSingle() {
		return (offsets == null || offsets.isSingle()) && blocks.isSingle();
	}
	
	@SuppressWarnings("unchecked")
	@Override
	public boolean setTime(final int time) {
		return super.setTime(time, blocks,
				BlockPlaceEvent.class, BlockBreakEvent.class,
				BlockFormEvent.class, BlockFadeEvent.class);
	}
	
	@Override
	public boolean isDefault() {
		return offsets == null;
	}
}
