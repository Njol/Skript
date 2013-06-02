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
import java.util.Set;
import java.util.regex.Pattern;

import org.bukkit.enchantments.Enchantment;

import ch.njol.skript.aliases.ItemType;
import ch.njol.skript.localization.Language;
import ch.njol.skript.localization.LanguageChangeListener;

/**
 * @author Peter Güttinger
 */
public class EnchantmentType {
	
	private final Enchantment type;
	private final int level;
	
	public EnchantmentType(final Enchantment type, final int level) {
		assert type != null;
		this.type = type;
		this.level = level;
	}
	
	/**
	 * @return level or 1 if level == -1
	 */
	public int getLevel() {
		return level == -1 ? 1 : level;
	}
	
	/**
	 * @return the internal level, can be -1
	 */
	public int getInternalLevel() {
		return level;
	}
	
	public Enchantment getType() {
		return type;
	}
	
	public boolean has(final ItemType item) {
		if (item.getEnchantments() == null)
			return false;
		final Integer l = item.getEnchantments().get(type);
		if (l == null)
			return false;
		if (level == -1)
			return true;
		return l == level;
	}
	
	@Override
	public String toString() {
		return Language.get("enchantments.names." + type.getName()) + (level == -1 ? "" : " " + level);
	}
	
	public static String toString(final Enchantment e) {
		return Language.get("enchantments.names." + e.getName());
	}
	
	// TODO flags
	public static String toString(final Enchantment e, final int flags) {
		return Language.get("enchantments.names." + e.getName());
	}
	
	private final static Map<String, Enchantment> enchantmentNames = new HashMap<String, Enchantment>();
	static {
		Language.addListener(new LanguageChangeListener() {
			@Override
			public void onLanguageChange() {
				enchantmentNames.clear();
				for (final Enchantment e : Enchantment.values())
					enchantmentNames.put(Language.get("enchantments.names." + e.getName()).toLowerCase(), e);
			}
		});
	}
	
	private final static Pattern pattern = Pattern.compile(".+ \\d+");
	
	public static EnchantmentType parse(final String s) {
		if (pattern.matcher(s).matches()) {
			final Enchantment ench = enchantmentNames.get(s.substring(0, s.lastIndexOf(' ')).toLowerCase());
			if (ench == null)
				return null;
			return new EnchantmentType(ench, Utils.parseInt(s.substring(s.lastIndexOf(' ') + 1)));
		}
		final Enchantment ench = enchantmentNames.get(s.toLowerCase());
		if (ench == null)
			return null;
		return new EnchantmentType(ench, -1);
	}
	
	public static Enchantment parseEnchantment(final String s) {
		return enchantmentNames.get(s.toLowerCase());
	}
	
	public final static Set<String> getNames() {
		return enchantmentNames.keySet();
	}
	
}
