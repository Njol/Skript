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

package ch.njol.skript.variables.base;

import org.bukkit.event.Event;

import ch.njol.skript.Skript;
import ch.njol.skript.api.Changer.ChangeMode;
import ch.njol.skript.api.Converter;
import ch.njol.skript.api.intern.ConvertedVariable;
import ch.njol.skript.lang.SimpleVariable;
import ch.njol.skript.lang.Variable;
import ch.njol.util.Validate;

/**
 * Represents a variable which is a wrapper of another one. To use, set the protected field {@link #var} to the variable you want to wrap.<br/>
 * If you don't override {@link #getConvertedVar(Class)} you should not override any other methods of this class.
 * 
 * @author Peter Güttinger
 */
public abstract class VarVariable<T> extends SimpleVariable<T> {
	
	/** the wrapped variable */
	protected SimpleVariable<? extends T> var;
	
	protected VarVariable() {}
	
	public VarVariable(final SimpleVariable<? extends T> var) {
		Validate.notNull(var);
		this.var = var;
	}
	
	@SuppressWarnings("unchecked")
	@Override
	protected <R> ConvertedVariable<T, ? extends R> getConvertedVar(final Class<R> to) {
		final Converter<? super T, ? extends R> conv = (Converter<? super T, ? extends R>) Skript.getConverter(getReturnType(), to);
		if (conv == null)
			return null;
		return new ConvertedVariable<T, R>(var, to, conv) {
			@Override
			public String getDebugMessage(final Event e) {
				return "{" + VarVariable.this.getDebugMessage(e) + "}->" + to.getName();
			}
		};
	}
	
	@Override
	protected T[] getAll(final Event e) {
		return var.getArray(e);
	}
	
	@Override
	public boolean isSingle() {
		return var.isSingle();
	}
	
	@Override
	public void setAnd(final boolean and) {
		super.setAnd(and);
		var.setAnd(and);
	}
	
	@Override
	public Class<? extends T> getReturnType() {
		return var.getReturnType();
	}
	
	@Override
	public Class<?> acceptChange(final ChangeMode mode) {
		return var.acceptChange(mode);
	}
	
	@Override
	public void change(final Event e, final Variable<?> delta, final ChangeMode mode) {
		var.change(e, delta, mode);
	}
	
	@Override
	public String toString() {
		return var.toString();
	}
	
}
