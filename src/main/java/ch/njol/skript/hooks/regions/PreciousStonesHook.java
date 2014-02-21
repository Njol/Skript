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
import org.eclipse.jdt.annotation.Nullable;

import ch.njol.skript.hooks.regions.Factions2Hook.FactionsRegion;
import ch.njol.skript.hooks.regions.classes.Region;
import ch.njol.skript.registrations.Classes;
import ch.njol.skript.util.AABB;
import ch.njol.util.NullableChecker;
import ch.njol.util.coll.iterator.CheckedIterator;
import ch.njol.util.coll.iterator.EmptyIterator;
import ch.njol.yggdrasil.Fields;

/**
 * @author Peter Güttinger
 */
public class PreciousStonesHook extends RegionsPlugin<PreciousStones> {
	
	public PreciousStonesHook() throws IOException {}
	
	@Override
	public String getName() {
		return "PreciousStones";
	}
	
	@Override
	public boolean canBuild_i(final Player p, final Location l) {
		return PreciousStones.API().canPlace(p, l);
	}
	
	public final class PreciousStonesRegion extends Region {
		
		transient Field field;
		
		public PreciousStonesRegion(final Field f) {
			field = f;
		}
		
		@Override
		public boolean contains(final Location l) {
			return field.envelops(l);
		}
		
		@Override
		public boolean isMember(final OfflinePlayer p) {
			return field.isAllowed(p.getName());
		}
		
		@Override
		public Collection<OfflinePlayer> getMembers() {
			final Collection<String> allowed = field.getAllAllowed();
			final Collection<OfflinePlayer> r = new ArrayList<OfflinePlayer>(allowed.size());
			for (final String a : allowed)
				r.add(Bukkit.getOfflinePlayer(a));
			return r;
		}
		
		@Override
		public boolean isOwner(final OfflinePlayer p) {
			return field.isOwner(p.getName());
		}
		
		@SuppressWarnings("null")
		@Override
		public Collection<OfflinePlayer> getOwners() {
			return Arrays.asList(Bukkit.getOfflinePlayer(field.getOwner()));
		}
		
		@Override
		public Iterator<Block> getBlocks() {
			final World w = Bukkit.getWorld(field.getWorld());
			if (w == null)
				return EmptyIterator.get();
			return new CheckedIterator<Block>(new AABB(w, new Vector(field.getMinx(), field.getMiny(), field.getMinz()), new Vector(field.getMaxx() + 1, field.getMaxy() + 1, field.getMaxz() + 1)).iterator(),
					new NullableChecker<Block>() {
						@Override
						public boolean check(final @Nullable Block b) {
							return b != null && field.envelops(b);
						}
					});
		}
		
		@Override
		public Fields serialize() {
			final Fields f = new Fields();
			f.putObject("block", field.getBlock());
			return f;
		}
		
		@Override
		public void deserialize(final Fields fields) throws StreamCorruptedException {
			final Block b = fields.getObject("block", Block.class);
			final Field f = plugin.getForceFieldManager().getField(b);
			if (f == null)
				throw new StreamCorruptedException("No field at block " + b);
			field = f;
		}
		
		@Override
		public String toString() {
			return "field at " + Classes.toString(field.getBlock().getLocation());
		}
		
		@Override
		public RegionsPlugin<?> getPlugin() {
			return PreciousStonesHook.this;
		}
		
		@Override
		public boolean equals(final @Nullable Object o) {
			if (o == this)
				return true;
			if (o == null)
				return false;
			if (!(o instanceof PreciousStonesRegion))
				return false;
			return field.equals(((PreciousStonesRegion) o).field);
		}
		
		@Override
		public int hashCode() {
			return field.hashCode();
		}
		
	}
	
	@SuppressWarnings("null")
	@Override
	public Collection<? extends Region> getRegionsAt_i(final Location l) {
		final Collection<Field> fields = plugin.getForceFieldManager().getSourceFields(l, FieldFlag.ALL); // includes disabled fields
		final Collection<Region> r = new ArrayList<Region>(fields.size());
		for (final Field f : fields)
			r.add(new PreciousStonesRegion(f));
		return r;
	}
	
	@Override
	@Nullable
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
