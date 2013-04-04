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

import java.io.Serializable;
import java.util.HashMap;

import ch.njol.skript.SkriptConfig;
import ch.njol.util.Pair;
import ch.njol.util.StringUtils;

/**
 * @author Peter Güttinger
 */
@SuppressWarnings("serial")
public class Timespan implements Serializable {
	
	static final HashMap<String, Integer> parseValues = new HashMap<String, Integer>();
	static {
		parseValues.put("tick", 50);
		
		parseValues.put("second", 1000);
		parseValues.put("minute", 1000 * 60);
		parseValues.put("hour", 1000 * 60 * 60);
		parseValues.put("day", 1000 * 60 * 60 * 24);
	}
	
	public static final Timespan parse(final String s) {
		if (s.isEmpty())
			return null;
		int t = 0;
		boolean minecraftTime = false;
		boolean isMinecraftTimeSet = false;
		if (s.matches("^\\d+:\\d\\d(:\\d\\d)?(\\.\\d{1,4})?$")) {
			final String[] ss = s.split("[:.]");
			final int[] times = {1000 * 60 * 60, 1000 * 60, 1000, 1};
			final int offset = ss.length == 3 && !s.contains(".") || ss.length == 4 ? 1 : 0;
			for (int i = 0; i < ss.length; i++) {
				t += times[offset + i] * Utils.parseInt(ss[i]);
			}
		} else {
			final String[] subs = s.toLowerCase().split("\\s+");
			for (int i = 0; i < subs.length; i++) {
				String sub = subs[i];
				
				if (sub.equals("and")) {
					if (i == 0 || i == subs.length - 1)
						return null;
					continue;
				}
				
				float amount = 1;
				if (sub.equalsIgnoreCase("a") || sub.equalsIgnoreCase("an")) {
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
				
				if (sub.equals("real") || sub.equals("rl") || sub.equals("irl")) {
					if (i == subs.length - 1 || isMinecraftTimeSet && minecraftTime)
						return null;
					sub = subs[++i];
				} else if (sub.equals("mc") || sub.equals("minecraft")) {
					if (i == subs.length - 1 || isMinecraftTimeSet && !minecraftTime)
						return null;
					minecraftTime = true;
					sub = subs[++i];
				}
				
				if (minecraftTime)
					amount /= 72f;
				
				if (sub.endsWith(","))
					sub = sub.substring(0, sub.length() - 1);
				
				final Pair<String, Boolean> p = Utils.getEnglishPlural(sub);
				sub = p.first;
				
				if (!parseValues.containsKey(sub))
					return null;
				
				if (sub.equals("tick") && minecraftTime)
					amount *= 72f;
				
				t += Math.round(amount * parseValues.get(sub));
				
				isMinecraftTimeSet = true;
				
			}
		}
		return new Timespan(t);
	}
	
	private final long millis;
	
	public Timespan(final long millis) {
		if (millis < 0)
			throw new IllegalArgumentException("millis must be >= 0");
		this.millis = millis;
	}
	
	public static Timespan fromTicks(final int ticks) {
		return new Timespan(ticks * 50);
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
	
	@SuppressWarnings("unchecked")
	final static Pair<String, Integer>[] simpleValues = (Pair<String, Integer>[]) new Pair<?, ?>[] {
			new Pair<String, Integer>("day", 1000 * 60 * 60 * 24),
			new Pair<String, Integer>("hour", 1000 * 60 * 60),
			new Pair<String, Integer>("minute", 1000 * 60),
			new Pair<String, Integer>("second", 1000)
	};
	
	public static String toString(final long millis) {
		for (int i = 0; i < simpleValues.length - 1; i++) {
			if (millis >= simpleValues[i].second) {
				if (millis % simpleValues[i].second != 0) {
					return toString(Math.floor(1. * millis / simpleValues[i].second), simpleValues[i]) + " and " + toString(1. * (millis % simpleValues[i].second) / simpleValues[i + 1].second, simpleValues[i + 1]);
				} else {
					return toString(1. * millis / simpleValues[i].second, simpleValues[i]);
				}
			}
		}
		return toString(1. * millis / simpleValues[simpleValues.length - 1].second, simpleValues[simpleValues.length - 1]);
	}
	
	private static String toString(final double amount, final Pair<String, Integer> p) {
		return StringUtils.toString(amount, SkriptConfig.numberAccuracy.value()) + " " + Utils.toEnglishPlural(p.first, amount != 1);
	}
	
}
