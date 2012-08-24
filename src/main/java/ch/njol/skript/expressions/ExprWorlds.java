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

import java.util.Iterator;

import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.event.Event;

import ch.njol.skript.Skript;
import ch.njol.skript.Skript.ExpressionType;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.skript.lang.util.SimpleExpression;

/**
 * @author Peter Güttinger
 * 
 */
public class ExprWorlds extends SimpleExpression<World> {
	
	static {
		Skript.registerExpression(ExprWorlds.class, World.class, ExpressionType.SIMPLE, "[(the|all)] worlds");
	}
	
	@Override
	public boolean init(final Expression<?>[] exprs, final int matchedPattern, final int isDelayed, final ParseResult parseResult) {
		return true;
	}
	
	@Override
	public boolean isSingle() {
		return Bukkit.getWorlds().size() == 1;
	}
	
	@Override
	public Class<? extends World> getReturnType() {
		return World.class;
	}
	
	@Override
	protected World[] get(final Event e) {
		return Bukkit.getWorlds().toArray(new World[0]);
	}
	
	@Override
	public Iterator<World> iterator(final Event e) {
		return Bukkit.getWorlds().iterator();
	}
	
	@Override
	public boolean canLoop() {
		return true;
	}
	
	@Override
	public String toString(final Event e, final boolean debug) {
		return "worlds";
	}
	
	@Override
	public boolean getAnd() {
		return true;
	}
	
}
