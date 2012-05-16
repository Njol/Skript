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

package ch.njol.skript.api.intern;

import org.bukkit.event.Event;

import ch.njol.skript.api.Changer.ChangeMode;
import ch.njol.skript.api.exception.InitException;
import ch.njol.skript.api.exception.ParseException;
import ch.njol.skript.lang.ExprParser.ParseResult;
import ch.njol.skript.lang.SimpleVariable;
import ch.njol.skript.lang.Variable;

/**
 * Represents a variable converted to another type. This, and not Variable, is the required return type of {@link SimpleVariable#getConvertedVar(Class)} because this class<br/>
 * <ol>
 * <li>automatically lets the source variable handle everything apart from the get() methods</li>
 * <li>will never convert itself to another type, but rather request a new converted variable from the source variable.</li>
 * </ol>
 * 
 * @author Peter Güttinger
 * 
 */
public abstract class ConvertedVariable<T> extends SimpleVariable<T> {
	
	@Override
	public final void init(final Variable<?>[] vars, final int matchedPattern, final ParseResult matcher) throws InitException, ParseException {
		throw new RuntimeException();
	}
	
	protected SimpleVariable<?> source;
	protected Class<T> to;
	
	public ConvertedVariable(final SimpleVariable<?> source, final Class<T> to) {
		this.source = source;
		setAnd(source.getAnd());
		this.to = to;
	}
	
	@Override
	public String getDebugMessage(final Event e) {
		if (e == null)
			return "{" + source.getDebugMessage(e) + "}->" + to.getName();
		return source.getDebugMessage(e);
	}
	
	@Override
	public String toString() {
		return source.toString();
	}
	
	@Override
	public Class<T> getReturnType() {
		return to;
	}
	
	@Override
	public boolean isSingle() {
		return source.isSingle();
	}
	
	@SuppressWarnings("unchecked")
	@Override
	public <R> ConvertedVariable<? extends R> getConvertedVar(final Class<R> to) {
		if (to.isAssignableFrom(getReturnType()))
			return (ConvertedVariable<? extends R>) this;
		return (ConvertedVariable<? extends R>) source.getConvertedVariable(to);
	}
	
	@Override
	public void setAnd(final boolean and) {
		super.setAnd(and);
		source.setAnd(and);
	}
	
	@Override
	public Class<?> acceptChange(final ChangeMode mode) {
		return source.acceptChange(mode);
	}
	
	@Override
	public void change(final Event e, final Variable<?> delta, final ChangeMode mode) {
		source.change(e, delta, mode);
	}
	
}
