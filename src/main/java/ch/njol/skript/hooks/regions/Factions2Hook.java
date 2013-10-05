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
import java.util.Arrays;
import java.util.Collection;
import java.util.Iterator;
import java.util.NoSuchElementException;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.OfflinePlayer;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;

import ch.njol.skript.hooks.regions.classes.Region;
import ch.njol.skript.util.AABB;
import ch.njol.util.coll.iterator.EmptyIterator;

import com.massivecraft.factions.Factions;
import com.massivecraft.factions.entity.BoardColls;
import com.massivecraft.factions.entity.Faction;
import com.massivecraft.factions.entity.FactionColls;
import com.massivecraft.factions.entity.UPlayer;
import com.massivecraft.factions.listeners.FactionsListenerMain;
import com.massivecraft.mcore.ps.PS;

/**
 * @author Peter Güttinger
 */
public class Factions2Hook extends RegionsPlugin<Factions> {
	
	@Override
	public String getName() {
		return "Factions";
	}
	
	@Override
	protected boolean init() {
		if (!plugin.getClass().getName().equals("com.massivecraft.factions.Factions"))
			return false;
		super.init();
		return true;
	}
	
	@Override
	public boolean canBuild_i(final Player p, final Location l) {
		return FactionsListenerMain.canPlayerBuildAt(p, PS.valueOf(l), false);
	}
	
	public final class FactionsRegion extends Region {
		
		private final Faction f;
		
		public FactionsRegion(final Faction f) {
			this.f = f;
		}
		
		@Override
		public boolean contains(final Location l) {
			return BoardColls.get().getFactionAt(PS.valueOf(l)) == f;
		}
		
		@Override
		public boolean isMember(final OfflinePlayer p) {
			for (final UPlayer up : f.getUPlayers()) {
				if (up.getName().equalsIgnoreCase(p.getName()))
					return true;
			}
			return false;
		}
		
		@Override
		public Collection<OfflinePlayer> getMembers() {
			final Collection<UPlayer> ps = f.getUPlayers();
			final Collection<OfflinePlayer> r = new ArrayList<OfflinePlayer>(ps.size());
			for (final UPlayer p : ps)
				r.add(Bukkit.getOfflinePlayer(p.getName()));
			return r;
		}
		
		@Override
		public boolean isOwner(final OfflinePlayer p) {
			return f.getLeader().getName().equalsIgnoreCase(p.getName());
		}
		
		@Override
		public Collection<OfflinePlayer> getOwners() {
			return Arrays.asList(Bukkit.getOfflinePlayer(f.getLeader().getName()));
		}
		
		@Override
		public Iterator<Block> getBlocks() {
			final Iterator<PS> cs = BoardColls.get().get(f).getChunks(f).iterator();
			if (!cs.hasNext())
				return EmptyIterator.get();
			return new Iterator<Block>() {
				Iterator<Block> current = new AABB(cs.next().asBukkitChunk()).iterator();
				
				@Override
				public boolean hasNext() {
					if (!current.hasNext() && cs.hasNext()) {
						current = new AABB(cs.next().asBukkitChunk()).iterator();
					}
					return current.hasNext();
				}
				
				@Override
				public Block next() {
					if (!hasNext())
						throw new NoSuchElementException();
					return null;
				}
				
				@Override
				public void remove() {
					throw new UnsupportedOperationException();
				}
				
			};
		}
		
		@Override
		public String toString() {
			return f.getName();
		}
		
		@Override
		public String serialize() {
			return f.getUniverse() + ":" + f.getId();
		}
		
		@Override
		public RegionsPlugin<?> getPlugin() {
			return Factions2Hook.this;
		}
		
		@Override
		public boolean equals(final Object o) {
			if (o == this)
				return true;
			if (o == null)
				return false;
			if (!(o instanceof FactionsRegion))
				return false;
			return f.equals(((FactionsRegion) o).f);
		}
		
		@Override
		public int hashCode() {
			return f.hashCode();
		}
		
	}
	
	@Override
	public Collection<? extends Region> getRegionsAt_i(final Location l) {
		return Arrays.asList(new FactionsRegion(BoardColls.get().getFactionAt(PS.valueOf(l))));
	}
	
	@Override
	public Region getRegion_i(final World world, final String name) {
		final Faction f = FactionColls.get().getForUniverse(world.getName()).getByName(name);
		if (f != null)
			return new FactionsRegion(f);
		return null;
	}
	
	@Override
	public boolean hasMultipleOwners_i() {
		return false;
	}
	
	@Override
	protected Region deserializeRegion_i(final String s) {
		final String[] split = s.split(":", 2);
		if (split.length != 2)
			return null;
		final Faction f = FactionColls.get().getForUniverse(split[0]).get(split[1]);
		if (f == null)
			return null;
		return new FactionsRegion(f);
	}
}
