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

package ch.njol.skript.entity;

import java.util.ArrayList;
import java.util.List;

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
import org.bukkit.entity.ItemFrame;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.MagmaCube;
import org.bukkit.entity.MushroomCow;
import org.bukkit.entity.Painting;
import org.bukkit.entity.PigZombie;
import org.bukkit.entity.Projectile;
import org.bukkit.entity.Silverfish;
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
import ch.njol.util.CollectionUtils;

/**
 * @author Peter Güttinger
 */
@SuppressWarnings("serial")
public class SimpleEntityData extends EntityData<Entity> {
	
	// TODO falling blocks
	
	private final static class SimpleEntityDataInfo {
		final String codeName;
		final Class<? extends Entity> c;
		final boolean isSupertype;
		
		SimpleEntityDataInfo(final String codeName, final Class<? extends Entity> c) {
			this(codeName, c, false);
		}
		
		SimpleEntityDataInfo(final String codeName, final Class<? extends Entity> c, final boolean isSupertype) {
			this.codeName = codeName;
			this.c = c;
			this.isSupertype = isSupertype;
		}
	}
	
	private final static List<SimpleEntityDataInfo> types = new ArrayList<SimpleEntityDataInfo>();
	static {
		types.add(new SimpleEntityDataInfo("arrow", Arrow.class));
		types.add(new SimpleEntityDataInfo("boat", Boat.class));
		types.add(new SimpleEntityDataInfo("blaze", Blaze.class));
		types.add(new SimpleEntityDataInfo("chicken", Chicken.class));
		types.add(new SimpleEntityDataInfo("cow", Cow.class));
		types.add(new SimpleEntityDataInfo("cave spider", CaveSpider.class));
		types.add(new SimpleEntityDataInfo("egg", Egg.class));
		types.add(new SimpleEntityDataInfo("ender crystal", EnderCrystal.class));
		types.add(new SimpleEntityDataInfo("ender dragon", EnderDragon.class));
		types.add(new SimpleEntityDataInfo("ender pearl", EnderPearl.class));
		types.add(new SimpleEntityDataInfo("fireball", Fireball.class));
		types.add(new SimpleEntityDataInfo("small fireball", SmallFireball.class));
		types.add(new SimpleEntityDataInfo("fish", Fish.class));
		types.add(new SimpleEntityDataInfo("ghast", Ghast.class));
		types.add(new SimpleEntityDataInfo("giant", Giant.class));
		types.add(new SimpleEntityDataInfo("iron golem", IronGolem.class));
		types.add(new SimpleEntityDataInfo("dropped item", Item.class));
		types.add(new SimpleEntityDataInfo("magma cube", MagmaCube.class));
		types.add(new SimpleEntityDataInfo("mooshroom", MushroomCow.class));
		types.add(new SimpleEntityDataInfo("painting", Painting.class));
		types.add(new SimpleEntityDataInfo("zombie pigman", PigZombie.class));
		types.add(new SimpleEntityDataInfo("silverfish", Silverfish.class));
		types.add(new SimpleEntityDataInfo("slime", Slime.class));
		types.add(new SimpleEntityDataInfo("snowball", Snowball.class));
		types.add(new SimpleEntityDataInfo("snow golem", Snowman.class));
		types.add(new SimpleEntityDataInfo("spider", Spider.class));
		types.add(new SimpleEntityDataInfo("squid", Squid.class));
		types.add(new SimpleEntityDataInfo("bottle of enchanting", ThrownExpBottle.class));
		types.add(new SimpleEntityDataInfo("thrown potion", ThrownPotion.class));
		types.add(new SimpleEntityDataInfo("tnt", TNTPrimed.class));
		types.add(new SimpleEntityDataInfo("zombie", Zombie.class));
		
		if (Skript.isRunningMinecraft(1, 4)) {
			types.add(new SimpleEntityDataInfo("item frame", ItemFrame.class));
			types.add(new SimpleEntityDataInfo("bat", Bat.class));
			types.add(new SimpleEntityDataInfo("witch", Witch.class));
			types.add(new SimpleEntityDataInfo("wither", Wither.class));
			types.add(new SimpleEntityDataInfo("wither skull", WitherSkull.class));
		}
		
		// TODO !Update with every version [entities]
		
		// supertypes
		types.add(new SimpleEntityDataInfo("human", HumanEntity.class, true));
		types.add(new SimpleEntityDataInfo("creature", Creature.class, true));
		types.add(new SimpleEntityDataInfo("projectile", Projectile.class, true));
		types.add(new SimpleEntityDataInfo("living entity", LivingEntity.class, true));
		types.add(new SimpleEntityDataInfo("entity", Entity.class, true));
	}
	
	static {
		final String[] codeNames = new String[types.size()];
		int i = 0;
		for (final SimpleEntityDataInfo info : types) {
			codeNames[i++] = info.codeName;
		}
		EntityData.register(SimpleEntityData.class, "simple", Entity.class, codeNames);
	}
	
	public SimpleEntityData() {
		this(Entity.class);
	}
	
	public SimpleEntityData(final Class<? extends Entity> c) {
		assert c != null && c.isInterface() : c;
		super.info = getInfo(getClass());
		int i = 0;
		for (final SimpleEntityDataInfo info : types) {
			if (info.c == c || info.isSupertype && info.c.isAssignableFrom(c)) {
				this.info = info;
				matchedPattern = i;
				return;
			}
			i++;
		}
		assert false;
	}
	
	private SimpleEntityDataInfo info;
	
	@Override
	protected boolean init(final Literal<?>[] exprs, final int matchedPattern, final ParseResult parseResult) {
		info = types.get(matchedPattern);
		return true;
	}
	
	@Override
	public void set(final Entity entity) {}
	
	@Override
	public boolean match(final Entity e) {
		return info.isSupertype ? info.c.isAssignableFrom(e.getClass()) : CollectionUtils.contains(e.getClass().getInterfaces(), info.c);
	}
	
	@Override
	public Class<? extends Entity> getType() {
		return info.c;
	}
	
	@Override
	public int hashCode() {
		return info.c.hashCode();
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
		return info.c == other.info.c;
	}
	
	@Override
	public String serialize() {
		return info.c.getName();
	}
	
	@Override
	protected boolean deserialize(final String s) {
		try {
			final Class<?> c = Class.forName(s);
			for (final SimpleEntityDataInfo i : types) {
				if (i.c == c) {
					info = i;
					return true;
				}
			}
			return false;
		} catch (final ClassNotFoundException e) {
			return false;
		}
	}
	
	@Override
	protected boolean isSupertypeOf_i(final EntityData<? extends Entity> e) {
		return e.getType() == info.c || info.isSupertype && info.c.isAssignableFrom(e.getType());
	}
	
}
