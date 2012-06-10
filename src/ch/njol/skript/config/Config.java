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

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;

import ch.njol.skript.Skript;

/**
 * Represents a config file.
 * 
 * @author Peter Güttinger
 * 
 */
public class Config {
	
	File file;
	
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
	
	public Config(final File file, final boolean simple, final String defaultSeparator) throws IOException {
		
		this.file = file;
		this.simple = simple;
		this.defaultSeparator = defaultSeparator;
		separator = defaultSeparator;
		
		if (file.length() == 0) {
			main = new SectionNode(this);
			Skript.warning("'" + getFileName() + "' is empty");
			return;
		}
		
		if (Skript.logVeryHigh())
			Skript.info("loading '" + file.getName() + "'");
		
		final ConfigReader r = new ConfigReader(file);
		
		try {
			main = SectionNode.load(this, r);
		} catch (final IOException e) {
			r.close();
			throw e;
		}
		
		r.close();
	}
	
	void setIndentation(final String indent) {
		indentation = indent;
		indentationName = (indent.charAt(0) == ' ' ? "space" : "tab") + (indent.length() == 1 ? "" : "s");
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
		return file.getName();
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
}
