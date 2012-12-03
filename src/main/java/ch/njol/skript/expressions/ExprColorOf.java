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
import ch.njol.skript.classes.Changer.ChangeMode;
import ch.njol.skript.classes.Converter;
import ch.njol.skript.expressions.base.PropertyExpression;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.ExpressionType;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.skript.util.Color;
import ch.njol.skript.util.ItemType;
import ch.njol.skript.util.Utils;
import ch.njol.util.Kleenean;

/**
 * @author Peter Güttinger
 */
public class ExprColorOf extends PropertyExpression<ItemStack, Color> {
	private static final long serialVersionUID = 4412920468773410611L;
	
	static {
		Skript.registerExpression(ExprColorOf.class, Color.class, ExpressionType.PROPERTY, "colo[u]r[s] of %itemstacks%", "%itemstacks%'[s] colo[u]r[s]");
	}
	
	private Expression<ItemStack> types;
	
	@SuppressWarnings("unchecked")
	@Override
	public boolean init(final Expression<?>[] exprs, final int matchedPattern, final Kleenean isDelayed, final ParseResult parseResult) {
		types = (Expression<ItemStack>) exprs[0];
		setExpr(types);
		return true;
	}
	
	@Override
	public Class<Color> getReturnType() {
		return Color.class;
	}
	
	@Override
	public String toString(final Event e, final boolean debug) {
		return "color of " + types.toString(e, debug);
	}
	
	@Override
	protected Color[] get(final Event e, final ItemStack[] source) {
		return get(source, new Converter<ItemStack, Color>() {
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
	
	@SuppressWarnings("unchecked")
	@Override
	public Class<?>[] acceptChange(final ChangeMode mode) {
		if (mode != ChangeMode.SET || !types.isSingle())
			return null;
		if (types.acceptChange(mode) != null && Utils.containsAny(types.acceptChange(mode), ItemStack.class, ItemType.class))
			return Skript.array(Color.class);
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
		
		if (Utils.contains(types.acceptChange(mode), ItemStack.class))
			types.change(e, is, mode);
		else
			types.change(e, new ItemType(is), mode);
	}
	
}
