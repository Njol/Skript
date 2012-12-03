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

package ch.njol.skript.entity;

import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.bukkit.entity.Arrow;
import org.bukkit.entity.Bat;
import org.bukkit.entity.Blaze;
import org.bukkit.entity.Boat;
import org.bukkit.entity.CaveSpider;
import org.bukkit.entity.Chicken;
import org.bukkit.entity.Cow;
import org.bukkit.entity.Creature;
import org.bukkit.entity.Egg;
import org.bukkit.entity.EnderCrystal;
import org.bukkit.entity.EnderDragon;
import org.bukkit.entity.EnderPearl;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Fireball;
import org.bukkit.entity.Fish;
import org.bukkit.entity.Ghast;
import org.bukkit.entity.Giant;
import org.bukkit.entity.HumanEntity;
import org.bukkit.entity.IronGolem;
import org.bukkit.entity.Item;
import org.bukkit.entity.MagmaCube;
import org.bukkit.entity.MushroomCow;
import org.bukkit.entity.Painting;
import org.bukkit.entity.Pig;
import org.bukkit.entity.PigZombie;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.entity.Silverfish;
import org.bukkit.entity.Skeleton;
import org.bukkit.entity.Slime;
import org.bukkit.entity.SmallFireball;
import org.bukkit.entity.Snowball;
import org.bukkit.entity.Snowman;
import org.bukkit.entity.Spider;
import org.bukkit.entity.Squid;
import org.bukkit.entity.TNTPrimed;
import org.bukkit.entity.ThrownExpBottle;
import org.bukkit.entity.ThrownPotion;
import org.bukkit.entity.Witch;
import org.bukkit.entity.Wither;
import org.bukkit.entity.WitherSkull;
import org.bukkit.entity.Zombie;

import ch.njol.skript.Skript;
import ch.njol.skript.lang.Literal;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.skript.util.Utils;
import ch.njol.util.Pair;

/**
 * @author Peter Güttinger
 */
public class SimpleEntityData extends EntityData<Entity> {
	private static final long serialVersionUID = -1021159610746515883L;
	
	private final static Map<String, Class<? extends Entity>> names = new LinkedHashMap<String, Class<? extends Entity>>();
	private final static Set<Class<?>> superTypes = new HashSet<Class<?>>();
	static {
		names.put("arrow", Arrow.class);
		names.put("boat", Boat.class);
		names.put("blaze", Blaze.class);
		names.put("chicken", Chicken.class);
		names.put("cow", Cow.class);
		names.put("cave spider", CaveSpider.class);
		names.put("cavespider", CaveSpider.class);
		names.put("egg", Egg.class); // TODO fix comparisions of eggs, arrows, etc. (e.g. 'projectile is an arrow')
		names.put("ender crystal", EnderCrystal.class);
		names.put("ender dragon", EnderDragon.class);
		names.put("ender pearl", EnderPearl.class);
		names.put("fireball", Fireball.class);
		names.put("ghast fireball", Fireball.class);
		names.put("small fireball", SmallFireball.class);
		names.put("blaze fireball", SmallFireball.class);
		names.put("fish", Fish.class);
		names.put("ghast", Ghast.class);
		names.put("giant", Giant.class);
		names.put("giant zombie", Giant.class);
		names.put("iron golem", IronGolem.class);
		names.put("item entity", Item.class);
		names.put("dropped item", Item.class);
		names.put("magma cube", MagmaCube.class);
		names.put("magma slime", MagmaCube.class);
		names.put("mooshroom", MushroomCow.class);
		names.put("painting", Painting.class);
		names.put("pig", Pig.class);
		names.put("zombie pigman", PigZombie.class);
		names.put("pig zombie", PigZombie.class);
		names.put("pigzombie", PigZombie.class);
		names.put("player", Player.class);
		names.put("silverfish", Silverfish.class);
		names.put("skeleton", Skeleton.class);
		names.put("slime", Slime.class);
		names.put("snowball", Snowball.class);
		names.put("snow golem", Snowman.class);
		names.put("snowgolem", Snowman.class);
		names.put("snowman", Snowman.class);
		names.put("spider", Spider.class);
		names.put("squid", Squid.class);
		names.put("bottle o' enchanting", ThrownExpBottle.class);
		names.put("bottle of enchanting", ThrownExpBottle.class);
		names.put("thrown potion", ThrownPotion.class);
		names.put("primed TNT", TNTPrimed.class);
		names.put("tnt", TNTPrimed.class);
		names.put("primed tnt", TNTPrimed.class);
		names.put("zombie", Zombie.class);
		
		if (Skript.isRunningBukkit(1, 4)) {
			// TODO wither skeleton?
			names.put("bat", Bat.class);
			names.put("witch", Witch.class);
			names.put("wither", Wither.class);
			names.put("wither skull", WitherSkull.class);
		}
		
		// supertypes
		names.put("human", HumanEntity.class);
		superTypes.add(HumanEntity.class);
		names.put("creature", Creature.class);
		superTypes.add(Creature.class);
		names.put("projectile", Projectile.class);
		superTypes.add(Projectile.class);
		names.put("entity", Entity.class);
		superTypes.add(Entity.class);
	}
	
	static {
		final String[] patterns = new String[names.size() * 2];
		int i = 0;
		for (final String name : names.keySet()) {
			patterns[i++] = name;
			patterns[i++] = Utils.toPlural(name);
		}
		EntityData.register(SimpleEntityData.class, "simple", Entity.class, patterns);
	}
	
	public SimpleEntityData() {}
	
	public SimpleEntityData(final Class<? extends Entity> c) {
		assert c != null;
		this.c = c;
	}
	
	private Class<? extends Entity> c = Entity.class;
	
	private boolean plural;
	
	@Override
	protected boolean init(final Literal<?>[] exprs, final int matchedPattern, final ParseResult parseResult) {
		final Pair<String, Boolean> p = Utils.getPlural(parseResult.expr.toLowerCase());
		final String s = p.first;
		c = names.get(s);
		plural = p.second;
		if (c == null)
			return false;
		return true;
	}
	
	@Override
	public void set(final Entity entity) {}
	
	@Override
	public boolean isInstance(final Entity e) {
		return superTypes.contains(c) ? c.isAssignableFrom(e.getClass()) : e.getClass() == c;
	}
	
	@Override
	public boolean match(final Entity entity) {
		return true;
	}
	
	@Override
	public Class<? extends Entity> getType() {
		return c;
	}
	
	private transient String name = null;
	
	@Override
	public String toString() {
		if (name == null) {
			for (final Entry<String, Class<? extends Entity>> e : names.entrySet()) {
				if (e.getValue().isAssignableFrom(c)) {
					return name = e.getKey();
				}
			}
			assert false;
		}
		return name;
	}
	
	@Override
	public boolean isPlural() {
		return plural;
	}
	
	@Override
	public int hashCode() {
		return c.hashCode();
	}
	
	@Override
	public boolean equals(final Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (!(obj instanceof SimpleEntityData))
			return false;
		final SimpleEntityData other = (SimpleEntityData) obj;
		return c == other.c;
	}
	
	@Override
	public String serialize() {
		return c.getName();
	}
	
	@Override
	protected boolean deserialize(final String s) {
		try {
			c = (Class<? extends Entity>) Class.forName(s);
			return true;
		} catch (final ClassNotFoundException e) {
			return false;
		}
	}
	
}
