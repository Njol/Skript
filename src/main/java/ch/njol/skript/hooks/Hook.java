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

package ch.njol.skript.hooks;

import java.io.IOException;

import org.bukkit.plugin.Plugin;

import ch.njol.skript.Skript;
import ch.njol.skript.localization.ArgsMessage;

/**
 * @author Peter Güttinger
 */
public abstract class Hook {
	
	private final static ArgsMessage m_hooked = new ArgsMessage("hooks.hooked");
	
	public Hook() {}
	
	public boolean load() throws IOException {
		if (!init())
			return false;
		Skript.getAddonInstance().loadClasses(getClass().getPackage().getName());
		if (Skript.logHigh())
			Skript.info(m_hooked.toString(getPlugin().getName()));
		return true;
	}
	
	protected abstract boolean init();
	
	public abstract Plugin getPlugin();
	
}
