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

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

import ch.njol.skript.expressions.base.EventValueExpression;
import ch.njol.skript.lang.DefaultExpression;
import ch.njol.skript.lang.util.SimpleLiteral;
import ch.njol.skript.localization.Language;
import ch.njol.skript.localization.Language.LanguageChangeListener;

/**
 * @author Peter Güttinger
 * 
 * @param <T> The class this info is for
 */
public class ClassInfo<T> {
	
	private final Class<T> c;
	private final String codeName;
	private String name;
	
	private DefaultExpression<T> defaultExpression = null;
	
	private Parser<? extends T> parser = null;
	
	private Pattern[] userInputPatterns = null;
	
	private SerializableChanger<? super T, ?> changer = null;
	
	private Serializer<? super T> serializer = null;
	private Class<?> serializeAs = null;
	
	private Arithmetic<T, ?> math = null;
	private Class<?> mathRelativeType = null;
	
	/**
	 * @param c The class
	 * @param codeName The name used in patterns
	 * @param name This class' name as displayed to the user
	 */
	public ClassInfo(final Class<T> c, final String codeName, final String name) {
		this.c = c;
		if (!codeName.matches("[a-z0-9]+"))
			throw new IllegalArgumentException("Code names for classes must be lowercase and only consist of latin letters and arabic numbers");
		this.codeName = codeName;
		this.name = name;
		Language.addListener(new LanguageChangeListener() {
			@Override
			public void onLanguageChange() {
				ClassInfo.this.name = Language.get("types." + codeName);
			}
		});
	}
	
	// === FACTORY METHODS ===
	
	/**
	 * @param parser A parser to parse values of this class or null if not applicable
	 */
	public ClassInfo<T> parser(final Parser<? extends T> parser) {
		assert this.parser == null;
		this.parser = parser;
		return this;
	}
	
	/**
	 * @param name The name of this class as it is displayed to players
	 * @param userInputPatterns <u>Regex</u> patterns to match &lt;arg type&gt;s in commands. These patterns must match singular and plural.
	 * @throws PatternSyntaxException If any of the patterns' syntaxes is invalid
	 */
	public ClassInfo<T> user(final String... userInputPatterns) throws PatternSyntaxException {
		assert this.userInputPatterns == null;
		this.userInputPatterns = new Pattern[userInputPatterns.length];
		for (int i = 0; i < userInputPatterns.length; i++) {
			this.userInputPatterns[i] = Pattern.compile("^" + userInputPatterns[i] + "$");
		}
		return this;
	}
	
	/**
	 * @param defaultExpression The default (event) value of this class or null if not applicable
	 * @see EventValueExpression
	 * @see SimpleLiteral
	 */
	public ClassInfo<T> defaultExpression(final DefaultExpression<T> defaultExpression) {
		assert this.defaultExpression == null;
		if (!defaultExpression.isDefault())
			throw new IllegalArgumentException("defaultExpression.isDefault() must return true for the default expression of a class");
		this.defaultExpression = defaultExpression;
		return this;
	}
	
	public ClassInfo<T> serializer(final Serializer<? super T> serializer) {
		assert this.serializer == null;
		if (serializeAs != null)
			throw new IllegalStateException("Can't set a serializer if this class is set to be serialized as another one");
		this.serializer = serializer;
		return this;
	}
	
	public ClassInfo<T> serializeAs(final Class<?> serializeAs) {
		assert this.serializeAs == null;
		if (serializer != null)
			throw new IllegalStateException("Can't set this class to be serialized as another one if a serializer is already set");
		this.serializeAs = serializeAs;
		return this;
	}
	
	public ClassInfo<T> changer(final SerializableChanger<? super T, ?> changer) {
		assert this.changer == null;
		this.changer = changer;
		return this;
	}
	
	public <R> ClassInfo<T> math(final Class<R> relativeType, final Arithmetic<T, R> math) {
		assert this.math == null;
		this.math = math;
		mathRelativeType = relativeType;
		return this;
	}
	
	// === GETTERS ===
	
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
	
	public Parser<? extends T> getParser() {
		return parser;
	}
	
	public Pattern[] getUserInputPatterns() {
		return userInputPatterns;
	}
	
	public SerializableChanger<? super T, ?> getChanger() {
		return changer;
	}
	
	public Serializer<? super T> getSerializer() {
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
	
	// === ORDERING ===
	
	private final Set<String> before = new HashSet<String>();
	private Set<String> after;
	
	/**
	 * Sets one or more classes that should occur before this class in the class infos list. This only affects the order in which classes are parsed if it's unknown of which type
	 * the parsed string is.
	 * 
	 * <p>
	 * Please note that subclasses will always be registered before superclasses, no matter what is defined here or in {@link #after(String...)}.
	 * 
	 * <p>
	 * This list can safely contain classes that may not exist.
	 * 
	 * @param before
	 * @return this ClassInfo
	 */
	public ClassInfo<T> before(final String... before) {
		this.before.addAll(Arrays.asList(before));
		return this;
	}
	
	/**
	 * Sets one or more classes that should occur after this class in the class infos list. This only affects the order in which classes are parsed if it's unknown of which type
	 * the parsed string is.
	 * 
	 * <p>
	 * Please note that subclasses will always be registered before superclasses, no matter what is defined here or in {@link #before(String...)}.
	 * 
	 * <p>
	 * This list can safely contain classes that may not exist.
	 * 
	 * @param after
	 * @return this ClassInfo
	 */
	public ClassInfo<T> after(final String... after) {
		assert this.after == null;
		this.after = new HashSet<String>(Arrays.asList(after));
		return this;
	}
	
	/**
	 * 
	 * @return never null
	 */
	public Set<String> before() {
		return before;
	}
	
	/**
	 * 
	 * @return maybe null
	 */
	public Set<String> after() {
		return after;
	}
	
	// === GENERAL ===
	
	@Override
	public String toString() {
		return codeName + " (" + c.getName() + ")";
	}
}
