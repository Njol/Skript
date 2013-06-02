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

/**
 * An empty line or a comment.
 * <p>
 * The subclass {@link InvalidNode} is used for invalid non-empty nodes, i.e. where a parsing error ocurred.
 * 
 * @author Peter Güttinger
 */
public class VoidNode extends Node {
	
	VoidNode(final SectionNode parent, final String line, final int lineNum) {
		super(line, parent, line, lineNum);
	}
	
	VoidNode(final SectionNode parent, final ConfigReader r) {
		super(r.getLine(), parent, r.getLine(), r.getLineNum());
	}
	
	public void set(final String s) {
		orig = s;
		modified();
	}
	
	/**
	 * Overridden to use the original indentation
	 */
	@Override
	void save(final PrintWriter w) {
		w.println(orig);
	}
	
	@Override
	String save() {
		assert false;
		return orig.trim();
	}
}
