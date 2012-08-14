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

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.StringReader;

public class ConfigReader extends BufferedReader {
	
	private String line, lastLine;
	private boolean reset = false;
	private int ln = 0;
	
	private boolean hasNonEmptyLine = false;
	
	public ConfigReader(final File file) throws FileNotFoundException {
		super(new FileReader(file));
	}
	
	public ConfigReader(final String s) {
		super(new StringReader(s));
	}
	
	@Override
	public String readLine() throws IOException {
		if (reset) {
			reset = false;
			return lastLine;
		}
		lastLine = line;
		line = stripUTF8BOM(super.readLine());
		ln++;
		return line;
	}
	
	private final String stripUTF8BOM(final String line) {
		if (!hasNonEmptyLine && line != null && !line.isEmpty()) {
			hasNonEmptyLine = true;
			if (line.startsWith("ï»¿")) {
				return line.substring(3);
			} else if (line.startsWith("﻿")) {// <- that's the BOM there as actual bytes
				return line.substring(1);
			}
		}
		return line;
	}
	
	@Override
	public void reset() throws IOException {
		if (reset)
			throw new IOException("reset was called twice without a readLine inbetween");
		reset = true;
	}
	
	public int getLineNum() {
		return ln;
	}
	
	public String getLine() {
		return line;
	}
	
	@Override
	public boolean markSupported() {
		return false;
	}
	
}
