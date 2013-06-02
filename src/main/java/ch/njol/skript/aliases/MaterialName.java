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

package ch.njol.skript.aliases;

import java.util.HashMap;

import ch.njol.skript.Skript;
import ch.njol.skript.localization.Noun;
import ch.njol.util.Pair;

final class MaterialName {
	String singular;
	String plural;
	int gender = 0;
	private final int id;
	final HashMap<Pair<Short, Short>, Pair<String, String>> names = new HashMap<Pair<Short, Short>, Pair<String, String>>();
	
	public MaterialName(final int id, final String singular, final String plural, final int gender) {
		this.id = id;
		this.singular = singular;
		this.plural = plural;
		this.gender = gender;
	}
	
	public String toString(final short dataMin, final short dataMax, final int flags) {
		if (names == null)
			return Noun.toString(singular, plural, gender, flags);
		Pair<String, String> s = names.get(new Pair<Short, Short>(dataMin, dataMax));
		if (s != null)
			return Noun.toString(s.first, s.second, gender, flags);
		if (dataMin == -1 && dataMax == -1 || dataMin == 0 && dataMax == 0)
			return Noun.toString(singular, plural, gender, flags);
		s = names.get(new Pair<Short, Short>((short) -1, (short) -1));
		if (s != null)
			return Noun.toString(s.first, s.second, gender, flags);
		return Noun.toString(singular, plural, gender, flags);
	}
	
	public String getDebugName(final short dataMin, final short dataMax, final int flags) {
		if (names == null)
			return Noun.toString(singular, plural, gender, flags);
		final Pair<String, String> s = names.get(new Pair<Short, Short>(dataMin, dataMax));
		if (s != null)
			return Noun.toString(s.first, s.second, gender, flags);
		if (dataMin == -1 && dataMax == -1 || dataMin == 0 && dataMax == 0)
			return Noun.toString(singular, plural, gender, flags);
		return Noun.toString(singular, plural, gender, flags) + ":" + (dataMin == -1 ? 0 : dataMin) + (dataMin == dataMax ? "" : "-" + (dataMax == -1 ? (id <= Skript.MAXBLOCKID ? 15 : Short.MAX_VALUE) : dataMax));
	}
}
