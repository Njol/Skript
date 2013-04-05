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

import org.bukkit.entity.Minecart;
import org.bukkit.entity.PoweredMinecart;
import org.bukkit.entity.StorageMinecart;
import org.bukkit.entity.minecart.ExplosiveMinecart;
import org.bukkit.entity.minecart.HopperMinecart;
import org.bukkit.entity.minecart.RideableMinecart;
import org.bukkit.entity.minecart.SpawnerMinecart;

import ch.njol.skript.Skript;
import ch.njol.skript.lang.Literal;
import ch.njol.skript.lang.SkriptParser.ParseResult;

/**
 * @author Peter Güttinger
 */
@SuppressWarnings({"serial", "deprecation"})
public class MinecartData extends EntityData<Minecart> {
	
	private static enum MinecartType {
		ANY(Minecart.class, "minecart", "[mine]cart(1¦s|)"),
		NORMAL(Skript.isRunningMinecraft(1, 5) ? RideableMinecart.class : Minecart.class, "regular minecart", "(normal|regular) [mine]cart(1¦s|)"),
		STORAGE(Skript.isRunningMinecraft(1, 5) ? org.bukkit.entity.minecart.StorageMinecart.class : StorageMinecart.class, "storage minecart", "(storage [mine]cart(1¦s|)|minecart(1¦s|) with chest[s])"),
		POWERED(Skript.isRunningMinecraft(1, 5) ? org.bukkit.entity.minecart.PoweredMinecart.class : PoweredMinecart.class, "powered minecart", "(powered [mine]cart(1¦s|)|minecart(1¦s|) with furnace[s])"),
		// 1.5
		HOPPER(Skript.isRunningMinecraft(1, 5) ? HopperMinecart.class : null, "hopper minecart", "(hopper [mine]cart(1¦s|)|minecart(1¦s|) with hopper[s])"),
		EXPLOSIVE(Skript.isRunningMinecraft(1, 5) ? ExplosiveMinecart.class : null, "explosive minecart", "((explosive|TNT) [mine]cart(1¦s|)|minecart(1¦s|) with TNT[s])"),
		SPAWNER(Skript.isRunningMinecraft(1, 5) ? SpawnerMinecart.class : null, "spawner minecart", "(spawner [mine]cart(1¦s|)|minecart(1¦s|) with (mob|monster|) spawner[s])");
		
		final Class<? extends Minecart> c;
		private final String name;
		final String pattern;
		
		MinecartType(final Class<? extends Minecart> c, final String name, final String pattern) {
			this.c = c;
			this.name = name;
			this.pattern = pattern;
		}
		
		static String[] patterns = new String[values().length - (Skript.isRunningMinecraft(1, 5) ? 0 : 3)];
		static MinecartType[] byPattern = values();
		static {
			for (int i = 0; i < patterns.length; i++) {
				patterns[i] = byPattern[i].pattern;
			}
		}
		
		@Override
		public String toString() {
			return name;
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
		plural = parseResult.mark == 1;
		return true;
	}
	
	@Override
	public void set(final Minecart entity) {}
	
	@Override
	public boolean match(final Minecart entity) {
		if (type == MinecartType.NORMAL && type.c == Minecart.class) // pre-1.5
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
	
	@Override
	public int hashCode() {
		return type.hashCode();
	}
	
	@Override
	public boolean equals(final Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (!(obj instanceof MinecartData))
			return false;
		final MinecartData other = (MinecartData) obj;
		return type == other.type;
	}
	
	@Override
	public String serialize() {
		return type.name();
	}
	
	@Override
	protected boolean deserialize(final String s) {
		try {
			type = MinecartType.valueOf(s);
			return true;
		} catch (final IllegalArgumentException e) {
			return false;
		}
	}
	
	@Override
	protected boolean isSupertypeOf_i(final EntityData<? extends Minecart> e) {
		if (e instanceof MinecartData)
			return type == MinecartType.ANY || ((MinecartData) e).type == type;
		return false;
	}
	
}
