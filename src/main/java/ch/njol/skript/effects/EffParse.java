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

package ch.njol.skript.effects;

import java.util.regex.PatternSyntaxException;

import org.bukkit.event.Event;

import ch.njol.skript.Skript;
import ch.njol.skript.lang.Effect;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.skript.log.ParseLogHandler;
import ch.njol.skript.log.SkriptLogger;
import ch.njol.util.Kleenean;

/**
 * TODO (>2.0)
 * 
 * @author Peter Güttinger
 */
@SuppressWarnings("serial")
public class EffParse extends Effect {
	static {
//		Skript.registerEffect(EffParse.class, "?");
		//ideas:
		// "parse %string% as %string%" with variables directly included in the pattern somehow
		// "parse %string% as <type>" and an expression 'parsed [<type>]'
		// better NOT something like an expression "parse ..." (used like "set {var}/{var::*} to parse ...") to prevent users from parsing the same thing multiple times
		//    e.g. in conditions (parse ... is air: ...; else if parse ... is dirt: ... etc.)
		//
		// in any case an expression 'parse error' in case something went wrong
	}
	
	Expression<String> string, pattern;
	
	@SuppressWarnings("unchecked")
	@Override
	public boolean init(final Expression<?>[] exprs, final int matchedPattern, final Kleenean isDelayed, final ParseResult parseResult) {
		string = (Expression<String>) exprs[0];
		pattern = (Expression<String>) exprs[1];
		return true;
	}
	
	@Override
	protected void execute(final Event e) {
		final String s = string.getSingle(e);
		final String p = pattern.getSingle(e);
		if (s == null || p == null)
			return;
		try {
			final ParseLogHandler log = SkriptLogger.startParseLogHandler();
			try {
//				SkriptParser.parseStatic(s, new SingleItemIterator<SyntaxElementInfo<?>>(new SyntaxElementInfo(new String[] {p}, )), null);
			} finally {
				log.stop();
			}
		} catch (final PatternSyntaxException ex) {
			Skript.error("Invalid pattern '" + p + "': " + ex.getLocalizedMessage());
		}
	}
	
	@Override
	public String toString(final Event e, final boolean debug) {
		return "parse " + " as ";
	}
	
}
