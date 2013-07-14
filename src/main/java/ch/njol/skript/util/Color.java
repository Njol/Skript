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
	
	BLACK(DyeColor.BLACK, ChatColor.BLACK, org.bukkit.Color.fromRGB(0x191919)),
	DARK_GREY(DyeColor.GRAY, ChatColor.DARK_GRAY, org.bukkit.Color.fromRGB(0x4C4C4C)),
	LIGHT_GREY(DyeColor.SILVER, ChatColor.GRAY, org.bukkit.Color.fromRGB(0x999999)),
	WHITE(DyeColor.WHITE, ChatColor.WHITE, org.bukkit.Color.fromRGB(0xffffff)),
	
	DARK_BLUE(DyeColor.BLUE, ChatColor.DARK_BLUE, org.bukkit.Color.fromRGB(0x334CB2)),
	BROWN(DyeColor.BROWN, ChatColor.BLUE, org.bukkit.Color.fromRGB(0x664C33)),
	DARK_CYAN(DyeColor.CYAN, ChatColor.DARK_AQUA, org.bukkit.Color.fromRGB(0x4C7F99)),
	LIGHT_CYAN(DyeColor.LIGHT_BLUE, ChatColor.AQUA, org.bukkit.Color.fromRGB(0x6699D8)),
	
	DARK_GREEN(DyeColor.GREEN, ChatColor.DARK_GREEN, org.bukkit.Color.fromRGB(0x667F33)),
	LIGHT_GREEN(DyeColor.LIME, ChatColor.GREEN, org.bukkit.Color.fromRGB(0x7FCC19)),
	
	YELLOW(DyeColor.YELLOW, ChatColor.YELLOW, org.bukkit.Color.fromRGB(0xE5E533)),
	ORANGE(DyeColor.ORANGE, ChatColor.GOLD, org.bukkit.Color.fromRGB(0xD87F33)),
	
	DARK_RED(DyeColor.RED, ChatColor.DARK_RED, org.bukkit.Color.fromRGB(0x993333)),
	LIGHT_RED(DyeColor.PINK, ChatColor.RED, org.bukkit.Color.fromRGB(0xF27FA5)),
	
	DARK_PURPLE(DyeColor.PURPLE, ChatColor.DARK_PURPLE, org.bukkit.Color.fromRGB(0x7F3FB2)),
	LIGHT_PURPLE(DyeColor.MAGENTA, ChatColor.LIGHT_PURPLE, org.bukkit.Color.fromRGB(0xB24CD8)), ;
	
	private final DyeColor wool;
	private final ChatColor chat;
	private final org.bukkit.Color bukkit;
	private String[] names = null;
	
	private Color(final DyeColor wool, final ChatColor chat, final org.bukkit.Color bukkit) {
		this.wool = wool;
		this.chat = chat;
		this.bukkit = bukkit;
	}
	
	private final static Color[] byWool = new Color[16];
	static {
		for (final Color c : values()) {
			byWool[c.wool.getData()] = c;
		}
	}
	
	private final static Map<String, Color> byName = new HashMap<String, Color>();
	private final static Map<String, Color> byEnglishName = new HashMap<String, Color>();
	static {
		Language.addListener(new LanguageChangeListener() {
			@Override
			public void onLanguageChange() {
				final boolean english = byEnglishName.isEmpty();
				byName.clear();
				for (final Color c : values()) {
					c.names = Language.getList("colors." + c.name());
					for (final String name : c.names) {
						byName.put(name.toLowerCase(), c);
						if (english)
							byEnglishName.put(name.toLowerCase(), c);
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
	
	public static final Color byEnglishName(final String name) {
		return byEnglishName.get(name.toLowerCase());
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
	
	public static final Color byWoolColor(final DyeColor color) {
		return byWool(color.getData());
	}
	
	public final org.bukkit.Color getBukkitColor() {
		return bukkit;
	}
	
}
