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

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

import com.massivecraft.factions.P;
import com.massivecraft.factions.listeners.FactionsListenerMain;
import com.massivecraft.mcore.ps.PS;

/**
 * @author Peter Güttinger
 */
public class FactionsHook extends RegionsPlugin {
	
	private static Plugin factions = null;
	
	boolean factions2 = false;
	
	@Override
	protected boolean init() {
		final Plugin p = Bukkit.getPluginManager().getPlugin("Factions");
		if (p != null) {
			factions = p;
			factions2 = !factions.getClass().getName().equals("com.massivecraft.factions.P");
			return super.init();
		}
		return false;
	}
	
	@Override
	public Plugin getPlugin() {
		return factions;
	}
	
	@Override
	public boolean canBuild_i(final Player p, final Location l) {
		return factions2 ? FactionsListenerMain.canPlayerBuildAt(p, PS.valueOf(l), false) : ((P) factions).isPlayerAllowedToBuildHere(p, l);
	}
	
}
