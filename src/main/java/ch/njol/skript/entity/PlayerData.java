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

package ch.njol.skript.entity;

import org.bukkit.Location;
import org.bukkit.entity.Player;

import ch.njol.skript.lang.Literal;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.util.StringUtils;

/**
 * @author Peter Güttinger
 */
@SuppressWarnings("serial")
public class PlayerData extends EntityData<Player> {
	
	static {
		EntityData.register(PlayerData.class, "player", Player.class, "non-op[s]", "player[s]", "op[s]");
	}
	
	// used by EntityData.getAll
	int op = 0;
	
	private boolean plural;
	
	@Override
	protected boolean init(final Literal<?>[] exprs, final int matchedPattern, final ParseResult parseResult) {
		op = matchedPattern - 1;
		plural = StringUtils.endsWithIgnoreCase(parseResult.expr, "s");
		return true;
	}
	
	@Override
	public void set(final Player p) {
		if (op != 0)
			p.setOp(op == 1);
	}
	
	@Override
	protected boolean match(final Player p) {
		return op == 0 || p.isOp() == (op == 1);
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
	
	@Override
	public boolean isPlural() {
		return plural;
	}
	
	@Override
	public int hashCode() {
		return op;
	}
	
	@Override
	public boolean equals(final Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (!(obj instanceof PlayerData))
			return false;
		final PlayerData other = (PlayerData) obj;
		return op == other.op;
	}
	
	@Override
	public String serialize() {
		return "" + op;
	}
	
	@Override
	protected boolean deserialize(final String s) {
		try {
			op = Integer.parseInt(s);
			return true;
		} catch (final NumberFormatException e) {
			return false;
		}
	}
	
	@Override
	protected boolean isSupertypeOf_i(final EntityData<? extends Player> e) {
		if (e instanceof PlayerData)
			return op == 0 || ((PlayerData) e).op == op;
		return false;
	}
	
}
