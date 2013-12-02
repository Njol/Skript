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

package ch.njol.skript.hooks.regions;

import java.io.StreamCorruptedException;
import java.util.Arrays;
import java.util.Collection;
import java.util.Iterator;

import me.ryanhamshire.GriefPrevention.Claim;
import me.ryanhamshire.GriefPrevention.GriefPrevention;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.OfflinePlayer;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;

import ch.njol.skript.hooks.regions.classes.Region;
import ch.njol.skript.util.AABB;
import ch.njol.yggdrasil.Fields;

/**
 * @author Peter Güttinger
 */
public class GriefPreventionHook extends RegionsPlugin<GriefPrevention> {
	
	@Override
	public String getName() {
		return "GriefPrevention";
	}
	
	@Override
	public boolean canBuild_i(final Player p, final Location l) {
		return plugin.allowBuild(p, l) == null; // returns reason string if not allowed to build
	}
	
	public final class GriefPreventionRegion extends Region {
		
		private transient Claim c;
		
		public GriefPreventionRegion(final Claim c) {
			this.c = c;
		}
		
		@Override
		public boolean contains(final Location l) {
			return c.contains(l, false, false);
		}
		
		@Override
		public boolean isMember(final OfflinePlayer p) {
			return isOwner(p);
		}
		
		@Override
		public Collection<OfflinePlayer> getMembers() {
			return getOwners();
		}
		
		@Override
		public boolean isOwner(final OfflinePlayer p) {
			return p.getName().equalsIgnoreCase(c.getOwnerName());
		}
		
		@Override
		public Collection<OfflinePlayer> getOwners() {
			return Arrays.asList(Bukkit.getOfflinePlayer(c.getOwnerName()));
		}
		
		@Override
		public Iterator<Block> getBlocks() {
			final Location upper = c.getGreaterBoundaryCorner();
			upper.setY(upper.getWorld().getMaxHeight());
			upper.setX(upper.getBlockX() + 1);
			upper.setZ(upper.getBlockZ() + 1);
			return new AABB(c.getLesserBoundaryCorner(), upper).iterator();
		}
		
		@Override
		public String toString() {
			return "Claim #" + c.getID();
		}
		
		@Override
		public Fields serialize() {
			final Fields f = new Fields();
			f.putPrimitive("id", c.getID());
			return f;
		}
		
		@Override
		public void deserialize(final Fields fields) throws StreamCorruptedException {
			final long id = fields.getPrimitive("id", long.class);
			c = plugin.dataStore.getClaim(id);
			if (c == null)
				throw new StreamCorruptedException("Invalid claim " + id);
		}
		
		@Override
		public RegionsPlugin<?> getPlugin() {
			return GriefPreventionHook.this;
		}
		
		@Override
		public boolean equals(final Object o) {
			if (o == this)
				return true;
			if (o == null)
				return false;
			if (!(o instanceof GriefPreventionRegion))
				return false;
			return c.equals(((GriefPreventionRegion) o).c);
		}
		
		@Override
		public int hashCode() {
			return c.hashCode();
		}
		
	}
	
	@Override
	public Collection<? extends Region> getRegionsAt_i(final Location l) {
		final Claim c = plugin.dataStore.getClaimAt(l, false, null);
		if (c != null)
			return Arrays.asList(new GriefPreventionRegion(c));
		return null;
	}
	
	@Override
	public Region getRegion_i(final World world, final String name) {
		try {
			final Claim c = plugin.dataStore.getClaim(Long.parseLong(name));
			if (c != null && world.equals(c.getLesserBoundaryCorner().getWorld()))
				return new GriefPreventionRegion(c);
			return null;
		} catch (final NumberFormatException e) {
			return null;
		}
	}
	
	@Override
	public boolean hasMultipleOwners_i() {
		return false;
	}
	
	@Override
	protected Class<? extends Region> getRegionClass() {
		return GriefPreventionRegion.class;
	}
}
