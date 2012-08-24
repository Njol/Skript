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

import ch.njol.skript.Skript;
import ch.njol.skript.classes.Arithmetic;
import ch.njol.skript.classes.ClassInfo;
import ch.njol.skript.classes.Parser;
import ch.njol.skript.classes.Serializer;
import ch.njol.skript.lang.ParseContext;
import ch.njol.util.Pair;
import ch.njol.util.StringUtils;

/**
 * @author Peter Güttinger
 * 
 */
public class Timespan {
	
	static {
		final HashMap<String, Integer> simpleValues = new HashMap<String, Integer>();
		simpleValues.put("tick", 1);
		
		simpleValues.put("second", 20);
		simpleValues.put("minute", 20 * 60);
		simpleValues.put("hour", 20 * 60 * 60);
		simpleValues.put("day", 20 * 60 * 60 * 24);
		
		Skript.registerClass(new ClassInfo<Timespan>(Timespan.class, "timespan", "time span")
				.user("time ?spans?")
				.parser(new Parser<Timespan>() {
					@Override
					public Timespan parse(final String s, final ParseContext context) {
						if (s.isEmpty())
							return null;
						int t = 0;
						boolean minecraftTime = false;
						boolean isMinecraftTimeSet = false;
						if (s.matches("^\\d+:\\d\\d$")) {
							final String[] ss = s.split(":");
							final int[] times = {20 * 60, 20};
							for (int i = 0; i < ss.length; i++) {
								t += times[i] * Skript.parseInt(ss[i]);
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
								
								final Pair<String, Boolean> p = Utils.getPlural(sub);
								sub = p.first;
								
								if (!simpleValues.containsKey(sub))
									return null;
								
								if (sub.equals("tick") && minecraftTime)
									amount *= 72f;
								
								t += Math.round(amount * simpleValues.get(sub));
								
								isMinecraftTimeSet = true;
								
							}
						}
						return new Timespan(t);
					}
					
					@Override
					public String toString(final Timespan t) {
						return t.toString();
					}
					
					@Override
					public String toCodeString(final Timespan o) {
						return "timespan:" + o.ticks;
					}
				}).serializer(new Serializer<Timespan>() {
					@Override
					public String serialize(final Timespan t) {
						return "" + t.ticks;
					}
					
					@Override
					public Timespan deserialize(final String s) {
						try {
							return new Timespan(Integer.parseInt(s));
						} catch (final NumberFormatException e) {
							return null;
						}
					}
				})
				.math(Timespan.class, new Arithmetic<Timespan, Timespan>() {
					@Override
					public Timespan difference(final Timespan t1, final Timespan t2) {
						return new Timespan(Math.abs(t1.getTicks() - t2.getTicks()));
					}
				}));
	}
	
	private final int ticks;
	
	public Timespan(final int ticks) {
		if (ticks < 0)
			throw new IllegalArgumentException("ticks must be >= 0");
		this.ticks = ticks;
	}
	
	public int getTicks() {
		return ticks;
	}
	
	@Override
	public String toString() {
		return toString(ticks);
	}
	
	@SuppressWarnings("unchecked")
	final static Pair<String, Integer>[] simpleValues = (Pair<String, Integer>[]) new Pair<?, ?>[] {
			new Pair<String, Integer>("day", 20 * 60 * 60 * 24),
			new Pair<String, Integer>("hour", 20 * 60 * 60),
			new Pair<String, Integer>("minute", 20 * 60),
			new Pair<String, Integer>("second", 20)
	};
	
	public static String toString(final int ticks) {
		for (int i = 0; i < simpleValues.length - 1; i++) {
			if (ticks >= simpleValues[i].second) {
				if (ticks % simpleValues[i].second != 0) {
					return toString(Math.floor(1. * ticks / simpleValues[i].second), simpleValues[i]) + " and " + toString(1. * (ticks % simpleValues[i].second) / simpleValues[i + 1].second, simpleValues[i + 1]);
				} else {
					return toString(1. * ticks / simpleValues[i].second, simpleValues[i]);
				}
			}
		}
		return toString(1. * ticks / simpleValues[simpleValues.length - 1].second, simpleValues[simpleValues.length - 1]);
	}
	
	private static String toString(final double amount, final Pair<String, Integer> p) {
		return StringUtils.toString(amount, Skript.NUMBERACCURACY) + " " + Utils.toPlural(p.first, amount != 1);
	}
	
}
