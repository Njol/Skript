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

import java.util.List;

import org.bukkit.event.Event;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.inventory.ItemStack;

import ch.njol.skript.Skript;
import ch.njol.skript.TriggerFileLoader;
import ch.njol.skript.api.Changer.ChangeMode;
import ch.njol.skript.lang.ExprParser.ParseResult;
import ch.njol.skript.lang.SimpleVariable;
import ch.njol.skript.lang.Variable;
import ch.njol.skript.util.ItemType;
import ch.njol.skript.util.Utils;

/**
 * @author Peter Güttinger
 * 
 */
public class VarDrops extends SimpleVariable<ItemStack> {
	
	static {
		Skript.registerVariable(VarDrops.class, ItemStack.class, "[the] drops");
	}
	
	@Override
	public boolean init(final Variable<?>[] vars, final int matchedPattern, final ParseResult parser) {
		if (Utils.indexOf(TriggerFileLoader.currentEvents, EntityDeathEvent.class) == -1) {
			Skript.error("'drops' can only be used in death events");
			return false;
		}
		return true;
	}
	
	@Override
	protected ItemStack[] getAll(final Event e) {
		if (!(e instanceof EntityDeathEvent))
			return null;
		return ((EntityDeathEvent) e).getDrops().toArray(new ItemStack[0]);
	}
	
	@Override
	public Class<?> acceptChange(final ChangeMode mode) {
		return ItemType.class;
	}
	
	@SuppressWarnings("unchecked")
	@Override
	public void change(final Event e, final Variable<?> delta, final ChangeMode mode) {
		if (!(e instanceof EntityDeathEvent))
			return;
		final List<ItemStack> drops = ((EntityDeathEvent) e).getDrops();
		switch (mode) {
			case SET:
				drops.clear();
				//$FALL-THROUGH$
			case ADD:
				for (final ItemType i : ((Variable<ItemType>) delta).getArray(e)) {
					i.addTo(drops);
				}
			break;
			case REMOVE:
				for (final ItemType i : ((Variable<ItemType>) delta).getArray(e)) {
					i.removeFrom(drops);
				}
			break;
			case CLEAR:
				drops.clear();
			break;
		}
	}
	
	@Override
	public Class<? extends ItemStack> getReturnType() {
		return ItemStack.class;
	}
	
	@Override
	public String getDebugMessage(final Event e) {
		if (e == null)
			return "drops";
		return Skript.getDebugMessage(getAll(e));
	}
	
	@Override
	public String toString() {
		return "the drops";
	}
	
	@Override
	public boolean isSingle() {
		return false;
	}
	
}
