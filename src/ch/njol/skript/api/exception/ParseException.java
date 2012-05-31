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

package ch.njol.skript.api.exception;

import ch.njol.skript.lang.ExprParser.ParseResult;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.Variable;

/**
 * @author Peter Güttinger
 * @see Expression#init(Variable[], int, ParseResult)
 */
public class ParseException extends Exception {
	
	private static final long serialVersionUID = 3060684476033398334L;
	
	public ParseException(final String cause) {
		super(cause);
	}
	
	/**
	 * If this constructor is used, Skript's error cause will not be changed. This is usually not what you want.
	 */
	public ParseException() {}
	
	/**
	 * alias of {@link #getMessage()}
	 * 
	 * @return
	 */
	public String getError() {
		return getMessage();
	}
	
}
