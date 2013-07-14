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
import java.util.Locale;
import java.util.Map;

import ch.njol.skript.Skript;
import ch.njol.skript.SkriptAPIException;
import ch.njol.skript.config.validate.EntryValidator;
import ch.njol.skript.config.validate.SectionValidator;
import ch.njol.skript.log.SkriptLogger;
import ch.njol.util.Checker;
import ch.njol.util.iterator.CheckedIterator;

/**
 * @author Peter Güttinger
 */
public class SectionNode extends Node implements Iterable<Node> {
	
	public SectionNode(final String key, final SectionNode parent, final ConfigReader r) {
		super(key, parent, r);
	}
	
	SectionNode(final Config c) {
		super(c);
	}
	
	public SectionNode(final String key, final SectionNode parent, final String orig, final int lineNum) {
		super(key, parent, orig, lineNum);
	}
	
	private final ArrayList<Node> nodes = new ArrayList<Node>();
	private transient Map<String, Node> nodeMap = null;
	
	public int size() {
		return nodes.size();
	}
	
	public void add(final Node n) {
		modified();
		nodes.add(n);
		nodeMap.put(n.key.toLowerCase(Locale.ENGLISH), n);
	}
	
	public void remove(final Node n) {
		modified();
		nodes.remove(n);
		nodeMap.remove(n.key.toLowerCase(Locale.ENGLISH));
	}
	
	public void remove(final String key) {
		modified();
		nodes.remove(nodeMap.remove(key.toLowerCase(Locale.ENGLISH)));
	}
	
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
	 * Get a subnode (EntryNode or SectionNode) with the specified name
	 * 
	 * @param key
	 * @return
	 */
	public Node get(final String key) {
		if (nodeMap == null) {
			nodeMap = new HashMap<String, Node>();
			for (final Node node : nodes) {
				if (node.isVoid())
					continue;
				nodeMap.put(node.key.toLowerCase(Locale.ENGLISH), node);
			}
		}
		return nodeMap.get(key.toLowerCase(Locale.ENGLISH));
	}
	
	/**
	 * Gets an entry's value or the default value if it doesn't exist or is not an EntryNode.
	 * 
	 * @param name The name of the node (case insensitive)
	 * @param def The default value
	 * @return
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
		} else if (n == null) {
			add(new EntryNode(key, value, this));
		}
	}
	
	public boolean isEmpty() {
		for (final Node node : nodes) {
			if (!node.isVoid())
				return false;
		}
		return true;
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
	
	private final Node getEntry(final String line, final String orig, final int lineNum, final String separator) {
		final int x = line.indexOf(separator);
		if (x == -1) {
			final InvalidNode n = new InvalidNode(this, line, lineNum);
			EntryValidator.notAnEntryError(n);
			SkriptLogger.setNode(this);
			return n;
		}
		final String key = line.substring(0, x).trim();
		final String value = line.substring(x + separator.length()).trim();
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
	
	// TODO breaks saving!
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
			nodes.set(i, getEntry(n.getKey(), n.getOrig(), n.lineNum, separator));
		}
	}
	
	@Override
	void save(final PrintWriter w) {
		if (parent != null) {
			if (!modified) {
				w.println(getIndentation() + orig.trim());
			} else {
				w.println(getIndentation() + key + ":" + getComment());
			}
		}
		for (final Node node : nodes)
			node.save(w);
	}
	
	@Override
	String save() {
		assert false;
		return key + ":" + getComment();
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
	
	boolean setValues(final SectionNode other) {
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
