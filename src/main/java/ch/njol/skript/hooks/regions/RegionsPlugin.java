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

import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

import ch.njol.skript.hooks.Hook;
import ch.njol.skript.hooks.regions.classes.Region;

/**
 * @author Peter Güttinger
 */
// TODO support GriefPrevention, PreciousStones and maybe other plugins
// TODO add 'region(s) at <location>', 'owner of <region>', '<player> is member of <region>', 'members of <region>', etc.
public abstract class RegionsPlugin<P extends Plugin> extends Hook<P> {
	
	public static Collection<RegionsPlugin<?>> plugins = new ArrayList<RegionsPlugin<?>>(2);
	
	@Override
	protected boolean init() {
		plugins.add(this);
		return true;
	}
	
	public abstract boolean canBuild_i(Player p, Location l);
	
	public final static boolean canBuild(final Player p, final Location l) {
		for (final RegionsPlugin<?> pl : plugins) {
			if (!pl.canBuild_i(p, l))
				return false;
		}
		return true;
	}
	
	public abstract Collection<? extends Region> getRegionsAt_i(Location l);
	
	public final static Collection<? extends Region> getRegionsAt(final Location l) {
		final ArrayList<Region> r = new ArrayList<Region>();
		for (final RegionsPlugin<?> pl : plugins) {
			r.addAll(pl.getRegionsAt_i(l));
		}
		return r;
	}
	
	public abstract Region getRegion_i(World world, String name);
	
	public final static Region getRegion(final World world, final String name) {
		for (final RegionsPlugin<?> pl : plugins) {
			return pl.getRegion_i(world, name);
		}
		return null;
	}
	
	public abstract boolean hasMultipleOwners_i();
	
	public final static boolean hasMultipleOwners() {
		for (final RegionsPlugin<?> pl : plugins) {
			if (pl.hasMultipleOwners_i())
				return true;
		}
		return false;
	}
	
	public final static String serializeRegion(final Region r) {
		return r.getPlugin() + ":" + r.serialize();
	}
	
	protected abstract Region deserializeRegion_i(String s);
	
	public final static Region deserializeRegion(final String s) {
		final String[] split = s.split(":", 2);
		if (split.length < 2)
			return null;
		final RegionsPlugin<?> p = getPlugin(split[0]);
		if (p == null)
			return null;
		return p.deserializeRegion_i(split[1]);
	}
	
	public static RegionsPlugin<?> getPlugin(final String name) {
		for (final RegionsPlugin<?> pl : plugins) {
			if (pl.getName().equalsIgnoreCase(name))
				return pl;
		}
		return null;
	}
	
}
