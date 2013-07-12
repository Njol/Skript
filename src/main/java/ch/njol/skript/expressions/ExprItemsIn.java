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

import org.bukkit.event.Event;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import ch.njol.skript.Skript;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Examples;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.ExpressionType;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.skript.lang.util.SimpleExpression;
import ch.njol.util.Kleenean;

/**
 * @author Peter Güttinger
 */
@SuppressWarnings("serial")
@Name("Items In")
@Description({"All items in an inventory. Useful for looping or storing in a list variable.",
		"Please note that the positions of the items in the inventory are not saved, only their order is preserved."})
@Examples({"loop all items in the player's inventory:",
		"	loop-item is enchanted" +
				"	remove loop-item from the player",
		"set {inventory.%player%} to items in the player's inventory"})
@Since("2.0")
public class ExprItemsIn extends SimpleExpression<ItemStack> {
	static {
		Skript.registerExpression(ExprItemsIn.class, ItemStack.class, ExpressionType.PROPERTY,
				"[all] items (in|of|contained in|out of) %inventories%");
	}
	
	Expression<Inventory> invis;
	
	@SuppressWarnings("unchecked")
	@Override
	public boolean init(final Expression<?>[] exprs, final int matchedPattern, final Kleenean isDelayed, final ParseResult parseResult) {
		invis = (Expression<Inventory>) exprs[0];
		return true;
	}
	
	@Override
	protected ItemStack[] get(final Event e) {
		final ArrayList<ItemStack> r = new ArrayList<ItemStack>();
		for (final Inventory invi : invis.getArray(e)) {
			for (final ItemStack is : invi) {
				if (is != null)
					r.add(is);
			}
		}
		return r.toArray(new ItemStack[r.size()]);
	}
	
	@Override
	public String toString(final Event e, final boolean debug) {
		return "items in " + invis.toString(e, debug);
	}
	
	@Override
	public boolean isSingle() {
		return false;
	}
	
	@Override
	public Class<? extends ItemStack> getReturnType() {
		return ItemStack.class;
	}
	
}
