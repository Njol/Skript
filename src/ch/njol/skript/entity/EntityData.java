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

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;

import ch.njol.skript.Skript;
import ch.njol.skript.classes.ClassInfo;
import ch.njol.skript.classes.Parser;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.Literal;
import ch.njol.skript.lang.ParseContext;
import ch.njol.skript.lang.SkriptParser;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.skript.lang.SyntaxElement;
import ch.njol.skript.lang.SyntaxElementInfo;
import ch.njol.util.Validate;

/**
 * @author Peter Güttinger
 * 
 */
@SuppressWarnings("rawtypes")
public abstract class EntityData<E extends Entity> implements SyntaxElement {
	
	static {
		Skript.registerClass(new ClassInfo<EntityData>(EntityData.class, "entitydata", "entity type")
				.user("entity ?types?", "entit(y|ies)")
				.parser(new Parser<EntityData>() {
					@Override
					public String toString(final EntityData d) {
						return d.toString();
					}
					
					@Override
					public EntityData parse(final String s, final ParseContext context) {
						return EntityData.parse(s);
					}
					
					@Override
					public String toCodeString(final EntityData o) {
						return "entitydata:" + o.toString();
					}
				})/*.serializer(new Serializer<EntityData>() {
					@SuppressWarnings("unchecked")
					@Override
					public String serialize(final EntityData d) {
						final String s = d.serialize();
						return classes.getKey(d.getType()) + (s == null || s.isEmpty() ? "" : ":" + s);
					}
					
					@Override
					public EntityData deserialize(final String s) {
						final String[] split = s.split(":", 2);
						
						final Class<? extends Entity> c = classes.get(split[0]);
						if (c == null)
							return null;
						try {
							final EntityData d = types.get(c).newInstance();
							if (split.length == 1)
								return d;
							return d.deserialize(split[1]) ? d : null;
						} catch (final Exception e) {
							return null;
						}
					}
					})*/);
	}
	
	private final static class EntityDataInfo<T extends EntityData<?>> extends SyntaxElementInfo<T> {
		
		@SuppressWarnings("unused")
		final String codeName;
		final Class<? extends Entity> entityClass;
		
		public EntityDataInfo(final Class<T> dataClass, final String codeName, final Class<? extends Entity> entityClass, final String[] patterns) {
			super(patterns, dataClass);
			this.codeName = codeName;
			this.entityClass = entityClass;
		}
	}
	
	private static List<EntityDataInfo<?>> infos = new ArrayList<EntityDataInfo<?>>();
	
	static <E extends Entity, T extends EntityData<E>> void register(final Class<T> dataClass, final String name, final Class<E> entityClass, final String... patterns) {
		final EntityDataInfo<T> info = new EntityDataInfo<T>(dataClass, name, entityClass, patterns);
		for (int i = 0; i < infos.size(); i++) {
			if (infos.get(i).entityClass.isAssignableFrom(entityClass)) {
				infos.add(i, info);
				return;
			}
		}
		infos.add(info);
	}
	
	@Override
	public boolean init(final Expression<?>[] exprs, final int matchedPattern, final boolean isDelayed, final ParseResult parseResult) {
		return init(Arrays.copyOf(exprs, exprs.length, Literal[].class), matchedPattern, parseResult);
	}
	
	/**
	 * Prints errors.
	 * 
	 * @param s
	 * @return
	 */
	@SuppressWarnings("unchecked")
	public final static EntityData<?> parse(String s) {
		final String lower = s.toLowerCase();
		if (lower.startsWith("a "))
			s = s.substring(2);
		else if (lower.startsWith("an "))
			s = s.substring(3);
		else if (lower.startsWith("any "))
			s = s.substring(4);
		return SkriptParser.parseStatic(s, (Iterator) infos.iterator(), null);
	}
	
	/**
	 * Prints errors.
	 * 
	 * @param s
	 * @return
	 */
	@SuppressWarnings("unchecked")
	public final static EntityData<?> parseWithoutAnOrAny(final String s) {
		return SkriptParser.parseStatic(s, (Iterator) infos.iterator(), s);
	}
	
	public E spawn(final Location loc) {
		Validate.notNull(loc, "loc");
		try {
			final E e = loc.getWorld().spawn(loc, getType());
			set(e);
			return e;
		} catch (final IllegalArgumentException e) {
			return null;
		}
	}
	
	public E[] getAll(final World... worlds) {
		final List<E> list = new ArrayList<E>();
		for (final World w : worlds) {
			for (final E e : w.getEntitiesByClass(getType()))
				if (match(e))
					list.add(e);
		}
		return list.toArray((E[]) Array.newInstance(getType(), list.size()));
	}
	
	/**
	 * 
	 * @param types
	 * @param type
	 * @param worlds world or null for all
	 * @return
	 */
	public final static <E extends Entity> E[] getAll(final EntityData<?>[] types, final Class<E> type, World[] worlds) {
		if (worlds == null && type == Player.class)
			return (E[]) Bukkit.getOnlinePlayers();
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
	
	public static <E extends Entity> EntityData<? super E> fromClass(final Class<E> c) {
		for (final EntityDataInfo<?> info : infos) {
			if (info.entityClass != Entity.class && info.entityClass.isAssignableFrom(c)) {
				try {
					return (EntityData<E>) info.c.newInstance();
				} catch (final Exception e) {
					assert false;
					return null;
				}
			}
		}
		return new SimpleEntityData(c);
	}
	
	public static <E extends Entity> EntityData<? super E> fromEntity(final E e) {
		return fromClass((Class<E>) e.getClass());
	}
	
	public static final String toString(final Entity e) {
		return fromEntity(e).toString();
	}
	
	public static final String toString(final Class<? extends Entity> c) {
		return fromClass(c).toString();
	}
	
	@SuppressWarnings("unchecked")
	public boolean isInstance(final Entity e) {
		return getType().isInstance(e) && match((E) e);
	}
	
	protected abstract boolean init(Literal<?>[] exprs, int matchedPattern, ParseResult parseResult);
	
	protected abstract void set(E entity);
	
	protected abstract boolean match(E entity);
	
	public abstract Class<? extends E> getType();
	
	@Override
	public abstract String toString();
	
	public abstract boolean isPlural();
	
//	/**
//	 * Serializes this entity data.
//	 * 
//	 * @return additional data to save or null if there's none.
//	 */
//	public abstract String serialize();
//	
//	/**
//	 * Deserializes this entity data. This method is not called if {@link #serialize()} returned an empty string or null.
//	 * 
//	 * @param s
//	 * @return
//	 */
//	public abstract boolean deserialize(String s);
	
}
