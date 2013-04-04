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

package ch.njol.skript.config;

import java.io.PrintWriter;
import java.util.Map.Entry;

/**
 * @author Peter Güttinger
 */
public class EntryNode extends Node implements Entry<String, String> {
	
	private String value;
	
	public EntryNode(final String key, final String value, final SectionNode parent, final ConfigReader r) {
		super(key, parent, r);
		this.value = value;
	}
	
	public EntryNode(final String key, final String value, final String orig, final SectionNode parent, final int lineNum) {
		super(key, parent, orig, lineNum);
		this.value = value;
	}
	
	@Override
	public String getKey() {
		return name;
	}
	
	@Override
	public String getValue() {
		return value;
	}
	
	@Override
	public String setValue(final String v) {
		final String r = value;
		value = v;
		modified();
		return r;
	}
	
	@Override
	void save(final PrintWriter w) {
		if (!modified) {
			w.println(getIndentation() + orig);
			return;
		}
		w.append(getIndentation() + name + config.separator + value + getComment());
		modified = false;
	}
	
}
