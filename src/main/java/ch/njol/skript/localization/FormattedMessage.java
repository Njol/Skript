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

import ch.njol.skript.util.StaticGetter;
import ch.njol.util.Reference;

public final class FormattedMessage {
	private final String key;
	private final Object[] args;
	
	/**
	 * 
	 * @param key
	 * @param args an array of Objects, {@link StaticGetter}s and/or {@link Reference}s.
	 */
	public FormattedMessage(final String key, final Object... args) {
		assert args.length > 0;
		this.key = key;
		this.args = args;
	}
	
	@Override
	public String toString() {
		final Object[] args = this.args.clone();
		for (int i = 0; i < args.length; i++) {
			if (this.args[i] instanceof Reference)
				args[i] = ((Reference<?>) this.args[i]).get();
			if (this.args[i] instanceof StaticGetter)
				args[i] = ((StaticGetter<?>) this.args[i]).get();
		}
		return Language.format(key, args);
	}
	
}
