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

import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Map.Entry;

import org.bukkit.entity.Arrow;
import org.bukkit.entity.Boat;
import org.bukkit.entity.Chicken;
import org.bukkit.entity.Cow;
import org.bukkit.entity.Creature;
import org.bukkit.entity.Creeper;
import org.bukkit.entity.Egg;
import org.bukkit.entity.EnderCrystal;
import org.bukkit.entity.EnderDragon;
import org.bukkit.entity.EnderPearl;
import org.bukkit.entity.Enderman;
import org.bukkit.entity.Entity;
import org.bukkit.entity.ExperienceOrb;
import org.bukkit.entity.Fireball;
import org.bukkit.entity.Fish;
import org.bukkit.entity.Ghast;
import org.bukkit.entity.Giant;
import org.bukkit.entity.HumanEntity;
import org.bukkit.entity.IronGolem;
import org.bukkit.entity.Item;
import org.bukkit.entity.MagmaCube;
import org.bukkit.entity.Minecart;
import org.bukkit.entity.MushroomCow;
import org.bukkit.entity.Ocelot;
import org.bukkit.entity.Painting;
import org.bukkit.entity.Pig;
import org.bukkit.entity.PigZombie;
import org.bukkit.entity.Player;
import org.bukkit.entity.PoweredMinecart;
import org.bukkit.entity.Sheep;
import org.bukkit.entity.Silverfish;
import org.bukkit.entity.Skeleton;
import org.bukkit.entity.Slime;
import org.bukkit.entity.Snowball;
import org.bukkit.entity.Snowman;
import org.bukkit.entity.Spider;
import org.bukkit.entity.Squid;
import org.bukkit.entity.StorageMinecart;
import org.bukkit.entity.TNTPrimed;
import org.bukkit.entity.ThrownExpBottle;
import org.bukkit.entity.ThrownPotion;
import org.bukkit.entity.Wolf;
import org.bukkit.entity.Zombie;

import ch.njol.util.Pair;
import ch.njol.util.Validate;

/**
 * 
 * @author Peter Güttinger
 * 
 */
public class EntityType {
	// TODO: powered creepers, angry wolves, etc.
	// creepers: bool (powered)
	// wolves: bool (angry), String (owner)
	// sheep: byte (color), bool (sheared)
	// TNT: int? (fuse length)
	// enderman: int + short (held item with data)
	// xp orb: int (xp)
	// item: itemstack (id, data, amount, enchantments)
	// powered minecart: int (powered ticks)
	// ocelot: bool? (tamed)
	//
	// i.e. all sorts of data => how to organize?
	
	public final Class<? extends Entity> c;
	
	public int amount = 1;
	
	public EntityType(final Class<? extends Entity> c, final int amount) {
		Validate.notNull(c);
		this.c = c;
		this.amount = amount;
	}
	
	public EntityType(final Entity e) {
		c = e.getClass();
	}
	
	public boolean isInstance(final Entity entity) {
		return c.isInstance(entity);
	}
	
	@Override
	public String toString() {
		return amount == 1 ? entityName(c) : amount + " " + Utils.toPlural(entityName(c));
	}
	
	public static String toString(final Entity e) {
		return new EntityType(e).toString();
	}
	
	public boolean sameType(final EntityType other) {
		if (other == null)
			return false;
		return c == other.c;
	}
	
	@Override
	public int hashCode() {
		return amount * c.hashCode();
	}
	
	@Override
	public boolean equals(final Object obj) {
		if (obj == this)
			return true;
		if (!(obj instanceof EntityType))
			return false;
		final EntityType other = (EntityType) obj;
		return (amount == other.amount && c == other.c);
	}
	
	private final static Map<String, Class<? extends Entity>> names = new LinkedHashMap<String, Class<? extends Entity>>();
	static {
		names.put("arrow", Arrow.class);
		names.put("boat", Boat.class);
		names.put("chicken", Chicken.class);
		names.put("cow", Cow.class);
		names.put("creeper", Creeper.class);
		names.put("egg", Egg.class);
		names.put("ender crystal", EnderCrystal.class);
		names.put("ender dragon", EnderDragon.class);
		names.put("enderman", Enderman.class);
		names.put("ender pearl", EnderPearl.class);
		names.put("experience orb", ExperienceOrb.class);
		names.put("fireball", Fireball.class);
		names.put("fish", Fish.class);
		names.put("ghast", Ghast.class);
		names.put("giant", Giant.class);
		names.put("iron golem", IronGolem.class);
		names.put("dropped item", Item.class);
		names.put("magma cube", MagmaCube.class);
		names.put("powered minecart", PoweredMinecart.class);
		names.put("storage minecart", StorageMinecart.class);
		names.put("minecart", Minecart.class);
		names.put("mooshroom", MushroomCow.class);
		names.put("ocelot", Ocelot.class);
		names.put("painting", Painting.class);
		names.put("pig", Pig.class);
		names.put("pig zombie", PigZombie.class);
		names.put("player", Player.class);
		names.put("sheep", Sheep.class);
		names.put("silverfish", Silverfish.class);
		names.put("skeleton", Skeleton.class);
		names.put("slime", Slime.class);
		names.put("snowball", Snowball.class);
		names.put("snowman", Snowman.class);
		names.put("spider", Spider.class);
		names.put("squid", Squid.class);
		names.put("bottle o' enchanting", ThrownExpBottle.class);
		names.put("thrown potion", ThrownPotion.class);
		names.put("TNT", TNTPrimed.class);
		names.put("tnt", TNTPrimed.class);
		names.put("wolf", Wolf.class);
		names.put("zombie", Zombie.class);
		
		// supertypes
		names.put("human", HumanEntity.class);
		names.put("creature", Creature.class);
		names.put("entity", Entity.class);
	}
	
	public static EntityType parse(String s) {
		int amount = 1;
		if (s.matches("\\d+ .+")) {
			amount = Integer.parseInt(s.split(" ", 2)[0]);
			s = s.split(" ", 2)[1];
		} else if (s.matches("(?i)an? .+")) {
			s = s.split(" ", 2)[1];
		}
		final Pair<String, Boolean> p = Utils.getPlural(s, amount != 1);
		s = p.first;
		final Class<? extends Entity> c = names.get(s.toLowerCase(Locale.ENGLISH));
		if (c == null)
			return null;
		return new EntityType(c, amount);
	}
	
	private final static String entityName(final Class<? extends Entity> c) {
		for (final Entry<String, Class<? extends Entity>> p : names.entrySet()) {
			if (p.getValue().isAssignableFrom(c))
				return p.getKey();
		}
		return "unknown entity type";
	}
	
}
