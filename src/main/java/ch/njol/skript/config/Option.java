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

package ch.njol.skript.config;

import ch.njol.skript.Skript;
import ch.njol.skript.classes.ClassInfo;
import ch.njol.skript.classes.Converter;
import ch.njol.skript.lang.ParseContext;
import ch.njol.skript.registrations.Classes;
import ch.njol.util.Setter;

/**
 * @author Peter Güttinger
 */
public class Option<T> {
	
	public final String key;
	private boolean optional = false;
	
	private String value = null;
	private final Converter<String, ? extends T> parser;
	private T defaultValue = null;
	private T parsedValue = null;
	
	private Setter<? super T> setter;
	
	public Option(final String key, final Class<T> c) {
		this.key = key;
		if (c == String.class) {
			parser = new Converter<String, T>() {
				@SuppressWarnings("unchecked")
				@Override
				public T convert(final String s) {
					return (T) s;
				}
			};
		} else {
			final ClassInfo<T> ci = Classes.getExactClassInfo(c);
			if (ci == null || ci.getParser() == null)
				throw new IllegalArgumentException();
			this.parser = new Converter<String, T>() {
				@Override
				public T convert(final String s) {
					final T t = ci.getParser().parse(s, ParseContext.CONFIG);
					if (t != null)
						return t;
					Skript.error("'" + s + "' is not " + ci.getName().withIndefiniteArticle());
					return null;
				}
			};
		}
	}
	
	public Option(final String key, final Converter<String, ? extends T> parser) {
		if (parser == null)
			throw new IllegalArgumentException();
		this.key = key;
		this.parser = parser;
	}
	
	public final Option<T> setter(final Setter<? super T> setter) {
		this.setter = setter;
		return this;
	}
	
	public final Option<T> optional(final boolean optional) {
		this.optional = optional;
		return this;
	}
	
	public final Option<T> defaultValue(final T def) {
		this.defaultValue = def;
		parsedValue = def;
		return this;
	}
	
	public final void set(final Config config, final String path) {
		final String oldValue = value;
		value = config.getByPath(path + key);
		if (value == null && !optional)
			Skript.error("Required entry '" + path + key + "' is missing in " + config.getFileName() + ". Please make sure that you have the latest version of the config.");
		if ((value == null ^ oldValue == null) || value != null && !value.equals(oldValue)) {
			parsedValue = value == null ? defaultValue : parser.convert(value);
			if (parsedValue == null)
				parsedValue = defaultValue;
			onValueChange();
		}
	}
	
	protected void onValueChange() {
		if (setter != null)
			setter.set(parsedValue);
	}
	
	public final T value() {
		return parsedValue;
	}
	
	public final boolean isOptional() {
		return optional;
	}
	
}
