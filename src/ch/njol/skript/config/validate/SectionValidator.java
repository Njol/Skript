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

import java.util.HashMap;
import java.util.Map.Entry;

import ch.njol.skript.Skript;
import ch.njol.skript.SkriptLogger;
import ch.njol.skript.config.EntryNode;
import ch.njol.skript.config.Node;
import ch.njol.skript.config.SectionNode;
import ch.njol.util.Setter;
import ch.njol.util.Validate;

/**
 * @author Peter Güttinger
 * 
 */
public class SectionValidator implements NodeValidator {
	
	private final static class NodeInfo {
		public NodeValidator v;
		public boolean optional;
		
		public NodeInfo(final NodeValidator v, final boolean optional) {
			this.v = v;
			this.optional = optional;
		}
	}
	
	private final HashMap<String, NodeInfo> nodes = new HashMap<String, NodeInfo>();
	private boolean allowUndefinedSections = false;
	private boolean allowUndefinedEntries = false;
	
	public SectionValidator() {
		allowUndefinedSections = true;
		allowUndefinedEntries = true;
	}
	
	public SectionValidator addNode(final String name, final NodeValidator v, final boolean optional) {
		Validate.notNull(name, v);
		nodes.put(name, new NodeInfo(v, optional));
		return this;
	}
	
	public SectionValidator addEntry(final String name, final boolean optional) {
		addNode(name, new EntryValidator(), optional);
		return this;
	}
	
	public SectionValidator addEntry(final String name, final Setter<String> setter, final boolean optional) {
		addNode(name, new EntryValidator(setter), optional);
		return this;
	}
	
	public <T> SectionValidator addEntry(final String name, final Class<T> c, final Setter<T> setter, final boolean optional) {
		addNode(name, new ParsedEntryValidator<T>(c, setter), optional);
		return this;
	}
	
	public SectionValidator addSection(final String name, final boolean optional) {
		addNode(name, new SectionValidator(), optional);
		return this;
	}

	public boolean validate(final Node node, boolean printErrors) {
		if (!validate(node)) {
			if (printErrors && Skript.getCurrentErrorSession() != null)
				Skript.getCurrentErrorSession().printErrors();
			return false;
		}
		return true;
	}
	
	@Override
	public boolean validate(final Node node) {
		if (!(node instanceof SectionNode)) {
			notASectionError(node);
			return false;
		}
		boolean ok = true;
		for (final Entry<String, NodeInfo> e : nodes.entrySet()) {
			final Node n = ((SectionNode) node).get(e.getKey());
			if (n == null && !e.getValue().optional) {
				Skript.error("Required entry '" + e.getKey() + "' is missing in '" + node.getName() + "' ("+ node.getConfig().getFileName() + ", starting at line "+node.getLine() + ")");
				ok = false;
			} else if (n != null) {
				ok &= e.getValue().v.validate(n);
			}
		}
		if (allowUndefinedSections && allowUndefinedEntries)
			return ok;
		for (final Node n : (SectionNode) node) {
			if (!nodes.containsKey(n.getName())) {
				if (n instanceof SectionNode && allowUndefinedSections || n instanceof EntryNode && allowUndefinedEntries)
					continue;
				SkriptLogger.setNode(n);
				Skript.error("'" + n.getName() + "' is invalid. Check whether it's spelled correctly or remove it.");
				ok = false;
			}
		}
		SkriptLogger.setNode(null);
		return ok;
	}
	
	public static final void notASectionError(final Node node) {
		Skript.error("'" + node.getName() + "' is not a section (like 'blah:', followed by one or more indented lines)");
	}
	
	public SectionValidator setAllowUndefinedSections(final boolean b) {
		allowUndefinedSections = b;
		return this;
	}
	
	public SectionValidator setAllowUndefinedEntries(final boolean b) {
		allowUndefinedEntries = b;
		return this;
	}
	
}
