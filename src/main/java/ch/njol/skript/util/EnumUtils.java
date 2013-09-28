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

package ch.njol.skript.util;

import java.util.HashMap;

import org.bukkit.event.entity.EntityDamageEvent.DamageCause;

import ch.njol.skript.localization.Language;
import ch.njol.skript.localization.LanguageChangeListener;
import ch.njol.util.StringUtils;

/**
 * @author Peter Güttinger
 */
public final class EnumUtils<E extends Enum<E>> {
	private final Class<E> c;
	private final String languageNode;
	
	public EnumUtils(final Class<E> c, final String languageNode) {
		this.c = c;
		assert !languageNode.endsWith(".");
		this.languageNode = languageNode;
	}
	
	private final HashMap<String, E> parseMap = new HashMap<String, E>();
	private final String[] names = new String[DamageCause.values().length];
	{
		Language.addListener(new LanguageChangeListener() {
			@Override
			public void onLanguageChange() {
				parseMap.clear();
				for (final E e : c.getEnumConstants()) {
					final String[] ls = Language.getList(languageNode + "." + e.name());
					names[e.ordinal()] = ls[0];
					for (final String l : ls)
						parseMap.put(l, e);
				}
			}
		});
	}
	
	public final E parse(final String s) {
		return parseMap.get(s.toLowerCase());
	}
	
	public final String toString(final E e, final int flags) {
		return names[e.ordinal()];
	}
	
	public final String getAllNames() {
		return StringUtils.join(names, ", ");
	}
	
}
