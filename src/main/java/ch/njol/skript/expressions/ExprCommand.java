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

package ch.njol.skript.expressions;

import org.bukkit.event.Event;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import org.bukkit.event.server.ServerCommandEvent;

import ch.njol.skript.ScriptLoader;
import ch.njol.skript.Skript;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Examples;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.ExpressionType;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.skript.lang.util.SimpleExpression;
import ch.njol.util.Kleenean;

/**
 * @author Peter Güttinger
 */
@SuppressWarnings("serial")
@Name("Command")
@Description("The command that caused an 'on command' event (excluding the leading slash and all arguments)")
@Examples({"# prevent any commands except for the /exit command during some game",
		"on command:",
		"{game.%player%.is playing} is true",
		"command is not \"exit\"",
		"message \"You're not allowed to use commands during the game\"",
		"cancel the event"})
@Since("2.0")
public class ExprCommand extends SimpleExpression<String> {
	static {
		Skript.registerExpression(ExprCommand.class, String.class, ExpressionType.SIMPLE, "[the] command [label]");
	}
	
	@SuppressWarnings("unchecked")
	@Override
	public boolean init(final Expression<?>[] exprs, final int matchedPattern, final Kleenean isDelayed, final ParseResult parseResult) {
		if (!ScriptLoader.isCurrentEvent(PlayerCommandPreprocessEvent.class, ServerCommandEvent.class)) {
			Skript.error("The 'command' expression can only be used in a command event");
			return false;
		}
		return true;
	}
	
	@Override
	protected String[] get(final Event e) {
		final String s;
		if (e instanceof PlayerCommandPreprocessEvent) {
			s = ((PlayerCommandPreprocessEvent) e).getMessage().substring(1);
		} else if (e instanceof ServerCommandEvent) {
			s = ((ServerCommandEvent) e).getCommand();
		} else {
			return null;
		}
		final int c = s.indexOf(' ');
		return new String[] {c == -1 ? s : s.substring(0, c)};
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
		return false;
	}
	
	@Override
	public String toString(final Event e, final boolean debug) {
		return "the command";
	}
	
}
