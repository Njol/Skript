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

import org.bukkit.inventory.ItemStack;

import ch.njol.skript.Skript;
import ch.njol.skript.Skript.ExpressionType;
import ch.njol.skript.expressions.base.EventValueExpression;

/**
 * @author Peter Güttinger
 * 
 */
public class ExprItem extends EventValueExpression<ItemStack> {
	
	private static final long serialVersionUID = 1124422770106475966L;
	
	static {
		Skript.registerExpression(ExprItem.class, ItemStack.class, ExpressionType.SIMPLE, "[the] item");
	}
	
	public ExprItem() {
		super(ItemStack.class);
	}
	
}
