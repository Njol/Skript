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

package ch.njol.skript;

import org.bukkit.Bukkit;
import org.bukkit.plugin.RegisteredServiceProvider;

import ch.njol.skript.util.Money;

/**
 * @author Peter Güttinger
 * 
 */
public abstract class Economy {
	
	private static net.milkbowl.vault.economy.Economy economy = null;
	
	public static net.milkbowl.vault.economy.Economy getEconomy() {
		return economy;
	}
	
	public static String plural = null;
	public static String pluralLower = null;
	public static String singular = null;
	public static String singularLower = null;
	
	static final void load() {
		if (Bukkit.getPluginManager().getPlugin("Vault") != null) {
			final RegisteredServiceProvider<net.milkbowl.vault.economy.Economy> p = Bukkit.getServicesManager().getRegistration(net.milkbowl.vault.economy.Economy.class);
			if (p != null) {
				economy = p.getProvider();
				plural = Economy.getEconomy().currencyNamePlural();
				pluralLower = plural.toLowerCase();
				singular = Economy.getEconomy().currencyNameSingular();
				singularLower = singular.toLowerCase();
			}
		}
		
//		if (logNormal() && economy != null)
//			Skript.info("hooked into Vault");
		
		new Money(0);
		
	}
}
