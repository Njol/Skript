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

import org.bukkit.event.Event;

import ch.njol.skript.Skript;
import ch.njol.skript.classes.Changer;
import ch.njol.skript.classes.Changer.ChangeMode;
import ch.njol.skript.classes.Changer.ChangerUtils;
import ch.njol.skript.classes.ClassInfo;
import ch.njol.skript.lang.Effect;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.Literal;
import ch.njol.skript.lang.SkriptParser;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.skript.log.ErrorQuality;
import ch.njol.skript.log.SimpleLog;
import ch.njol.skript.log.SkriptLogger;
import ch.njol.skript.registrations.Classes;
import ch.njol.skript.util.Patterns;

/**
 * @author Peter Güttinger
 */
public class EffChange extends Effect {
	
	private static final long serialVersionUID = 5756451063750867855L;
	
	private static Patterns<ChangeMode> patterns = new Patterns<ChangeMode>(new Object[][] {
			
			{"(add|give) %objects% to %objects%", ChangeMode.ADD},
			{"increase %objects% by %objects%", ChangeMode.ADD},
			{"give %objects% %objects%", ChangeMode.ADD},
			
			{"set %objects% to %objects%", ChangeMode.SET},
			
			{"(remove|subtract) %objects% from %objects%", ChangeMode.REMOVE},
			
			{"(clear|delete) %objects%", ChangeMode.CLEAR},
	
	});
	
	static {
		Skript.registerEffect(EffChange.class, patterns.getPatterns());
	}
	
	private Expression<?> changed;
	private Expression<?> changer = null;
	
	private ChangeMode mode;
	
	private boolean single = true;
	private Changer<?, ?> c = null;
	
	@Override
	public boolean init(final Expression<?>[] vars, final int matchedPattern, final int isDelayed, final ParseResult parser) {
		
		mode = patterns.getInfo(matchedPattern);
		
		switch (mode) {
			case ADD:
				if (matchedPattern == 0) {
					changer = vars[0];
					changed = vars[1];
				} else {
					changed = vars[0];
					changer = vars[1];
				}
				break;
			case SET:
				changer = vars[1];
				changed = vars[0];
				break;
			case REMOVE:
				changer = vars[0];
				changed = vars[1];
				break;
			case CLEAR:
				changed = vars[0];
				break;
		}
		
		if (changed instanceof Literal<?>)
			return false;
		
		final int e = SkriptLogger.getNumErrors();
		Class<?>[] rs = changed.acceptChange(mode);
		final boolean hasError = SkriptLogger.getNumErrors() != e;
		if (rs == null) {
			final ClassInfo<?> ci = Classes.getSuperClassInfo(changed.getReturnType());
			if (ci == null || ci.getChanger() == null || ci.getChanger().acceptChange(mode) == null) {
				if (hasError)
					return false;
				if (mode == ChangeMode.SET)
					Skript.error(changed + " can't be set", ErrorQuality.SEMANTIC_ERROR);
				else if (mode == ChangeMode.CLEAR)
					Skript.error(changed + " can't be cleared/deleted", ErrorQuality.SEMANTIC_ERROR);
				else
					Skript.error(changed + " can't have anything " + (mode == ChangeMode.ADD ? "added to" : "removed from") + " it", ErrorQuality.SEMANTIC_ERROR);
				return false;
			}
			c = ci.getChanger();
			rs = c.acceptChange(mode);
		}
		
		if (changer != null) {
			final ErrorQuality q = changer.getReturnType() == Object.class ? ErrorQuality.NOT_AN_EXPRESSION : ErrorQuality.SEMANTIC_ERROR;
			final SimpleLog log = SkriptLogger.startSubLog();
			Expression<?> v = null;
			Class<?> x = null;
			for (final Class<?> r : rs) {
				v = changer.getConvertedExpression(r.isArray() ? r.getComponentType() : r);
				if (v != null) {
					x = r;
					break;
				}
			}
			log.stop();
			if (v == null) {
				if (log.hasErrors()) {
					SkriptLogger.log(log.getFirstError());
					return false;
				}
				if (rs.length == 1 && rs[0] == Object.class)
					Skript.error("Can't understand this expression: " + changer, ErrorQuality.NOT_AN_EXPRESSION);
				else if (mode == ChangeMode.SET)
					Skript.error(changed + " can't be set to " + changer + " because the latter is " + SkriptParser.notOfType(rs), q);
				else
					Skript.error(changer + " can't be " + (mode == ChangeMode.ADD ? "added to" : "removed from") + " " + changed + " because the former is " + SkriptParser.notOfType(rs), q);
				return false;
			}
			
			if (x.isArray()) {
				single = false;
				x = x.getComponentType();
			}
			changer = v;
			
			if (!changer.isSingle() && single) {
				if (mode == ChangeMode.SET)
					Skript.error(changed + " can only be set to one " + Classes.getSuperClassInfo(x).getName() + ", but multiple are given", ErrorQuality.SEMANTIC_ERROR);
				else
					Skript.error("only one " + Classes.getSuperClassInfo(x).getName() + " can be " + (mode == ChangeMode.ADD ? "added to" : "removed from") + " " + changed + ", but multiple are given", ErrorQuality.SEMANTIC_ERROR);
				return false;
			}
		}
		return true;
	}
	
	@Override
	protected void execute(final Event e) {
		if (c != null)
			ChangerUtils.change(c, changed.getArray(e), changer == null ? null : single ? changer.getSingle(e) : changer.getArray(e), mode);
		else
			changed.change(e, changer == null ? null : single ? changer.getSingle(e) : changer.getArray(e), mode);
	}
	
	@Override
	public String toString(final Event e, final boolean debug) {
		switch (mode) {
			case ADD:
				return "add " + changer.toString(e, debug) + " to " + changed.toString(null, true);
			case CLEAR:
				return "clear " + changed.toString(null, true);
			case REMOVE:
				return "remove " + changer.toString(e, debug) + " from " + changed.toString(null, true);
			case SET:
				return "set " + changed.toString(e, debug) + " to " + changer.toString(null, true);
			default:
				throw new IllegalStateException();
		}
	}
	
}
