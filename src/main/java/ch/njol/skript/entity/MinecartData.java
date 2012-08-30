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

import org.bukkit.entity.Minecart;
import org.bukkit.entity.PoweredMinecart;
import org.bukkit.entity.StorageMinecart;

import ch.njol.skript.lang.Literal;
import ch.njol.skript.lang.SkriptParser.ParseResult;

/**
 * @author Peter Güttinger
 */
public class MinecartData extends EntityData<Minecart> {
	
	private static enum MinecartType {
		ANY(Minecart.class, "minecart", "[mine]cart[s]"),
		NORMAL(Minecart.class, "regular minecart", "(normal|regular) [mine]cart[s]"),
		STORAGE(StorageMinecart.class, "storage minecart", "storage [mine]cart[s]"),
		POWERED(PoweredMinecart.class, "powered minecart", "powered [mine]cart[s]");
		
		final Class<? extends Minecart> c;
		final String name;
		final String pattern;
		
		MinecartType(final Class<? extends Minecart> c, final String name, final String pattern) {
			this.c = c;
			this.name = name;
			this.pattern = pattern;
		}
		
		static String[] patterns = new String[values().length];
		static MinecartType[] byPattern = values();
		static {
			for (int i = 0; i < byPattern.length; i++) {
				patterns[i] = byPattern[i].pattern;
			}
		}
		
	}
	
	static {
		register(MinecartData.class, "minecart", Minecart.class, MinecartType.patterns);
	}
	
	private MinecartType type = MinecartType.ANY;
	
	private boolean plural;
	
	@Override
	protected boolean init(final Literal<?>[] exprs, final int matchedPattern, final ParseResult parseResult) {
		type = MinecartType.byPattern[matchedPattern];
		plural = parseResult.expr.endsWith("s");
		return true;
	}
	
	@Override
	public void set(final Minecart entity) {}
	
	@Override
	public boolean match(final Minecart entity) {
		if (type == MinecartType.NORMAL)
			return !(entity instanceof PoweredMinecart || entity instanceof StorageMinecart);
		return type.c.isInstance(entity);
	}
	
	@Override
	public Class<? extends Minecart> getType() {
		return type.c;
	}
	
	@Override
	public String toString() {
		return type.name;
	}
	
	@Override
	public boolean isPlural() {
		return plural;
	}
}
