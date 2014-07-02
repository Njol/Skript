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

import org.bukkit.event.Event;
import org.eclipse.jdt.annotation.Nullable;

import ch.njol.skript.Skript;
import ch.njol.skript.classes.ClassInfo;
import ch.njol.skript.config.Node;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.SkriptParser;
import ch.njol.skript.log.RetainingLogHandler;
import ch.njol.skript.log.SkriptLogger;
import ch.njol.util.StringUtils;
import ch.njol.util.coll.CollectionUtils;

/**
 * @author Peter Güttinger
 */
public class FunctionReference<T> {
	
	final String functionName;
	
	private Function<? extends T> function;
	
	private final Expression<?>[] parameters;
	
	private boolean single;
	@Nullable
	private final Class<? extends T>[] returnTypes;
	
	@Nullable
	private final Node node;
	
	@SuppressWarnings("null")
	public FunctionReference(final String functionName, final @Nullable Node node, @Nullable final Class<? extends T>[] returnTypes, final Expression<?>[] params) {
		this.functionName = functionName;
		this.node = node;
		this.returnTypes = returnTypes;
		parameters = params;
	}
	
	@SuppressWarnings("unchecked")
	public boolean validateFunction(final boolean first) {
		final Function<?> newFunc = Functions.getFunction(functionName);
		SkriptLogger.setNode(node);
		if (newFunc == null) {
			if (first)
				Skript.error("The function '" + functionName + "' does not exist.");
			else
				Skript.error("The function '" + functionName + "' was deleted or renamed, but is still used in other script(s). These will continue to use the old version of the function.");
			return false;
		}
		if (newFunc == function)
			return true;
		
		final Class<? extends T>[] returnTypes = this.returnTypes;
		if (returnTypes != null) {
			final ClassInfo<?> rt = newFunc.returnType;
			if (rt == null) {
				if (first)
					Skript.error("The function '" + functionName + "' doesn't return any value.");
				else
					Skript.error("The function '" + functionName + "' was redefined with no return value, but is still used in other script(s). These will continue to use the old version of the function.");
				return false;
			}
			if (!CollectionUtils.containsAnySuperclass(returnTypes, rt.getC())) {
				if (first)
					Skript.error("The returned value of the function '" + functionName + "', " + newFunc.returnType + ", is " + SkriptParser.notOfType(returnTypes) + ".");
				else
					Skript.error("The function '" + functionName + "' was redefined with a different, incompatible return type, but is still used in other script(s). These will continue to use the old version of the function.");
				return false;
			}
			if (first) {
				single = newFunc.single;
			} else if (single && !newFunc.single) {
				Skript.error("The function '" + functionName + "' was redefined with a different, incompatible return type, but is still used in other script(s). These will continue to use the old version of the function.");
				return false;
			}
		}
		
		if (parameters.length > newFunc.getMaxParamaters()) {
			if (first)
				Skript.error("The function '" + functionName + "' has only " + newFunc.getMaxParamaters() + " arguments, but " + parameters.length + " are given.");
			else
				Skript.error("The function '" + functionName + "' was redefined with a different, incompatible amount of arguments, but is still used in other script(s). These will continue to use the old version of the function.");
			return false;
		}
		if (parameters.length < newFunc.getMinParameters()) {
			if (first)
				Skript.error("The function '" + functionName + "' requires at least " + newFunc.getMinParameters() + " arguments, but " + parameters.length + " are given.");
			else
				Skript.error("The function '" + functionName + "' was redefined with a different, incompatible amount of arguments, but is still used in other script(s). These will continue to use the old version of the function.");
			return false;
		}
		for (int i = 0; i < parameters.length; i++) {
			final Parameter<?> p = newFunc.parameters[i];
			final RetainingLogHandler log = SkriptLogger.startRetainingLog();
			try {
				final Expression<?> e = parameters[i].getConvertedExpression(p.type.getC());
				if (e == null) {
					if (first)
						Skript.error("The " + StringUtils.fancyOrderNumber(i + 1) + " argument of the function '" + functionName + "' is not of the required type " + p.type + ".");
					else
						Skript.error("The function '" + functionName + "' was redefined with different, incompatible arguments, but is still used in other script(s). These will continue to use the old version of the function.");
					return false;
				}
				parameters[i] = e;
			} finally {
				log.printLog();
			}
		}
		
		function = (Function<? extends T>) newFunc;
		Functions.registerCaller(this);
		
		return true;
	}
	
	@Nullable
	protected T[] execute(final Event e) {
		final Object[][] params = new Object[parameters.length][];
		for (int i = 0; i < params.length; i++)
			params[i] = parameters[i].getArray(e); // TODO what if an argument is not available? pass null or abort?
		return function.execute(params);
	}
	
	public boolean isSingle() {
		return single;
	}
	
	@SuppressWarnings("null")
	public Class<? extends T> getReturnType() {
		return function.returnType.getC();
	}
	
	public String toString(@Nullable final Event e, final boolean debug) {
		final StringBuilder b = new StringBuilder(functionName + "(");
		for (int i = 0; i < parameters.length; i++) {
			if (i != 0)
				b.append(", ");
			b.append(parameters[i].toString(e, debug));
		}
		return "" + b.append(")");
	}
	
}
