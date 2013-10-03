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

package ch.njol.skript.hooks.regions.classes;

import java.util.Collection;
import java.util.Iterator;

import org.bukkit.Location;
import org.bukkit.OfflinePlayer;
import org.bukkit.block.Block;

import ch.njol.skript.classes.ClassInfo;
import ch.njol.skript.classes.Parser;
import ch.njol.skript.classes.Serializer;
import ch.njol.skript.hooks.regions.RegionsPlugin;
import ch.njol.skript.lang.ParseContext;
import ch.njol.skript.registrations.Classes;

/**
 * @author Peter Güttinger
 */
public abstract class Region {
	static {
		Classes.registerClass(new ClassInfo<Region>(Region.class, "region")
				.name("Region")
				.parser(new Parser<Region>() {
					@Override
					public Region parse(final String s, final ParseContext context) {
						return null;
					}
					
					@Override
					public String toString(final Region r, final int flags) {
						return r.toString();
					}
					
					@Override
					public String toVariableNameString(final Region r) {
						return RegionsPlugin.serializeRegion(r);
					}
					
					@Override
					public String getVariableNamePattern() {
						return ".+";
					}
				})
				.serializer(new Serializer<Region>() {
					@Override
					public String serialize(final Region r) {
						return RegionsPlugin.serializeRegion(r);
					}
					
					@Override
					public Region deserialize(final String s) {
						return RegionsPlugin.deserializeRegion(s);
					}
					
					@Override
					public boolean mustSyncDeserialization() {
						return true;
					}
				}));
	}
	
	public abstract boolean contains(Location l);
	
	public abstract boolean isMember(OfflinePlayer p);
	
	public abstract Collection<OfflinePlayer> getMembers();
	
	public abstract boolean isOwner(OfflinePlayer p);
	
	public abstract Collection<OfflinePlayer> getOwners();
	
	public abstract Iterator<Block> getBlocks();
	
	@Override
	public abstract String toString();
	
	public abstract String serialize();
	
	public abstract RegionsPlugin<?> getPlugin();
	
	@Override
	public abstract boolean equals(Object o);
	
	@Override
	public abstract int hashCode();
	
}
