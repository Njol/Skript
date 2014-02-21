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

import java.io.IOException;
import java.io.StreamCorruptedException;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;

import me.ryanhamshire.GriefPrevention.Claim;
import me.ryanhamshire.GriefPrevention.GriefPrevention;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.OfflinePlayer;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.eclipse.jdt.annotation.Nullable;

import ch.njol.skript.hooks.regions.classes.Region;
import ch.njol.skript.util.AABB;
import ch.njol.util.coll.iterator.EmptyIterator;
import ch.njol.yggdrasil.Fields;

/**
 * @author Peter Güttinger
 */
public class GriefPreventionHook extends RegionsPlugin<GriefPrevention> {
	
	public GriefPreventionHook() throws IOException {}
	
	@Override
	public String getName() {
		return "GriefPrevention";
	}
	
	@Override
	public boolean canBuild_i(final Player p, final Location l) {
		return plugin.allowBuild(p, l) == null; // returns reason string if not allowed to build
	}
	
	public final class GriefPreventionRegion extends Region {
		
		private transient Claim claim;
		
		public GriefPreventionRegion(final Claim c) {
			claim = c;
		}
		
		@Override
		public boolean contains(final Location l) {
			return claim.contains(l, false, false);
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
			return p.getName().equalsIgnoreCase(claim.getOwnerName());
		}
		
		@SuppressWarnings("null")
		@Override
		public Collection<OfflinePlayer> getOwners() {
			return Arrays.asList(Bukkit.getOfflinePlayer(claim.getOwnerName()));
		}
		
		@Override
		public Iterator<Block> getBlocks() {
			final Location lower = claim.getLesserBoundaryCorner(), upper = claim.getGreaterBoundaryCorner();
			if (lower == null || upper == null || lower.getWorld() == null || upper.getWorld() == null || lower.getWorld() != upper.getWorld())
				return EmptyIterator.get();
			upper.setY(upper.getWorld().getMaxHeight());
			upper.setX(upper.getBlockX() + 1);
			upper.setZ(upper.getBlockZ() + 1);
			return new AABB(lower, upper).iterator();
		}
		
		@Override
		public String toString() {
			return "Claim #" + claim.getID();
		}
		
		@SuppressWarnings("null")
		@Override
		public Fields serialize() {
			final Fields f = new Fields();
			f.putPrimitive("id", claim.getID());
			return f;
		}
		
		@Override
		public void deserialize(final Fields fields) throws StreamCorruptedException {
			final long id = fields.getPrimitive("id", long.class);
			final Claim c = plugin.dataStore.getClaim(id);
			if (c == null)
				throw new StreamCorruptedException("Invalid claim " + id);
			claim = c;
		}
		
		@Override
		public RegionsPlugin<?> getPlugin() {
			return GriefPreventionHook.this;
		}
		
		@Override
		public boolean equals(final @Nullable Object o) {
			if (o == this)
				return true;
			if (o == null)
				return false;
			if (!(o instanceof GriefPreventionRegion))
				return false;
			return claim.equals(((GriefPreventionRegion) o).claim);
		}
		
		@Override
		public int hashCode() {
			return claim.hashCode();
		}
		
	}
	
	@SuppressWarnings("null")
	@Override
	public Collection<? extends Region> getRegionsAt_i(final Location l) {
		final Claim c = plugin.dataStore.getClaimAt(l, false, null);
		if (c != null)
			return Arrays.asList(new GriefPreventionRegion(c));
		return Collections.emptySet();
	}
	
	@Override
	@Nullable
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
