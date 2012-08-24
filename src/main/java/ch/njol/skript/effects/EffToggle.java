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

package ch.njol.skript.effects;

import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.event.Event;

import ch.njol.skript.Skript;
import ch.njol.skript.lang.Effect;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.SkriptParser.ParseResult;

/**
 * @author Peter Güttinger
 */
public class EffToggle extends Effect {
	
	static {
		Skript.registerEffect(EffToggle.class, "(close|turn off|de[-]activate) %blocks%", "(toggle|switch) [[the] state of] %blocks%", "(open|turn on|activate) %blocks%");
	}
	
	private Expression<Block> blocks;
	private int toggle;
	
	@SuppressWarnings("unchecked")
	@Override
	public boolean init(final Expression<?>[] vars, final int matchedPattern, final int isDelayed, final ParseResult parseResult) {
		blocks = (Expression<Block>) vars[0];
		toggle = matchedPattern - 1;
		return true;
	}
	
	@Override
	public String toString(final Event e, final boolean debug) {
		return "toggle " + blocks.toString(e, debug);
	}
	
	private final static byte[] bitFlags = new byte[Skript.MAXBLOCKID + 1];
	static {
		bitFlags[Material.DETECTOR_RAIL.getId()] = 0x8;
		bitFlags[Material.WOODEN_DOOR.getId()] = 0x4;
		bitFlags[Material.IRON_DOOR_BLOCK.getId()] = 0x4;
		bitFlags[Material.LEVER.getId()] = 0x8;
		bitFlags[Material.STONE_PLATE.getId()] = 0x1;
		bitFlags[Material.WOOD_PLATE.getId()] = 0x1;
		bitFlags[Material.STONE_BUTTON.getId()] = 0x8;
		bitFlags[Material.TRAP_DOOR.getId()] = 0x4;
		bitFlags[Material.FENCE_GATE.getId()] = 0x4;
	}
	
	@Override
	protected void execute(final Event e) {
		for (Block b : blocks.getArray(e)) {
			if ((b.getType() == Material.WOODEN_DOOR || b.getType() == Material.IRON_DOOR_BLOCK) && (b.getData() & 0x8) == 0x8) {
				b = b.getRelative(BlockFace.DOWN);
				if (b.getType() != Material.WOODEN_DOOR && b.getType() != Material.IRON_DOOR_BLOCK)
					continue;
			}
			final int type = b.getTypeId();
			final byte data = b.getData();
			if (toggle == -1)
				b.setData((byte) (data & ~bitFlags[type]));
			else if (toggle == 0)
				b.setData((byte) (data ^ bitFlags[type]));
			else
				b.setData((byte) (data | bitFlags[type]));
		}
	}
	
}
