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

package ch.njol.skript.expressions;

import org.bukkit.entity.Entity;
import org.bukkit.event.Event;

import ch.njol.skript.Skript;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.SimpleExpression;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.skript.util.EntityType;

/**
 * @author Peter Güttinger
 *
 */
public class ExprTypeOf extends SimpleExpression<String> {

	static {
		Skript.registerExpression(ExprTypeOf.class, String.class, "[the] type of %entitytype|itemtype%");
	}
	
	private Expression<?> expr;
	
	@Override
	public boolean init(Expression<?>[] exprs, int matchedPattern, ParseResult parseResult) {
		expr = exprs[0];
		return true;
	}

	@Override
	public boolean isSingle() {
		return true;
	}

	@Override
	public Class<? extends String> getReturnType() {
		return String.class;
	}

	@Override
	public String getDebugMessage(Event e) {
		return "type of "+expr.getDebugMessage(e);
	}

	@Override
	protected String[] getAll(Event e) {
		Object o = expr.getSingle(e);
		if (o instanceof Entity) {
			return new String[] {EntityType.toString((Entity) o)};
		} else if (o instanceof EntityType) {
			return new String[] {EntityType.toString(((EntityType) o).c)};
		} else if (o instanceof ItemType) {
			
		}
		return null;
	}

	@Override
	public String toString() {
		return "the type of "+expr;
	}
	
}
