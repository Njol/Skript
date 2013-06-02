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

package ch.njol.skript.classes;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

import org.bukkit.event.Event;

import ch.njol.skript.expressions.base.EventValueExpression;
import ch.njol.skript.lang.Debuggable;
import ch.njol.skript.lang.DefaultExpression;
import ch.njol.skript.lang.util.SimpleLiteral;
import ch.njol.skript.localization.Noun;

/**
 * @author Peter Güttinger
 * @param <T> The class this info is for
 */
public class ClassInfo<T> implements Debuggable {
	
	private final Class<T> c;
	private final String codeName;
	private final Noun name;
	
	private DefaultExpression<T> defaultExpression = null;
	
	private Parser<? extends T> parser = null;
	
	private Pattern[] userInputPatterns = null;
	
	private SerializableChanger<? super T, ?> changer = null;
	
	private Serializer<? super T> serializer = null;
	private Class<?> serializeAs = null;
	
	private Arithmetic<T, ?> math = null;
	private Class<?> mathRelativeType = null;
	
	private String docName = null;
	private String[] description = null;
	private String[] usage = null;
	private String[] examples = null;
	private String since = null;
	
	/**
	 * @param c The class
	 * @param codeName The name used in patterns
	 */
	public ClassInfo(final Class<T> c, final String codeName) {
		this.c = c;
		if (!codeName.matches("[a-z0-9]+"))
			throw new IllegalArgumentException("Code names for classes must be lowercase and only consist of latin letters and arabic numbers");
		this.codeName = codeName;
		name = new Noun("types." + codeName);
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
	 * @param userInputPatterns <u>Regex</u> patterns to match this class, e.g. in the expressions loop-[type], random [type] out of ..., or as command arguments. These patterns
	 *            must be english and match singular and plural.
	 * @throws PatternSyntaxException If any of the patterns' syntaxes is invalid
	 */
	public ClassInfo<T> user(final String... userInputPatterns) throws PatternSyntaxException {
		assert this.userInputPatterns == null;
		this.userInputPatterns = new Pattern[userInputPatterns.length];
		for (int i = 0; i < userInputPatterns.length; i++) {
			this.userInputPatterns[i] = Pattern.compile(userInputPatterns[i]);
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
	
	/**
	 * Use this as {@link #name(String)} to suppress warnings about missing documentation.
	 */
	public final static String NO_DOC = new String();
	
	/**
	 * Only used for Skript's documentation.
	 * 
	 * @param name
	 * @return
	 */
	public ClassInfo<T> name(final String name) {
		assert this.docName == null;
		this.docName = name;
		return this;
	}
	
	/**
	 * Only used for Skript's documentation.
	 * 
	 * @param description
	 * @return
	 */
	public ClassInfo<T> description(final String... description) {
		assert this.description == null;
		this.description = description;
		return this;
	}
	
	/**
	 * Use this as {@link #usage(String...)} to generate the usage from the names of the enum constants of this class
	 */
	public final static String ENUM_USAGE = new String();
	
	/**
	 * Only used for Skript's documentation.
	 * 
	 * @param usage
	 * @return
	 */
	public ClassInfo<T> usage(final String... usage) {
		assert this.usage == null;
		if (usage != null && usage.length == 1 && usage[0] == ENUM_USAGE) {
			final Object[] os = c.getEnumConstants();
			final StringBuilder b = new StringBuilder(os[0].toString().toLowerCase().replace('_', ' '));
			for (int i = 1; i < os.length; i++) {
				b.append(", ");
				b.append(os[i].toString().toLowerCase().replace('_', ' '));
			}
			this.usage = new String[] {b.toString()};
		} else {
			this.usage = usage;
		}
		return this;
	}
	
	/**
	 * Only used for Skript's documentation.
	 * 
	 * @param examples
	 * @return
	 */
	public ClassInfo<T> examples(final String... examples) {
		assert this.examples == null;
		this.examples = examples;
		return this;
	}
	
	/**
	 * Only used for Skript's documentation.
	 * 
	 * @param since
	 * @return
	 */
	public ClassInfo<T> since(final String since) {
		assert this.since == null;
		this.since = since;
		return this;
	}
	
	// === GETTERS ===
	
	public Class<T> getC() {
		return c;
	}
	
	public Noun getName() {
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
	
	public String[] getDescription() {
		return description;
	}
	
	public String[] getUsage() {
		return usage;
	}
	
	public String[] getExamples() {
		return examples;
	}
	
	public String getSince() {
		return since;
	}
	
	public String getDocName() {
		return docName;
	}
	
	// === ORDERING ===
	
	private final Set<String> before = new HashSet<String>();
	private Set<String> after;
	
	/**
	 * Sets one or more classes that should occur before this class in the class infos list. This only affects the order in which classes are parsed if it's unknown of which type
	 * the parsed string is.
	 * <p>
	 * Please note that subclasses will always be registered before superclasses, no matter what is defined here or in {@link #after(String...)}.
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
	 * <p>
	 * Please note that subclasses will always be registered before superclasses, no matter what is defined here or in {@link #before(String...)}.
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
	 * @return never null
	 */
	public Set<String> before() {
		return before;
	}
	
	/**
	 * @return maybe null
	 */
	public Set<String> after() {
		return after;
	}
	
	// === GENERAL ===
	
	@Override
	public String toString() {
		return getName().getSingular();
	}
	
	public String toString(final int flags) {
		return getName().toString(flags);
	}
	
	@Override
	public String toString(final Event e, final boolean debug) {
		if (debug)
			return codeName + " (" + c.getCanonicalName() + ")";
		return getName().getSingular();
	}
	
}
