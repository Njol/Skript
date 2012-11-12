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

import org.bukkit.event.Event;

import ch.njol.skript.Skript;
import ch.njol.skript.classes.Comparator;
import ch.njol.skript.classes.Comparator.Relation;
import ch.njol.skript.lang.Condition;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.skript.lang.util.SimpleExpression;
import ch.njol.skript.log.ErrorQuality;
import ch.njol.skript.log.SkriptLogger;
import ch.njol.skript.log.SubLog;
import ch.njol.skript.registrations.Classes;
import ch.njol.skript.registrations.Comparators;
import ch.njol.skript.util.Patterns;
import ch.njol.skript.util.Utils;
import ch.njol.util.Checker;

/**
 * @author Peter Güttinger
 */
public class CondIs extends Condition {
	
	private static final long serialVersionUID = 6336144625887239693L;
	
	private final static Patterns<Relation> patterns = new Patterns<Relation>(new Object[][] {
			{"%objects% ((is|are) ((greater|more|higher|bigger) than|above)|\\>) %objects%", Relation.GREATER},
			{"%objects% ((is|are) (greater|more|higher|bigger|above) [than] or (equal to|the same as)|(is not|are not|isn't|aren't) ((less|smaller) than|below)|\\>=) %objects%", Relation.GREATER_OR_EQUAL},
			{"%objects% ((is|are) ((less|smaller) than|below)|\\<) %objects%", Relation.SMALLER},
			{"%objects% ((is|are) (less|smaller|below) [than] or (equal to|the same as)|(is not|are not|isn't|aren't) ((greater|more|higher|bigger) than|above)|\\<=) %objects%", Relation.SMALLER_OR_EQUAL},
			{"%objects% ((is|are) (not|neither)|isn't|aren't|!=) [equal to] %objects%", Relation.NOT_EQUAL},
			{"%objects% (is|are|=) [(equal to|the same as)] %objects%", Relation.EQUAL},
			{"%objects% (is|are) between %objects% and %objects%", Relation.EQUAL},
			{"%objects% (is not|are not|isn't|aren't) between %objects% and %objects%", Relation.NOT_EQUAL},
			
			{"%objects@-1% (was|were) ((greater|more|higher|bigger) than|above) %objects%", Relation.GREATER},
			{"%objects@-1% ((was|were) (greater|more|higher|bigger|above) [than] or (equal to|the same as)|(was not|were not|wasn't|weren't) ((less|smaller) than|below)) %objects%", Relation.GREATER_OR_EQUAL},
			{"%objects@-1% (was|were) ((less|smaller) than|below) %objects%", Relation.SMALLER},
			{"%objects@-1% ((was|were) (less|smaller|below) [than] or (equal to|the same as)|(was not|were not|wasn't|weren't) ((greater|more|higher|bigger) than|above)) %objects%", Relation.SMALLER_OR_EQUAL},
			{"%objects@-1% ((was|were) (not|neither)|wasn't|weren't) [equal to] %objects%", Relation.NOT_EQUAL},
			{"%objects@-1% (was|were) [(equal to|the same as)] %objects%", Relation.EQUAL},
			{"%objects@-1% (was|were) between %objects% and %objects%", Relation.EQUAL},
			{"%objects@-1% (was not|were not|wasn't|weren't) between %objects% and %objects%", Relation.NOT_EQUAL},
			
			{"%objects@1% will be ((greater|more|higher|bigger) than|above) %objects%", Relation.GREATER},
			{"%objects@1% (will be (greater|more|higher|bigger|above) [than] or (equal to|the same as)|(will not|won't) be ((less|smaller) than|below)) %objects%", Relation.GREATER_OR_EQUAL},
			{"%objects@1% will be ((less|smaller) than|below) %objects%", Relation.SMALLER},
			{"%objects@1% (will be (less|smaller|below) [than] or (equal to|the same as)|(will not|won't) be ((greater|more|higher|bigger) than|above)) %objects%", Relation.SMALLER_OR_EQUAL},
			{"%objects@1% ((will (not|neither) be|won't be)|(isn't|aren't|is not|are not) (turning|changing) [in]to) [equal to] %objects%", Relation.NOT_EQUAL},
			{"%objects@1% (will be [(equal to|the same as)]|(is|are) (turning|changing) [in]to) %objects%", Relation.EQUAL},
			{"%objects@1% will be between %objects% and %objects%", Relation.EQUAL},
			{"%objects@1% (will not be|won't be) between %objects% and %objects%", Relation.NOT_EQUAL}
	});
	
	static {
		Skript.registerCondition(CondIs.class, patterns.getPatterns());
	}
	
	private Expression<?> first, second, third;
	private Relation relation;
	@SuppressWarnings("rawtypes")
	private Comparator comp;
	
	@Override
	public boolean init(final Expression<?>[] vars, final int matchedPattern, final int isDelayed, final ParseResult parser) {
		first = vars[0];
		second = vars[1];
		if (vars.length == 3)
			third = vars[2];
		relation = patterns.getInfo(matchedPattern);
		final boolean b = init();
		if (!b) {
			if (third == null && first.getReturnType() == Object.class && second.getReturnType() == Object.class) {
				return false;
			} else {
				Skript.error("Can't compare " + f(first) + " with " + f(second) + (third == null ? "" : " and " + f(third)), ErrorQuality.NOT_AN_EXPRESSION);
				return false;
			}
		}
		if (comp != null) {
			if (third == null) {
				if (!relation.isEqualOrInverse() && !comp.supportsOrdering()) {
					Skript.error("Can't test " + f(first) + " for being '" + relation + "' " + f(second), ErrorQuality.NOT_AN_EXPRESSION);
					return false;
				}
			} else {
				if (!comp.supportsOrdering()) {
					Skript.error("Can't test " + f(first) + " for being 'between' " + f(second) + " and " + f(third), ErrorQuality.NOT_AN_EXPRESSION);
					return false;
				}
			}
		}
		return true;
	}
	
	public final static String f(final Expression<?> e) {
		if (e.getReturnType() == Object.class)
			return e.toString(null, false);
		return Utils.a(Classes.getSuperClassInfo(e.getReturnType()).getName());
	}
	
	/**
	 * Does not print errors
	 * 
	 * @return
	 */
	private boolean init() {
		final SubLog log = SkriptLogger.startSubLog();
		if (first.getReturnType() == Object.class) {
			final Expression<?> e = first.getConvertedExpression(Object.class);
			if (e == null) {
				log.stop();
				return false;
			}
			first = e;
		}
		if (second.getReturnType() == Object.class) {
			final Expression<?> e = second.getConvertedExpression(Object.class);
			if (e == null) {
				log.stop();
				return false;
			}
			second = e;
		}
		if (third != null && third.getReturnType() == Object.class) {
			final Expression<?> e = third.getConvertedExpression(Object.class);
			if (e == null) {
				log.stop();
				return false;
			}
			third = e;
		}
		log.stop();
		
		final Class<?> f = first.getReturnType(), s = third == null ? second.getReturnType() : Utils.getSuperType(second.getReturnType(), third.getReturnType());
		if (f == Object.class || s == Object.class)
			return true;
		comp = Comparators.getComparator(f, s);
		
		return comp != null;
	}
	
	@Override
	public boolean check(final Event e) {
		return first.check(e, new Checker<Object>() {
			@Override
			public boolean check(final Object o1) {
				return SimpleExpression.check(second.getAll(e), new Checker<Object>() {
					@Override
					public boolean check(final Object o2) {
						if (third == null)
							return relation.is(comp != null ? comp.compare(o1, o2) : Comparators.compare(o1, o2));
						return third.check(e, new Checker<Object>() {
							@Override
							public boolean check(final Object o3) {
								return relation == Relation.NOT_EQUAL ^
										(Relation.GREATER_OR_EQUAL.is(comp != null ? comp.compare(o1, o2) : Comparators.compare(o1, o2))
										&& Relation.SMALLER_OR_EQUAL.is(comp != null ? comp.compare(o1, o3) : Comparators.compare(o1, o3)));
							}
						});
					}
				}, false, third == null ? relation == Relation.NOT_EQUAL ^ second.getAnd() : second.getAnd());
			}
		});
	}
	
	@Override
	public String toString(final Event e, final boolean debug) {
		if (third == null)
			return first.toString(e, debug) + " is " + relation + " " + second.toString(e, debug);
		return first.toString(e, debug) + " is" + (relation == Relation.EQUAL ? "" : " not") + " between " + second.toString(e, debug) + " and " + third.toString(e, debug);
	}
	
}
