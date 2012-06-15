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

import org.bukkit.block.Block;
import org.bukkit.entity.LivingEntity;
import org.bukkit.event.Event;

import ch.njol.skript.Skript;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.SimpleExpression;
import ch.njol.skript.lang.SkriptParser.ParseResult;

/**
 * @author Peter Güttinger
 * 
 */
public class ExprTargetedBlock extends SimpleExpression<Block> {
	
	static {
		Skript.registerExpression(ExprTargetedBlock.class, Block.class, "[the] target[ed] block[s] [of %players%]", "%player%'[s] target[ed] block[s]");
	}
	
	private Expression<LivingEntity> entities;
	
	@SuppressWarnings("unchecked")
	@Override
	public boolean init(final Expression<?>[] vars, final int matchedPattern, final ParseResult parser) {
		entities = (Expression<LivingEntity>) vars[0];
		return true;
	}
	
	@Override
	public String getDebugMessage(final Event e) {
		if (e == null)
			return "targeted block of " + entities.getDebugMessage(e);
		return Skript.getDebugMessage(getAll(e));
	}
	
	@Override
	protected Block[] getAll(final Event evt) {
		final ArrayList<Block> targets = new ArrayList<Block>();
		for (final LivingEntity e : entities.getArray(evt)) {
			final Block t = e.getTargetBlock(null, Skript.TARGETBLOCKMAXDISTANCE);
			if (t != null && t.getTypeId() != 0)// air block = max distance or end of world (top/bottom) reached
				targets.add(t);
		}
		return targets.toArray(new Block[0]);
	}
	
	@Override
	public String toString() {
		return "the targeted block" + (isSingle() ? "" : "s") + " of " + entities;
	}
	
	@Override
	public Class<? extends Block> getReturnType() {
		return Block.class;
	}
	
	@Override
	public boolean isSingle() {
		return entities.isSingle();
	}
	
}
