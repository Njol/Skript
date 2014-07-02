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

package ch.njol.skript.lang.function;

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.eclipse.jdt.annotation.Nullable;

import ch.njol.skript.Skript;
import ch.njol.skript.SkriptAPIException;
import ch.njol.skript.classes.ClassInfo;
import ch.njol.skript.config.SectionNode;
import ch.njol.skript.lang.ParseContext;
import ch.njol.skript.lang.SkriptParser;
import ch.njol.skript.registrations.Classes;
import ch.njol.skript.util.Utils;
import ch.njol.util.NonNullPair;
import ch.njol.util.StringUtils;

/**
 * @author Peter Güttinger
 */
public abstract class Functions {
	private Functions() {}
	
	final static class FunctionData {
		final Function<?> function;
		final Collection<FunctionReference<?>> calls = new ArrayList<FunctionReference<?>>();
		
		public FunctionData(final Function<?> function) {
			this.function = function;
		}
	}
	
	@Nullable
	public static ScriptFunction<?> currentFunction = null;
	
	final static Map<String, FunctionData> functions = new HashMap<String, FunctionData>();
	
	public final static void registerFunction(final JavaFunction<?> function) {
		Skript.checkAcceptRegistrations();
		if (!function.name.matches(functionNamePattern))
			throw new SkriptAPIException("Invalid function name '" + function.name + "'");
		if (functions.containsKey(function.name))
			throw new SkriptAPIException("Duplicate function " + function.name);
		functions.put(function.name, new FunctionData(function));
	}
	
	final static void registerCaller(final FunctionReference<?> r) {
		final FunctionData d = functions.get(r.functionName);
		assert d != null;
		d.calls.add(r);
	}
	
	public final static String functionNamePattern = "[\\p{IsAlphabetic}_]+";
	
	@SuppressWarnings("null")
	private final static Pattern functionPattern = Pattern.compile("function (" + functionNamePattern + ")\\((.*)\\)(?: :: (.+))?", Pattern.CASE_INSENSITIVE),
			paramPattern = Pattern.compile("\\s*(.+?)\\s*:\\s*(.+?)(?:\\s*=\\s*(.+))?\\s*");
	
	@SuppressWarnings("unchecked")
	@Nullable
	public final static Function<?> loadFunction(final SectionNode node) {
		final String definition = node.getKey();
		assert definition != null;
		final Matcher m = functionPattern.matcher(definition);
		if (!m.matches())
			return error("Invalid function definition. Please check for typos and that the function's name only contains letters and underscores. Refer to the documentation for more information.");
		final String name = "" + m.group(1);
		final String args = m.group(2);
		final String returnType = m.group(3);
		final List<Parameter<?>> params = new ArrayList<Parameter<?>>();
		int j = 0;
		for (int i = 0; i <= args.length(); i = SkriptParser.next(args, i, ParseContext.DEFAULT)) {
			if (i == -1)
				return error("Invalid text/variables/parentheses in the arguments of this function");
			if (i == args.length() || args.charAt(i) == ',') {
				final String arg = args.substring(j, i);
				final Matcher n = paramPattern.matcher(arg);
				if (!n.matches())
					return error("The " + StringUtils.fancyOrderNumber(params.size() + 1) + " argument's definition is invalid. It should look like 'name: type' or 'name: type = default value'.");
				final String paramName = "" + n.group(1);
				for (final Parameter<?> p : params) {
					if (p.name.toLowerCase(Locale.ENGLISH).equals(paramName.toLowerCase(Locale.ENGLISH)))
						return error("Each argument's name must be unique, but the name '" + paramName + "' occurs at least twice.");
				}
				ClassInfo<?> c;
				c = Classes.getClassInfoFromUserInput("" + n.group(2));
				final NonNullPair<String, Boolean> pl = Utils.getEnglishPlural("" + n.group(2));
				if (c == null)
					c = Classes.getClassInfoFromUserInput(pl.getFirst());
				if (c == null)
					return error("Cannot recognise the type '" + n.group(2) + "'");
				final Parameter<?> p = Parameter.newInstance(paramName, c, !pl.getSecond(), n.group(3));
				if (p == null)
					return null;
				params.add(p);
				
				j = i + 1;
			}
			if (i == args.length())
				break;
		}
		ClassInfo<?> c;
		final NonNullPair<String, Boolean> p;
		if (returnType == null) {
			c = null;
			p = null;
		} else {
			c = Classes.getClassInfoFromUserInput("" + returnType);
			p = Utils.getEnglishPlural("" + returnType);
			if (c == null)
				c = Classes.getClassInfoFromUserInput(p.getFirst());
			if (c == null) {
				Skript.error("Cannot recognise the type '" + returnType + "'");
				return null;
			}
		}
		
		if (Skript.debug() || node.debug())
			Skript.debug("function " + name + "(" + StringUtils.join(params, ", ") + ")" + (c != null && p != null ? " :: " + Utils.toEnglishPlural(c.getCodeName(), p.getSecond()) : "") + ":");
		
		@SuppressWarnings("null")
		final Function<?> f = new ScriptFunction<Object>(name, params.toArray(new Parameter[params.size()]), node, (ClassInfo<Object>) c, p == null ? false : !p.getSecond());
//		functions.put(name, new FunctionData(f)); // in constructor
		return f;
	}
	
	@Nullable
	private final static Function<?> error(final String error) {
		Skript.error(error);
		return null;
	}
	
	@Nullable
	public final static Function<?> getFunction(final String name) {
		final FunctionData d = functions.get(name);
		if (d == null)
			return null;
		return d.function;
	}
	
	private final static Collection<FunctionReference<?>> toValidate = new ArrayList<FunctionReference<?>>();
	
	/**
	 * Remember to call {@link #validateFunctions()} after calling this
	 * 
	 * @param script
	 * @return
	 */
	public final static int clearFunctions(final File script) {
		int r = 0;
		final Iterator<FunctionData> iter = functions.values().iterator();
		while (iter.hasNext()) {
			final FunctionData d = iter.next();
			if (d.function instanceof ScriptFunction && script.equals(((ScriptFunction<?>) d.function).trigger.getScript())) {
				iter.remove();
				r++;
				for (final FunctionReference<?> c : d.calls)
					toValidate.add(c);
			}
		}
		return r;
	}
	
	public final static void validateFunctions() {
		for (final FunctionReference<?> c : toValidate)
			c.validateFunction(false);
		toValidate.clear();
	}
	
	public final static void clearFunctions() {
		functions.clear();
	}
	
}
