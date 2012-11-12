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

package ch.njol.skript;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;

import ch.njol.skript.config.Config;
import ch.njol.skript.config.EntryNode;
import ch.njol.skript.config.Node;
import ch.njol.skript.config.SectionNode;
import ch.njol.skript.config.validate.SectionValidator;
import ch.njol.skript.localization.Language;
import ch.njol.skript.log.SimpleLog;
import ch.njol.skript.log.SkriptLogger;
import ch.njol.skript.util.EnchantmentType;
import ch.njol.skript.util.ItemData;
import ch.njol.skript.util.ItemType;
import ch.njol.skript.util.Utils;
import ch.njol.util.Pair;
import ch.njol.util.Setter;

/**
 * @author Peter Güttinger
 */
public abstract class Aliases {
	
	/**
	 * Note to self: never use this, use {@link #getAlias(String)} instead.
	 */
	private final static HashMap<String, ItemType> aliases = new HashMap<String, ItemType>(2500);
	
	private final static ItemType getAlias(final String s) {
		final ItemType t = ScriptLoader.currentAliases.get(s);
		if (t != null)
			return t;
		return aliases.get(s);
	}
	
	private final static HashMap<Integer, MaterialName> materialNames = new HashMap<Integer, MaterialName>(Material.values().length);
	
	private final static ItemType everything = new ItemType();
	static {
		everything.setAll(true);
		everything.add(new ItemData());
		// this is not an alias!
	}
	
	/**
	 * 
	 * @param name mixedcase string
	 * @param value
	 * @param variations
	 * @return
	 */
	private static HashMap<String, ItemType> getAliases(final String name, final ItemType value, final HashMap<String, HashMap<String, ItemType>> variations) {
		final HashMap<String, ItemType> r = new HashMap<String, ItemType>();
		Matcher m;
		if ((m = Pattern.compile("\\[(.+?)\\]").matcher(name)).find()) {
			r.putAll(getAliases(m.replaceFirst("").replace("  ", " "), value, variations));
			r.putAll(getAliases(m.replaceFirst("$1"), value, variations));
		} else if ((m = Pattern.compile("\\((.+?)\\)").matcher(name)).find()) {
			final String[] split = m.group(1).split("\\|");
			if (split.length == 1) {
				Skript.error("brackets have a special meaning in aliases and cannot be used as usual");
			}
			for (final String s : split) {
				r.putAll(getAliases(m.replaceFirst(s), value, variations));
			}
		} else if ((m = Pattern.compile("\\{(.+?)\\}").matcher(name)).find()) {
			if (variations.get(m.group(1)) != null) {
				boolean hasDefault = false;
				for (final Entry<String, ItemType> v : variations.get(m.group(1)).entrySet()) {
					String n;
					if (v.getKey().equalsIgnoreCase("{default}")) {
						hasDefault = true;
						n = m.replaceFirst("").replace("  ", " ");
					} else {
						n = m.replaceFirst(v.getKey());
					}
					final ItemType t = v.getValue().intersection(value);
					if (t != null)
						r.putAll(getAliases(n, t, variations));
					else
						Skript.warning("'" + n + "' results in an empty alias (i.e. it doesn't map to any id/data), it will thus be ignored");
				}
				if (!hasDefault)
					r.putAll(getAliases(m.replaceFirst("").replace("  ", " "), value, variations));
			} else {
				Skript.error("unknown variation {" + m.group(1) + "}");
			}
		} else {
			r.put(name, value);
		}
		return r;
	}
	
	/**
	 * Parses & adds new aliases
	 * 
	 * @param name mixedcase string
	 * @param value
	 * @param variations
	 * @return amount of added aliases
	 */
	static int addAliases(final String name, final String value, final HashMap<String, HashMap<String, ItemType>> variations) {
		final ItemType t = parseAlias(value);
		if (t == null) {
			return 0;
		}
		final HashMap<String, ItemType> as = getAliases(name, t, variations);
		boolean printedStartingWithNumberError = false;
//		boolean printedSyntaxError = false;
		for (final Entry<String, ItemType> e : as.entrySet()) {
			final String s = e.getKey().trim().replaceAll("\\s+", " ");
			final String lc = s.toLowerCase();
			if (lc.matches("\\d+ .*")) {
				if (!printedStartingWithNumberError) {
					Skript.error("aliases must not start with a number");
					printedStartingWithNumberError = true;
				}
				continue;
			}
//			if (lc.contains(",") || lc.contains(" and ") || lc.contains(" or ")) {
//				if (!printedSyntaxError) {
//					Skript.error("aliases must not contain syntax elements (comma, 'and', 'or')");
//					printedSyntaxError = true;
//				}
//				continue;
//			}
			aliases.put(lc, e.getValue());
			//if (logSpam()) <- =P
			//	info("added alias " + s + " for " + e.getValue());
			
			if (e.getValue().getTypes().size() == 1) {
				final ItemData d = e.getValue().getTypes().get(0);
				MaterialName n = materialNames.get(Integer.valueOf(d.getId()));
				if (d.dataMin == -1 && d.dataMax == -1) {
					if (n != null) {
						if (n.name.equals("" + d.getId()))
							n.name = s;
						continue;
					}
					materialNames.put(Integer.valueOf(d.getId()), new MaterialName(d.getId(), s));
				} else {
					if (n == null)
						materialNames.put(Integer.valueOf(d.getId()), n = new MaterialName(d.getId(), "" + d.getId()));
					n.names.put(new Pair<Short, Short>(d.dataMin, d.dataMax), s);
				}
			}
		}
		return as.size();
	}
	
	private final static class MaterialName {
		private String name;
		private final int id;
		private final HashMap<Pair<Short, Short>, String> names = new HashMap<Pair<Short, Short>, String>();
		
		public MaterialName(final int id, final String name) {
			this.id = id;
			this.name = name;
		}
		
		public String toString(final short dataMin, final short dataMax) {
			if (names == null)
				return name;
			String s = names.get(new Pair<Short, Short>(dataMin, dataMax));
			if (s != null)
				return s;
			if (dataMin == -1 && dataMax == -1 || dataMin == 0 && dataMax == 0)
				return name;
			s = names.get(new Pair<Short, Short>((short) -1, (short) -1));
			if (s != null)
				return s;
			return name;
		}
		
		public String getDebugMessage(final short dataMin, final short dataMax) {
			if (names == null)
				return name;
			final String s = names.get(new Pair<Short, Short>(dataMin, dataMax));
			if (s != null)
				return s;
			if (dataMin == -1 && dataMax == -1 || dataMin == 0 && dataMax == 0)
				return name;
			return name + ":" + (dataMin == -1 ? 0 : dataMin) + (dataMin == dataMax ? "" : "-" + (dataMax == -1 ? (id <= Skript.MAXBLOCKID ? 15 : Short.MAX_VALUE) : dataMax));
		}
	}
	
	/**
	 * Gets the custom name of of a material, or the default if none is set.
	 * 
	 * @param id
	 * @param data
	 * @return
	 */
	public final static String getMaterialName(final int id, final short data) {
		return getMaterialName(id, data, data);
	}
	
	public final static String getDebugMaterialName(final int id, final short data) {
		return getDebugMaterialName(id, data, data);
	}
	
	public final static String getMaterialName(final int id, final short dataMin, final short dataMax) {
		final MaterialName n = materialNames.get(Integer.valueOf(id));
		if (n == null) {
			return "" + id;
		}
		return n.toString(dataMin, dataMax);
	}
	
	public final static String getDebugMaterialName(final int id, final short dataMin, final short dataMax) {
		final MaterialName n = materialNames.get(Integer.valueOf(id));
		if (n == null) {
			return "" + id + ":" + dataMin + (dataMax == dataMin ? "" : "-" + dataMax);
		}
		return n.getDebugMessage(dataMin, dataMax);
	}
	
	/**
	 * @return how many ids are missing an alias, including the 'any id' (-1)
	 */
	final static int addMissingMaterialNames() {
		int r = 0;
		final StringBuilder missing = new StringBuilder("There are no aliases defined for the following ids: ");
		for (final Material m : Material.values()) {
			if (materialNames.get(Integer.valueOf(m.getId())) == null) {
				materialNames.put(Integer.valueOf(m.getId()), new MaterialName(m.getId(), m.toString().toLowerCase().replace('_', ' ')));
				missing.append(m.getId() + ", ");
				r++;
			}
		}
		final MaterialName m = materialNames.get(Integer.valueOf(-1));
		if (m == null) {
			materialNames.put(Integer.valueOf(-1), new MaterialName(-1, "anything"));
			missing.append("<any>, ");
			r++;
		}
		if (r > 0)
			Skript.warning(missing.substring(0, missing.length() - 2));
		return r;
	}
	
	/**
	 * Parses an ItemType to be used as an alias, i.e. it doesn't parse 'all'/'every' and the amount.
	 * 
	 * @param s mixed case string
	 * @return
	 */
	public static ItemType parseAlias(final String s) {
		if (s == null || s.isEmpty()) {
			Skript.error("'' is not an item type");
			return null;
		}
		if (s.equals("*"))
			return everything;
		
		final ItemType t = new ItemType();
		
		final String[] types = s.split("\\s*,\\s*");
		for (final String type : types) {
			if (parseType(type, t) == null)
				return null;
		}
		
		return t;
	}
	
	/**
	 * Parses an ItemType.<br>
	 * Prints errors.
	 * 
	 * @param s
	 * @return The parsed ItemType or null if the input is invalid.
	 */
	public static ItemType parseItemType(String s) {
		if (s == null || s.isEmpty())
			return null;
		s = s.trim();
		String lc = s.toLowerCase();
		
		final ItemType t = new ItemType();
		
		if (lc.matches("\\d+ of (all|every) .+")) {
			t.setAmount(Skript.parseInt(s.split(" ", 2)[0]));
			t.setAll(true);
			s = s.split(" ", 4)[3];
		} else if (lc.matches("\\d+ (of )?.+")) {
			t.setAmount(Skript.parseInt(s.split(" ", 2)[0]));
			if (s.matches("\\d+ of .+"))
				s = s.split(" ", 3)[2];
			else
				s = s.split(" ", 2)[1];
		} else if (lc.startsWith("a ") || lc.startsWith("an ")) {
			t.setAmount(1);
			s = s.split(" ", 2)[1];
		} else if (lc.startsWith("all ") || lc.startsWith("every ")) {
			t.setAll(true);
			s = s.split(" ", 2)[1];
		}
		
		lc = s.toLowerCase();
		final String of = Language.getSpaced("enchantments.of").toLowerCase();
		int c = -1;
		outer: while ((c = lc.indexOf(of, c + 1)) != -1) {
			final ItemType t2 = t.clone();
			final SimpleLog log = SkriptLogger.startSubLog();
			if (parseType(s.substring(0, c), t2) == null) {
				log.stop();
				continue;
			}
			log.stop();
			if (t2.numTypes() == 0)
				continue;
			final Map<Enchantment, Integer> enchantments = new HashMap<Enchantment, Integer>();
			final String[] enchs = lc.substring(c + of.length(), lc.length()).split("\\s*(,|" + Language.get("and") + ")\\s*");
			for (final String ench : enchs) {
				final EnchantmentType e = EnchantmentType.parse(ench);
				if (e == null)
					continue outer;
				enchantments.put(e.getType(), e.getLevel());
			}
			t2.addEnchantments(enchantments);
			return t2;
		}
		
		if (parseType(s, t) == null)
			return null;
		
		if (t.numTypes() == 0)
			return null;
		
		return t;
	}
	
	/**
	 * Prints errors.
	 * 
	 * @param s The string holding the type, can be either a number or an alias, plus an optional data part. Case does not matter.
	 * @param t The ItemType to add the parsed ItemData(s) to (i.e. this ItemType will be modified)
	 * @return The given item type or null if the input couldn't be parsed.
	 */
	private final static ItemType parseType(final String s, final ItemType t) {
		ItemType i;
		int c = s.indexOf(':');
		if (c == -1)
			c = s.length();
		final String type = s.substring(0, c);
		ItemData data = null;
		if (c != s.length()) {
			data = parseData(s.substring(c + 1));
			if (data == null) {
				Skript.error("'" + s.substring(c) + "' is no a valid item data");
				return null;
			}
		}
		if (type.isEmpty()) {
			t.add(data == null ? new ItemData() : data);
			return t;
		} else if (type.matches("\\d+")) {
			ItemData d = new ItemData(Skript.parseInt(type));
			if (Material.getMaterial(d.getId()) == null) {
				Skript.error("There doesn't exist a material with id " + d.getId() + "!");
				return null;
			}
			if (data != null) {
				if (d.getId() <= Skript.MAXBLOCKID && (data.dataMax > 15 || data.dataMin > 15)) {
					Skript.error("Blocks only have data values from 0 to 15");
					return null;
				}
				d = d.intersection(data);
			}
			t.add(d);
			return t;
		} else if ((i = getAlias(type, t.getAmount() == 1)) != null) {
			for (ItemData d : i) {
				if (data != null) {
					if (d.getId() <= Skript.MAXBLOCKID && (data.dataMax > 15 || data.dataMin > 15)) {
						Skript.error("Blocks only have data values from 0 to 15");
						return null;
					}
					d = d.intersection(data);
				} else {
					d = d.clone();
				}
				t.add(d);
			}
			return t;
		}
		Skript.error("'" + s + "' is not an item type");
		return null;
	}
	
	/**
	 * Gets an alias from the aliases defined in the config.
	 * 
	 * @param s The alias to get, case does not matter
	 * @param singular If false plural endings will be stripped
	 * @param ignorePluralCheck Prevents warnings about invalid plural.
	 * @return The ItemType represented by the given alias or null if no such alias exists.
	 */
	private final static ItemType getAlias(String s, final boolean singular) {
		ItemType i;
		String lc = s.toLowerCase();
		if ((i = getAlias(lc)) != null)
			return i.clone();
		if (lc.startsWith("any ")) {
			return getAlias(s.substring("any ".length()), true);
		}
		final Pair<String, Boolean> p = Utils.getPlural(s);
		s = p.first;
		lc = s.toLowerCase();
		if (lc.endsWith(" block")) {
			if ((i = getAlias(s.substring(0, s.length() - " block".length()), true)) != null) {
				for (int j = 0; j < i.numTypes(); j++) {
					final ItemData d = i.getTypes().get(j);
					if (d.getId() > Skript.MAXBLOCKID) {
						i.remove(d);
						j--;
					}
				}
				if (i.getTypes().isEmpty())
					return null;
				return i;
			}
		} else if (lc.endsWith(" item")) {
			if ((i = getAlias(s.substring(0, s.length() - " item".length()), true)) != null) {
				for (int j = 0; j < i.numTypes(); j++) {
					final ItemData d = i.getTypes().get(j);
					if (d.getId() != -1 && d.getId() <= Skript.MAXBLOCKID) {
						i.remove(d);
						j--;
					}
				}
				if (i.getTypes().isEmpty())
					return null;
				return i;
			}
		}
		return getAlias(lc);
	}
	
	/**
	 * Gets the data part of an item data
	 * 
	 * @param s Everything after ':'
	 * @return ItemData with only the dataMin and dataMax set
	 */
	private final static ItemData parseData(final String s) {
		if (s.isEmpty())
			return new ItemData();
		if (!s.matches("\\d+(-\\d+)?"))
			return null;
		final ItemData t = new ItemData();
		int i = s.indexOf('-');
		if (i == -1)
			i = s.length();
		try {
			t.dataMin = Short.parseShort(s.substring(0, i));
			t.dataMax = (i == s.length() ? t.dataMin : Short.parseShort(s.substring(i + 1, s.length())));
		} catch (final NumberFormatException e) { // overflow
			Skript.error("Item datas must be between 0 and " + Short.MAX_VALUE + " (inclusive)");
			return null;
		}
		if (t.dataMax < t.dataMin) {
			Skript.error("The first number of a data range must be smaller than the second");
			return null;
		}
		return t;
	}
	
	public static void clear() {
		aliases.clear();
		materialNames.clear();
	}
	
	private static Config aliasConfig;
	
	public static void load() {
		
		try {
			aliasConfig = new Config(new File(Skript.getInstance().getDataFolder(), "aliases.sk"), false, true, "=");
		} catch (final IOException e) {
			Skript.error("Could not load the aliases config: " + e.getLocalizedMessage());
			return;
		}
		
		final ArrayList<String> aliasNodes = new ArrayList<String>();
		
		new SectionValidator()
				.addEntry("aliases", new Setter<String>() {
					@Override
					public void set(final String s) {
						for (final String n : s.split(","))
							aliasNodes.add(n.trim());
					}
				}, false)
				.setAllowUndefinedSections(true)
				.validate(aliasConfig.getMainNode());
		
		for (final Node node : aliasConfig.getMainNode()) {
			if (node instanceof SectionNode) {
				if (!aliasNodes.contains(node.getName())) {
					Skript.error("Invalid section '" + node.getName() + "'. If this is an alias section add it to 'aliases' so it will be loaded.");
				}
			}
		}
		
		final HashMap<String, HashMap<String, ItemType>> variations = new HashMap<String, HashMap<String, ItemType>>();
		int num = 0;
		for (final String an : aliasNodes) {
			final Node node = aliasConfig.getMainNode().get(an);
			SkriptLogger.setNode(node);
			if (node == null) {
				Skript.error("alias section '" + an + "' not found!");
				continue;
			}
			if (!(node instanceof SectionNode)) {
				Skript.error("aliases have to be in sections, but '" + an + "' is not a section!");
				continue;
			}
			int i = 0;
			for (final Node n : (SectionNode) node) {
				if (n instanceof EntryNode) {
					i += addAliases(((EntryNode) n).getKey(), ((EntryNode) n).getValue(), variations);
				} else if (n instanceof SectionNode) {
					if (!(n.getName().startsWith("{") && n.getName().endsWith("}"))) {
						Skript.error("unexpected non-variation section");
						continue;
					}
					final HashMap<String, ItemType> vs = new HashMap<String, ItemType>();
					for (final Node a : (SectionNode) n) {
						if (a instanceof SectionNode) {
							Skript.error("unexpected section");
							continue;
						}
						final ItemType t = Aliases.parseAlias(((EntryNode) a).getValue());
						if (t != null)
							vs.put(((EntryNode) a).getKey(), t);
					}
					variations.put(n.getName().substring(1, n.getName().length() - 1), vs);
				}
			}
			if (Skript.logHigh())
				Skript.info("loaded " + i + " alias" + (i == 1 ? "" : "es") + " from " + node.getName());
			num += i;
		}
		SkriptLogger.setNode(null);
		
		if (Skript.logNormal())
			Skript.info("loaded a total of " + num + " aliases");
		
		Aliases.addMissingMaterialNames();
		
		if (!SkriptConfig.keepConfigsLoaded)
			aliasConfig = null;
		
	}
	
}
