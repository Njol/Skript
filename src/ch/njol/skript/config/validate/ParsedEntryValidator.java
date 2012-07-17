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

package ch.njol.skript.config.validate;

import ch.njol.skript.Skript;
import ch.njol.skript.api.Parser;
import ch.njol.skript.api.intern.SkriptAPIException;
import ch.njol.skript.config.EntryNode;
import ch.njol.skript.config.Node;
import ch.njol.util.Setter;

/**
 * @author Peter Güttinger
 * 
 */
public class ParsedEntryValidator<T> extends EntryValidator {
	
	private final Parser<? extends T> parser;
	private final Setter<T> setter;
	
	public ParsedEntryValidator(final Class<T> c, final Setter<T> setter) {
		parser = Skript.getParser(c);
		if (parser == null)
			throw new SkriptAPIException("There's no parser registered for " + c.getName());
		this.setter = setter;
	}
	
	@Override
	public boolean validate(final Node node) {
		if (!super.validate(node))
			return false;
		final T t = parser.parse(((EntryNode) node).getValue(), null);
		if (t == null)
			return false;
		setter.set(t);
		return true;
	}
	
}
