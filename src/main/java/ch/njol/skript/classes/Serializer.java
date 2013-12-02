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

package ch.njol.skript.classes;

import java.io.NotSerializableException;
import java.io.StreamCorruptedException;
import java.util.concurrent.Callable;

import org.bukkit.Bukkit;
import org.bukkit.World;

import ch.njol.skript.SkriptAPIException;
import ch.njol.skript.util.Task;
import ch.njol.yggdrasil.Fields;
import ch.njol.yggdrasil.YggdrasilSerializer;

/**
 * @author Peter Güttinger
 */
public abstract class Serializer<T> extends YggdrasilSerializer<T> {
	
	protected ClassInfo<? extends T> info = null;
	
	void register(final ClassInfo<? extends T> info) {
		assert this.info == null && info != null;
		this.info = info;
	}
	
	@Override
	public Class<? extends T> getClass(final String id) {
		return id.equals(info.getCodeName()) ? info.getC() : null;
	}
	
	@Override
	public String getID(final Class<?> c) {
		return info.getC().isAssignableFrom(c) ? info.getCodeName() : null;
	}
	
	@Override
	public <E extends T> E newInstance(final Class<E> c) {
		assert info.getC().isAssignableFrom(c);
		try {
			return c.newInstance();
		} catch (final InstantiationException e) {
			throw new SkriptAPIException("Serializer of " + info.getCodeName() + " must override newInstance(), canBeInstantiated() or mustSyncDeserialization() if its class does not have a public nullary constructor");
		} catch (final IllegalAccessException e) {
			throw new SkriptAPIException("Serializer of " + info.getCodeName() + " must override newInstance(), canBeInstantiated() or mustSyncDeserialization() if its class does not have a public nullary constructor");
		}
	}
	
	/**
	 * {@inheritDoc}
	 * <p>
	 * <b>This method must be thread-safe</b>. Use {@link Task#callSync(Callable)} if you need to serialise on Bukkit's main thread.
	 */
	@Override
	public abstract Fields serialize(T o) throws NotSerializableException;
	
	@Override
	public abstract void deserialize(T o, Fields f) throws StreamCorruptedException, NotSerializableException;
	
	/**
	 * @return Whether deserialisation must be done on Bukkit's main thread. You must override and use {@link #deserialize(Fields)} if this method returns true!
	 */
	public abstract boolean mustSyncDeserialization();
	
	@Override
	public boolean canBeInstantiated(final Class<? extends T> c) {
		assert info.getC().isAssignableFrom(c);
		return !mustSyncDeserialization();
	}
	
	@SuppressWarnings("unchecked")
	@Override
	public <E extends T> E deserialize(final Class<E> c, final Fields fields) throws StreamCorruptedException, NotSerializableException {
		assert info.getC().isAssignableFrom(c);
		return (E) deserialize(fields);
	}
	
	/**
	 * Used to deserialise Bukkit objects and other stuff that cannot be instantiated, e.g. a plugin may and should not create a new instance of {@link World}, but use
	 * {@link Bukkit#getWorld(String)} to get an existing world object.
	 * 
	 * @param fields The Fields object that holds the information about the serialised object
	 * @return The deserialised object. Must not be null (throw an exception instead).
	 * @throws StreamCorruptedException If the given data is invalid or incomplete
	 * @throws NotSerializableException
	 */
	@SuppressWarnings("unused")
	protected T deserialize(final Fields fields) throws StreamCorruptedException, NotSerializableException {
		if (false)
			throw new StreamCorruptedException();
		throw new SkriptAPIException("deserialize(Fields) has not been overridden in " + getClass());
	}
	
	/**
	 * Deserialises an object from a string returned by this serializer or an earlier version thereof.
	 * <p>
	 * This method should only return null if the input is invalid (i.e. not produced by {@link #serialize(Object)} or an older version of that method)
	 * <p>
	 * This method must only be called from Bukkit's main thread if {@link #mustSyncDeserialization()} returned true.
	 * 
	 * @param s
	 * @return The deserialised object or null if the input is invalid. An error message may be logged to specify the cause.
	 */
	@Deprecated
	public T deserialize(final String s) {
		return null; // if this method is not overridden then no objects of this class will ever have been saved using the old format, so any input is invalid.
	}
	
}
