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
import ch.njol.skript.entity.EntityData;
import ch.njol.skript.expressions.base.PropertyExpression;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.SkriptParser.ParseResult;

/**
 * @author Peter Güttinger
 */
public class ExprTypeOf extends PropertyExpression<Object, Object> {
	private static final long serialVersionUID = -7288078858273805343L;
	
	static {
		Skript.registerExpression(ExprTypeOf.class, Object.class, ExpressionType.PROPERTY, "[the] type of %entitydata/itemstack%");
	}
	
	@Override
	public boolean init(final Expression<?>[] exprs, final int matchedPattern, final int isDelayed, final ParseResult parseResult) {
		setExpr(exprs[0]);
		return true;
	}
	
	@Override
	public Class<? extends Object> getReturnType() {
		return EntityData.class.isAssignableFrom(getExpr().getReturnType()) ? EntityData.class : ItemStack.class;
	}
	
	@Override
	protected Object[] get(final Event e, final Object[] source) {
		final Object o = getExpr().getSingle(e);
		if (o == null)
			return null;
		if (o instanceof EntityData) {
			return new EntityData[] {(EntityData<?>) o};
		} else if (o instanceof ItemStack) {
			return new ItemStack[] {new ItemStack(((ItemStack) o).getTypeId(), 1, ((ItemStack) o).getDurability())};
		}
		assert false;
		return null;
	}
	
	@Override
	public String toString(final Event e, final boolean debug) {
		return "the type of " + getExpr().toString(e, debug);
	}
	
}
