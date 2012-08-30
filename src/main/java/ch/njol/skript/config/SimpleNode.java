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

import java.io.PrintWriter;

/**
 * @author Peter Güttinger
 */
public class SimpleNode extends Node {
	
	public SimpleNode(final String value, final SectionNode parent, final ConfigReader r) {
		super(value, parent, r);
	}
	
	public SimpleNode(final Config c) {
		super(c);
	}
	
	@Override
	void save(final PrintWriter w) {
		w.print(name);
	}
	
	public void set(final String s) {
		orig = name = s;
	}
	
}
