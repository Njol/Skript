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
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Iterator;

import net.sacredlabyrinth.Phaed.PreciousStones.FieldFlag;
import net.sacredlabyrinth.Phaed.PreciousStones.PreciousStones;
import net.sacredlabyrinth.Phaed.PreciousStones.vectors.Field;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.OfflinePlayer;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;

import ch.njol.skript.hooks.regions.Factions2Hook.FactionsRegion;
import ch.njol.skript.hooks.regions.classes.Region;
import ch.njol.skript.registrations.Classes;
import ch.njol.skript.util.AABB;
import ch.njol.util.Checker;
import ch.njol.util.coll.iterator.CheckedIterator;
import ch.njol.yggdrasil.Fields;

/**
 * @author Peter Güttinger
 */
public class PreciousStonesHook extends RegionsPlugin<PreciousStones> {// TODO source

	@Override
	public String getName() {
		return "PreciousStones";
	}
	
	@Override
	public boolean canBuild_i(final Player p, final Location l) {
		return PreciousStones.API().canPlace(p, l);
	}
	
	public final class PreciousStonesRegion extends Region {
		
		transient Field f;
		
		public PreciousStonesRegion(final Field f) {
			this.f = f;
		}
		
		@Override
		public boolean contains(final Location l) {
			return f.envelops(l);
		}
		
		@Override
		public boolean isMember(final OfflinePlayer p) {
			return f.isAllowed(p.getName());
		}
		
		@Override
		public Collection<OfflinePlayer> getMembers() {
			final Collection<String> allowed = f.getAllAllowed();
			final Collection<OfflinePlayer> r = new ArrayList<OfflinePlayer>(allowed.size());
			for (final String a : allowed)
				r.add(Bukkit.getOfflinePlayer(a));
			return r;
		}
		
		@Override
		public boolean isOwner(final OfflinePlayer p) {
			return f.isOwner(p.getName());
		}
		
		@Override
		public Collection<OfflinePlayer> getOwners() {
			return Arrays.asList(Bukkit.getOfflinePlayer(f.getOwner()));
		}
		
		@Override
		public Iterator<Block> getBlocks() {
			return new CheckedIterator<Block>(new AABB(Bukkit.getWorld(f.getWorld()), new Vector(f.getMinx(), f.getMiny(), f.getMinz()), new Vector(f.getMaxx() + 1, f.getMaxy() + 1, f.getMaxz() + 1)).iterator(),
					new Checker<Block>() {
						@Override
						public boolean check(final Block b) {
							return f.envelops(b);
						}
					});
		}
		
		@Override
		public Fields serialize() {
			final Fields f = new Fields();
			f.putObject("block", this.f.getBlock());
			return f;
		}
		
		@Override
		public void deserialize(final Fields fields) throws StreamCorruptedException {
			final Block b = fields.getObject("block", Block.class);
			f = plugin.getForceFieldManager().getField(b);
			if (f == null)
				throw new StreamCorruptedException("No field at block " + b);
		}
		
		@Override
		public String toString() {
			return "field at " + Classes.toString(f.getBlock().getLocation());
		}
		
		@Override
		public RegionsPlugin<?> getPlugin() {
			return PreciousStonesHook.this;
		}
		
		@Override
		public boolean equals(final Object o) {
			if (o == this)
				return true;
			if (o == null)
				return false;
			if (!(o instanceof PreciousStonesRegion))
				return false;
			return f.equals(((PreciousStonesRegion) o).f);
		}
		
		@Override
		public int hashCode() {
			return f.hashCode();
		}
		
	}
	
	@Override
	public Collection<? extends Region> getRegionsAt_i(final Location l) {
		final Collection<Field> fields = plugin.getForceFieldManager().getSourceFields(l, FieldFlag.ALL); // includes disabled fields
		final Collection<Region> r = new ArrayList<Region>(fields.size());
		for (final Field f : fields)
			r.add(new PreciousStonesRegion(f));
		return r;
	}
	
	@Override
	public Region getRegion_i(final World world, final String name) {
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
