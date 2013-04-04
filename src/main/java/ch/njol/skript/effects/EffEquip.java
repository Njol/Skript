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

import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.inventory.ItemStack;

import ch.njol.skript.Skript;
import ch.njol.skript.aliases.ItemType;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Examples;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import ch.njol.skript.lang.Effect;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.skript.lang.Testable;
import ch.njol.util.Kleenean;

/**
 * @author Peter Güttinger
 */
@SuppressWarnings("serial")
@Name("Equip")
@Description("Equips a player with some given armor. This will replace any armor that the player is wearing.")
@Examples({"equip player with diamond helmet",
		"equip player with all diamond armor"})
@Since("1.0")
public class EffEquip extends Effect implements Testable {
	
	static {
		Skript.registerEffect(EffEquip.class,
				"equip [%players%] with %itemtypes%",
				"make %players% wear %itemtypes%");
	}
	
	private Expression<Player> players;
	private Expression<ItemType> types;
	
	@SuppressWarnings("unchecked")
	@Override
	public boolean init(final Expression<?>[] vars, final int matchedPattern, final Kleenean isDelayed, final ParseResult parser) {
		players = (Expression<Player>) vars[0];
		types = (Expression<ItemType>) vars[1];
		return true;
	}
	
	@Override
	public String toString(final Event e, final boolean debug) {
		return "equip " + players.toString(e, debug) + " with " + types.toString(e, debug);
	}
	
	@SuppressWarnings("deprecation")
	@Override
	protected void execute(final Event e) {
		final ItemType[] ts = types.getArray(e);
		for (final Player p : players.getArray(e)) {
			for (final ItemType t : ts) {
				for (final ItemStack item : t.getBlock().getAll()) {
					switch (item.getType()) {
						case LEATHER_BOOTS:
						case IRON_BOOTS:
						case GOLD_BOOTS:
						case CHAINMAIL_BOOTS:
						case DIAMOND_BOOTS:
							p.getInventory().setBoots(item);
							break;
						case LEATHER_LEGGINGS:
						case IRON_LEGGINGS:
						case GOLD_LEGGINGS:
						case CHAINMAIL_LEGGINGS:
						case DIAMOND_LEGGINGS:
							p.getInventory().setLeggings(item);
							break;
						case LEATHER_CHESTPLATE:
						case IRON_CHESTPLATE:
						case GOLD_CHESTPLATE:
						case CHAINMAIL_CHESTPLATE:
						case DIAMOND_CHESTPLATE:
							p.getInventory().setChestplate(item);
							break;
						default:
							if (!(item.getType().isBlock() || item.getTypeId() == 397 /* mob head */))
								continue;
							//$FALL-THROUGH$
						case LEATHER_HELMET:
						case IRON_HELMET:
						case GOLD_HELMET:
						case CHAINMAIL_HELMET:
						case DIAMOND_HELMET:
							p.getInventory().setHelmet(item);
					}
				}
			}
			p.updateInventory();
		}
	}
	
	@Override
	public boolean test(final Event e) {
//		final Iterable<Player> ps = players.getArray(e);
//		for (final ItemType t : types.getArray(e)) {
//			for (final Player p : ps) {
//				//TODO this + think...
//			}
//		}
		return false;
	}
	
}
