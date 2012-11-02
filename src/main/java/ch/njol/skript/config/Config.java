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

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.util.HashMap;

import ch.njol.skript.Skript;

/**
 * Represents a config file.
 * 
 * @author Peter Güttinger
 */
public class Config {
	
	boolean simple = false;
	private String indentation = null;
	private String indentationName = null;
	final String defaultSeparator;
	String separator;
	
	String line = "";
	
	int level = 0;
	
	private final SectionNode main;
	
	boolean modified = false;
	int errors = 0;
	
	final boolean allowEmptySections;
	
	String fileName;
	File file = null;
	
	public Config(final InputStream source, final String fileName, final boolean simple, final boolean allowEmptySections, final String defaultSeparator) throws IOException {
		
		this.fileName = fileName;
		this.simple = simple;
		this.allowEmptySections = allowEmptySections;
		this.defaultSeparator = defaultSeparator;
		separator = defaultSeparator;
		
		if (source.available() == 0) {
			main = new SectionNode(this);
			Skript.warning("'" + getFileName() + "' is empty");
			return;
		}
		
		if (Skript.logVeryHigh())
			Skript.info("loading '" + fileName + "'");
		
		final ConfigReader r = new ConfigReader(source);
		try {
			main = SectionNode.load(this, r);
		} finally {
			r.close();
		}
	}
	
	@SuppressWarnings("resource")
	public Config(final File file, final boolean simple, final boolean allowEmptySections, final String defaultSeparator) throws IOException {
		this(new FileInputStream(file), file.getName(), simple, allowEmptySections, defaultSeparator);
		this.file = file;
	}
	
	/**
	 * For testing
	 * 
	 * @param s
	 * @param fileName
	 * @param simple
	 * @param allowEmptySections
	 * @param defaultSeparator
	 * @throws IOException
	 */
	public Config(final String s, final String fileName, final boolean simple, final boolean allowEmptySections, final String defaultSeparator) throws IOException {
		this(new ByteArrayInputStream(s.getBytes("UTF-8")), fileName, simple, allowEmptySections, defaultSeparator);
	}
	
	void setIndentation(final String indent) {
		indentation = indent;
		indentationName = (indent.charAt(0) == ' ' ? "space" : "tab");
	}
	
	String getIndentation() {
		return indentation;
	}
	
	String getIndentationName() {
		return indentationName;
	}
	
	public SectionNode getMainNode() {
		return main;
	}
	
	public String getFileName() {
		return fileName;
	}
	
	public synchronized void save() throws IOException {
		if (!modified)
			return;
		separator = defaultSeparator;
		file.createNewFile();
		final PrintWriter w = new PrintWriter(new FileWriter(file));
		main.save(w);
		w.flush();
		w.close();
		modified = false;
	}
	
	public boolean isEnabled() {
		return !file.getName().startsWith("-");
	}
	
	public boolean setEnabled(final boolean b) {
		if (isEnabled() == b) {
			return false;
		}
		return file.renameTo(new File(file, b ? file.getName().substring(1) : "-" + file.getName()));
	}
	
	public File getFile() {
		return file;
	}
	
	/**
	 * 
	 * @return The separator last used. Only useful while the file is loading.
	 */
	public String getSeparator() {
		return separator;
	}
	
	public String getByPath(final String path) {
		return get(path.split("\\."));
	}
	
	public String get(final String... path) {
		SectionNode section = main;
		for (int i = 0; i < path.length; i++) {
			final Node n = section.get(path[i]);
			if (n == null)
				return null;
			if (n instanceof SectionNode) {
				if (i == path.length - 1)
					return null;
				section = (SectionNode) n;
			} else {
				if (i == path.length - 1)
					return ((EntryNode) n).getValue();
				else
					return null;
			}
		}
		return null;
	}
	
	public boolean isEmpty() {
		return main.isEmpty();
	}
	
	public HashMap<String, String> toMap(final String separator) {
		return main.toMap("", separator);
	}
	
}
