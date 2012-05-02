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

import ch.njol.skript.SkriptLogger;
import ch.njol.util.Checker;
import ch.njol.util.iterator.CheckedIterator;

public class ConfigNodeIterator extends CheckedIterator<Node> {
	
	private final Node node;
	
	public ConfigNodeIterator(final SectionNode node, final boolean includeVoid) {
		super(node.getNodeList().iterator(), new Checker<Node>() {
			@Override
			public boolean check(final Node n) {
				return includeVoid || !n.isVoid();
			}
		});
		this.node = node;
	}
	
	@Override
	public boolean hasNext() {
		final boolean hasNext = super.hasNext();
		if (!hasNext)
			SkriptLogger.setNode(node);
		return hasNext;
	}
	
	@Override
	public Node next() {
		final Node n = super.next();
		SkriptLogger.setNode(n);
		return n;
	}
	
	@Override
	public void remove() {
		throw new UnsupportedOperationException();
	}
	
}
