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

import org.bukkit.event.Event;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;

import ch.njol.skript.Skript;
import ch.njol.skript.api.Changer.ChangeMode;
import ch.njol.skript.api.Getter;
import ch.njol.skript.data.DefaultChangers;
import ch.njol.skript.lang.ExprParser.ParseResult;
import ch.njol.skript.lang.SimpleVariable;
import ch.njol.skript.lang.Variable;

/**
 * @author Peter Güttinger
 * 
 */
public class VarInventory extends SimpleVariable<Inventory> {
	
	static {
		Skript.addVariable(VarInventory.class, Inventory.class, "inventory of %inventoryholders%");
	}
	
	private Variable<InventoryHolder> holders;
	
	@SuppressWarnings("unchecked")
	@Override
	public void init(final Variable<?>[] vars, final int matchedPattern, final ParseResult parser) {
		holders = (Variable<InventoryHolder>) vars[0];
	}
	
	@Override
	public String getDebugMessage(final Event e) {
		return "inventory of " + holders.getDebugMessage(e);
	}
	
	@Override
	protected Inventory[] getAll(final Event e) {
		return holders.getArray(e, Inventory.class, new Getter<Inventory, InventoryHolder>() {
			@Override
			public Inventory get(final InventoryHolder h) {
				return h.getInventory();
			}
		});
	}
	
	@Override
	public Class<? extends Inventory> getReturnType() {
		return Inventory.class;
	}
	
	@Override
	public Class<?> acceptChange(final ChangeMode mode) {
		return DefaultChangers.inventoryChanger.acceptChange(mode);
	}
	
	@Override
	public void change(final Event e, final Variable<?> delta, final ChangeMode mode) {
		DefaultChangers.inventoryChanger.change(e, this, delta, mode);
	}
	
	@Override
	public String toString() {
		return "the inventory of " + holders;
	}
	
	@Override
	public boolean isSingle() {
		return holders.isSingle();
	}
	
}
