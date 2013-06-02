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
 * Copyright 2011-2013 Peter Güttinger
 * 
 */

package ch.njol.skript.expressions;

import org.bukkit.Material;
import org.bukkit.event.Event;
import org.bukkit.inventory.ItemStack;

import ch.njol.skript.aliases.ItemType;
import ch.njol.skript.classes.Changer.ChangeMode;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Examples;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import ch.njol.skript.expressions.base.SimplePropertyExpression;
import ch.njol.skript.util.Color;
import ch.njol.util.CollectionUtils;

/**
 * @author Peter Güttinger
 */
@SuppressWarnings("serial")
@Name("Colour of")
@Description("The <a href='../classes/#color'>colour</a> of an item, can also be used to colour chat messages with \"&lt;%color of ...%&gt;this text is coloured!\".")
@Examples({"on click on wool:",
		"	message \"This wool block is <%color of block%>%color of block%<reset>!\"",
		"	set the colour of the block to black"})
@Since("1.2")
public class ExprColorOf extends SimplePropertyExpression<ItemStack, Color> {
	
	static {
		register(ExprColorOf.class, Color.class, "colo[u]r[s]", "itemstacks");
	}
	
	@Override
	public Color convert(final ItemStack is) {
		if (is.getType() == Material.WOOL)
			return Color.byWool(is.getDurability());
		if (is.getType() == Material.INK_SACK)
			return Color.byDye(is.getDurability());
		return null;
	}
	
	@Override
	protected String getPropertyName() {
		return "color";
	}
	
	@Override
	public Class<Color> getReturnType() {
		return Color.class;
	}
	
	@SuppressWarnings("unchecked")
	@Override
	public Class<?>[] acceptChange(final ChangeMode mode) {
		if (mode != ChangeMode.SET || !getExpr().isSingle())
			return null;
		if (getExpr().acceptChange(mode) != null && CollectionUtils.containsAny(getExpr().acceptChange(mode), ItemStack.class, ItemType.class))
			return CollectionUtils.array(Color.class);
		return null;
	}
	
	@Override
	public void change(final Event e, final Object delta, final ChangeMode mode) throws UnsupportedOperationException {
		final ItemStack is = getExpr().getSingle(e);
		if (is == null)
			return;
		if (is.getType() == Material.WOOL)
			is.setDurability(((Color) delta).getWool());
		else if (is.getType() == Material.INK_SACK)
			is.setDurability(((Color) delta).getDye());
		else
			return;
		
		if (CollectionUtils.contains(getExpr().acceptChange(mode), ItemStack.class))
			getExpr().change(e, is, mode);
		else
			getExpr().change(e, new ItemType(is), mode);
	}
	
}
