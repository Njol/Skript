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
import org.bukkit.entity.Entity;
import org.bukkit.entity.Item;
import org.bukkit.entity.Sheep;
import org.bukkit.event.Event;
import org.bukkit.inventory.ItemStack;

import ch.njol.skript.aliases.ItemType;
import ch.njol.skript.classes.Changer.ChangeMode;
import ch.njol.skript.classes.Changer.ChangerUtils;
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
@Description("The <a href='../classes/#color'>colour</a> of an item, can also be used to colour chat messages with \"&lt;%colour of ...%&gt;this text is coloured!\".")
@Examples({"on click on wool:",
		"	message \"This wool block is <%colour of block%>%colour of block%<reset>!\"",
		"	set the colour of the block to black"})
@Since("1.2")
public class ExprColorOf extends SimplePropertyExpression<Object, Color> {
	static {
		register(ExprColorOf.class, Color.class, "colo[u]r[s]", "itemstacks/entities");
	}
	
	@Override
	public Color convert(final Object o) {
		if (o instanceof ItemStack || o instanceof Item) {
			final ItemStack is = o instanceof ItemStack ? (ItemStack) o : ((Item) o).getItemStack();
			if (is.getType() == Material.WOOL)
				return Color.byWool(is.getDurability());
			if (is.getType() == Material.INK_SACK)
				return Color.byDye(is.getDurability());
		} else if (o instanceof Sheep) {
			return Color.byWoolColor(((Sheep) o).getColor());
		}
		return null;
	}
	
	@Override
	protected String getPropertyName() {
		return "colour";
	}
	
	@Override
	public Class<Color> getReturnType() {
		return Color.class;
	}
	
	@SuppressWarnings("unchecked")
	@Override
	public Class<?>[] acceptChange(final ChangeMode mode) {
		if (mode != ChangeMode.SET)
			return null;
		if (Entity.class.isAssignableFrom(getExpr().getReturnType()))
			return CollectionUtils.array(Color.class);
		if (!getExpr().isSingle())
			return null;
		if (ChangerUtils.acceptsChange(getExpr(), mode, ItemStack.class, ItemType.class))
			return CollectionUtils.array(Color.class);
		return null;
	}
	
	@Override
	public void change(final Event e, final Object[] delta, final ChangeMode mode) throws UnsupportedOperationException {
		assert mode == ChangeMode.SET;
		
		final Color c = delta == null ? null : (Color) delta[0];
		final Object[] os = getExpr().getArray(e);
		if (os.length == 0)
			return;
		
		for (final Object o : os) {
			if (o instanceof ItemStack || o instanceof Item) {
				final ItemStack is = o instanceof ItemStack ? (ItemStack) o : ((Item) o).getItemStack();
				if (is.getType() == Material.WOOL)
					is.setDurability((c).getWool());
				else if (is.getType() == Material.INK_SACK)
					is.setDurability((c).getDye());
				else
					continue;
				
				if (o instanceof ItemStack) {
					if (ChangerUtils.acceptsChange(getExpr(), mode, ItemStack.class))
						getExpr().change(e, new ItemStack[] {is}, mode);
					else
						getExpr().change(e, new ItemType[] {new ItemType(is)}, mode);
				} else {
					((Item) o).setItemStack(is);
				}
			} else if (o instanceof Sheep) {
				((Sheep) o).setColor((c).getWoolColor());
			}
		}
	}
	
}
