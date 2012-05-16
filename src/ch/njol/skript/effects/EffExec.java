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

package ch.njol.skript.effects;

import org.bukkit.command.CommandSender;
import org.bukkit.event.Event;

import ch.njol.skript.Skript;
import ch.njol.skript.api.Effect;
import ch.njol.skript.api.exception.InitException;
import ch.njol.skript.lang.ExprParser.ParseResult;
import ch.njol.skript.lang.Variable;
import ch.njol.skript.util.ErrorSession;

/**
 * @author Peter Güttinger
 * 
 */
public class EffExec extends Effect {
	
	static {
		Skript.addEffect(EffExec.class, "exec[ute] %string%");
	}
	
	private Variable<String> input;
	
	@SuppressWarnings("unchecked")
	@Override
	public void init(final Variable<?>[] vars, final int matchedPattern, final ParseResult parser) throws InitException {
		input = (Variable<String>) vars[0];
	}
	
	@Override
	protected void execute(final Event e) {
		final ErrorSession session = Skript.startErrorSession();
		final String s = input.getSingle(e);
		if (s == null) {
			final CommandSender sender = Skript.getEventValue(e, CommandSender.class);
			session.printErrors(sender);
			Skript.stopErrorSession();
			return;
		}
		final Effect eff = Effect.parse(s);
		if (eff != null) {
			eff.run(e);
		} else {
			final CommandSender sender = Skript.getEventValue(e, CommandSender.class);
			session.printErrors(sender);
		}
		Skript.stopErrorSession();
	}
	
	@Override
	public String getDebugMessage(final Event e) {
		return "exec " + input.getDebugMessage(e);
	}
	
}
