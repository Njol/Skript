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

package ch.njol.skript.entity;

import org.bukkit.entity.Enderman;

import ch.njol.skript.lang.Literal;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.skript.util.ItemType;
import ch.njol.util.Checker;

/**
 * @author Peter Güttinger
 * 
 */
public class EndermanData extends EntityData<Enderman> {
	
	static {
		EntityData.register(EndermanData.class, "enderman", Enderman.class, "enderm(a|e)n [(carrying|holding) %-itemtypes%]");
	}
	
	private Literal<ItemType> hand = null;
	
	@SuppressWarnings("unchecked")
	@Override
	protected boolean init(final Literal<?>[] exprs, final int matchedPattern, final ParseResult parseResult) {
		hand = (Literal<ItemType>) exprs[0];
		return true;
	}
	
	@Override
	public void set(final Enderman entity) {
		if (hand != null)
			entity.setCarriedMaterial(hand.getSingle().getBlock().getRandom().getData());
	}
	
	@Override
	public boolean match(final Enderman entity) {
		return hand == null || hand.check(null, new Checker<ItemType>() {
			@Override
			public boolean check(final ItemType t) {
				return t.isOfType(entity.getCarriedMaterial().getItemTypeId(), entity.getCarriedMaterial().getData());
			}
		});
	}
	
	@Override
	public Class<Enderman> getType() {
		return Enderman.class;
	}
	
	@Override
	public String toString() {
		return "enderman carrying " + hand;
	}
	
}
