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

package ch.njol.skript.api;

import java.util.regex.Pattern;

public class ClassInfo<T> {
	private final Class<T> c;
	private final String name, codeName;
	private final DefaultVariable<T> defaultVariable;
	private final Parser<T> parser;
	private final Pattern[] userInputPatterns;
	
	/**
	 * @param name The name of this class as it is displayed to players
	 * @param codeName The name used in expression patterns
	 * @param c The class
	 * @param defaultVariable The defalut value of this class or null if not applicable
	 * @param parser A parser to parse values of this class or null if not applicable
	 * @param userInputPatterns Pegex patterns to match &lt;arg type&gt;s in commands
	 */
	public ClassInfo(final String name, final String codeName, final Class<T> c, final DefaultVariable<T> defaultVariable, final Parser<T> parser, final String... userInputPatterns) {
		this.c = c;
		this.name = name;
		this.codeName = codeName;
		this.defaultVariable = defaultVariable;
		this.parser = parser;
		this.userInputPatterns = new Pattern[userInputPatterns.length];
		for (int i = 0; i < userInputPatterns.length; i++) {
			this.userInputPatterns[i] = Pattern.compile("^" + userInputPatterns[i] + "$");
		}
	}
	
	public ClassInfo(final String codeName, final Class<T> c, final DefaultVariable<T> defaultVariable, final Parser<T> parser) {
		this.c = c;
		this.name = codeName;
		this.codeName = codeName;
		this.defaultVariable = defaultVariable;
		this.parser = parser;
		this.userInputPatterns = new Pattern[0];
	}
	
	public Class<T> getC() {
		return c;
	}
	
	public String getName() {
		return name;
	}
	
	public String getCodeName() {
		return codeName;
	}
	
	public DefaultVariable<T> getDefaultVariable() {
		return defaultVariable;
	}
	
	public Parser<T> getParser() {
		return parser;
	}
	
	public Pattern[] getUserInputPatterns() {
		return userInputPatterns;
	}
}
