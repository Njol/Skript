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

import java.util.EnumMap;
import java.util.HashMap;
import java.util.Map.Entry;

import org.bukkit.event.entity.EntityDamageEvent.DamageCause;

import ch.njol.skript.localization.Language;
import ch.njol.skript.localization.LanguageChangeListener;
import ch.njol.skript.localization.Noun;

/**
 * @author Peter Güttinger
 */
public abstract class DamageCauseUtils {
	private DamageCauseUtils() {}
	
	private final static EnumMap<DamageCause, Noun> names = new EnumMap<DamageCause, Noun>(DamageCause.class);
	static {
		for (final DamageCause c : DamageCause.values())
			names.put(c, new Noun("damage causes." + c.name()));
	}
	
	public final static Noun getName(final DamageCause c) {
		return names.get(c);
	}
	
	// lazy
	private final static HashMap<String, DamageCause> parseMap = new HashMap<String, DamageCause>();
	static {
		Language.addListener(new LanguageChangeListener() {
			@Override
			public void onLanguageChange() {
				parseMap.clear();
			}
		});
	}
	
	public final static DamageCause parse(final String s) {
		if (parseMap.isEmpty()) {
			for (final Entry<DamageCause, Noun> e : names.entrySet()) {
				parseMap.put(e.getValue().toString(), e.getKey());
			}
		}
		return parseMap.get(s);
	}
}
