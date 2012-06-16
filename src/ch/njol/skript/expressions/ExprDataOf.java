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
import ch.njol.skript.api.Changer.ChangeMode;
import ch.njol.skript.api.Converter;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.SimpleExpression;
import ch.njol.skript.lang.SkriptParser.ParseResult;

/**
 * @author Peter Güttinger
 * 
 */
public class ExprDataOf extends SimpleExpression<Short> {
	
	static {
		Skript.registerExpression(ExprDataOf.class, Short.class, "(data[s] [value[s]]|durabilit(y|ies)) of %itemstacks%", "%itemstacks%'[s] (data[s] [value[s]]|durabilit(y|ies))");
	}
	
	private Expression<ItemStack> types;
	
	@SuppressWarnings("unchecked")
	@Override
	public boolean init(final Expression<?>[] exprs, final int matchedPattern, final ParseResult parseResult) {
		types = (Expression<ItemStack>) exprs[0];
		return true;
	}
	
	@Override
	public boolean isSingle() {
		return types.isSingle();
	}
	
	@Override
	public Class<? extends Short> getReturnType() {
		return Short.class;
	}
	
	@Override
	protected Short[] getAll(final Event e) {
		return types.getArray(e, Short.class, new Converter<ItemStack, Short>() {
			@Override
			public Short convert(final ItemStack is) {
				return is.getDurability();
			}
		});
	}
	
	@Override
	public String toString() {
		return "data of " + types;
	}
	
	@Override
	public String getDebugMessage(final Event e) {
		return "data of " + types.getDebugMessage(e);
	}
	
	@Override
	public Class<?> acceptChange(final ChangeMode mode) {
		// TODO Auto-generated method stub
		return super.acceptChange(mode);
	}
	
	@Override
	public void change(final Event e, final Object delta, final ChangeMode mode) throws UnsupportedOperationException {
		// TODO Auto-generated method stub
		super.change(e, delta, mode);
	}
	
}
