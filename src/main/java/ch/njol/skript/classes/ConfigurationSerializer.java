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

package ch.njol.skript.classes;

import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.configuration.serialization.ConfigurationSerializable;

/**
 * @author Peter Güttinger
 */
public class ConfigurationSerializer<T extends ConfigurationSerializable> implements Serializer<T> {
	
	private final Class<T> c;
	
	public ConfigurationSerializer(final Class<T> c) {
		this.c = c;
	}
	
	@Override
	public String serialize(final T o) {
		return serializeCS(o);
	}
	
	@Override
	public T deserialize(final String s) {
		return deserializeCS(s, c);
	}
	
	public final static String serializeCS(final ConfigurationSerializable o) {
		final YamlConfiguration y = new YamlConfiguration();
		y.set("value", o);
		return y.saveToString().replace("\n", "\uFEFF");
	}
	
	@SuppressWarnings("unchecked")
	public final static <T extends ConfigurationSerializable> T deserializeCS(final String s, final Class<T> c) {
		final YamlConfiguration y = new YamlConfiguration();
		try {
			y.loadFromString(s.replace("\uFEFF", "\n"));
		} catch (final InvalidConfigurationException e) {
			return null;
		}
		final Object o = y.get("value");
		if (!c.isInstance(o))
			return null;
		return (T) o;
	}
	
	@Override
	public boolean mustSyncDeserialization() {
		return false;
	}
	
}
