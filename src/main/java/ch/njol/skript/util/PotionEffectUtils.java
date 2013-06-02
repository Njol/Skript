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
import java.util.Map;

import org.bukkit.entity.ThrownPotion;
import org.bukkit.potion.Potion;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.potion.PotionType;

import ch.njol.skript.localization.Language;
import ch.njol.skript.localization.LanguageChangeListener;

/**
 * @author Peter Güttinger
 */
public abstract class PotionEffectUtils {
	
	private PotionEffectUtils() {}
	
	private final static Map<String, PotionEffectType> types = new HashMap<String, PotionEffectType>();
	private final static String[] names = new String[PotionEffectType.values().length];
	static {
		Language.addListener(new LanguageChangeListener() {
			@Override
			public void onLanguageChange() {
				types.clear();
				for (final PotionEffectType t : PotionEffectType.values()) {
					if (t == null)
						continue;
					final String[] ls = Language.getList("potions." + t.getName());
					names[t.getId()] = ls[0];
					for (final String l : ls) {
						types.put(l.toLowerCase(), t);
					}
				}
			}
		});
	}
	
	public static PotionEffectType parse(final String s) {
		return types.get(s.toLowerCase());
	}
	
	public static String toString(final PotionEffectType t) {
		return names[t.getId()];
	}
	
	// TODO flags
	public static String toString(final PotionEffectType t, final int flags) {
		return names[t.getId()];
	}
	
	public final static String[] getNames() {
		return names;
	}
	
	public static short guessData(final ThrownPotion p) {
		if (p.getEffects().size() == 1) {
			final PotionEffect e = p.getEffects().iterator().next();
			final Potion d = new Potion(PotionType.getByEffect(e.getType())).splash();
			
			return d.toDamageValue();
		}
		return 0;
	}
}
