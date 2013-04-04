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

import org.bukkit.ChatColor;
import org.bukkit.DyeColor;

import ch.njol.skript.localization.Language;
import ch.njol.skript.localization.LanguageChangeListener;

/**
 * @author Peter Güttinger
 */
@SuppressWarnings("deprecation")
public enum Color {
	
	BLACK(DyeColor.BLACK, ChatColor.BLACK),
	DARK_GREY(DyeColor.GRAY, ChatColor.DARK_GRAY),
	LIGHT_GREY(DyeColor.SILVER, ChatColor.GRAY),
	WHITE(DyeColor.WHITE, ChatColor.WHITE),
	
	DARK_BLUE(DyeColor.BLUE, ChatColor.DARK_BLUE),
	BROWN(DyeColor.BROWN, ChatColor.BLUE),
	DARK_CYAN(DyeColor.CYAN, ChatColor.DARK_AQUA),
	LIGHT_CYAN(DyeColor.LIGHT_BLUE, ChatColor.AQUA),
	
	DARK_GREEN(DyeColor.GREEN, ChatColor.DARK_GREEN),
	LIGHT_GREEN(DyeColor.LIME, ChatColor.GREEN),
	
	YELLOW(DyeColor.YELLOW, ChatColor.YELLOW),
	ORANGE(DyeColor.ORANGE, ChatColor.GOLD),
	
	DARK_RED(DyeColor.RED, ChatColor.DARK_RED),
	LIGHT_RED(DyeColor.PINK, ChatColor.RED),
	
	DARK_PURPLE(DyeColor.PURPLE, ChatColor.DARK_PURPLE),
	LIGHT_PURPLE(DyeColor.MAGENTA, ChatColor.LIGHT_PURPLE), ;
	
	private final DyeColor wool;
	private final ChatColor chat;
	private String[] names = null;
	
	private Color(final DyeColor wool, final ChatColor chat) {
		this.wool = wool;
		this.chat = chat;
	}
	
	private final static Color[] byWool = new Color[16];
	static {
		for (final Color c : values()) {
			byWool[c.wool.getData()] = c;
		}
	}
	
	private final static Map<String, Color> byName = new HashMap<String, Color>();
	static {
		Language.addListener(new LanguageChangeListener() {
			@Override
			public void onLanguageChange() {
				byName.clear();
				for (final Color c : values()) {
					c.names = Language.getList("colors." + c.name());
					for (final String name : c.names) {
						byName.put(name.toLowerCase(), c);
					}
				}
			}
		});
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
