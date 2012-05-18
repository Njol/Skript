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

import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import ch.njol.skript.Skript;
import ch.njol.skript.SkriptLogger;
import ch.njol.skript.api.intern.SkriptAPIException;
import ch.njol.skript.config.validate.SectionValidator;

public class SectionNode extends Node implements Iterable<Node> {
	
	private final ArrayList<Node> nodes = new ArrayList<Node>();
	
	public SectionNode(final String name, final SectionNode parent, final ConfigReader r) {
		super(name, parent, r);
	}
	
	SectionNode(final Config c) {
		super(c);
	}
	
	public SectionNode(final String name, final SectionNode parent, final String orig, final int lineNum) {
		super(parent.getConfig(), name, orig, lineNum);
	}
	
	@Override
	public Iterator<Node> iterator() {
		return new ConfigNodeIterator(this, false);
	}
	
	public final List<Node> getNodeList() {
		return nodes;
	}
	
	/**
	 * get a subnode with the specified name
	 * 
	 * @param name
	 * @return
	 */
	public Node get(final String name) {
		for (final Node node : nodes) {
			if (node.name != null && node.name.equals(name))
				return node;
		}
		return null;
	}
	
	/**
	 * void here = void (incl. invalid)
	 * 
	 * @return
	 */
	public int getNumVoidNodes() {
		int r = 0;
		for (final Node node : nodes) {
			if (node instanceof VoidNode)
				r++;
		}
		return r;
	}
	
	static final SectionNode load(final Config c, final ConfigReader r) throws IOException {
		return new SectionNode(c).load_i(r);
	}
	
	static final SectionNode load(final String name, final SectionNode parent, final ConfigReader r) throws IOException {
		parent.config.level++;
		final SectionNode node = new SectionNode(name, parent, r).load_i(r);
		SkriptLogger.setNode(parent);
		parent.config.level--;
		return node;
	}
	
	private final static String readableWhitespace(final String s) {
		return "'" + s.replace("\t", "->").replace(' ', '_').replaceAll("\\s", "?") + "' [-> = tab, _ = space, ? = other whitespace]";
	}
	
	private final SectionNode load_i(final ConfigReader r) throws IOException {
		
		while (r.readLine() != null) {
			
			SkriptLogger.setNode(this);
			
			String line = r.getLine().replaceFirst("(?<!#)#(?!#).*$", "").replace("##", "#");
			
			if (config.getIndentation() == null && !line.matches("\\s*") && !line.matches("\\S.*")) {
				final String s = line.replaceFirst("\\S.*$", "");
				if (s.matches(" +") || s.matches("\t+")) {
					config.setIndentation(s);
				} else {
					Skript.error("indentation error: indent must only consist of spaces or tabs, but not mixed (found " + readableWhitespace(s) + ")");
					nodes.add(new InvalidNode(this, r));
					continue;
				}
			}
			if (!line.matches("\\s*") && !line.matches("^(" + config.getIndentation() + "){" + config.level + "}\\S.*")) {
				if (line.matches("^(" + config.getIndentation() + "){" + config.level + "}\\s.*")) {
					final String s = line.replaceFirst("\\S.*$", "");
					String found;
					if (s.matches(" +") || s.matches("\t+")) {
						found = s.length() + " " + (s.charAt(0) == ' ' ? "space" : "tab") + (s.length() == 1 ? "" : "s");
					} else {
						found = readableWhitespace(s);
					}
					Skript.error("indentation error, expected " + config.level * config.getIndentation().length() + " " + config.getIndentationName() + ", found " + found);
					nodes.add(new InvalidNode(this, r));
					continue;
				} else {
					r.reset();
					return this;
				}
			}
			
			line = line.trim();
			
			if (line.isEmpty()) {
				nodes.add(new VoidNode(this, r));
				continue;
			}
			
			if (line.startsWith("!") && line.indexOf('[') != -1 && line.endsWith("]")) {
				final String option = line.substring(1, line.indexOf('['));
				final String value = line.substring(line.indexOf('[') + 1, line.length() - 1);
				if (value.isEmpty()) {
					Skript.error("parse options must not be empty");
					nodes.add(new InvalidNode(this, r));
					continue;
				} else if (option.equalsIgnoreCase("separator")) {
					if (config.simple) {
						Skript.warning("trigger files don't have a separator");
						continue;
					}
					config.separator = value;
				} else {
					nodes.add(new InvalidNode(this, r));
					Skript.error("unknown parse option '" + option + "'");
					continue;
				}
				nodes.add(new ParseOptionNode(line.substring(0, line.indexOf('[')), this, r));
				continue;
			}
			
			if (line.endsWith(":") && (
					config.simple
							|| line.indexOf(config.separator) == -1
							|| config.separator.endsWith(":") && line.indexOf(config.separator) == line.length() - config.separator.length()
					)) {
				nodes.add(SectionNode.load(line.substring(0, line.length() - 1), this, r));
				continue;
			}
			
			if (config.simple) {
				nodes.add(new SimpleNode(line, this, r));
			} else {
				nodes.add(getEntry(line, r.getLine(), r.getLineNum(), config.separator));
			}
			
		}
		
		SkriptLogger.setNode(parent);
		
		return this;
	}
	
	private final Node getEntry(final String line, final String orig, final int lineNum, String separator) {
		final int x = line.indexOf(separator);
		if (x == -1) {
			final InvalidNode n = new InvalidNode(this, line, lineNum);
			Skript.error("missing separator '" + separator + "'");
			SkriptLogger.setNode(this);
			return n;
		}
		final String key = line.substring(0, x).trim();
		final String value = line.substring(x + separator.length()).trim();
		return new EntryNode(key, value, orig, this, lineNum);
	}
	
	public void convertToEntries(final int levels) {
		convertToEntries(levels, config.separator);
	}

	// FIXME: breaks saving!
	public void convertToEntries(int levels, String separator) {
		if (!config.simple)
			throw new SkriptAPIException("config is not simple");
		for (int i = 0; i < nodes.size(); i++) {
			final Node n = nodes.get(i);
			if (levels > 0 && n instanceof SectionNode) {
				((SectionNode) n).convertToEntries(levels - 1, separator);
			}
			if (!(n instanceof SimpleNode))
				continue;
			nodes.set(i, getEntry(n.getName(), n.getOrig(), n.lineNum, separator));
		}
	}
	
	@Override
	void save(final PrintWriter w) {
		if (parent != null) {
			if (!modified) {
				w.println(getIndentation() + orig.trim());
			} else {
				w.println(getIndentation() + name + ":" + getComment());
			}
		}
		for (final Node node : nodes)
			node.save(w);
		modified = false;
	}
	
	public boolean validate(final SectionValidator validator) {
		return validator.validate(this);
	}
	
	/**
	 * Gets an entry or the default value if it doesn't exist or is not an EntryNode.
	 * 
	 * @param name the exact name of the node
	 * @param def the default value
	 * @return
	 */
	public String get(final String name, final String def) {
		final Node n = this.get(name);
		if (n == null || !(n instanceof EntryNode))
			return def;
		return ((EntryNode) n).getValue();
	}

}
