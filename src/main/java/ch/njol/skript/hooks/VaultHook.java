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

package ch.njol.skript.hooks;

import net.milkbowl.vault.Vault;

import org.bukkit.Bukkit;
import org.bukkit.plugin.Plugin;

/**
 * @author Peter Güttinger
 */
public class VaultHook extends Hook {
	
	public static Plugin vault = null;
	static {
		final Plugin p = Bukkit.getPluginManager().getPlugin("Vault"); // TODO permissions hook (groups)
		if (p != null && p instanceof Vault)
			vault = p;
	}
	
	@Override
	protected boolean init() {
		return vault != null;
	}
	
	@Override
	public Plugin getPlugin() {
		return vault;
	}
	
}
