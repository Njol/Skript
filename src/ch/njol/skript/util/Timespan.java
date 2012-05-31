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
import java.util.Locale;

import ch.njol.skript.Skript;
import ch.njol.skript.api.ClassInfo;
import ch.njol.skript.api.Parser;

/**
 * @author Peter Güttinger
 * 
 */
public class Timespan {
	
	static {
		final HashMap<String, Float> simpleValues = new HashMap<String, Float>();
		simpleValues.put("year", 360 * 24000f);
		simpleValues.put("month", 30 * 24000f);
		simpleValues.put("week", 7 * 24000f);
		simpleValues.put("day", 24000f);
		simpleValues.put("hour", 1000f);
		simpleValues.put("minute", 1000f / 60);
		simpleValues.put("tick", 1f);
		simpleValues.put("second", 1000f / 3600);
		
		Skript.registerClass(new ClassInfo<Timespan>("timespan", Timespan.class, null, new Parser<Timespan>() {
			@Override
			public Timespan parse(String s) {
				if (s.isEmpty())
					return null;
				s = s.toLowerCase(Locale.ENGLISH);
				float t = 0;
				if (s.matches("^\\d+(:\\d\\d){1,2}$")) {
					final String[] ss = s.split(":");
					final float[] times = {1000f, 1000f / 60, 1000f / 3600};
					for (int i = 0; i < ss.length; i++) {
						t += times[i] * Integer.parseInt(ss[i]);
					}
				} else {
					final String[] subs = s.split("\\s+");
					for (int i = 0; i < subs.length; i++) {
						String sub = subs[i];
						
						if (sub.equals("and")) {
							if (i == 0)
								return null;
							continue;
						}
						
						int amount = 1;
						if (sub.matches("^\\d+$")) {
							if (i == subs.length)
								return null;
							amount = Integer.parseInt(sub);
							sub = subs[++i];
						}
						
						if (sub.equals("real") || sub.equals("rl") || sub.equals("irl")) {
							if (i == subs.length)
								return null;
							amount *= 72;
							sub = subs[++i];
						}
						
						if (sub.endsWith(","))
							sub = sub.substring(0, sub.length() - 1);
						
						if (amount == 1 && sub.endsWith("s") || amount != 1 && !sub.endsWith("s"))
							return null;
						if (amount != 1)
							sub = sub.substring(0, sub.length() - 1);
						
						if (!simpleValues.containsKey(sub))
							return null;
						
						t += amount * simpleValues.get(sub);
						
					}
				}
				return new Timespan(t);
			}
			
			@Override
			public String toString(final Timespan t) {
				return Math.floor(t.ticks / 1000) + ":" + Math.floor(t.ticks % 1000 * 60 / 1000);
			}
		}));
	}
	
	private final float ticks;
	
	public Timespan() {
		ticks = 0;
	}
	
	public Timespan(final float ticks) {
		this.ticks = ticks;
	}
	
	public float getTicks() {
		return ticks;
	}
	
}
