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

package ch.njol.skript.hooks.economy;

import net.milkbowl.vault.Vault;
import net.milkbowl.vault.economy.Economy;

import org.bukkit.Bukkit;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.RegisteredServiceProvider;

import ch.njol.skript.hooks.Hook;

/**
 * @author Peter Güttinger
 */
public class EconomyHook extends Hook {
	
	private static Vault vault = null;
	public static Economy economy = null;
	
	public static String plural = null;
	public static String singular = null;
	
	@Override
	protected boolean init() {
		final Plugin p = Bukkit.getPluginManager().getPlugin("Vault"); // TODO permissions (groups) hook
		if (p != null && p instanceof Vault) {
			vault = (Vault) p;
			final RegisteredServiceProvider<Economy> e = Bukkit.getServicesManager().getRegistration(Economy.class);
			if (e != null) {
				economy = e.getProvider();
				plural = economy.currencyNamePlural();
				singular = economy.currencyNameSingular();
				return true;
			}
		}
		return false;
	}
	
	@Override
	public Plugin getPlugin() {
		return vault;
	}
	
}
