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

import java.util.regex.Matcher;

import org.bukkit.block.Block;
import org.bukkit.event.Event;

import ch.njol.skript.Skript;
import ch.njol.skript.api.Changer.ChangeMode;
import ch.njol.skript.api.exception.InitException;
import ch.njol.skript.api.exception.ParseException;
import ch.njol.skript.api.intern.Variable;
import ch.njol.skript.data.DefaultChangers;
import ch.njol.skript.util.Offset;

/**
 * 
 * @author Peter Güttinger
 * 
 */
public class VarBlock extends Variable<Block> {
	
	static {
		Skript.addVariable(VarBlock.class, Block.class, "block(s? %-offset%( %block%)?)?");
	}
	
	private Variable<Offset> offsets;
	private Variable<Block> blocks;
	
	@SuppressWarnings("unchecked")
	@Override
	public void init(final Variable<?>[] vars, final int matchedPattern, final Matcher matcher) throws InitException, ParseException {
		offsets = (Variable<Offset>) vars[0];
		blocks = (Variable<Block>) vars[1];
	}
	
	@Override
	public String getDebugMessage(final Event e) {
		if (offsets == null)
			return blocks.getDebugMessage(e);
		return "block " + offsets.getDebugMessage(e) + " " + blocks.getDebugMessage(e);
	}
	
	@Override
	protected Block[] getAll(final Event e) {
		if (offsets == null)
			return blocks.get(e);
		return Offset.setOff(offsets.get(e), blocks.get(e));
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
		return "the block";
	}
	
}
