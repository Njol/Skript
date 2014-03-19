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
 * Copyright 2011-2014 Peter Güttinger
 * 
 */

package ch.njol.skript.util;

import java.util.HashMap;

import org.eclipse.jdt.annotation.Nullable;

import ch.njol.skript.localization.GeneralWords;
import ch.njol.skript.localization.Language;
import ch.njol.skript.localization.LanguageChangeListener;
import ch.njol.skript.localization.Noun;
import ch.njol.util.NonNullPair;
import ch.njol.util.coll.CollectionUtils;
import ch.njol.yggdrasil.YggdrasilSerializable;

/**
 * @author Peter Güttinger
 */
public class Timespan implements YggdrasilSerializable, Comparable<Timespan> { // REMIND unit

	private final static Noun m_tick = new Noun("time.tick");
	private final static Noun m_second = new Noun("time.second");
	private final static Noun m_minute = new Noun("time.minute");
	private final static Noun m_hour = new Noun("time.hour");
	private final static Noun m_day = new Noun("time.day");
	final static Noun[] names = {m_tick, m_second, m_minute, m_hour, m_day};
	final static int[] times = {50, 1000, 1000 * 60, 1000 * 60 * 60, 1000 * 60 * 60 * 24};
	final static HashMap<String, Integer> parseValues = new HashMap<String, Integer>();
	static {
		Language.addListener(new LanguageChangeListener() {
			@Override
			public void onLanguageChange() {
				for (int i = 0; i < names.length; i++) {
					parseValues.put(names[i].getSingular().toLowerCase(), times[i]);
					parseValues.put(names[i].getPlural().toLowerCase(), times[i]);
				}
			}
		});
	}
	
	@Nullable
	public final static Timespan parse(final String s) {
		if (s.isEmpty())
			return null;
		int t = 0;
		boolean minecraftTime = false;
		boolean isMinecraftTimeSet = false;
		if (s.matches("^\\d+:\\d\\d(:\\d\\d)?(\\.\\d{1,4})?$")) { // MM:SS[.ms] or HH:MM:SS[.ms]
			final String[] ss = s.split("[:.]");
			final int[] times = {1000 * 60 * 60, 1000 * 60, 1000, 1}; // h, m, s, ms
			final int offset = ss.length == 3 && !s.contains(".") || ss.length == 4 ? 0 : 1;
			for (int i = 0; i < ss.length; i++) {
				t += times[offset + i] * Utils.parseInt("" + ss[i]);
			}
		} else {
			final String[] subs = s.toLowerCase().split("\\s+");
			for (int i = 0; i < subs.length; i++) {
				String sub = subs[i];
				
				if (sub.equals(GeneralWords.and.toString())) {
					if (i == 0 || i == subs.length - 1)
						return null;
					continue;
				}
				
				float amount = 1;
				if (Noun.isIndefiniteArticle(sub)) {
					if (i == subs.length - 1)
						return null;
					amount = 1;
					sub = subs[++i];
				} else if (sub.matches("^\\d+(.\\d+)?$")) {
					if (i == subs.length - 1)
						return null;
					amount = Float.parseFloat(sub);
					sub = subs[++i];
				}
				
				if (CollectionUtils.contains(Language.getList("time.real"), sub)) {
					if (i == subs.length - 1 || isMinecraftTimeSet && minecraftTime)
						return null;
					sub = subs[++i];
				} else if (CollectionUtils.contains(Language.getList("time.minecraft"), sub)) {
					if (i == subs.length - 1 || isMinecraftTimeSet && !minecraftTime)
						return null;
					minecraftTime = true;
					sub = subs[++i];
				}
				
				if (sub.endsWith(","))
					sub = sub.substring(0, sub.length() - 1);
				
				final Integer d = parseValues.get(sub.toLowerCase());
				if (d == null)
					return null;
				
				if (minecraftTime && d != times[0]) // times[0] == tick
					amount *= 72f;
				
				t += Math.round(amount * d);
				
				isMinecraftTimeSet = true;
				
			}
		}
		return new Timespan(t);
	}
	
	private final long millis;
	
	public Timespan() {
		millis = 0;
	}
	
	public Timespan(final long millis) {
		if (millis < 0)
			throw new IllegalArgumentException("millis must be >= 0");
		this.millis = millis;
	}
	
	public static Timespan fromTicks(final int ticks) {
		return new Timespan((long) ticks * 50);
	}
	
	public long getMilliSeconds() {
		return millis;
	}
	
	public int getTicks() {
		return Math.round(millis / 50f);
	}
	
	@Override
	public String toString() {
		return toString(millis);
	}
	
	public String toString(final int flags) {
		return toString(millis, flags);
	}
	
	@SuppressWarnings("unchecked")
	final static NonNullPair<Noun, Integer>[] simpleValues = new NonNullPair[] {
			new NonNullPair<Noun, Integer>(m_day, 1000 * 60 * 60 * 24),
			new NonNullPair<Noun, Integer>(m_hour, 1000 * 60 * 60),
			new NonNullPair<Noun, Integer>(m_minute, 1000 * 60),
			new NonNullPair<Noun, Integer>(m_second, 1000)
	};
	
	public static String toString(final long millis) {
		return toString(millis, 0);
	}
	
	@SuppressWarnings("null")
	public static String toString(final long millis, final int flags) {
		for (int i = 0; i < simpleValues.length - 1; i++) {
			if (millis >= simpleValues[i].second) {
				if (millis % simpleValues[i].second != 0) {
					return toString(Math.floor(1. * millis / simpleValues[i].second), simpleValues[i], flags) + " " + GeneralWords.and + " " + toString(1. * (millis % simpleValues[i].second) / simpleValues[i + 1].second, simpleValues[i + 1], flags);
				} else {
					return toString(1. * millis / simpleValues[i].second, simpleValues[i], flags);
				}
			}
		}
		return toString(1. * millis / simpleValues[simpleValues.length - 1].second, simpleValues[simpleValues.length - 1], flags);
	}
	
	private static String toString(final double amount, final NonNullPair<Noun, Integer> p, final int flags) {
		return p.first.withAmount(amount, flags);
	}
	
	@Override
	public int compareTo(final Timespan o) {
		final long d = millis - o.millis;
		return d > 0 ? 1 : d < 0 ? -1 : 0;
	}
	
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + (int) (millis ^ (millis >>> 32));
		return result;
	}
	
	@Override
	public boolean equals(final @Nullable Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (!(obj instanceof Timespan))
			return false;
		final Timespan other = (Timespan) obj;
		if (millis != other.millis)
			return false;
		return true;
	}
	
}
