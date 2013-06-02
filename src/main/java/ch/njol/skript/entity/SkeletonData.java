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

import org.bukkit.entity.Skeleton;
import org.bukkit.entity.Skeleton.SkeletonType;

import ch.njol.skript.Skript;
import ch.njol.skript.lang.Literal;
import ch.njol.skript.lang.SkriptParser.ParseResult;

/**
 * @author Peter Güttinger
 */
@SuppressWarnings("serial")
public class SkeletonData extends EntityData<Skeleton> {
	
	static {
		if (Skript.isRunningMinecraft(1, 4))
			register(SkeletonData.class, "skeleton", Skeleton.class, "skeleton", "wither skeleton");
		else
			register(SkeletonData.class, "skeleton", Skeleton.class, "skeleton");
	}
	
	private boolean wither;
	
	public boolean isWither() {
		return wither;
	}
	
	@Override
	protected boolean init(final Literal<?>[] exprs, final int matchedPattern, final ParseResult parseResult) {
		wither = matchedPattern == 1;
		return true;
	}
	
	@Override
	public String serialize() {
		return wither ? "1" : "0";
	}
	
	@Override
	protected boolean deserialize(final String s) {
		if (s.equals("1"))
			wither = true;
		else if (s.equals("0"))
			wither = false;
		else
			return false;
		return true;
	}
	
	@Override
	public void set(final Skeleton entity) {
		entity.setSkeletonType(wither ? SkeletonType.WITHER : SkeletonType.NORMAL);
	}
	
	@Override
	protected boolean match(final Skeleton entity) {
		return (entity.getSkeletonType() == SkeletonType.WITHER) == wither;
	}
	
	@Override
	public Class<? extends Skeleton> getType() {
		return Skeleton.class;
	}
	
	@Override
	public boolean equals(final Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (!(obj instanceof SkeletonData))
			return false;
		final SkeletonData other = (SkeletonData) obj;
		return other.wither == wither;
	}
	
	@Override
	public int hashCode() {
		return wither ? 1 : 0;
	}
	
	@Override
	protected boolean isSupertypeOf_i(final EntityData<? extends Skeleton> e) {
		if (e instanceof SkeletonData)
			return ((SkeletonData) e).wither == wither;
		return false;
	}
	
}
