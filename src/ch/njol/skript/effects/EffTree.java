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

public class EffTree extends Effect {
	
	static {
		Skript.addEffect(EffTree.class,
				"(grow|create|generate) tree [of type %treetype%] [at %blocks%]",
				"(grow|create|generate) %treetype% [tree] [at %blocks%]");
	}
	
	private Variable<Block> blocks;
	private Variable<TreeType> type;
	
	@SuppressWarnings("unchecked")
	@Override
	public void init(final Variable<?>[] vars, final int matchedPattern, final ParseResult parser) {
		type = (Variable<TreeType>) vars[0];
		blocks = (Variable<Block>) vars[1];
	}
	
	@Override
	public void execute(final Event e) {
		final TreeType type = this.type.getSingle(e);
		if (type == null)
			return;
		for (final Block b : blocks.getArray(e)) {
			b.getWorld().generateTree(b.getLocation(), type);
		}
	}
	
	@Override
	public String getDebugMessage(final Event e) {
		return "grow tree of type " + type.getDebugMessage(e) + " at " + blocks.getDebugMessage(e);
	}
	
}
