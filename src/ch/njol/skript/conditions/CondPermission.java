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

package ch.njol.skript.conditions;

import org.bukkit.command.CommandSender;
import org.bukkit.event.Event;

import ch.njol.skript.Skript;
import ch.njol.skript.api.Condition;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.util.Checker;

/**
 * @author Peter Güttinger
 * 
 */
public class CondPermission extends Condition {
	
	static {
		Skript.registerCondition(CondPermission.class,
				"[%commandsenders%] (do[es]n't|don't|do[es] not) have [the] permission[s] %strings%",
				"[%commandsenders%] ha(s|ve) [the] permission[s] %strings%");
	}
	
	private Expression<String> permissions;
	private Expression<CommandSender> senders;
	
	@SuppressWarnings("unchecked")
	@Override
	public boolean init(final Expression<?>[] vars, final int matchedPattern, final ParseResult parser) {
		senders = (Expression<CommandSender>) vars[0];
		permissions = (Expression<String>) vars[1];
		setNegated(matchedPattern == 0);
		return true;
	}
	
	@Override
	public boolean run(final Event e) {
		return senders.check(e, new Checker<CommandSender>() {
			@Override
			public boolean check(final CommandSender s) {
				return permissions.check(e, new Checker<String>() {
					@Override
					public boolean check(final String perm) {
						if (s.hasPermission(perm))
							return true;
						// player has perm skript.foo.bar if he has skript.foo.* or skript.*, but not for other plugin's permissions since they can define their own *
						if (perm.startsWith("skript.")) {
							for (int i = perm.lastIndexOf('.'); i > 0; i = perm.lastIndexOf('.', i - 1)) {
								if (s.hasPermission(perm.substring(0, i + 1) + "*"))
									return true;
							}
						}
						return false;
					}
				});
			}
		}, this);
	}
	
	@Override
	public String getDebugMessage(final Event e) {
		return senders.getDebugMessage(e) + " " + (isNegated() ? "doesn't have" : "has") + " permission " + permissions.getDebugMessage(e);
	}
	
}
