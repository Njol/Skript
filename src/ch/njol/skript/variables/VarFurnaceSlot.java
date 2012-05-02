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

import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.Furnace;
import org.bukkit.event.Event;

import ch.njol.skript.Skript;
import ch.njol.skript.api.Changer.ChangeMode;
import ch.njol.skript.api.Getter;
import ch.njol.skript.api.intern.Variable;
import ch.njol.skript.data.DefaultChangers;
import ch.njol.skript.util.Slot;

/**
 * @author Peter Güttinger
 * 
 */
public class VarFurnaceSlot extends Variable<Slot> {
	
	// Slot IDs:
	// ore: 0, fuel: 1, result: 2
	
	static {
		Skript.addVariable(VarFurnaceSlot.class, Slot.class,
				"ore( slot)? of %block%", "ore slot(%block%){0}",
				"fuel( slot)? of %block%", "fuel slot(%block%){0}",
				"result( slot)? of %block%", "result slot(%block%){0}");
	}
	
	private Variable<Block> blocks;
	private int slot;
	
	private final static String[] slotNames = {"ore", "fuel", "result"};
	
	@SuppressWarnings("unchecked")
	@Override
	public void init(final Variable<?>[] vars, final int matchedPattern, final Matcher matcher) {
		blocks = (Variable<Block>) vars[0];
		slot = matchedPattern / 2;
	}
	
	@Override
	protected Slot[] getAll(final Event e) {
		return get(e, blocks, new Getter<Slot, Block>() {
			@Override
			public Slot get(final Block b) {
				if (b.getType() != Material.FURNACE)
					return null;
				return new Slot(((Furnace) b.getState()).getInventory(), slot);
			}
		}, false);
	}
	
	@Override
	public Class<? extends Slot> getReturnType() {
		return Slot.class;
	}
	
	@Override
	public String getDebugMessage(final Event e) {
		if (e == null)
			return slotNames[slot] + " slot of " + blocks.getDebugMessage(e);
		return Skript.toString(getAll(e)[0]);
	}
	
	@Override
	public Class<?> acceptChange(final ChangeMode mode) {
		return DefaultChangers.slotChanger.acceptChange(mode);
	}
	
	@Override
	public void change(final Event e, final Variable<?> delta, final ChangeMode mode) {
		DefaultChangers.slotChanger.change(e, this, delta, mode);
	}
	
	@Override
	public String toString() {
		return "the " + slotNames[slot] + " slot of " + blocks;
	}
	
}
