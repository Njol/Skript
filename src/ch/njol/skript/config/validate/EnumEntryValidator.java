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

import java.util.Locale;

import ch.njol.skript.Skript;
import ch.njol.skript.config.EntryNode;
import ch.njol.skript.config.Node;
import ch.njol.util.Setter;
import ch.njol.util.Validate;

/**
 * @author Peter Güttinger
 * 
 */
public class EnumEntryValidator<E extends Enum<E>> extends EntryValidator {
	
	private final Class<E> enumType;
	private final Setter<E> setter;
	private String allowedValues = null;
	
	public EnumEntryValidator(final Class<E> enumType, final Setter<E> setter) {
		Validate.notNull(enumType);
		this.enumType = enumType;
		this.setter = setter;
		if (enumType.getEnumConstants().length <= 12) {
			final StringBuilder b = new StringBuilder(enumType.getEnumConstants()[0].name());
			for (final E e : enumType.getEnumConstants()) {
				if (b.length() != 0)
					b.append(", ");
				b.append(e.name());
			}
			allowedValues = b.toString();
		}
	}
	
	public EnumEntryValidator(final Class<E> enumType, final Setter<E> setter, final String allowedValues) {
		Validate.notNull(enumType);
		this.enumType = enumType;
		this.setter = setter;
		this.allowedValues = allowedValues;
	}
	
	@Override
	public boolean validate(final Node node) {
		if (!super.validate(node))
			return false;
		final EntryNode n = (EntryNode) node;
		try {
			final E e = Enum.valueOf(enumType, n.getValue().toUpperCase(Locale.ENGLISH).replace(' ', '_'));
			if (setter != null)
				setter.set(e);
		} catch (final IllegalArgumentException e) {
			Skript.error("'" + n.getValue() + "' is not a valid value for '" + n.getKey() + "'" + (allowedValues == null ? "" : ". Allowed values are: " + allowedValues));
			return false;
		}
		return true;
	}
	
}
