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
import java.util.Map;

import org.bukkit.ChatColor;
import org.bukkit.DyeColor;

/**
 * @author Peter Güttinger
 */
public enum Color {
	
	BLACK(DyeColor.BLACK, ChatColor.BLACK, "black"),
	DARK_GREY(DyeColor.GRAY, ChatColor.DARK_GRAY, "dark grey", "dark gray"),
	LIGHT_GREY(DyeColor.SILVER, ChatColor.GRAY, "grey", "light grey", "gray", "light gray", "silver"),
	WHITE(DyeColor.WHITE, ChatColor.WHITE, "white"),
	
	DARK_BLUE(DyeColor.BLUE, ChatColor.DARK_BLUE, "blue", "dark blue"),
	CYAN(DyeColor.CYAN, ChatColor.DARK_AQUA, "cyan", "aqua", "dark cyan", "dark aqua"),
	LIGHT_BLUE(DyeColor.LIGHT_BLUE, ChatColor.AQUA, "light blue", "light cyan", "light aqua"),
	
	DARK_GREEN(DyeColor.GREEN, ChatColor.DARK_GREEN, "green", "dark green"),
	LIGHT_GREEN(DyeColor.LIME, ChatColor.GREEN, "light green", "lime", "lime green"),
	
	YELLOW(DyeColor.YELLOW, ChatColor.YELLOW, "yellow", "light yellow"),
	ORANGE(DyeColor.ORANGE, ChatColor.GOLD, "orange", "gold", "dark yellow"),
	
	DARK_RED(DyeColor.RED, ChatColor.DARK_RED, "red", "dark red"),
	LIGHT_RED(DyeColor.PINK, ChatColor.RED, "pink", "light red"),
	
	DARK_PURPLE(DyeColor.PURPLE, ChatColor.DARK_PURPLE, "purple", "dark purple"),
	LIGHT_PURPLE(DyeColor.MAGENTA, ChatColor.LIGHT_PURPLE, "magenta", "light purple"),
	
	BROWN(DyeColor.BROWN, ChatColor.BLUE, "brown", "indigo");
	
	private final DyeColor wool;
	private final ChatColor chat;
	private final String[] names;
	
	private final static Map<String, Color> byName = new HashMap<String, Color>();
	static {
		for (final Color c : values()) {
			for (final String name : c.names) {
				byName.put(name, c);
			}
		}
	}
	
	private final static Color[] byWool = new Color[16];
	static {
		for (final Color c : values()) {
			byWool[c.getWool()] = c;
		}
	}
	
	private Color(final DyeColor wool, final ChatColor chat, final String... names) {
		this.wool = wool;
		this.chat = chat;
		this.names = names;
		assert names.length > 0;
	}
	
	public byte getDye() {
		return (byte) (15 - wool.getData());
	}
	
	public DyeColor getWoolColor() {
		return wool;
	}
	
	public byte getWool() {
		return wool.getData();
	}
	
	public String getChat() {
		return chat.toString();
	}
	
	@Override
	public String toString() {
		return names[0];
	}
	
	public static final Color byName(final String name) {
		return byName.get(name.toLowerCase());
	}
	
	public static final Color byWool(final short data) {
		if (data < 0 || data >= 16)
			return null;
		return byWool[data];
	}
	
	public static final Color byDye(final short data) {
		if (data < 0 || data >= 16)
			return null;
		return byWool[15 - data];
	}
	
}
