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
import org.bukkit.entity.Player;

import ch.njol.skript.hooks.Hook;

/**
 * @author Peter Güttinger
 */
// TODO support GriefPrevention, PreciousStones and maybe other plugins
// TODO add 'region(s) at <location>', 'owner of <region>', '<player> is member of <region>', 'members of <region>', etc.
public abstract class RegionsPlugin extends Hook {
	
	public static Collection<RegionsPlugin> plugins = new ArrayList<RegionsPlugin>(2);
	
	@Override
	protected boolean init() {
		plugins.add(this);
		return true;
	}
	
	public abstract boolean canBuild_i(Player p, Location l);
	
	public static boolean canBuild(final Player p, final Location l) {
		for (final RegionsPlugin pl : plugins) {
			if (!pl.canBuild_i(p, l))
				return false;
		}
		return true;
	}
	
}
