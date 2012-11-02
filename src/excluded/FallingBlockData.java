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

import org.bukkit.Location;
import org.bukkit.inventory.ItemStack;

import ch.njol.skript.Skript;
import ch.njol.skript.lang.Literal;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.skript.util.ItemType;

/**
 * @author Peter Güttinger
 *
 */
public class FallingBlockData extends EntityData<FallingBlock> {

	static {
		register(FallingBlockData.class, "fallingblock", FallingBlock.class, "falling block[s]", "falling %itemtype%");
	}
	
	private Literal<ItemType> type;
	
	private boolean plural;
	
	@SuppressWarnings("unchecked")
	@Override
	protected boolean init(Literal<?>[] exprs, int matchedPattern, ParseResult parseResult) {
		type = exprs.length == 0 ? null : (Literal<ItemType>) exprs[0];
		plural = matchedPattern == 0 && parseResult.expr.endsWith("s");
		return true;
	}

	@Override
	public void set(FallingBlock entity) {
		
	}
	
	@Override
	public FallingBlock spawn(Location loc) {
		ItemStack type = this.type.getSingle().getBlock().getRandom();
		if (type.getTypeId() > Skript.MAXBLOCKID)
			return null;
		return loc.getWorld().spawnFallingBlock(loc, type.getTypeId(), (byte) type.getDurability());
	}

	@Override
	protected boolean match(FallingBlock entity) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public Class<? extends FallingBlock> getType() {
		return FallingBlock.class;
	}

	@Override
	public String toString() {
		return "falling "+type;
	}

	@Override
	public boolean isPlural() {
		return plural;
	}
	
}
