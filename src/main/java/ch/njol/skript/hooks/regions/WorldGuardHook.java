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

package ch.njol.skript.hooks.regions;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.OfflinePlayer;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;

import ch.njol.skript.hooks.regions.classes.Region;
import ch.njol.util.coll.iterator.EmptyIterator;

import com.sk89q.worldedit.BlockVector2D;
import com.sk89q.worldguard.bukkit.WorldGuardPlugin;
import com.sk89q.worldguard.protection.regions.ProtectedRegion;

/**
 * @author Peter Güttinger
 */
public class WorldGuardHook extends RegionsPlugin<WorldGuardPlugin> {
	
	@Override
	public String getName() {
		return "WorldGuard";
	}
	
	@Override
	public boolean canBuild_i(final Player p, final Location l) {
		return plugin.canBuild(p, l);
	}
	
	public final class WorldGuardRegion extends Region {
		
		private final World w;
		private final ProtectedRegion r;
		
		public WorldGuardRegion(final World w, final ProtectedRegion r) {
			this.w = w;
			this.r = r;
		}
		
		@Override
		public boolean contains(final Location l) {
			return l.getWorld().equals(w) && r.contains(l.getBlockX(), l.getBlockY(), l.getBlockZ());
		}
		
		@Override
		public boolean isMember(final OfflinePlayer p) {
			return r.isMember(p.getName());
		}
		
		@Override
		public Collection<OfflinePlayer> getMembers() {
			final Collection<String> ps = r.getMembers().getPlayers();
			final Collection<OfflinePlayer> r = new ArrayList<OfflinePlayer>(ps.size());
			for (final String p : ps)
				r.add(Bukkit.getOfflinePlayer(p));
			return r;
		}
		
		@Override
		public boolean isOwner(final OfflinePlayer p) {
			return r.isOwner(p.getName());
		}
		
		@Override
		public Collection<OfflinePlayer> getOwners() {
			final Collection<String> ps = r.getOwners().getPlayers();
			final Collection<OfflinePlayer> r = new ArrayList<OfflinePlayer>(ps.size());
			for (final String p : ps)
				r.add(Bukkit.getOfflinePlayer(p));
			return r;
		}
		
		@Override
		public Iterator<Block> getBlocks() {
			final Iterator<BlockVector2D> iter = r.getPoints().iterator();
			if (!iter.hasNext())
				return EmptyIterator.get();
			return new Iterator<Block>() {
				BlockVector2D current = iter.next();
				int height = 0;
				final int maxHeight = w.getMaxHeight();
				
				@Override
				public boolean hasNext() {
					if (height >= maxHeight && iter.hasNext()) {
						height = 0;
						current = iter.next();
					}
					return height < maxHeight;
				}
				
				@Override
				public Block next() {
					return w.getBlockAt(current.getBlockX(), height, current.getBlockZ());
				}
				
				@Override
				public void remove() {
					throw new UnsupportedOperationException();
				}
			};
		}
		
		@Override
		public String serialize() {
			return w.getName() + ":" + r.getId();
		}
		
		@Override
		public String toString() {
			return r.getId() + " in world " + w.getName();
		}
		
		@Override
		public RegionsPlugin<?> getPlugin() {
			return WorldGuardHook.this;
		}
		
		@Override
		public boolean equals(final Object o) {
			if (o == this)
				return true;
			if (o == null)
				return false;
			if (!(o instanceof WorldGuardRegion))
				return false;
			return w.equals(((WorldGuardRegion) o).w) && r.equals(((WorldGuardRegion) o).r);
		}
		
		@Override
		public int hashCode() {
			return w.hashCode() ^ r.hashCode();
		}
		
	}
	
	@Override
	public Collection<? extends Region> getRegionsAt_i(final Location l) {
		final Iterator<ProtectedRegion> i = plugin.getRegionManager(l.getWorld()).getApplicableRegions(l).iterator();
		final ArrayList<Region> r = new ArrayList<Region>();
		while (i.hasNext())
			r.add(new WorldGuardRegion(l.getWorld(), i.next()));
		return r;
	}
	
	@Override
	public Region getRegion_i(final World world, final String name) {
		final ProtectedRegion r = plugin.getRegionManager(world).getRegion(name);
		if (r != null)
			return new WorldGuardRegion(world, r);
		return null;
	}
	
	@Override
	public boolean hasMultipleOwners_i() {
		return true;
	}
	
	@Override
	protected Region deserializeRegion_i(final String s) {
		final String[] split = s.split(":", 2);
		final World w = Bukkit.getWorld(split[0]);
		if (w == null)
			return null;
		final ProtectedRegion r = plugin.getRegionManager(w).getRegionExact(split[1]);
		if (r == null)
			return null;
		return new WorldGuardRegion(w, r);
	}
	
}
