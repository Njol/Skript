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

import org.bukkit.enchantments.Enchantment;
import org.bukkit.event.Event;
import org.bukkit.inventory.ItemStack;

import ch.njol.skript.Skript;
import ch.njol.skript.classes.Changer.ChangeMode;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Examples;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import ch.njol.skript.lang.Effect;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.skript.util.EnchantmentType;
import ch.njol.skript.util.Utils;
import ch.njol.util.Kleenean;

/**
 * @author Peter Güttinger
 */
@SuppressWarnings("serial")
@Name("Enchant/Disenchant")
@Description("Enchant or disenchant an existing item")
@Examples({"enchant the player's tool with sharpness 5",
		"disenchant the player's tool"})
@Since("2.0")
public class EffEnchant extends Effect {
	static {
		Skript.registerEffect(EffEnchant.class,
				"enchant %~itemstack% with %enchantmenttypes%",
				"disenchant %~itemstack%");
	}
	
	private Expression<ItemStack> item;
	private Expression<EnchantmentType> enchs;
	
	@SuppressWarnings("unchecked")
	@Override
	public boolean init(final Expression<?>[] exprs, final int matchedPattern, final Kleenean isDelayed, final ParseResult parseResult) {
		item = (Expression<ItemStack>) exprs[0];
		if (!Utils.contains(item.acceptChange(ChangeMode.SET), ItemStack.class)) {
			Skript.error(item + " cannot be changed, thus it cannot be (dis)enchanted");
			return false;
		}
		if (matchedPattern == 1)
			enchs = (Expression<EnchantmentType>) exprs[1];
		return true;
	}
	
	@Override
	protected void execute(final Event e) {
		final ItemStack i = item.getSingle(e);
		if (i == null)
			return;
		if (enchs == null) {
			for (final Enchantment ench : i.getEnchantments().keySet()) {
				i.removeEnchantment(ench);
			}
			item.change(e, i, ChangeMode.SET);
		} else {
			final EnchantmentType[] types = enchs.getArray(e);
			if (types.length == 0)
				return;
			for (final EnchantmentType type : types) {
				i.addUnsafeEnchantment(type.getType(), type.getLevel());
			}
			item.change(e, i, ChangeMode.SET);
		}
	}
	
	@Override
	public String toString(final Event e, final boolean debug) {
		return enchs == null ? "disenchant " + item.toString(e, debug) : "enchant " + item.toString(e, debug) + " with " + enchs;
	}
	
}
