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

package ch.njol.skript.loops;

import java.util.Iterator;

import org.bukkit.Material;
import org.bukkit.event.Event;
import org.bukkit.inventory.ItemStack;

import ch.njol.skript.Skript;
import ch.njol.skript.api.LoopVar;
import ch.njol.skript.lang.ExprParser.ParseResult;
import ch.njol.skript.lang.Variable;
import ch.njol.skript.util.ItemType;
import ch.njol.util.Checker;
import ch.njol.util.iterator.ArrayIterator;
import ch.njol.util.iterator.CheckedIterator;

/**
 * @author Peter Güttinger
 * 
 */
public class LoopVarItem extends LoopVar<ItemStack> {
	
	static {
		Skript.addLoop(LoopVarItem.class, ItemStack.class,
				"[(all|every)] item(s|[ ]types)", "items of type[s] %itemtypes%",
				"[(all|every)] block(s|[ ]types)", "blocks of type[s] %itemtypes%");
	}
	
	private Variable<ItemType> types = null;
	private boolean blocks = false;
	
	@SuppressWarnings("unchecked")
	@Override
	public void init(final Variable<?>[] vars, final int matchedPattern, final ParseResult parser) {
		if (vars.length > 0)
			types = (Variable<ItemType>) vars[0];
		blocks = matchedPattern >= 2;
	}
	
	@Override
	protected Iterator<ItemStack> iterator(final Event e) {
		Iterator<ItemStack> iter;
		if (types == null) {
			iter = new Iterator<ItemStack>() {
				
				private final Iterator<Material> iter = new ArrayIterator<Material>(Material.values());
				
				@Override
				public boolean hasNext() {
					return iter.hasNext();
				}
				
				@Override
				public ItemStack next() {
					return new ItemStack(iter.next());
				}
				
				@Override
				public void remove() {}
				
			};
		} else {
			iter = new Iterator<ItemStack>() {
				
				private final Iterator<ItemType> iter = new ArrayIterator<ItemType>(types.getArray(e));
				Iterator<ItemStack> current;
				
				@Override
				public boolean hasNext() {
					return iter.hasNext() || current.hasNext();
				}
				
				@Override
				public ItemStack next() {
					if (current == null || !current.hasNext()) {
						current = iter.next().getAll().iterator();
					}
					return current.next();
				}
				
				@Override
				public void remove() {}
				
			};
		}
		
		if (!blocks)
			return iter;
		
		return new CheckedIterator<ItemStack>(iter, new Checker<ItemStack>() {
			@Override
			public boolean check(final ItemStack is) {
				return is.getTypeId() <= Skript.MAXBLOCKID;
			}
		});
	}
	
	@Override
	public Class<? extends ItemStack> getReturnType() {
		return ItemStack.class;
	}
	
	@Override
	public String getLoopDebugMessage(final Event e) {
		return (blocks ? "blocks" : "items") + " of type " + types.getDebugMessage(e);
	}
	
	@Override
	public String toString() {
		return "the loop-" + (blocks ? "block" : "item");
	}
	
	@Override
	public boolean isLoopOf(final String s) {
		return blocks && s.equalsIgnoreCase("block") || !blocks && s.equalsIgnoreCase("item");
	}
	
}
