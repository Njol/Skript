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

import ch.njol.skript.variables.Variables;
import ch.njol.yggdrasil.Fields;
import ch.njol.yggdrasil.YggdrasilSerializable;

/**
 * @author Peter Güttinger
 */
public class YggdrasilSerializer<T extends YggdrasilSerializable> extends Serializer<T> {
	
	@Override
	public Fields serialize(final T o) throws NotSerializableException {
		return new Fields(o);
	}
	
	@Override
	public void deserialize(final T o, final Fields f) throws StreamCorruptedException, NotSerializableException {
		f.setFields(o, Variables.yggdrasil);
	}
	
	@Override
	public boolean mustSyncDeserialization() {
		return false;
	}
	
}
