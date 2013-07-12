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

package ch.njol.skript.hooks.chat;

import net.milkbowl.vault.chat.Chat;

import org.bukkit.Bukkit;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.RegisteredServiceProvider;

import ch.njol.skript.hooks.Hook;
import ch.njol.skript.hooks.VaultHook;

/**
 * @author Peter Güttinger
 */
public class ChatHook extends Hook {
	
	public static Chat chat = null;
	
	@Override
	protected boolean init() {
		if (VaultHook.vault != null) {
			final RegisteredServiceProvider<Chat> e = Bukkit.getServicesManager().getRegistration(Chat.class);
			if (e != null) {
				chat = e.getProvider();
				return true;
			}
		}
		return false;
	}
	
	@Override
	public Plugin getPlugin() {
		return VaultHook.vault;
	}
	
}
