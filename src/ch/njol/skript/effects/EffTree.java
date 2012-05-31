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

import org.bukkit.TreeType;
import org.bukkit.block.Block;
import org.bukkit.event.Event;

import ch.njol.skript.Skript;
import ch.njol.skript.api.Effect;
import ch.njol.skript.lang.ExprParser.ParseResult;
import ch.njol.skript.lang.Variable;
import ch.njol.skript.util.Offset;

public class EffTree extends Effect {
	
	static {
		Skript.registerEffect(EffTree.class,
				"(grow|create|generate) tree [of type %treetype%] %offsets% %blocks%",
				"(grow|create|generate) %treetype% [tree] %offsets% %blocks%");
	}
	
	private Variable<Offset> offsets;
	private Variable<Block> blocks;
	private Variable<TreeType> type;
	
	@SuppressWarnings("unchecked")
	@Override
	public void init(final Variable<?>[] vars, final int matchedPattern, final ParseResult parser) {
		type = (Variable<TreeType>) vars[0];
		offsets = (Variable<Offset>) vars[1];
		blocks = (Variable<Block>) vars[2];
	}
	
	@Override
	public void execute(final Event e) {
		final TreeType type = this.type.getSingle(e);
		if (type == null)
			return;
		for (final Block b : Offset.setOff(offsets.getArray(e), blocks.getArray(e))) {
			b.getWorld().generateTree(b.getLocation(), type);
		}
	}
	
	@Override
	public String getDebugMessage(final Event e) {
		return "grow tree of type " + type.getDebugMessage(e) + " " + offsets.getDebugMessage(e) + " " + blocks.getDebugMessage(e);
	}
	
}
