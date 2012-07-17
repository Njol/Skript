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
import org.bukkit.entity.Player;

import ch.njol.skript.lang.Literal;
import ch.njol.skript.lang.SkriptParser.ParseResult;

/**
 * @author Peter Güttinger
 * 
 */
public class PlayerData extends EntityData<Player> {
	
	static {
		EntityData.register(PlayerData.class, "player", Player.class, "non-op[s]", "player[s]", "op[s]");
	}
	
	private int op = 0;
	
	@Override
	protected boolean init(final Literal<?>[] exprs, final int matchedPattern, final ParseResult parseResult) {
		op = matchedPattern - 1;
		return true;
	}
	
	@Override
	protected void set(final Player entity) {
		if (op != 0)
			entity.setOp(op == 1);
	}
	
	@Override
	protected boolean match(final Player entity) {
		return op == 0 || entity.isOp() == (op == 1);
	}
	
	@Override
	public Class<? extends Player> getType() {
		return Player.class;
	}
	
	@Override
	public String toString() {
		return op == -1 ? "non-op" : op == 1 ? "op" : "player";
	}
	
	@Override
	public Player spawn(final Location loc) {
		return null;
	}
	
}
