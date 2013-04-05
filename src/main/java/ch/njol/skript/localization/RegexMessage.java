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

package ch.njol.skript.localization;

import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

import ch.njol.skript.Skript;

/**
 * @author Peter Güttinger
 */
public class RegexMessage extends Message {
	
	private final String prefix, suffix;
	
	private final int flags;
	
	private Pattern pattern = null;
	
	public RegexMessage(final String key, final String prefix, final String suffix, final int flags) {
		super(key);
		this.prefix = prefix;
		this.suffix = suffix;
		this.flags = flags;
	}
	
	public RegexMessage(final String key, final String prefix, final String suffix) {
		this(key, prefix, suffix, 0);
	}
	
	public Pattern getPattern() {
		validate();
		return pattern;
	}
	
	@Override
	public String toString() {
		return prefix + getValue() + suffix;
	}
	
	@Override
	protected void onValueChange() {
		try {
			pattern = Pattern.compile(prefix + getValue() + suffix, flags);
		} catch (final PatternSyntaxException e) {
			Skript.error("Invalid Regex pattern '" + getValue() + "' found in language entry '" + key + "': " + e.getLocalizedMessage());
		}
	}
	
}
