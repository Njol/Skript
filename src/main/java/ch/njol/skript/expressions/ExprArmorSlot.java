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

import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.inventory.ItemStack;

import ch.njol.skript.classes.Changer.ChangeMode;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Examples;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import ch.njol.skript.expressions.base.SimplePropertyExpression;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.skript.util.Utils;
import ch.njol.util.Kleenean;

/**
 * @author Peter Güttinger
 */
@SuppressWarnings("serial")
@Name("Armour Slot")
@Description("A part of a player's armour, i.e. the boots, leggings, chestplate or helmet.")
@Examples({"set chestplate of the player to a diamond chestplate",
		"helmet of player is neither a helmet nor air # player is wearing a block, e.g. from another plugin"})
@Since("1.0")
public class ExprArmorSlot extends SimplePropertyExpression<Player, ItemStack> {
	static {
		register(ExprArmorSlot.class, ItemStack.class, "(0¦boot[s]|0¦shoe[s]|1¦leg[ging][s]|2¦chestplate[s]|3¦helm[et][s]) [slot]", "players");
	}
	
	private int slot;
	
	private final static String[] slotNames = {"boots", "leggings", "chestplate", "helmet"};
	
	@Override
	public boolean init(final Expression<?>[] exprs, final int matchedPattern, final Kleenean isDelayed, final ParseResult parseResult) {
		super.init(exprs, matchedPattern, isDelayed, parseResult);
		slot = parseResult.mark;
		return true;
	}
	
	@Override
	public ItemStack convert(final Player p) {
		return p.getInventory().getArmorContents()[slot];
//		return new PlayerSlot(p.getInventory()) {
//			@Override
//			public ItemStack getItem() {
//				return p.getInventory().getArmorContents()[slot];
//			}
//			
//			@Override
//			public void setItem(final ItemStack item) {
//				final ItemStack[] armour = p.getInventory().getArmorContents();
//				armour[slot] = item;
//				p.getInventory().setArmorContents(armour);
//			}
//			
//			@Override
//			public String toString(final Event e, final boolean debug) {
//				return slotNames[slot] + " of " + p.getName();
//			}
//		};
	}
	
	@Override
	protected String getPropertyName() {
		return slotNames[slot];
	}
	
	@Override
	public Class<ItemStack> getReturnType() {
		return ItemStack.class;
	}
	
	@SuppressWarnings("unchecked")
	@Override
	public Class<ItemStack>[] acceptChange(final ChangeMode mode) {
		if (mode == ChangeMode.SET || mode == ChangeMode.DELETE)
			return Utils.array(ItemStack.class);
		return null;
	}
	
	@SuppressWarnings("deprecation")
	@Override
	public void change(final Event e, final Object delta, final ChangeMode mode) {
		for (final Player p : getExpr().getArray(e)) {
			final ItemStack[] armour = p.getInventory().getArmorContents();
			armour[slot] = (ItemStack) delta; // both SET and DELETE
			p.getInventory().setArmorContents(armour);
			p.updateInventory();
		}
	}
	
}
