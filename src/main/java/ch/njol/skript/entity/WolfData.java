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

import org.bukkit.entity.Wolf;

import ch.njol.skript.lang.Literal;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.util.StringUtils;

/**
 * @author Peter Güttinger
 */
public class WolfData extends EntityData<Wolf> {
	private static final long serialVersionUID = -5534357579840376057L;
	
	private int angry = 0;
//	private String owner = null;
	private int tamed = 0;
	
	private boolean plural;
	
	static {
		EntityData.register(WolfData.class, "wolf", Wolf.class,
				"angry wol(f|ves)", "wol(f|ves)", "(peaceful|non-angry) wol(f|ves)",
				"(wild|untamed|unowned) wol(f|ves)", "(tamed|owned) wol(f|ves)");
	}
	
	@Override
	protected boolean init(final Literal<?>[] exprs, final int matchedPattern, final ParseResult parseResult) {
		if (matchedPattern <= 2)
			angry = matchedPattern - 1;
		else
			tamed = matchedPattern == 3 ? -1 : 1;
		plural = StringUtils.endsWithIgnoreCase(parseResult.expr, "ves");
		return true;
	}
	
	@Override
	public void set(final Wolf entity) {
		if (angry != 0)
			entity.setAngry(angry == 1);
		if (tamed != 0)
			entity.setTamed(tamed == 1);
//		if (owner != null) {
//			if (owner.isEmpty())
//				entity.setOwner(null);
//			else
//				entity.setOwner(Bukkit.getOfflinePlayer(owner));
//		}
	}
	
	@Override
	public boolean match(final Wolf entity) {
		return (angry == 0 || entity.isAngry() == (angry == 1)) && (tamed == 0 || entity.isTamed() == (tamed == 1));
//				&& (owner == null || owner.isEmpty() && entity.getOwner() == null || entity.getOwner() != null && entity.getOwner().getName().equalsIgnoreCase(owner));
	}
	
	@Override
	public Class<Wolf> getType() {
		return Wolf.class;
	}
	
	@Override
	public String toString() {
		return (angry == 1 ? "angry " : angry == -1 ? "non-angry " : "") + (tamed == 1 ? "tamed " : tamed == -1 ? "untamed " : "") + "wolf";
	}
	
	@Override
	public boolean isPlural() {
		return plural;
	}
	
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + angry;
		result = prime * result + tamed;
		return result;
	}
	
	@Override
	public boolean equals(final Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (!(obj instanceof WolfData))
			return false;
		final WolfData other = (WolfData) obj;
		if (angry != other.angry)
			return false;
		if (tamed != other.tamed)
			return false;
		return true;
	}
	
	@Override
	public String serialize() {
		return angry + "|" + tamed;
	}
	
	@Override
	protected boolean deserialize(final String s) {
		final String[] split = s.split("\\|");
		if (split.length != 2)
			return false;
		try {
			angry = Integer.parseInt(split[0]);
			tamed = Integer.parseInt(split[1]);
			return true;
		} catch (final NumberFormatException e) {
			return false;
		}
	}
}
