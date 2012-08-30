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

import org.bukkit.event.Event;
import org.bukkit.event.player.PlayerJoinEvent;

import ch.njol.skript.ScriptLoader;
import ch.njol.skript.Skript;
import ch.njol.skript.Skript.ExpressionType;
import ch.njol.skript.classes.Changer.ChangeMode;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.skript.lang.util.SimpleExpression;
import ch.njol.skript.util.Utils;

/**
 * @author Peter Güttinger
 */
public class ExprJoinMessage extends SimpleExpression<String> {
	
	static {
		Skript.registerExpression(ExprJoinMessage.class, String.class, ExpressionType.SIMPLE, "[the] (join|log[ ]in)( |-)message");
	}
	
	@Override
	public boolean init(final Expression<?>[] exprs, final int matchedPattern, final int isDelayed, final ParseResult parseResult) {
		if (!Utils.contains(ScriptLoader.currentEvents, PlayerJoinEvent.class)) {
			Skript.error("The join message can only be used in a join event");
			return false;
		}
		return true;
	}
	
	@Override
	protected String[] get(final Event e) {
		return new String[] {((PlayerJoinEvent) e).getJoinMessage()};
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
	public boolean getAnd() {
		return true;
	}
	
	@Override
	public String toString(final Event e, final boolean debug) {
		return "the message";
	}
	
	@Override
	public Class<?> acceptChange(final ChangeMode mode) {
		if (mode == ChangeMode.SET)
			return String.class;
		return null;
	}
	
	@Override
	public void change(final Event e, final Object delta, final ChangeMode mode) {
		((PlayerJoinEvent) e).setJoinMessage(Utils.replaceChatStyles((String) delta));
	}
	
}
