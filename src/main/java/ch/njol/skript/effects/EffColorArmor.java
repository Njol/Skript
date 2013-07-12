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

package ch.njol.skript.effects;

import org.bukkit.Material;
import org.bukkit.event.Event;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.LeatherArmorMeta;

import ch.njol.skript.Skript;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Examples;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import ch.njol.skript.lang.Effect;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.skript.util.Color;
import ch.njol.util.Kleenean;

/**
 * @author joeuguce99
 */
@SuppressWarnings("serial")
@Name("Color Armor")
@Description("Colors leather armor. You can also use RGB codes if you feel limited with the default 16 colors.")
@Examples({"dye player's helmet blue",
		"colour the player's tool red"})
@Since("2.0")
public class EffColorArmor extends Effect {
	
	static {
		Skript.registerEffect(EffColorArmor.class,
				"(dye|colo[u]r|paint) %itemstack% %color%",
				"(dye|color[u]|paint) %itemstack% (%number%, %number%, %number%)");
	}
	
	private Expression<ItemStack> item;
	private Expression<Color> color;
	private Expression<Number> red;
	private Expression<Number> green;
	private Expression<Number> blue;
	
	private boolean rgb;
	
	
	@SuppressWarnings("unchecked")
	@Override
	public boolean init(final Expression<?>[] vars, final int matchedPattern, final Kleenean isDelayed, final ParseResult parser) {
		item = (Expression<ItemStack>) vars[0];
		if (matchedPattern == 0) {
			color = (Expression<Color>) vars[1];
			rgb = false;
			return true;
		} else if(matchedPattern == 1){
			red = (Expression<Number>) vars[1];
			green = (Expression<Number>) vars[2];
			blue = (Expression<Number>) vars[3];
			rgb = true;
			return true;
		} else{
			return false;
		}
	}
	
	@Override
	public String toString(final Event e, final boolean debug) {
		return "dye " + item.toString(e, debug) + color.toString(e, debug);
	}
	
	@Override
	protected void execute(final Event e) {
		final ItemStack i = item.getSingle(e);
		if(i.getType() == Material.LEATHER_BOOTS || i.getType() == Material.LEATHER_CHESTPLATE || i.getType() == Material.LEATHER_HELMET || i.getType() == Material.LEATHER_LEGGINGS){
			if(!rgb){
				final Color c = color.getSingle(e);
				final LeatherArmorMeta m = (LeatherArmorMeta) i.getItemMeta();
				m.setColor(c.getBukkitColor(c));
				i.setItemMeta(m);
			} else{
				final int r = red.getSingle(e).intValue();
				final int g = green.getSingle(e).intValue();
				final int b = blue.getSingle(e).intValue();
				final LeatherArmorMeta m = (LeatherArmorMeta) i.getItemMeta();
				m.setColor(org.bukkit.Color.fromRGB(r, g, b));
				i.setItemMeta(m);
			}
		} else{
			Skript.error("Specified item is not leather armor");
		}
		
	}	
}
