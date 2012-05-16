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

package ch.njol.skript.variables.base;

import org.bukkit.block.Block;
import org.bukkit.event.Event;

import ch.njol.skript.api.Changer.ChangeMode;
import ch.njol.skript.data.DefaultChangers;
import ch.njol.skript.lang.SimpleVariable;
import ch.njol.skript.lang.Variable;

/**
 * A simple base for Block Variables
 * 
 * @author Peter Güttinger
 * 
 */
public abstract class BlockVariable extends SimpleVariable<Block> {
	
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
}
