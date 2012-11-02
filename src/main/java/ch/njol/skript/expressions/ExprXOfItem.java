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

import org.bukkit.event.Event;
import org.bukkit.inventory.ItemStack;

import ch.njol.skript.Skript;
import ch.njol.skript.Skript.ExpressionType;
import ch.njol.skript.classes.Converter;
import ch.njol.skript.expressions.base.PropertyExpression;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.Literal;
import ch.njol.skript.lang.SkriptParser.ParseResult;

/**
 * @author Peter Güttinger
 */
public class ExprXOfItem extends PropertyExpression<ItemStack, ItemStack> {
	private static final long serialVersionUID = 5709306242713886383L;
	
	static {
		Skript.registerExpression(ExprXOfItem.class, ItemStack.class, ExpressionType.PATTERN_MATCHES_EVERYTHING, "%number% of %itemstacks%");
	}
	
	private Expression<Number> amount;
	
	@SuppressWarnings("unchecked")
	@Override
	public boolean init(final Expression<?>[] exprs, final int matchedPattern, final int isDelayed, final ParseResult parseResult) {
		setExpr((Expression<? extends ItemStack>) exprs[1]);
		amount = (Expression<Number>) exprs[0];
		if (amount instanceof Literal && getExpr() instanceof Literal)// "x of y" is also an ItemType syntax
			return false;
		return true;
	}
	
	@Override
	public Class<? extends ItemStack> getReturnType() {
		return ItemStack.class;
	}
	
	@Override
	protected ItemStack[] get(final Event e, final ItemStack[] source) {
		return get(source, new Converter<ItemStack, ItemStack>() {
			@Override
			public ItemStack convert(final ItemStack is) {
				final Number a = amount.getSingle(e);
				if (a == null)
					return null;
				is.setAmount(a.intValue());
				return is;
			}
		});
	}
	
	@Override
	public String toString(final Event e, final boolean debug) {
		return amount + " of " + getExpr().toString(e, debug);
	}
	
}
