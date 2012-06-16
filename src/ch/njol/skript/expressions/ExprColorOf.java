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

import org.bukkit.Material;
import org.bukkit.event.Event;
import org.bukkit.inventory.ItemStack;

import ch.njol.skript.Skript;
import ch.njol.skript.api.Changer.ChangeMode;
import ch.njol.skript.api.Converter;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.SimpleExpression;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.skript.util.Color;
import ch.njol.skript.util.ItemType;

/**
 * @author Peter Güttinger
 * 
 */
public class ExprColorOf extends SimpleExpression<Color> {
	
	static {
		Skript.registerExpression(ExprColorOf.class, Color.class, "colo[u]r[s] of %itemstacks%", "%itemstacks%'[s] colo[u]r[s]");
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
	public Class<? extends Color> getReturnType() {
		return Color.class;
	}
	
	@Override
	public String getDebugMessage(final Event e) {
		return "color of " + types.getDebugMessage(e);
	}
	
	@Override
	protected Color[] getAll(final Event e) {
		return types.getArray(e, Color.class, new Converter<ItemStack, Color>() {
			@Override
			public Color convert(final ItemStack is) {
				if (is == null)
					return null;
				if (is.getType() == Material.WOOL)
					return Color.byWool(is.getDurability());
				if (is.getType() == Material.INK_SACK)
					return Color.byDye(is.getDurability());
				return null;
			}
		});
	}
	
	@Override
	public String toString() {
		return "color of " + types;
	}
	
	@Override
	public Class<?> acceptChange(final ChangeMode mode) {
		if (mode != ChangeMode.SET || !types.isSingle())
			return null;
		if (types.acceptChange(mode) == ItemStack.class || types.acceptChange(mode) == ItemType.class)
			return Color.class;
		return null;
	}
	
	@Override
	public void change(final Event e, final Object delta, final ChangeMode mode) throws UnsupportedOperationException {
		final ItemStack is = types.getSingle(e);
		if (is == null)
			return;
		if (is.getType() == Material.WOOL)
			is.setDurability(((Color) delta).getWool());
		else if (is.getType() == Material.INK_SACK)
			is.setDurability(((Color) delta).getDye());
		else
			return;
		
		if (types.acceptChange(mode) == ItemStack.class)
			types.change(e, is, mode);
		else
			types.change(e, new ItemType(is), mode);
	}
	
}
