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

import java.util.Arrays;
import java.util.logging.Level;

import org.bukkit.event.Event;

import ch.njol.skript.Skript;
import ch.njol.skript.classes.Changer;
import ch.njol.skript.classes.Changer.ChangeMode;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Examples;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import ch.njol.skript.lang.Effect;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.Literal;
import ch.njol.skript.lang.SkriptParser;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.skript.log.CountingLogHandler;
import ch.njol.skript.log.ErrorQuality;
import ch.njol.skript.log.RetainingLogHandler;
import ch.njol.skript.log.SkriptLogger;
import ch.njol.skript.registrations.Classes;
import ch.njol.skript.util.Patterns;
import ch.njol.util.Kleenean;

/**
 * @author Peter Güttinger
 */
@SuppressWarnings("serial")
@Name("Change: Set/Add/Remove/Delete")
@Description("A very general effect that can change many <a href='../expressions'>expressions</a>. Many expressions can only be set and/or deleted, while some can have things added to or removed from them.")
@Examples({"# set:",
		"Set the player's display name to \"<red>%name of player%\"",
		"set the block above the victim to lava",
		"# add:",
		"add 2 to the player's health # preferably use '<a href='#heal'>heal</a>' for this",
		"add argument to {blacklist::*}",
		"give a diamond pickaxe of efficiency 5 to the player",
		"increase the datavalue of the clicked block by 1",
		"# remove:",
		"remove 2 pickaxes from the victim",
		"remove all iron tools from the targeted block",
		"subtract 2.5 from {points.%player%}",
		"# delete:",
		"delete the block below the player",
		"clear drops",
		"delete {variable}"})
@Since("1.0")
public class EffChange extends Effect {
	private static Patterns<ChangeMode> patterns = new Patterns<ChangeMode>(new Object[][] {
			{"(add|give) %objects% to %~objects%", ChangeMode.ADD},
			{"increase %~objects% by %objects%", ChangeMode.ADD},
			{"give %~objects% %objects%", ChangeMode.ADD},
			
			{"set %~objects% to %objects%", ChangeMode.SET},
			
			{"(remove|subtract) %objects% from %~objects%", ChangeMode.REMOVE},
			{"reduce %~objects% by %objects%", ChangeMode.REMOVE},
			
			{"(clear|delete) %~objects%", ChangeMode.DELETE},
			
			// TODO distinguish clear/delete, add REMOVE_ALL
			// {"clear %~objects%", ChangeMode.CLEAR},
			// {"remove (all|every) %objects% from %~objects%", ChangeMode.REMOVE_ALL},
	});
	
	static {
		Skript.registerEffect(EffChange.class, patterns.getPatterns());
	}
	
	private Expression<?> changed;
	private Expression<?> changer = null;
	
	private ChangeMode mode;
	
	private boolean single = true;
	
//	private Changer<?, ?> c = null;
	
	@Override
	public boolean init(final Expression<?>[] exprs, final int matchedPattern, final Kleenean isDelayed, final ParseResult parser) {
		
		mode = patterns.getInfo(matchedPattern);
		
		switch (mode) {
			case ADD:
				if (matchedPattern == 0) {
					changer = exprs[0];
					changed = exprs[1];
				} else {
					changed = exprs[0];
					changer = exprs[1];
				}
				break;
			case SET:
				changer = exprs[1];
				changed = exprs[0];
				break;
			case REMOVE:
				if (matchedPattern == 4) {
					changer = exprs[0];
					changed = exprs[1];
				} else {
					changed = exprs[0];
					changer = exprs[1];
				}
				break;
			case DELETE:
				changed = exprs[0];
				break;
			default:
				assert false;
				return false;
		}
		
		final CountingLogHandler h = SkriptLogger.startLogHandler(new CountingLogHandler(Level.SEVERE));
		final Class<?>[] rs;
		final Changer<?, ?> c;
		final String what;
		try {
			rs = changed.acceptChange(mode);
			c = Classes.getSuperClassInfo(changed.getReturnType()).getChanger();
			what = c == null || !Arrays.equals(c.acceptChange(mode), rs) ? changed.toString(null, false) : Classes.getSuperClassInfo(changed.getReturnType()).getName().withIndefiniteArticle();
		} finally {
			h.stop();
		}
		if (rs == null) {
			if (h.getCount() > 0)
				return false;
			if (mode == ChangeMode.SET)
				Skript.error(what + " can't be 'set' to anything", ErrorQuality.SEMANTIC_ERROR);
			else if (mode == ChangeMode.DELETE)
				Skript.error(what + " can't be cleared/deleted", ErrorQuality.SEMANTIC_ERROR);
			else
				Skript.error(what + " can't have anything " + (mode == ChangeMode.ADD ? "added to" : "removed from") + " it", ErrorQuality.SEMANTIC_ERROR);
			return false;
		}
		
		if (changer != null) {
			final ErrorQuality q = changer.getReturnType() == Object.class ? ErrorQuality.NOT_AN_EXPRESSION : ErrorQuality.SEMANTIC_ERROR;
			Expression<?> v = null;
			Class<?> x = null;
			final RetainingLogHandler log = SkriptLogger.startRetainingLog();
			try {
				for (final Class<?> r : rs) {
					if ((r.isArray() ? r.getComponentType() : r).isAssignableFrom(changer.getReturnType())) {
						v = changer.getConvertedExpression(Object.class);
						x = r;
					}
				}
				if (v == null) {
					for (final Class<?> r : rs) {
						v = changer.getConvertedExpression(r.isArray() ? r.getComponentType() : r);
						if (v != null) {
							x = r;
							break;
						}
					}
				}
			} finally {
				log.stop();
			}
			if (v == null) {
				if (log.hasErrors()) {
					SkriptLogger.log(log.getFirstError());
					return false;
				}
				final Class<?>[] r = new Class[rs.length];
				for (int i = 0; i < rs.length; i++)
					r[i] = rs[i].isArray() ? rs[i].getComponentType() : rs[i];
				if (rs.length == 1 && rs[0] == Object.class)
					Skript.error("Can't understand this expression: " + changer, ErrorQuality.NOT_AN_EXPRESSION);
				else if (mode == ChangeMode.SET)
					Skript.error(what + " can't be set to " + changer + " because the latter is " + SkriptParser.notOfType(r), q);
				else
					Skript.error(what + " can't be " + (mode == ChangeMode.ADD ? "added to" : "removed from") + " " + changed + " because the former is " + SkriptParser.notOfType(r), q);
				return false;
			}
			
			if (x.isArray()) {
				single = false;
				x = x.getComponentType();
			}
			changer = v;
			
			if (!changer.isSingle() && single) {
				if (mode == ChangeMode.SET)
					Skript.error(changed + " can only be set to one " + Classes.getSuperClassInfo(x).getName() + ", not more", ErrorQuality.SEMANTIC_ERROR);
				else
					Skript.error("only one " + Classes.getSuperClassInfo(x).getName() + " can be " + (mode == ChangeMode.ADD ? "added to" : "removed from") + " " + changed + ", not more", ErrorQuality.SEMANTIC_ERROR);
				return false;
			}
		}
		return true;
	}
	
	@Override
	protected void execute(final Event e) {
		final Object delta = changer == null ? null : single ? changer.getSingle(e) : changer.getArray(e);
		if (delta == null && mode != ChangeMode.DELETE)
			return;
		changed.change(e, delta, mode);
//		changed.change(e, new Changer2<Object>() {
//			@Override
//			public Object change(Object o) {
//				return delta;
//			}
//		}, mode);
	}
	
	@Override
	public String toString(final Event e, final boolean debug) {
		switch (mode) {
			case ADD:
				return "add " + changer.toString(e, debug) + " to " + changed.toString(null, true);
			case DELETE:
				return "delete " + changed.toString(null, true);
			case REMOVE:
				return "remove " + changer.toString(e, debug) + " from " + changed.toString(null, true);
			case SET:
				return "set " + changed.toString(e, debug) + " to " + changer.toString(null, true);
			default:
				throw new IllegalStateException();
		}
	}
	
}
