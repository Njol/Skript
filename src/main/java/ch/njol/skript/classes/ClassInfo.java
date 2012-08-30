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

package ch.njol.skript.classes;

import java.util.regex.Pattern;

import ch.njol.skript.expressions.base.EventValueExpression;
import ch.njol.skript.lang.DefaultExpression;
import ch.njol.skript.lang.util.SimpleLiteral;

/**
 * @author Peter Güttinger
 * 
 * @param <T> The class this info is for
 */
public class ClassInfo<T> {
	
	private final Class<T> c;
	private final String codeName;
	private final String name;
	
	private DefaultExpression<T> defaultExpression = null;
	
	private Parser<T> parser = null;
	
	private Pattern[] userInputPatterns = null;
	
	private Changer<? super T, ?> changer = null;
	
	private Serializer<T> serializer = null;
	private Class<?> serializeAs = null;
	
	private Arithmetic<T, ?> math = null;
	private Class<?> mathRelativeType = null;
	
	private Validator<? super T> validator = null;
	
	/**
	 * @param c The class
	 * @param codeName The name used in expression patterns
	 */
	public ClassInfo(final Class<T> c, final String codeName, final String name) {
		this.c = c;
		this.codeName = codeName;
		this.name = name;
	}
	
	/**
	 * @param parser A parser to parse values of this class or null if not applicable
	 */
	public ClassInfo<T> parser(final Parser<T> parser) {
		this.parser = parser;
		return this;
	}
	
	/**
	 * @param name The name of this class as it is displayed to players
	 * @param userInputPatterns <u>Regex</u> patterns to match &lt;arg type&gt;s in commands. These patterns must match singular and plural.
	 */
	public ClassInfo<T> user(final String... userInputPatterns) {
		this.userInputPatterns = new Pattern[userInputPatterns.length];
		for (int i = 0; i < userInputPatterns.length; i++) {
			this.userInputPatterns[i] = Pattern.compile("^" + userInputPatterns[i] + "$");
		}
		return this;
	}
	
	/**
	 * @param defaultExpression The defalut (event) value of this class or null if not applicable
	 * @see EventValueExpression
	 * @see SimpleLiteral
	 */
	public ClassInfo<T> defaultExpression(final DefaultExpression<T> defaultExpression) {
		if (!defaultExpression.isDefault())
			throw new IllegalArgumentException("defaultExpression.isDefault() must return true for the default expression of a class");
		this.defaultExpression = defaultExpression;
		return this;
	}
	
	public ClassInfo<T> serializer(final Serializer<T> serializer) {
		if (serializeAs != null)
			throw new IllegalStateException("Can't set a serializer if this class is set to be serialized as another one");
		this.serializer = serializer;
		return this;
	}
	
	public ClassInfo<T> serializeAs(final Class<?> serializeAs) {
		if (serializer != null)
			throw new IllegalStateException("Can't set this class to be serialized as another one if a serializer is already set");
		this.serializeAs = serializeAs;
		return this;
	}
	
	public ClassInfo<T> changer(final Changer<? super T, ?> changer) {
		this.changer = changer;
		return this;
	}
	
	public <R> ClassInfo<T> math(final Class<R> relativeType, final Arithmetic<T, R> math) {
		this.math = math;
		mathRelativeType = relativeType;
		return this;
	}
	
	public ClassInfo<T> validator(final Validator<? super T> validator) {
		this.validator = validator;
		return this;
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
	
	public DefaultExpression<T> getDefaultExpression() {
		return defaultExpression;
	}
	
	public Parser<T> getParser() {
		return parser;
	}
	
	public Pattern[] getUserInputPatterns() {
		return userInputPatterns;
	}
	
	public Changer<? super T, ?> getChanger() {
		return changer;
	}
	
	public Serializer<T> getSerializer() {
		return serializer;
	}
	
	public Class<?> getSerializeAs() {
		return serializeAs;
	}
	
	public Arithmetic<T, ?> getMath() {
		return math;
	}
	
	public Class<?> getMathRelativeType() {
		return mathRelativeType;
	}
	
	public Validator<? super T> getValidator() {
		return validator;
	}
	
}
