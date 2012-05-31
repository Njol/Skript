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

package ch.njol.skript.lang;

import ch.njol.skript.api.exception.InitException;
import ch.njol.skript.api.exception.ParseException;
import ch.njol.skript.api.intern.TopLevelExpression;
import ch.njol.skript.lang.ExprParser.ParseResult;

/**
 * Represents a general part of the syntax. Implementing classes are {@link SimpleVariable} and {@link TopLevelExpression}.
 * 
 * @author Peter Güttinger
 * 
 */
public interface Expression {
	
	/**
	 * called just after the constructor.
	 * 
	 * @param vars all %var%s included in the matching pattern in the order they appear in the pattern. If an optional value was left out it will still be included in this list
	 *            holding the default value of the desired type which usually depends on the event.
	 * @param matchedPattern the index of the pattern which matched
	 * @param parseResult The parser osed to parse this expression. Might hold useful information in the future.
	 * 
	 * @throws InitException Throwing this has the same effect as if no pattern matched. This is an exception, meaning it should only be thrown in exceptional cases where a pattern
	 *             is not enough.
	 * @throws ParseException Throwns if some part of the expression was parsed and found to be invalid, but the whole expression still matched correctly.<br/>
	 *             This will immediately print an error and it's cause which is set to the cause passed to the exception.
	 */
	public void init(Variable<?>[] vars, int matchedPattern, ParseResult parseResult) throws InitException, ParseException;
	
}
