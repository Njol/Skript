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
 * Copyright 2011-2014 Peter Güttinger
 * 
 */

package ch.njol.skript.hooks.regions;

import java.io.IOException;
import java.io.StreamCorruptedException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Iterator;
import java.util.NoSuchElementException;

import org.bukkit.Bukkit;
import org.bukkit.Chunk;
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
	
	public Factions2Hook() throws IOException {}
	
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
		
		private transient Faction faction;
		
		public FactionsRegion(final Faction f) {
			faction = f;
		}
		
		@Override
		public boolean contains(final Location l) {
			return BoardColls.get().getFactionAt(PS.valueOf(l)) == faction;
		}
		
		@Override
		public boolean isMember(final OfflinePlayer p) {
			for (final UPlayer up : faction.getUPlayers()) {
				if (up.getName().equalsIgnoreCase(p.getName()))
					return true;
			}
			return false;
		}
		
		@Override
		public Collection<OfflinePlayer> getMembers() {
			final Collection<UPlayer> ps = faction.getUPlayers();
			final Collection<OfflinePlayer> r = new ArrayList<OfflinePlayer>(ps.size());
			for (final UPlayer p : ps)
				r.add(Bukkit.getOfflinePlayer(p.getName()));
			return r;
		}
		
		@Override
		public boolean isOwner(final OfflinePlayer p) {
			return faction.getLeader().getName().equalsIgnoreCase(p.getName());
		}
		
		@SuppressWarnings("null")
		@Override
		public Collection<OfflinePlayer> getOwners() {
			return Arrays.asList(Bukkit.getOfflinePlayer(faction.getLeader().getName()));
		}
		
		@Override
		public Iterator<Block> getBlocks() {
			final Iterator<PS> cs = BoardColls.get().get(faction).getChunks(faction).iterator();
			if (!cs.hasNext())
				return EmptyIterator.get();
			return new Iterator<Block>() {
				@Nullable
				Iterator<Block> current;
				
				@Override
				public boolean hasNext() {
					Iterator<Block> current = this.current;
					while ((current == null || !current.hasNext()) && cs.hasNext()) {
						final Chunk c = cs.next().asBukkitChunk();
						if (c == null)
							continue;
						current = new AABB(c).iterator();
					}
					this.current = current;
					return current != null && current.hasNext();
				}
				
				@SuppressWarnings("null")
				@Override
				public Block next() {
					if (!hasNext())
						throw new NoSuchElementException();
					assert current != null;
					return current.next();
				}
				
				@Override
				public void remove() {
					throw new UnsupportedOperationException();
				}
			};
		}
		
		@Override
		public String toString() {
			return "" + faction.getName();
		}
		
		@Override
		public Fields serialize() {
			final Fields fields = new Fields();
			fields.putObject("universe", faction.getUniverse());
			fields.putObject("id", faction.getId());
			return fields;
		}
		
		@Override
		public void deserialize(final Fields fields) throws StreamCorruptedException {
			final String universe = fields.getObject("universe", String.class);
			final String id = fields.getObject("id", String.class);
			final Faction f = FactionColls.get().getForUniverse(universe).get(id); // getForUniverse creates a new "Coll" if none exists
			if (f == null)
				throw new StreamCorruptedException("Invalid faction " + id + " in universe " + universe);
			faction = f;
		}
		
		@Override
		public RegionsPlugin<?> getPlugin() {
			return Factions2Hook.this;
		}
		
		@Override
		public boolean equals(final @Nullable Object o) {
			if (o == this)
				return true;
			if (o == null)
				return false;
			if (!(o instanceof FactionsRegion))
				return false;
			return faction.equals(((FactionsRegion) o).faction);
		}
		
		@Override
		public int hashCode() {
			return faction.hashCode();
		}
		
	}
	
	@SuppressWarnings("null")
	@Override
	public Collection<? extends Region> getRegionsAt_i(final Location l) {
		return Arrays.asList(new FactionsRegion(BoardColls.get().getFactionAt(PS.valueOf(l))));
	}
	
	@Override
	@Nullable
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
	protected Class<? extends Region> getRegionClass() {
		return FactionsRegion.class;
	}
	
}
