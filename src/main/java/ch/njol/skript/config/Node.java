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
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import ch.njol.skript.Skript;
import ch.njol.skript.log.SkriptLogger;

/**
 * @author Peter Güttinger
 */
public abstract class Node {
	
	protected String key;
	
	protected final int lineNum;
	/**
	 * "original", untrimmed String. This is not final as void nodes directly change this if they are modified.
	 */
	protected String orig;
	protected boolean modified = false;
	private final boolean debug;
	
	protected final SectionNode parent;
	protected final Config config;
	
	protected Node(final String key, final SectionNode parent, final String orig, final int lineNum) {
		this.key = key;
		config = parent.getConfig();
		this.parent = parent;
		this.orig = orig;
		debug = orig.endsWith("#DEBUG#");
		this.lineNum = lineNum;
		SkriptLogger.setNode(this);
	}
	
	protected Node(final String key, final SectionNode parent, final ConfigReader r) {
		this.key = key;
		config = parent.getConfig();
		this.parent = parent;
		orig = r.getLine();
		debug = orig.endsWith("#DEBUG#");
		lineNum = r.getLineNum();
		SkriptLogger.setNode(this);
	}
	
	/**
	 * Reserved for {@link SectionNode#SectionNode(Config)}
	 * 
	 * @param c
	 */
	protected Node(final Config c) {
		key = null;
		orig = null;
		debug = false;
		lineNum = -1;
		config = c;
		parent = null;
		SkriptLogger.setNode(this);
	}
	
	protected Node(final String key, final SectionNode parent) {
		this.key = key;
		config = parent.getConfig();
		this.parent = parent;
		orig = null;
		debug = false;
		lineNum = -1;
		SkriptLogger.setNode(this);
	}
	
	public final String getKey() {
		return key;
	}
	
	public final Config getConfig() {
		return config;
	}
	
	public void rename(final String newname) {
		if (key == null) {
			Skript.error("can't rename an anonymous node!");
			return;
		}
		key = newname;
		modified();
	}
	
//	public void move(final SectionNode newParent) {
//		if (parent == null) {
//			Skript.error("can't move the main node!");
//			return;
//		}
//		parent.getNodeList().remove(this);
//		parent = newParent;
//		newParent.getNodeList().add(this);
//		config.modified = true;
//	}
	
	protected void modified() {
		modified = true;
		config.modified = true;
	}
	
	private final static Pattern commentPattern = Pattern.compile("\\s*(?<!#)#(?!#).*$");
	
	protected String getComment() {
		final Matcher m = commentPattern.matcher(orig);
		if (!m.find())
			return "";
		return m.group();
	}
	
	protected String getIndentation() {
		String s = "";
		Node n = this;
		while ((n = n.parent) != config.getMainNode()) {
			s += config.getIndentation();
		}
		return s;
	}
	
	void save(final PrintWriter w) {
		if (!modified)
			w.println(getIndentation() + orig.trim());
		else
			w.println(getIndentation() + save() + getComment());
	}
	
	abstract String save();
	
	public SectionNode getParent() {
		return parent;
	}
	
	public void delete() {
		parent.remove(this);
	}
	
	public String getOrig() {
		return orig;
	}
	
	public int getLine() {
		return lineNum;
	}
	
	/**
	 * @return Whether this node does not hold information (i.e. is empty or invalid)
	 */
	public boolean isVoid() {
		return this instanceof VoidNode;// || this instanceof ParseOptionNode;
	}
	
	/**
	 * get a node via path:to:the:node. relative paths are possible by starting with a ':'; a double colon '::' will go up a node.<br/>
	 * selecting the n-th node can be done with #n.
	 * 
	 * @param path
	 * @return the node at the given path or null if the path is invalid
	 */
//	public Node getNode(final String path) {
//		return getNode(path, false);
//	}
//	
//	public Node getNode(String path, final boolean create) {
//		Node n;
//		if (path.startsWith(":")) {
//			path = path.substring(1);
//			n = this;
//		} else {
//			n = config.getMainNode();
//		}
//		for (final String s : path.split(":")) {
//			if (s.isEmpty()) {
//				n = n.getParent();
//				if (n == null) {
//					n = config.getMainNode();
//				}
//				continue;
//			}
//			if (!(n instanceof SectionNode)) {
//				return null;
//			}
//			if (s.startsWith("#")) {
//				int i = -1;
//				try {
//					i = Integer.parseInt(s.substring(1));
//				} catch (final NumberFormatException e) {
//					return null;
//				}
//				if (i <= 0 || i > ((SectionNode) n).getNodeList().size())
//					return null;
//				n = ((SectionNode) n).getNodeList().get(i - 1);
//			} else {
//				final Node oldn = n;
//				n = ((SectionNode) n).get(s);
//				if (n == null) {
//					if (!create)
//						return null;
//					((SectionNode) oldn).getNodeList().add(n = new SectionNode(s, (SectionNode) oldn, "", -1));
//				}
//			}
//		}
//		return n;
//	}
	
	/**
	 * returns information about this node which looks like the following:<br/>
	 * <code>original node line #including comments (config.cfg, line xyz)</code>
	 */
	@Override
	public String toString() {
		if (parent == null)
			return config.getFileName();
		return getOrig().trim() + " (" + getConfig().getFileName() + ", line " + getLine() + ")";
	}
	
	public boolean debug() {
		return debug;
	}
	
}
