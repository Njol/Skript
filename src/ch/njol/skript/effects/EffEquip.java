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

package ch.njol.skript.effects;

import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.inventory.ItemStack;

import ch.njol.skript.Skript;
import ch.njol.skript.lang.Effect;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.skript.lang.Testable;
import ch.njol.skript.util.ItemType;

/**
 * @author Peter Güttinger
 * 
 */
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
	public boolean init(final Expression<?>[] vars, final int matchedPattern, final boolean isDelayed, final ParseResult parser) {
		players = (Expression<Player>) vars[0];
		types = (Expression<ItemType>) vars[1];
		return true;
	}
	
	@Override
	public String toString(final Event e, final boolean debug) {
		return "equip " + players.toString(e, debug) + " with " + types.toString(e, debug);
	}
	
	@Override
	protected void execute(final Event e) {
		final Player[] ps = players.getArray(e);
		for (final ItemType t : types.getArray(e)) {
			for (final ItemStack item : t.getBlock().getAll()) {
				switch (item.getType()) {
					case LEATHER_BOOTS:
					case IRON_BOOTS:
					case GOLD_BOOTS:
					case DIAMOND_BOOTS:
						for (final Player p : ps) {
							p.getInventory().setBoots(item);
						}
					break;
					case LEATHER_LEGGINGS:
					case IRON_LEGGINGS:
					case GOLD_LEGGINGS:
					case DIAMOND_LEGGINGS:
						for (final Player p : ps) {
							p.getInventory().setLeggings(item);
						}
					break;
					case LEATHER_CHESTPLATE:
					case IRON_CHESTPLATE:
					case GOLD_CHESTPLATE:
					case DIAMOND_CHESTPLATE:
						for (final Player p : ps) {
							p.getInventory().setChestplate(item);
						}
					break;
					default:
						if (!item.getType().isBlock())
							continue;
						//$FALL-THROUGH$
					case LEATHER_HELMET:
					case IRON_HELMET:
					case GOLD_HELMET:
					case DIAMOND_HELMET:
						for (final Player p : ps) {
							p.getInventory().setHelmet(item);
						}
				}
			}
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
