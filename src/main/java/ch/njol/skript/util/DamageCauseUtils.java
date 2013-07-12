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

package ch.njol.skript.util;

import java.util.HashMap;

import org.bukkit.event.entity.EntityDamageEvent.DamageCause;

import ch.njol.skript.localization.Language;
import ch.njol.skript.localization.LanguageChangeListener;
import ch.njol.util.StringUtils;

/**
 * @author Peter Güttinger
 */
public abstract class DamageCauseUtils {
	private DamageCauseUtils() {}
	
	private final static HashMap<String, DamageCause> parseMap = new HashMap<String, DamageCause>();
	private final static String[] names = new String[DamageCause.values().length];
	static {
		Language.addListener(new LanguageChangeListener() {
			@Override
			public void onLanguageChange() {
				parseMap.clear();
				for (final DamageCause dc : DamageCause.values()) {
					final String[] ls = Language.getList("damage causes." + dc.name());
					names[dc.ordinal()] = ls[0];
					for (final String l : ls)
						parseMap.put(l, dc);
				}
			}
		});
	}
	
	public final static DamageCause parse(final String s) {
		return parseMap.get(s.toLowerCase());
	}
	
	public static String toString(final DamageCause dc, final int flags) {
		return names[dc.ordinal()];
	}
	
	public final static String getAllNames() {
		return StringUtils.join(names, ", ");
	}
	
}
