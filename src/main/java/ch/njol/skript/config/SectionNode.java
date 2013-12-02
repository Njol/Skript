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

import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;

import ch.njol.skript.Skript;
import ch.njol.skript.SkriptAPIException;
import ch.njol.skript.config.validate.EntryValidator;
import ch.njol.skript.config.validate.SectionValidator;
import ch.njol.skript.log.SkriptLogger;
import ch.njol.util.Checker;
import ch.njol.util.coll.iterator.CheckedIterator;

/**
 * @author Peter Güttinger
 */
public class SectionNode extends Node implements Iterable<Node> {
	
	private final ArrayList<Node> nodes = new ArrayList<Node>();
	
	public SectionNode(final String key, final SectionNode parent, final ConfigReader r) {
		super(key, parent, r);
	}
	
	SectionNode(final Config c) {
		super(c);
	}
	
	public SectionNode(final String key) {
		this.key = key;
	}
	
	public SectionNode(final String key, final SectionNode parent, final String orig, final int lineNum) {
		super(key, parent, orig, lineNum);
	}
	
	/**
	 * Note to self: use getNodeMap()
	 */
	private NodeMap nodeMap = null;
	
	private NodeMap getNodeMap() {
		if (nodeMap == null) {
			nodeMap = new NodeMap();
			for (final Node node : nodes)
				nodeMap.put(node);
		}
		return nodeMap;
	}
	
	/**
	 * Total amount of nodes (including void nodes) in this section.
	 * 
	 * @return
	 */
	public int size() {
		return nodes.size();
	}
	
	/**
	 * Adds the given node at the end of this section.
	 * 
	 * @param n
	 */
	public void add(final Node n) {
		n.remove();
		nodes.add(n);
		n.parent = this;
		getNodeMap().put(n);
	}
	
	/**
	 * Inserts the given node into this section at the specified position.
	 * 
	 * @param n
	 * @param index between 0 and {@link #size()}, inclusive
	 */
	public void insert(final Node n, final int index) {
		nodes.add(index, n);
		n.parent = this;
		getNodeMap().put(n);
	}
	
	/**
	 * Removes the given node from this section.
	 * 
	 * @param n
	 */
	public void remove(final Node n) {
		nodes.remove(n);
		n.parent = null;
		getNodeMap().remove(n);
	}
	
	/**
	 * Removes an entry with the given key.
	 * 
	 * @param key
	 * @return
	 */
	public Node remove(final String key) {
		final Node n = getNodeMap().remove(key);
		if (n == null)
			return null;
		nodes.remove(n);
		n.parent = null;
		return n;
	}
	
	/**
	 * Iterator over all non-void nodes of this section.
	 */
	@Override
	public Iterator<Node> iterator() {
		return new CheckedIterator<Node>(nodes.iterator(), new Checker<Node>() {
			@Override
			public boolean check(final Node n) {
				return !n.isVoid();
			}
		}) {
			@Override
			public boolean hasNext() {
				final boolean hasNext = super.hasNext();
				if (!hasNext)
					SkriptLogger.setNode(SectionNode.this);
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
		};
	}
	
	/**
	 * Gets a subnode (EntryNode or SectionNode) with the specified name.
	 * 
	 * @param key
	 * @return The node with the given name
	 */
	public Node get(final String key) {
		return getNodeMap().get(key);
	}
	
	public String getValue(final String key) {
		final Node n = get(key);
		if (n instanceof EntryNode)
			return ((EntryNode) n).getValue();
		return null;
	}
	
	/**
	 * Gets an entry's value or the default value if it doesn't exist or is not an EntryNode.
	 * 
	 * @param name The name of the node (case insensitive)
	 * @param def The default value
	 * @return The value of the entry node with the give node, or <tt>def</tt> if there's no entry with the given name.
	 */
	public String get(final String name, final String def) {
		final Node n = this.get(name);
		if (n == null || !(n instanceof EntryNode))
			return def;
		return ((EntryNode) n).getValue();
	}
	
	public void set(final String key, final String value) {
		final Node n = get(key);
		if (n instanceof EntryNode) {
			((EntryNode) n).setValue(value);
		} else {
			add(new EntryNode(key, value, this));
		}
	}
	
	void renamed(final Node node, final String oldKey) {
		if (!nodes.contains(node))
			throw new IllegalArgumentException();
		getNodeMap().remove(oldKey);
		getNodeMap().put(node);
	}
	
	public boolean isEmpty() {
		for (final Node node : nodes) {
			if (!node.isVoid())
				return false;
		}
		return true;
	}
	
	final static SectionNode load(final Config c, final ConfigReader r) throws IOException {
		return new SectionNode(c).load_i(r);
	}
	
	final static SectionNode load(final String name, final SectionNode parent, final ConfigReader r) throws IOException {
		parent.config.level++;
		final SectionNode node = new SectionNode(name, parent, r).load_i(r);
		SkriptLogger.setNode(parent);
		parent.config.level--;
		return node;
	}
	
	private final static String readableWhitespace(final String s) {
		if (s.matches(" +"))
			return s.length() + " space" + (s.length() == 1 ? "" : "s");
		if (s.matches("\t+"))
			return s.length() + " tab" + (s.length() == 1 ? "" : "s");
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
					nodes.add(new InvalidNode(this, r));
					Skript.error("indentation error: indent must only consist of either spaces or tabs, but not mixed (found " + readableWhitespace(s) + ")");
					continue;
				}
			}
			if (!line.matches("\\s*") && !line.matches("^(" + config.getIndentation() + "){" + config.level + "}\\S.*")) {
				if (line.matches("^(" + config.getIndentation() + "){" + config.level + "}\\s.*") || !line.matches("^(" + config.getIndentation() + ")*\\S.*")) {
					final String s = line.replaceFirst("\\S.*$", "");
					nodes.add(new InvalidNode(this, r));
					Skript.error("indentation error: expected " + config.level * config.getIndentation().length() + " " + config.getIndentationName() + (config.level * config.getIndentation().length() == 1 ? "" : "s") + ", but found " + readableWhitespace(s));
					continue;
				} else {
					if (parent != null && !config.allowEmptySections && isEmpty()) {
						Skript.warning("Empty configuration section! You might want to indent one or more of the subsequent lines to make them belong to this section" +
								" or remove the colon at the end of the line if you don't want this line to start a section.");
					}
					r.reset();
					return this;
				}
			}
			
			line = line.trim();
			
			if (line.isEmpty()) {
				nodes.add(new VoidNode(this, r));
				continue;
			}
			
//			if (line.startsWith("!") && line.indexOf('[') != -1 && line.endsWith("]")) {
//				final String option = line.substring(1, line.indexOf('['));
//				final String value = line.substring(line.indexOf('[') + 1, line.length() - 1);
//				if (value.isEmpty()) {
//					nodes.add(new InvalidNode(this, r));
//					Skript.error("parse options must not be empty");
//					continue;
//				} else if (option.equalsIgnoreCase("separator")) {
//					if (config.simple) {
//						Skript.warning("scripts don't have a separator");
//						continue;
//					}
//					config.separator = value;
//				} else {
//					final Node n = new InvalidNode(this, r);
//					SkriptLogger.setNode(n);
//					nodes.add(n);
//					Skript.error("unknown parse option '" + option + "'");
//					continue;
//				}
//				nodes.add(new ParseOptionNode(line.substring(0, line.indexOf('[')), this, r));
//				continue;
//			}
			
			if (line.endsWith(":") && (config.simple
					|| line.indexOf(config.separator) == -1
					|| config.separator.endsWith(":") && line.indexOf(config.separator) == line.length() - config.separator.length()
					) && !r.getLine().matches("([^#]|##)*#-#(\\s.*)?")) {
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
	
	private Node getEntry(final String keyAndValue, final String orig, final int lineNum, final String separator) {
		final int x = keyAndValue.indexOf(separator);
		if (x == -1) {
			final InvalidNode in = new InvalidNode(this, orig, lineNum);
			EntryValidator.notAnEntryError(in);
			SkriptLogger.setNode(this);
			return in;
		}
		final String key = keyAndValue.substring(0, x).trim();
		final String value = keyAndValue.substring(x + separator.length()).trim();
		return new EntryNode(key, value, orig, this, lineNum);
	}
	
	/**
	 * Converts all SimpleNodes in this section to EntryNodes.
	 * 
	 * @param levels Amount of levels to go down, e.g. 0 to only convert direct subnodes of this section or -1 for all subnodes including subnodes of subnodes etc.
	 */
	public void convertToEntries(final int levels) {
		convertToEntries(levels, config.separator);
	}
	
	/**
	 * REMIND breaks saving - separator argument can be different from config.sepator
	 * 
	 * @param levels Maximum depth of recursion, <tt>-1</tt> for no limit.
	 * @param separator Some separator, e.g. ":" or "=".
	 */
	public void convertToEntries(final int levels, final String separator) {
		if (levels < -1)
			throw new IllegalArgumentException("levels must be >= -1");
		if (!config.simple)
			throw new SkriptAPIException("config is not simple");
		for (int i = 0; i < nodes.size(); i++) {
			final Node n = nodes.get(i);
			if (levels != 0 && n instanceof SectionNode) {
				((SectionNode) n).convertToEntries(levels == -1 ? -1 : levels - 1, separator);
			}
			if (!(n instanceof SimpleNode))
				continue;
			nodes.set(i, getEntry(n.key, n.save(), n.lineNum, separator));
		}
	}
	
	@Override
	public void save(final PrintWriter w) {
		if (parent != null)
			w.println(save());
		for (final Node node : nodes)
			node.save(w);
	}
	
	@Override
	String save_i() {
		return key + ":";
	}
	
	public boolean validate(final SectionValidator validator) {
		return validator.validate(this);
	}
	
	HashMap<String, String> toMap(final String prefix, final String separator) {
		final HashMap<String, String> r = new HashMap<String, String>();
		for (final Node n : this) {
			if (n instanceof EntryNode) {
				r.put(prefix + n.getKey(), ((EntryNode) n).getValue());
			} else {
				r.putAll(((SectionNode) n).toMap(prefix + n.getKey() + separator, separator));
			}
		}
		return r;
	}
	
	/**
	 * @param other
	 * @return <tt>false</tt> iff this and the other SectionNode contain the exact same set of keys
	 */
	public boolean setValues(final SectionNode other) {
		boolean r = false;
		for (final Node n : this) {
			final Node o = other.get(n.key);
			if (o == null) {
				r = true;
			} else {
				if (n instanceof SectionNode) {
					if (o instanceof SectionNode) {
						r |= ((SectionNode) n).setValues((SectionNode) o);
					} else {
						r = true;
					}
				} else if (n instanceof EntryNode) {
					if (o instanceof EntryNode) {
						((EntryNode) n).setValue(((EntryNode) o).getValue());
					} else {
						r = true;
					}
				}
			}
		}
		if (!r) {
			for (final Node o : other) {
				if (this.get(o.key) == null) {
					r = true;
					break;
				}
			}
		}
		return r;
	}
	
}
