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

import ch.njol.skript.lang.SimpleVariable;

public class ClassInfo<T> {
	private final Class<T> c;
	private final String name, codeName;
	private final Class<? extends SimpleVariable<T>> defaultSimpleVariable;
	private final Parser<T> parser;
	private final Pattern[] userInputPatterns;
	
	/**
	 * @param name name used in expression patterns
	 * @param c the class
	 * @param defaultVariable the defalut value of this class or null if not applicable
	 * @param parser a parser to parse values of this class or null if not applicable
	 * @param userInputPatterns patterns to match &lt;arg type&gt;s in commands
	 */
	public ClassInfo(final String name, final String codeName, final Class<T> c, final Class<? extends SimpleVariable<T>> defaultVariable, final Parser<T> parser, final String... userInputPatterns) {
		this.c = c;
		this.name = name;
		this.codeName = codeName;
		this.defaultSimpleVariable = defaultVariable;
		this.parser = parser;
		this.userInputPatterns = new Pattern[userInputPatterns.length];
		for (int i = 0; i < userInputPatterns.length; i++) {
			this.userInputPatterns[i] = Pattern.compile("^" + userInputPatterns[i] + "$");
		}
	}
	
	public ClassInfo(final String codeName, final Class<T> c, final Class<? extends SimpleVariable<T>> defaultVariable, final Parser<T> parser) {
		this.c = c;
		this.name = codeName;
		this.codeName = codeName;
		this.defaultSimpleVariable = defaultVariable;
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
	
	public Class<? extends SimpleVariable<T>> getDefaultSimpleVariable() {
		return defaultSimpleVariable;
	}
	
	public Parser<T> getParser() {
		return parser;
	}
	
	public Pattern[] getUserInputPatterns() {
		return userInputPatterns;
	}
}
