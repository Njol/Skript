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

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Ageable;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;

import ch.njol.skript.Skript;
import ch.njol.skript.SkriptAPIException;
import ch.njol.skript.classes.ClassInfo;
import ch.njol.skript.classes.Parser;
import ch.njol.skript.classes.Serializer;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.Literal;
import ch.njol.skript.lang.ParseContext;
import ch.njol.skript.lang.SkriptParser;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.skript.lang.SyntaxElement;
import ch.njol.skript.lang.SyntaxElementInfo;
import ch.njol.skript.lang.util.SimpleLiteral;
import ch.njol.skript.localization.Language;
import ch.njol.skript.localization.Noun;
import ch.njol.skript.registrations.Classes;
import ch.njol.util.Kleenean;
import ch.njol.util.coll.CollectionUtils;

/**
 * @author Peter Güttinger
 */
@SuppressWarnings({"rawtypes", "serial"})
public abstract class EntityData<E extends Entity> implements SyntaxElement {// TODO extended horse support, zombie villagers // TODO unit

	// must be here to be initialised before 'new SimpleLiteral' is called in the register block below
	private static final List<EntityDataInfo<?>> infos = new ArrayList<EntityDataInfo<?>>();
	
	public static Serializer<EntityData> serializer = new Serializer<EntityData>() {
		@Override
		public String serialize(final EntityData d) {
			return getInfo((Class<? extends EntityData<?>>) d.getClass()).codeName + ":" + d.serialize();
		}
		
		@Override
		public EntityData deserialize(final String s) {
			final String[] split = s.split(":", 2);
			if (split.length != 2)
				return null;
			final EntityDataInfo<?> i = getInfo(split[0]);
			if (i == null)
				return null;
			EntityData<?> d;
			try {
				d = i.c.newInstance();
			} catch (final Exception e) {
				Skript.exception(e, "Can't create an instance of " + i.c.getCanonicalName());
				return null;
			}
			if (!d.deserialize(split[1]))
				return null;
			return d;
		}
		
		@Override
		public boolean mustSyncDeserialization() {
			return false;
		}
	};
	
	static {
		Classes.registerClass(new ClassInfo<EntityData>(EntityData.class, "entitydata")
				.user("entity ?types?")
				.name("Entity Type")
				.description("The type of an <a href='#entity'>entity</a>, e.g. player, wolf, powered creeper, etc.")
				.usage("<i>Detailed usage will be added eventually</i>")
				.examples("victim is a cow",
						"spawn a creeper")
				.since("1.3")
				.defaultExpression(new SimpleLiteral<EntityData>(new SimpleEntityData(Entity.class), true))
				.before("entitytype")
				.parser(new Parser<EntityData>() {
					@Override
					public String toString(final EntityData d, final int flags) {
						return d.toString(flags);
					}
					
					@Override
					public EntityData parse(final String s, final ParseContext context) {
						return EntityData.parse(s);
					}
					
					@Override
					public String toVariableNameString(final EntityData o) {
						return "entitydata:" + o.toString();
					}
					
					@Override
					public String getVariableNamePattern() {
						return "entitydata:.+";
					}
				}).serializer(serializer));
	}
	
	private final static class EntityDataInfo<T extends EntityData<?>> extends SyntaxElementInfo<T> {
		final String codeName;
		final Class<? extends Entity> entityClass;
		final Noun[] names;
		
		public EntityDataInfo(final Class<T> dataClass, final String codeName, final Class<? extends Entity> entityClass, final String[] patterns, final Noun[] names) throws IllegalArgumentException {
			super(patterns, dataClass);
			assert codeName != null && entityClass != null && names != null && names.length > 0;
			this.codeName = codeName;
			this.entityClass = entityClass;
			this.names = names;
		}
	}
	
	static <E extends Entity, T extends EntityData<E>> void register(final Class<T> dataClass, final String name, final Class<E> entityClass, final String... codeNames) throws IllegalArgumentException {
		final String[] patterns = new String[codeNames.length];
		final Noun[] names = new Noun[codeNames.length];
		final String agePattern = Language.get("entities.age pattern");
		for (int i = 0; i < codeNames.length; i++) {
			assert codeNames[i] != null;
			patterns[i] = Language.get("entities." + codeNames[i] + ".pattern").replace("<age>", agePattern);
			names[i] = new Noun("entities." + codeNames[i] + ".name");
		}
		final EntityDataInfo<T> info = new EntityDataInfo<T>(dataClass, name, entityClass, patterns, names);
		for (int i = 0; i < infos.size(); i++) {
			if (infos.get(i).entityClass.isAssignableFrom(entityClass)) {
				infos.add(i, info);
				return;
			}
		}
		infos.add(info);
	}
	
	public final static EntityDataInfo<?> getInfo(final Class<? extends EntityData<?>> c) {
		for (final EntityDataInfo<?> i : infos) {
			if (i.c == c)
				return i;
		}
		throw new SkriptAPIException("Unregistered EntityData class " + c.getName());
	}
	
	public final static EntityDataInfo<?> getInfo(final String codeName) {
		for (final EntityDataInfo<?> i : infos) {
			if (i.codeName.equals(codeName))
				return i;
		}
		return null;
	}
	
	/**
	 * Prints errors.
	 * 
	 * @param s
	 * @return
	 */
	public final static EntityData<?> parse(final String s) {
		return SkriptParser.parseStatic(Noun.stripIndefiniteArticle(s), infos.iterator(), null);
	}
	
	/**
	 * Prints errors.
	 * 
	 * @param s
	 * @return
	 */
	public final static EntityData<?> parseWithoutIndefiniteArticle(final String s) {
		return SkriptParser.parseStatic(s, infos.iterator(), null);
	}
	
	public E spawn(final Location loc) {
		assert loc != null;
		try {
			final E e = loc.getWorld().spawn(loc, getType());
			if (baby.isTrue() && e instanceof Ageable)
				((Ageable) e).setBaby();
			set(e);
			return e;
		} catch (final IllegalArgumentException e) {
			if (Skript.testing())
				Skript.error("Can't spawn " + getType().getName());
			return null;
		}
	}
	
	public E[] getAll(final World... worlds) {
		assert worlds != null && worlds.length > 0 : Arrays.toString(worlds);
		final List<E> list = new ArrayList<E>();
		for (final World w : worlds) {
			for (final E e : w.getEntitiesByClass(getType()))
				if (match(e))
					list.add(e);
		}
		return list.toArray((E[]) Array.newInstance(getType(), list.size()));
	}
	
	/**
	 * @param types
	 * @param type
	 * @param worlds worlds or null for all
	 * @return
	 */
	public final static <E extends Entity> E[] getAll(final EntityData<?>[] types, final Class<E> type, World[] worlds) {
		assert types.length > 0;
		if (type == Player.class) {
			if (worlds == null && types.length == 1 && types[0] instanceof PlayerData && ((PlayerData) types[0]).op == 0)
				return (E[]) Bukkit.getOnlinePlayers();
			final List<Player> list = new ArrayList<Player>();
			for (final Player p : Bukkit.getOnlinePlayers()) {
				if (worlds != null && !CollectionUtils.contains(worlds, p.getWorld()))
					continue;
				for (final EntityData<?> t : types) {
					if (t.isInstance(p)) {
						list.add(p);
						break;
					}
				}
			}
			return (E[]) list.toArray(new Player[list.size()]);
		}
		final List<E> list = new ArrayList<E>();
		if (worlds == null)
			worlds = Bukkit.getWorlds().toArray(new World[0]);
		for (final World w : worlds) {
			for (final E e : w.getEntitiesByClass(type)) {
				for (final EntityData<?> t : types) {
					if (t.isInstance(e)) {
						list.add(e);
						break;
					}
				}
			}
		}
		return list.toArray((E[]) Array.newInstance(type, list.size()));
	}
	
	private static <E extends Entity> EntityData<? super E> getData(final Class<E> c, final E e) {
		assert c == null ^ e == null;
		assert c == null || c.isInterface();
		for (final EntityDataInfo<?> info : infos) {
			if (info.entityClass != Entity.class && (e == null ? info.entityClass.isAssignableFrom(c) : info.entityClass.isInstance(e))) {
				try {
					final EntityData<E> d = (EntityData<E>) info.c.newInstance();
					if (d.init(c, e))
						return d;
				} catch (final Exception ex) {
					Skript.exception(ex);
					return null;
				}
			}
		}
		return e == null ? new SimpleEntityData(c) : new SimpleEntityData(e);
	}
	
	public static <E extends Entity> EntityData<? super E> fromClass(final Class<E> c) {
		return getData(c, null);
	}
	
	public static <E extends Entity> EntityData<? super E> fromEntity(final E e) {
		return getData(null, e);
	}
	
	public static final String toString(final Entity e) {
		return fromEntity(e).getSuperType().toString();
	}
	
	public static final String toString(final Class<? extends Entity> c) {
		return fromClass(c).toString();
	}
	
	public static final String toString(final Entity e, final int flags) {
		return fromEntity(e).getSuperType().toString(flags);
	}
	
	public static final String toString(final Class<? extends Entity> c, final int flags) {
		return fromClass(c).toString(flags);
	}
	
	@SuppressWarnings("unchecked")
	public final boolean isInstance(final Entity e) {
		if (e == null)
			return false;
		if (!baby.isUnknown() && e instanceof Ageable && ((Ageable) e).isAdult() != baby.isFalse())
			return false;
		return getType().isInstance(e) && match((E) e);
	}
	
	public abstract boolean isSupertypeOf(EntityData<?> e);
	
	public abstract String serialize();
	
	protected abstract boolean deserialize(final String s);
	
	protected EntityDataInfo<?> info;
	protected int matchedPattern = 0;
	private Kleenean plural = Kleenean.UNKNOWN;
	private Kleenean baby = Kleenean.UNKNOWN;
	
	public EntityData() {
		for (final EntityDataInfo<?> i : infos) {
			if (getClass() == i.c) {
				info = i;
				break;
			}
		}
		assert info != null;
	}
	
	@Override
	public final boolean init(final Expression<?>[] exprs, final int matchedPattern, final Kleenean isDelayed, final ParseResult parseResult) {
		this.matchedPattern = matchedPattern;
		this.plural = Kleenean.get(2 - (parseResult.mark & 0x3));
		this.baby = Kleenean.get(1 - (((parseResult.mark >> 2) & 0x3) ^ 0x1));
		return init(Arrays.copyOf(exprs, exprs.length, Literal[].class), matchedPattern, parseResult);
	}
	
	protected abstract boolean init(final Literal<?>[] exprs, final int matchedPattern, final ParseResult parseResult);
	
	/**
	 * @param c null iff e != null
	 * @param e null iff c != null
	 * @return
	 */
	protected abstract boolean init(Class<? extends E> c, E e);
	
	public abstract void set(E entity);
	
	protected abstract boolean match(E entity);
	
	public abstract Class<? extends E> getType();
	
	/**
	 * Returns the super type of this entity data, e.g. 'wolf' for 'angry wolf'. If this type is already such a supertype it should return itself.
	 * 
	 * @return
	 */
	public abstract EntityData getSuperType();
	
	@Override
	public String toString() { // TODO baby/adult
		return info.names[matchedPattern].getSingular();
	}
	
	public String toString(final int flags) {
		return info.names[matchedPattern].toString(flags);
	}
	
	public Kleenean isPlural() {
		return plural;
	}
	
	public Kleenean isBaby() {
		return baby;
	}
	
	@Override
	public abstract boolean equals(Object obj);
	
	@Override
	public abstract int hashCode();
}
