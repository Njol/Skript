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

import java.util.ArrayList;

import org.bukkit.block.Block;
import org.bukkit.entity.LivingEntity;
import org.bukkit.event.Event;

import ch.njol.skript.Skript;
import ch.njol.skript.api.Changer.ChangeMode;
import ch.njol.skript.data.DefaultChangers;
import ch.njol.skript.lang.ExprParser.ParseResult;
import ch.njol.skript.lang.SimpleVariable;
import ch.njol.skript.lang.Variable;

/**
 * @author Peter Güttinger
 * 
 */
public class VarTargetedBlock extends SimpleVariable<Block> {
	
	static {
		Skript.addVariable(VarTargetedBlock.class, Block.class, "targeted block[s] [of %livingentities%]");
	}
	
	private Variable<LivingEntity> entities;
	
	@SuppressWarnings("unchecked")
	@Override
	public void init(final Variable<?>[] vars, final int matchedPattern, final ParseResult parser) {
		entities = (Variable<LivingEntity>) vars[0];
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
	public Class<?> acceptChange(final ChangeMode mode) {
		return DefaultChangers.blockChanger.acceptChange(mode);
	}
	
	@Override
	public void change(final Event e, final Variable<?> delta, final ChangeMode mode) {
		DefaultChangers.blockChanger.change(e, this, delta, mode);
	}
	
	@Override
	public boolean isSingle() {
		return entities.isSingle();
	}
	
}
