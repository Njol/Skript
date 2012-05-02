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

import ch.njol.skript.api.Changer.ChangeMode;
import ch.njol.skript.api.intern.ConvertedVariable;
import ch.njol.skript.api.intern.Variable;
import ch.njol.util.Validate;

/**
 * Represents a variable which is a wrapper of another one. To use, set the protected field {@link #var} to the variable you want to wrap.<br/>
 * If you don't override {@link #getConvertedVar(Class)} you should not override any other methods of this class.
 * 
 * @author Peter Güttinger
 */
public abstract class VarVariable<T> extends Variable<T> {
	
	/** the wrapped variable */
	protected Variable<? extends T> var;
	
	protected VarVariable() {}
	
	public VarVariable(final Variable<? extends T> var) {
		Validate.notNull(var);
		this.var = var;
	}
	
	@Override
	protected <R> ConvertedVariable<? extends R> getConvertedVar(final Class<R> to) {
		final Variable<?> siht = this;
		final Variable<? extends R> v = var.getConvertedVariable(to);
		return new ConvertedVariable<R>(v, to) {
			@Override
			protected R[] getAll(final Event e) {
				return v.get(e);
			}
			
			@Override
			public String getDebugMessage(final Event e) {
				return "{" + siht.getDebugMessage(e) + "}->" + to.getName();
			}
		};
	}
	
	@Override
	protected T[] getAll(final Event e) {
		return var.get(e);
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
