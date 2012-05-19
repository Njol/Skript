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

import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Map.Entry;

import org.bukkit.Material;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Animals;
import org.bukkit.entity.Arrow;
import org.bukkit.entity.Boat;
import org.bukkit.entity.CaveSpider;
import org.bukkit.entity.Chicken;
import org.bukkit.entity.Cow;
import org.bukkit.entity.Creature;
import org.bukkit.entity.Creeper;
import org.bukkit.entity.Egg;
import org.bukkit.entity.Enderman;
import org.bukkit.entity.Entity;
import org.bukkit.entity.ExperienceOrb;
import org.bukkit.entity.Explosive;
import org.bukkit.entity.FallingSand;
import org.bukkit.entity.Fireball;
import org.bukkit.entity.Fish;
import org.bukkit.entity.Flying;
import org.bukkit.entity.Ghast;
import org.bukkit.entity.Giant;
import org.bukkit.entity.HumanEntity;
import org.bukkit.entity.Item;
import org.bukkit.entity.LightningStrike;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Minecart;
import org.bukkit.entity.Monster;
import org.bukkit.entity.Painting;
import org.bukkit.entity.Pig;
import org.bukkit.entity.PigZombie;
import org.bukkit.entity.Player;
import org.bukkit.entity.PoweredMinecart;
import org.bukkit.entity.Projectile;
import org.bukkit.entity.Sheep;
import org.bukkit.entity.Silverfish;
import org.bukkit.entity.Skeleton;
import org.bukkit.entity.Slime;
import org.bukkit.entity.Snowball;
import org.bukkit.entity.Spider;
import org.bukkit.entity.Squid;
import org.bukkit.entity.StorageMinecart;
import org.bukkit.entity.TNTPrimed;
import org.bukkit.entity.Vehicle;
import org.bukkit.entity.WaterMob;
import org.bukkit.entity.Wolf;
import org.bukkit.entity.Zombie;
import org.bukkit.event.Event;
import org.bukkit.inventory.ItemStack;
import org.bukkit.util.Vector;

import ch.njol.skript.Skript;
import ch.njol.skript.SkriptLogger;
import ch.njol.skript.effects.EffTeleport;
import ch.njol.util.Pair;

/**
 * 
 * Utility class. these functions are really useful if you intend to make new conditions, effects and variables.
 * 
 * @author Peter Güttinger
 * 
 */
public abstract class Utils {
	private Utils() {}
	
	public static boolean parseBoolean(final String s) {
		boolean r = (s.equalsIgnoreCase("true") || s.equalsIgnoreCase("yes"));
		if (r)
			return true;
		r = s.equalsIgnoreCase("false") || s.equalsIgnoreCase("no");
		if (r)
			return false;
		SkriptLogger.expectationError("boolean (true/yes or false/no)", s);
		return false;
	}
	
	public static byte parseBooleanNoError(final String s) {
		if (s.equalsIgnoreCase("true") || s.equalsIgnoreCase("yes"))
			return 1;
		if (s.equalsIgnoreCase("false") || s.equalsIgnoreCase("no"))
			return 0;
		return -1;
	}
	
	public static short parseShort(final String s) {
		try {
			return Short.parseShort(s);
		} catch (final NumberFormatException e) {
			SkriptLogger.expectationError("integer between -32768 and 32767", s);
		}
		return 0;
	}
	
	public static int parseInt(final String s) {
		try {
			return Integer.parseInt(s);
		} catch (final NumberFormatException e) {
			SkriptLogger.expectationError("integer", s);
		}
		return 0;
	}
	
	public static float parseFloat(final String s) {
		try {
			return Float.parseFloat(s);
		} catch (final NumberFormatException e) {
			SkriptLogger.expectationError("number", s);
		}
		return 0;
	}
	
	public static double parseDouble(final String s) {
		try {
			return Double.parseDouble(s);
		} catch (final NumberFormatException e) {
			SkriptLogger.expectationError("number", s);
		}
		return 0;
	}
	
	/**
	 * 
	 * @param array the array to search in
	 * @param o the object to search for
	 * @return the index of the first occurrence of the given object or -1 if not found
	 */
	public static <T> int contains(final T[] array, final T o) {
		if (array == null)
			return -1;
		int i = 0;
		for (final T a : array) {
			if (a.equals(o))
				return i;
			i++;
		}
		return -1;
	}
	
	public static int containsIgnoreCase(final String[] array, final String s) {
		if (array == null)
			return -1;
		int i = 0;
		for (final String a : array) {
			if (a.equalsIgnoreCase(s))
				return i;
			i++;
		}
		return -1;
	}
	
	/**
	 * Finds an object in an array using {@link Object#equals(Object)} to test equality.
	 * 
	 * @param array
	 * @param o
	 * @return the index of the first occurrence of the given object or -1 if not found
	 */
	public static <T> int contains(final Iterable<T> array, final T o) {
		if (array == null)
			return -1;
		int i = 0;
		for (final T a : array) {
			if (a.equals(o))
				return i;
			i++;
		}
		return -1;
	}
	
	/**
	 * finds a string in an array of strings (ignoring case).
	 * 
	 * @param array
	 * @param s
	 * @return the index of the first occurrence of the given string or -1 if not found
	 */
	public static int containsIgnoreCase(final Iterable<String> array, final String s) {
		if (array == null)
			return -1;
		int i = 0;
		for (final String a : array) {
			if (a.equalsIgnoreCase(s))
				return i;
			i++;
		}
		return -1;
	}
	
	public static <T, U> Entry<T, U> containsKey(final Map<T, U> m, final Object key) {
		if (m == null)
			return null;
		for (final Entry<T, U> e : m.entrySet()) {
			if (e.getKey().equals(key))
				return e;
		}
		return null;
	}
	
	public static <U> Entry<String, U> containsKeyIgnoreCase(final Map<String, U> m, final String key) {
		if (m == null)
			return null;
		for (final Entry<String, U> e : m.entrySet()) {
			if (e.getKey().equalsIgnoreCase(key))
				return e;
		}
		return null;
	}
	
	public static BlockFace getBlockFace(String s, final boolean printError) {
		if (s == null || s.isEmpty())
			return null;
		s = s.toUpperCase(Locale.ENGLISH).replace(' ', '_');
		try {
			if (s.equals("ABOVE"))
				return BlockFace.UP;
			if (s.equals("BELOW"))
				return BlockFace.DOWN;
			return BlockFace.valueOf(s);
		} catch (final IllegalArgumentException e1) {
			if (s.equals("U"))
				return BlockFace.UP;
			if (s.equals("D"))
				return BlockFace.DOWN;
			if (s.length() <= 3) {
				try {
					String r = "";
					for (int i = 0; i < s.length(); i++) {
						switch (s.charAt(i)) {
							case 'n':
								r += "NORTH_";
							break;
							case 'e':
								r += "EAST_";
							break;
							case 's':
								r += "SOUTH_";
							break;
							case 'w':
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
			Skript.error("invalid direction '" + s + "'");
		return null;
	}
	
	public static final int[] getBlockFaceDir(final BlockFace f) {
		return new int[] {f.getModX(), f.getModY(), f.getModZ()};
	}
	
	public static final int getBlockFaceDir(final BlockFace f, final int axis) {
		return getBlockFaceDir(f)[axis];
	}
	
	public static EntityType getEntityType(String s) {
		int amount = 1;
		if (s.matches("\\d+ .+")) {
			amount = Integer.parseInt(s.split(" ", 2)[0]);
			s = s.split(" ", 2)[1];
		} else if (s.matches("(?i)an? .*")) {
			s = s.split(" ", 2)[1];
		}
		final Pair<String, Boolean> p = getPlural(s, amount != 1);
		s = p.first;
		final Class<? extends Entity> c = getEntityClass(s.toLowerCase(Locale.ENGLISH));
		if (c == null)
			return null;
		return new EntityType(c, amount);
	}
	
	private static Class<? extends Entity> getEntityClass(final String name) {
		if (name.equals("any") || name.equals("entity")) {
			return Entity.class;
		} else if (name.equals("animal")) {
			return Animals.class;
		} else if (name.equals("arrow")) {
			return Arrow.class;
		} else if (name.equals("boat")) {
			return Boat.class;
		} else if (name.equals("chicken")) {
			return Chicken.class;
		} else if (name.equals("cow")) {
			return Cow.class;
		} else if (name.equals("creature")) {
			return Creature.class;
		} else if (name.equals("creeper")) {
			return Creeper.class;
		} else if (name.equals("egg")) {
			return Egg.class;
		} else if (name.equals("explosive")) {
			return Explosive.class;
		} else if (name.equals("falling")) {
			return FallingSand.class;
		} else if (name.equals("fireball")) {
			return Fireball.class;
		} else if (name.equals("fish")) {
			return Fish.class;
		} else if (name.equals("flying")) {
			return Flying.class;
		} else if (name.equals("ghast")) {
			return Ghast.class;
		} else if (name.equals("giant")) {
			return Giant.class;
		} else if (name.equals("human")) {
			return HumanEntity.class;
		} else if (name.equals("item")) {
			return Item.class;
		} else if (name.equals("lightning strike")) {
			return LightningStrike.class;
		} else if (name.equals("living")) {
			return LivingEntity.class;
		} else if (name.equals("minecart")) {
			return Minecart.class;
		} else if (name.equals("monster")) {
			return Monster.class;
		} else if (name.equals("painting")) {
			return Painting.class;
		} else if (name.equals("pig")) {
			return Pig.class;
		} else if (name.equals("pigzombie")) {
			return PigZombie.class;
		} else if (name.equals("player")) {
			return Player.class;
		} else if (name.equals("powered minecart")) {
			return PoweredMinecart.class;
		} else if (name.equals("projectile")) {
			return Projectile.class;
		} else if (name.equals("sheep")) {
			return Sheep.class;
		} else if (name.equals("skeleton")) {
			return Skeleton.class;
		} else if (name.equals("slime")) {
			return Slime.class;
		} else if (name.equals("snowball")) {
			return Snowball.class;
		} else if (name.equals("spider")) {
			return Spider.class;
		} else if (name.equals("squid")) {
			return Squid.class;
		} else if (name.equals("storage minecart")) {
			return StorageMinecart.class;
		} else if (name.equals("tnt")) {
			return TNTPrimed.class;
		} else if (name.equals("vehicle")) {
			return Vehicle.class;
		} else if (name.equals("water")) {
			return WaterMob.class;
		} else if (name.equals("wolf")) {
			return Wolf.class;
		} else if (name.equals("zombie")) {
			return Zombie.class;
		} else if (name.equals("cave spider")) {
			return CaveSpider.class;
		} else if (name.equals("enderman")) {
			return Enderman.class;
		} else if (name.equals("experience orb")) {
			return ExperienceOrb.class;
		} else if (name.equals("silverfish")) {
			return Silverfish.class;
		}
		return null;
	}
	
	public static String join(final VariableString[] strings, final Event e, final boolean and) {
		final StringBuilder b = new StringBuilder();
		for (int i = 0; i < strings.length; i++) {
			if (i != 0) {
				if (i != strings.length - 1)
					b.append(", ");
				else
					b.append(and ? " and " : " or ");
			}
			b.append(strings[i].getDebugMessage(e));
		}
		return b.toString();
	}
	
	public static String join(final String[] strings) {
		return join(strings, ", ");
	}
	
	public static String join(final String[] strings, final String delimiter) {
		return join(strings, delimiter, 0, strings.length);
	}
	
	public static String join(final String[] strings, final String delimiter, final int start, final int end) {
		final StringBuilder b = new StringBuilder();
		for (int i = start; i < end; i++) {
			if (i != 0)
				b.append(delimiter);
			b.append(strings[i]);
		}
		return b.toString();
	}
	
	public static String join(final Object[] objects) {
		final StringBuilder b = new StringBuilder();
		for (int i = 0; i < objects.length; i++) {
			if (i != 0)
				b.append(", ");
			b.append(Skript.toString(objects[i]));
		}
		return b.toString();
	}
	
	public static String join(final List<?> objects) {
		final StringBuilder b = new StringBuilder();
		for (int i = 0; i < objects.size(); i++) {
			if (i != 0)
				b.append(", ");
			b.append(Skript.toString(objects.get(i)));
		}
		return b.toString();
	}
	
	public static <T> T getRandom(final T[] os) {
		if (os == null || os.length == 0)
			return null;
		return os[Skript.random.nextInt(os.length)];
	}
	
	public static <T> T getRandom(final T[] os, final int start) {
		if (os == null || os.length == 0)
			return null;
		return os[Skript.random.nextInt(os.length - start) + start];
	}
	
	public static <T> T getRandom(final List<T> os) {
		if (os == null || os.size() == 0)
			return null;
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
		return is1.getTypeId() == is2.getTypeId() && is1.getData().getData() == is2.getData().getData() && is1.getDurability() == is2.getDurability();
	}
	
	/**
	 * note: changes 'from'
	 * 
	 * @param what
	 * @param from
	 * @return from
	 */
	public static final ItemStack remove(final ItemStack what, final ItemStack from) {
		if (what == null || from == null || !itemStacksEqual(what, from))
			return from;
		from.setAmount(Math.max(from.getAmount() - what.getAmount(), 0));
		return from;
	}
	
	/**
	 * note: changes 'to'
	 * 
	 * @param what
	 * @param to
	 * @return to
	 */
	public static final ItemStack add(final ItemStack what, final ItemStack to) {
		if (what == null || to == null || !itemStacksEqual(what, to))
			return to;
		to.setAmount(Math.max(to.getAmount() + what.getAmount(), 0));
		return to;
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
		if (entity == null)
			return null;
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
			return new Pair<String, Integer>(s.split(" ", 3)[2], Integer.valueOf(s.split(" ", 2)[0]));
		} else if (s.matches("\\d+ .+")) {
			return new Pair<String, Integer>(s.split(" ", 2)[1], Integer.valueOf(s.split(" ", 2)[0]));
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
			return new AmountResponse(s.split(" ", 4)[3], Integer.parseInt(s.split(" ", 2)[0]), true);
		} else if (s.matches("\\d+ of .+")) {
			return new AmountResponse(s.split(" ", 3)[2], Integer.parseInt(s.split(" ", 2)[0]));
		} else if (s.matches("\\d+ .+")) {
			return new AmountResponse(s.split(" ", 2)[1], Integer.parseInt(s.split(" ", 2)[0]));
		} else if (s.matches("an? .+")) {
			return new AmountResponse(s.split(" ", 2)[1], 1);
		} else if (s.matches("(all|every) .+")) {
			return new AmountResponse(s.split(" ", 2)[1], true);
		}
		return new AmountResponse(s);
	}
	
	/**
	 * equal to {@link #getPlural(String)}, but prints a warning if the found plural is not <code>expectPlural</code>.
	 * 
	 * @param s
	 * @param expectPlural
	 * @return
	 */
	private static Pair<String, Boolean> getPlural(final String s, final boolean expectPlural) {
		final Pair<String, Boolean> p = getPlural(s);
		if (p.second ^ expectPlural)
			Skript.warning("Possible invalid plural detected in '" + s + "'");
		return p;
	}
	
	/**
	 * 
	 * @param s trimmed string
	 * @return pair of singular string + boolean whether it was plural
	 */
	public static final Pair<String, Boolean> getPlural(final String s) {
		final String l = s.toLowerCase();
		if (l.endsWith("ves")) {
			return new Pair<String, Boolean>(s.substring(0, s.length() - 2) + "f", Boolean.TRUE);
		} else if (l.endsWith("ies")) {
			return new Pair<String, Boolean>(s.substring(0, s.length() - 3) + "y", Boolean.TRUE);
		} else if (l.endsWith("s")) {
			return new Pair<String, Boolean>(s.substring(0, s.length() - 1), Boolean.TRUE);
		} else if (l.endsWith("i")) {
			return new Pair<String, Boolean>(s.substring(0, s.length() - 1) + "us", Boolean.TRUE);
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
		if (s.endsWith("f")) {
			return s.substring(0, s.length() - 1) + "ves";
		} else if (s.endsWith("s")) {
			return s + "es";
		} else if (s.endsWith("y")) {
			return s.substring(0, s.length() - 1) + "ies";
		} else if (s.endsWith("us")) {
			return s.substring(0, s.length() - 2) + "i";
		}
		return s + "s";
	}
	
	/**
	 * Adds 'a' or 'an' to the given string, depending on the first character of the string.
	 * 
	 * @param s the string to add the article to
	 * @return string with an appended a/an and a space at the beginning
	 * @see #a(String, boolean)
	 */
	public static final String a(final String s) {
		return a(s, false);
	}
	
	/**
	 * Adds 'a' or 'an' to the given string, depending on the first character of the string.
	 * 
	 * @param s the string to add the article to
	 * @param capA Whether to use a capital a or not
	 * @return string with an appended a/an (or A/An if capA is true) and a space at the beginning
	 */
	public static final String a(final String s, final boolean capA) {
		if (s == null || s.isEmpty())
			return "";
		switch (Character.toLowerCase(s.charAt(0))) {
			case 'a':
			case 'e':
			case 'i':
			case 'o':
			case 'u':
				//			case 'y':
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
				return 1.5;
			case CAULDRON:
				return 5. / 16;
			default:
				return 1;
		}
	}
}
