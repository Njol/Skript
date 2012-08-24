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
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Random;
import java.util.regex.Matcher;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Creature;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.util.Vector;

import ch.njol.skript.Skript;
import ch.njol.skript.effects.EffTeleport;
import ch.njol.util.Callback;
import ch.njol.util.Pair;
import ch.njol.util.StringUtils;
import ch.njol.util.Validate;

/**
 * Utility class.
 * 
 * @author Peter Güttinger
 */
public abstract class Utils {
	
	private Utils() {}
	
	private final static Random random = new Random();
	
	/**
	 * Finds an object in an array using {@link Object#equals(Object)}.
	 * 
	 * @param array the array to search in
	 * @param o the object to search for
	 * @return the index of the first occurrence of the given object or -1 if not found
	 */
	public static <T> int indexOf(final T[] array, final T t) {
		return indexOf(array, t, 0, array.length);
	}
	
	public static <T> int indexOf(final T[] array, final T t, final int start, final int end) {
		Validate.notNull(array, "array");
		for (int i = start; i < end; i++) {
			if (array[i] == null ? t == null : array[i].equals(t))
				return i;
		}
		return -1;
	}
	
	public static <T> boolean contains(final T[] array, final T o) {
		return indexOf(array, o) != -1;
	}
	
	public static <T> boolean containsAny(final T[] array, final T... os) {
		Validate.notNull(array, os);
		for (final T o : os) {
			if (indexOf(array, o) != -1)
				return true;
		}
		return false;
	}
	
	public static <T> boolean containsAll(final T[] array, final T... os) {
		Validate.notNull(array, os);
		for (final T o : os) {
			if (indexOf(array, o) == -1)
				return false;
		}
		return true;
	}
	
	public static int indexOf(final int[] array, final int num) {
		return indexOf(array, num, 0, array.length);
	}
	
	public static int indexOf(final int[] array, final int num, final int start) {
		return indexOf(array, num, start, array.length);
	}
	
	public static int indexOf(final int[] array, final int num, final int start, final int end) {
		Validate.notNull(array, "array");
		for (int i = start; i < end; i++) {
			if (array[i] == num)
				return i;
		}
		return -1;
	}
	
	public static final boolean contains(final int[] array, final int num) {
		return indexOf(array, num) != -1;
	}
	
	public static boolean containsIgnoreCase(final String[] array, final String s) {
		return indexOfIgnoreCase(array, s) != -1;
	}
	
	/**
	 * finds a string in an array of strings (ignoring case).
	 * 
	 * @param array the array to search in
	 * @param s the string to search for
	 * @return the index of the first occurrence of the given string or -1 if not found
	 */
	public static int indexOfIgnoreCase(final String[] array, final String s) {
		Validate.notNull(array, "array");
		int i = 0;
		for (final String a : array) {
			if (a.equalsIgnoreCase(s))
				return i;
			i++;
		}
		return -1;
	}
	
	/**
	 * Finds an object in an iterable using {@link Object#equals(Object)}.
	 * 
	 * @param iter the iterable to search in
	 * @param o the object to search for
	 * @return the index of the first occurrence of the given object or -1 if not found
	 */
	public static <T> int indexOf(final Iterable<T> iter, final T o) {
		Validate.notNull(iter, "iter");
		int i = 0;
		for (final T a : iter) {
			if (a.equals(o))
				return i;
			i++;
		}
		return -1;
	}
	
	/**
	 * Finds a string in a collection of strings (ignoring case).
	 * 
	 * @param iter the iterable to search in
	 * @param s the string to search for
	 * @return the index of the first occurrence of the given string or -1 if not found
	 */
	public static int indexOfIgnoreCase(final Iterable<String> iter, final String s) {
		Validate.notNull(iter, "iter");
		int i = 0;
		for (final String a : iter) {
			if (a.equalsIgnoreCase(s))
				return i;
			i++;
		}
		return -1;
	}
	
	/**
	 * 
	 * @param map
	 * @param key
	 * @return a new entry object
	 */
	public static <T, U> Entry<T, U> containsKey(final Map<T, U> map, final T key) {
		Validate.notNull(map, "map");
		final U u = map.get(key);
		if (u == null)
			return null;
		return new Pair<T, U>(key, u);
	}
	
	public static <U> Entry<String, U> containsKeyIgnoreCase(final Map<String, U> map, final String key) {
		Validate.notNull(map, "map");
		for (final Entry<String, U> e : map.entrySet()) {
			if (e.getKey().equalsIgnoreCase(key))
				return e;
		}
		return null;
	}
	
	public static BlockFace getBlockFace(final String s, final boolean printError) {
		Validate.notNullOrEmpty(s, "s");
		final String supper = s.toUpperCase().replace(' ', '_');
		try {
			if (supper.equals("ABOVE"))
				return BlockFace.UP;
			if (supper.equals("BELOW"))
				return BlockFace.DOWN;
			return BlockFace.valueOf(supper);
		} catch (final IllegalArgumentException e1) {
			if (supper.equals("U"))
				return BlockFace.UP;
			if (supper.equals("D"))
				return BlockFace.DOWN;
			if (supper.length() <= 3) {
				try {
					String r = "";
					for (int i = 0; i < supper.length(); i++) {
						switch (supper.charAt(i)) {
							case 'N':
								r += "NORTH_";
							break;
							case 'E':
								r += "EAST_";
							break;
							case 'S':
								r += "SOUTH_";
							break;
							case 'W':
								r += "WEST_";
							break;
							default:
								if (printError)
									Skript.error("invalid direction '" + s + "'");
								return null;
						}
					}
					return BlockFace.valueOf(r.substring(0, r.length() - 1));
				} catch (final IllegalArgumentException e2) {}
			}
		}
		if (printError)
			Skript.error("invalid direction '" + supper + "'");
		return null;
	}
	
	public static final int[] getBlockFaceDir(final BlockFace f) {
		Validate.notNull(f, "f");
		return new int[] {f.getModX(), f.getModY(), f.getModZ()};
	}
	
	public static final int getBlockFaceDir(final BlockFace f, final int axis) {
		Validate.notNull(f, "f");
		switch (axis) {
			case 0:
				return f.getModX();
			case 1:
				return f.getModY();
			case 2:
				return f.getModZ();
		}
		throw new IllegalArgumentException("axis must be between 0 and 2 inclusive");
	}
	
	public static String join(final String[] strings) {
		return join(strings, ", ");
	}
	
	public static String join(final String[] strings, final String delimiter) {
		return join(strings, delimiter, 0, strings.length);
	}
	
	public static String join(final String[] strings, final String delimiter, final int start, final int end) {
		Validate.notNull(strings, "strings");
		final StringBuilder b = new StringBuilder();
		for (int i = start; i < end; i++) {
			if (i != start)
				b.append(delimiter);
			b.append(strings[i]);
		}
		return b.toString();
	}
	
	public static String join(final Object[] objects) {
		Validate.notNull(objects, "objects");
		final StringBuilder b = new StringBuilder();
		for (int i = 0; i < objects.length; i++) {
			if (i != 0)
				b.append(", ");
			b.append(Skript.toString(objects[i]));
		}
		return b.toString();
	}
	
	public static String join(final List<?> objects) {
		Validate.notNull(objects, "objects");
		final StringBuilder b = new StringBuilder();
		for (int i = 0; i < objects.size(); i++) {
			if (i != 0)
				b.append(", ");
			b.append(Skript.toString(objects.get(i)));
		}
		return b.toString();
	}
	
	public static <T> T getRandom(final T[] os) {
		Validate.notNullOrEmpty(os, "os");
		return os[Skript.random.nextInt(os.length)];
	}
	
	public static <T> T getRandom(final T[] os, final int start) {
		Validate.notNullOrEmpty(os, "os");
		return os[Skript.random.nextInt(os.length - start) + start];
	}
	
	public static <T> T getRandom(final List<T> os) {
		Validate.notNullOrEmpty(os, "os");
		return os.get(Skript.random.nextInt(os.size()));
	}
	
	/**
	 * tests whether two item stacks are of the same type, i.e. it ignores the amounts.
	 * 
	 * @param is1
	 * @param is2
	 * @return
	 */
	public static boolean itemStacksEqual(final ItemStack is1, final ItemStack is2) {
		if (is1 == null || is2 == null)
			return is1 == is2;
		return is1.getTypeId() == is2.getTypeId() && is1.getDurability() == is2.getDurability() && is1.getEnchantments().equals(is2.getEnchantments());
	}
	
	public static Player getTargetPlayer(final Player player) {
		return getTarget(player, player.getWorld().getPlayers());
	}
	
	public static Entity getTargetEntity(final LivingEntity entity, final Class<? extends Entity> type) {
		if (entity instanceof Creature)
			return ((Creature) entity).getTarget();
		return getTarget(entity, entity.getWorld().getEntitiesByClass(type));
	}
	
	public static <T extends Entity> T getTarget(final LivingEntity entity, final Iterable<T> entities) {
		Validate.notNull(entity, entities);
		T target = null;
		double targetDistanceSquared = Double.MAX_VALUE;
		final double radiusSquared = 1;
		final Vector l = entity.getEyeLocation().toVector(), n = entity.getLocation().getDirection().normalize();
		final double cos = Math.cos(Math.PI / 4);
		for (final T other : entities) {
			if (other == entity)
				continue;
			if (target == null || targetDistanceSquared > other.getLocation().distanceSquared(entity.getLocation())) {
				final Vector t = other.getLocation().toVector().subtract(l);
				if (n.clone().crossProduct(t).lengthSquared() < radiusSquared && t.normalize().dot(n) >= cos) {
					target = other;
					targetDistanceSquared = target.getLocation().distanceSquared(entity.getLocation());
				}
			}
		}
		return target;
	}
	
	public static final Pair<String, Integer> getAmount(final String s) {
		if (s.matches("\\d+ of .+")) {
			return new Pair<String, Integer>(s.split(" ", 3)[2], Skript.parseInt(s.split(" ", 2)[0]));
		} else if (s.matches("\\d+ .+")) {
			return new Pair<String, Integer>(s.split(" ", 2)[1], Skript.parseInt(s.split(" ", 2)[0]));
		} else if (s.matches("an? .+")) {
			return new Pair<String, Integer>(s.split(" ", 2)[1], 1);
		}
		return new Pair<String, Integer>(s, Integer.valueOf(-1));
	}
	
	public static final class AmountResponse {
		public final String s;
		public final int amount;
		public final boolean every;
		
		public AmountResponse(final String s, final int amount, final boolean every) {
			this.s = s;
			this.amount = amount;
			this.every = every;
		}
		
		public AmountResponse(final String s, final boolean every) {
			this.s = s;
			amount = -1;
			this.every = every;
		}
		
		public AmountResponse(final String s, final int amount) {
			this.s = s;
			this.amount = amount;
			every = false;
		}
		
		public AmountResponse(final String s) {
			this.s = s;
			amount = -1;
			every = false;
		}
	}
	
	public static final AmountResponse getAmountWithEvery(final String s) {
		if (s.matches("\\d+ of (all|every) .+")) {
			return new AmountResponse(s.split(" ", 4)[3], Skript.parseInt(s.split(" ", 2)[0]), true);
		} else if (s.matches("\\d+ of .+")) {
			return new AmountResponse(s.split(" ", 3)[2], Skript.parseInt(s.split(" ", 2)[0]));
		} else if (s.matches("\\d+ .+")) {
			return new AmountResponse(s.split(" ", 2)[1], Skript.parseInt(s.split(" ", 2)[0]));
		} else if (s.matches("an? .+")) {
			return new AmountResponse(s.split(" ", 2)[1], 1);
		} else if (s.matches("(all|every) .+")) {
			return new AmountResponse(s.split(" ", 2)[1], true);
		}
		return new AmountResponse(s);
	}
	
	private final static String[][] plurals = {
			
			{"fe", "ves"},// most -f words' plurals can end in -fs as well as -ves
			
			{"axe", "axes"},
			{"x", "xes"},
			
			{"ay", "ays"},
			{"ey", "eys"},
			{"iy", "iys"},
			{"oy", "oys"},
			{"uy", "uys"},
			{"kie", "kies"},
			{"zombie", "zombies"},
			{"y", "ies"},
			
			{"h", "hes"},
			
			{"man", "men"},
			
			{"us", "i"},
			
			{"hoe", "hoes"},
			{"toe", "toes"},
			{"o", "oes"},
			
			{"alias", "aliases"},
			{"gas", "gases"},
			
			{"child", "children"},
			
			{"sheep", "sheep"},
			
			// general ending
			{"", "s"},
	};
	
	/**
	 * 
	 * @param s trimmed string
	 * @return Pair of singular string + boolean whether it was plural
	 */
	public static final Pair<String, Boolean> getPlural(final String s) {
		Validate.notNull(s, "s");
		if (s.isEmpty())
			return new Pair<String, Boolean>("", Boolean.FALSE);
		for (final String[] p : plurals) {
			if (s.endsWith(p[1]))
				return new Pair<String, Boolean>(s.substring(0, s.length() - p[1].length()) + p[0], Boolean.TRUE);
			if (s.endsWith(p[1].toUpperCase()))
				return new Pair<String, Boolean>(s.substring(0, s.length() - p[1].length()) + p[0].toUpperCase(), Boolean.TRUE);
		}
		return new Pair<String, Boolean>(s, Boolean.FALSE);
	}
	
	/**
	 * Gets the english plural of a word.
	 * 
	 * @param s
	 * @return
	 */
	public static final String toPlural(final String s) {
		Validate.notNullOrEmpty(s, "s");
		for (final String[] p : plurals) {
			if (s.endsWith(p[0]))
				return s.substring(0, s.length() - p[0].length()) + p[1];
		}
		assert false;
		return s + "s";
	}
	
	/**
	 * Gets the plural of a word (or not if p = false)
	 * 
	 * @param s
	 * @param p
	 * @return
	 */
	public final static String toPlural(final String s, final boolean p) {
		if (p)
			return toPlural(s);
		return s;
	}
	
	/**
	 * Adds 'a' or 'an' to the given string, depending on the first character of the string.
	 * 
	 * @param s the string to add the article to
	 * @return string with an appended a/an and a space at the beginning
	 * @see #A(String)
	 * @see #a(String, boolean)
	 */
	public static final String a(final String s) {
		return a(s, false);
	}
	
	/**
	 * Adds 'A' or 'An' to the given string, depending on the first character of the string.
	 * 
	 * @param s the string to add the article to
	 * @return string with an appended A/An and a space at the beginning
	 * @see #a(String)
	 * @see #a(String, boolean)
	 */
	public static final String A(final String s) {
		return a(s, true);
	}
	
	/**
	 * Adds 'a' or 'an' to the given string, depending on the first character of the string.
	 * 
	 * @param s the string to add the article to
	 * @param capA Whether to use a capital a or not
	 * @return string with an appended a/an (or A/An if capA is true) and a space at the beginning
	 * @see #a(String)
	 */
	public static final String a(final String s, final boolean capA) {
		Validate.notNullOrEmpty(s, "s");
		switch (Character.toLowerCase(s.charAt(0))) {
			case 'a':
			case 'e':
			case 'i':
			case 'o':
			case 'u':
				if (capA)
					return "An " + s;
				return "an " + s;
			default:
				if (capA)
					return "A " + s;
				return "a " + s;
		}
	}
	
	/**
	 * Gets the approximate collision height of non-transparent blocks at the center of the block. This is mostly for use in the {@link EffTeleport teleport effect}.
	 * 
	 * @param type
	 * @return
	 */
	public static double getBlockHeight(final Material type) {
		Validate.notNull(type, "type");
		switch (type) {
			case DIODE_BLOCK_OFF:
			case DIODE_BLOCK_ON:
				return 2. / 16;
			case TRAP_DOOR:
				return 3. / 16;
			case CAKE_BLOCK:
				return 7. / 16;
			case STEP:
				return 0.5;
			case BED_BLOCK:
				return 9. / 16;
			case ENCHANTMENT_TABLE:
				return 12. / 16;
			case ENDER_PORTAL_FRAME:
				return 13. / 16;
			case SOUL_SAND:
				return 14. / 16;
			case BREWING_STAND:
				return 14. / 16;
			case FENCE:
			case FENCE_GATE:
			case NETHER_FENCE:
				return 1.5;
			case CAULDRON:
				return 5. / 16;
			default:
				return 1;
		}
	}
	
	private final static Map<String, String> chat = new HashMap<String, String>();
	static {
		chat.put("bold", ChatColor.BOLD.toString());
		chat.put("b", ChatColor.BOLD.toString());
		
		chat.put("italics", ChatColor.ITALIC.toString());
		chat.put("italic", ChatColor.ITALIC.toString());
		chat.put("i", ChatColor.ITALIC.toString());
		
		chat.put("strikethrough", ChatColor.STRIKETHROUGH.toString());
		chat.put("strike", ChatColor.STRIKETHROUGH.toString());
		chat.put("s", ChatColor.STRIKETHROUGH.toString());
		
		chat.put("underlined", ChatColor.UNDERLINE.toString());
		chat.put("underline", ChatColor.UNDERLINE.toString());
		chat.put("u", ChatColor.UNDERLINE.toString());
		
		chat.put("magic", ChatColor.MAGIC.toString());
		
		chat.put("reset", ChatColor.RESET.toString());
		chat.put("<none>", ChatColor.RESET.toString());
	}
	
	public static final String prepareMessage(String message) {
		Validate.notNull(message, "message");
		message = replaceChatStyles(message);
		message = StringUtils.fixCapitalization(message);
		return message;
	}
	
	public final static String replaceChatStyles(String message) {
		message = StringUtils.replaceAll(message, "<([^<>]+|<none>)>", new Callback<String, Matcher>() {
			@Override
			public String run(final Matcher m) {
				final Color c = Color.byName(m.group(1));
				if (c != null)
					return c.getChat();
				final String f = chat.get(m.group(1).toLowerCase());
				if (f != null)
					return f;
				return m.group();
			}
		});
		message = ChatColor.translateAlternateColorCodes('&', message);
		return message;
	}
	
	/*
	 * Validating = find out whether a new reference is needed - it breaks several things otherwise.
	 */
	@SuppressWarnings("unchecked")
	public static final <E extends Entity> E validate(final E e) {
		if (e == null)
			return null;
		if (e instanceof Player) {// FIXME improve this
			final Player p = Bukkit.getPlayerExact(((Player) e).getName());
			return p == null ? e : (E) p;
		}
		return e;
	}
	
	private final static double sqrt2i = 1. / Math.sqrt(2);
	
	public static final BlockFace getFacing(final Location loc, final boolean horizontal) {
		final Vector dir = loc.getDirection();
		if (!horizontal) {
			if (dir.getY() > sqrt2i)
				return BlockFace.UP;
			if (dir.getY() < sqrt2i)
				return BlockFace.DOWN;
		}
		for (final BlockFace f : new BlockFace[] {BlockFace.NORTH, BlockFace.EAST, BlockFace.SOUTH, BlockFace.WEST}) {
			if (f.getModX() * dir.getX() + f.getModZ() * dir.getZ() >= sqrt2i - Skript.EPSILON)
				return f;
		}
		assert false;
		return null;
	}
	
	/**
	 * Creates a permutation of the all integers in the interval [start, end)
	 * 
	 * @param start The lowest number which will be included in the permutation
	 * @param end The highest number which will just not be included in the permutation
	 * @return an array of length end - start + 1
	 */
	public static final int[] permutation(final int start, final int end) {
		final int length = end - start + 1;
		final int[] r = new int[length];
		for (int i = 0; i < length; i++)
			r[i] = start + i;
		for (int i = 0; i < length; i++) {
			final int j = random.nextInt(length);
			final int b = r[i];
			r[i] = r[j];
			r[j] = b;
		}
		return r;
	}
	
	/**
	 * Gets a random value between the first value (inclusive) and the second value (exclusive)
	 * 
	 * @param start
	 * @param end
	 * @return
	 */
	public static int random(final int start, final int end) {
		if (end <= start)
			throw new IllegalArgumentException("end (" + end + ") must be greater than start (" + start + ")");
		return start + random.nextInt(end - start);
	}
	
}
