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
import ch.njol.skript.SkriptLogger;
import ch.njol.skript.SkriptLogger.SubLog;
import ch.njol.skript.api.Comparator;
import ch.njol.skript.api.Comparator.ComparatorInfo;
import ch.njol.skript.api.Comparator.Relation;
import ch.njol.skript.api.Condition;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.skript.lang.UnparsedLiteral;
import ch.njol.skript.util.Patterns;
import ch.njol.util.Checker;

/**
 * @author Peter Güttinger
 * 
 */
public class CondIs extends Condition {
	
	private final static Patterns<Object> patterns = new Patterns<Object>(new Object[][] {
			{"%objects% ((is|are) (greater|more|higher|bigger) than|\\>) %objects%", Relation.GREATER},
			{"%objects% ((is|are) (greater|more|higher|bigger) or equal to|(is not|are not|isn't|aren't) (less|smaller) than|\\>=) %objects%", Relation.GREATER_OR_EQUAL},
			{"%objects% ((is|are) (less|smaller) than|\\<) %objects%", Relation.SMALLER},
			{"%objects% ((is|are) (less|smaller) or equal to|(is not|are not|isn't|aren't) (greater|more|higher|bigger) than|\\<=) %objects%", Relation.SMALLER_OR_EQUAL},
			{"%objects% ((is|are) (not|neither)|isn't|aren't|!=) [equal to] %objects%", Relation.NOT_EQUAL},
			{"%objects% (is|are|=) [equal to] %objects%", Relation.EQUAL},
			{"%objects% (is|are) between %objects% and %objects%", true},
			{"%objects% (is not|are not|isn't|aren't) between %objects% and %objects%", false},
			
			{"%objects@-1% (was|were) (greater|more|higher|bigger) than %objects%", Relation.GREATER},
			{"%objects@-1% ((was|were) (greater|more|higher|bigger) or equal to|(was not|were not|wasn't|weren't) (less|smaller) than) %objects%", Relation.GREATER_OR_EQUAL},
			{"%objects@-1% (was|were) (less|smaller) than %objects%", Relation.SMALLER},
			{"%objects@-1% ((was|were) (less|smaller) or equal to|(was not|were not|wasn't|weren't) (greater|more|higher|bigger) than) %objects%", Relation.SMALLER_OR_EQUAL},
			{"%objects@-1% ((was|were) (not|neither)|wasn't|weren't) [equal to] %objects%", Relation.NOT_EQUAL},
			{"%objects@-1% (was|were) [equal to] %objects%", Relation.EQUAL},
			{"%objects@-1% (was|were) between %objects% and %objects%", true},
			{"%objects@-1% (was not|were not|wasn't|weren't) between %objects% and %objects%", false},
			
			{"%objects@1% will be (greater|more|higher|bigger) than %objects%", Relation.GREATER},
			{"%objects@1% (will be (greater|more|higher|bigger) or equal to|(will not|won't) be (less|smaller) than) %objects%", Relation.GREATER_OR_EQUAL},
			{"%objects@1% will be (less|smaller) than %objects%", Relation.SMALLER},
			{"%objects@1% (will be (less|smaller) or equal to|(will not|won't) be (greater|more|higher|bigger) than) %objects%", Relation.SMALLER_OR_EQUAL},
			{"%objects@1% ((will (not|neither) be|won't be)|(isn't|aren't|is not|are not) (turning|changing) [in]to) [equal to] %objects%", Relation.NOT_EQUAL},
			{"%objects@1% (will be [equal to]|(is|are) (turning|changing) [in]to) %objects%", Relation.EQUAL},
			{"%objects@1% will be between %objects% and %objects%", true},
			{"%objects@1% (will not be|won't be) between %objects% and %objects%", false}
	});
	
	static {
		Skript.registerCondition(CondIs.class, patterns.getPatterns());
	}
	
	private Expression<?> first, second, third;
	private Relation relation;
	private Comparator<?, ?> comp;
	/**
	 * True to compare like comp.compare(second, first), false for comp.compare(first, second)
	 */
	private boolean reverseOrder = false;
	
	@Override
	public boolean init(final Expression<?>[] vars, final int matchedPattern, final ParseResult parser) {
		first = vars[0];
		second = vars[1];
		relation = null;
		final Object r = patterns.getInfo(matchedPattern);
		if (r instanceof Relation) {
			relation = (Relation) r;
			if (relation == Relation.NOT_EQUAL) {
				relation = Relation.EQUAL;
				setNegated(true);
			}
		} else {
			third = vars[2];
			relation = Relation.get((Boolean) r);
		}
		final boolean b = init();
		if (!b) {
			if (Skript.debug())
				Skript.info("Can't compare " + first.getDebugMessage(null) + " with " + second.getDebugMessage(null) + (third == null ? "" : " and " + third.getDebugMessage(null)));
			if (first instanceof UnparsedLiteral || second instanceof UnparsedLiteral || third instanceof UnparsedLiteral) {
				return false;
			} else {
				if (third == null)
					Skript.error(first + " and " + second + " can't be compared");
				else
					Skript.error(first + " can't be compared with " + second + " and/or " + third);
				return false;
			}
		}
		if (third == null) {
			if (!relation.isEqualOrInverse() && !comp.supportsOrdering()) {
				Skript.error("The given objects can't be compared with '" + relation + "'");
				return false;
			}
		} else {
			if (!comp.supportsOrdering()) {
				Skript.error("The given objects can't be checked for being 'in between'");
				return false;
			}
		}
		return true;
	}
	
	/**
	 * Does not print errors
	 * 
	 * @return
	 */
	private boolean init() {
		final Class<?> f = first.getReturnType(), s = second.getReturnType(), t = third == null ? null : third.getReturnType();
		final int[] zeroOne = {0, 1};
		
		for (final ComparatorInfo<?, ?> info : Skript.getComparators()) {
			if (info.c1.isAssignableFrom(f) && info.c2.isAssignableFrom(s) && (third == null || info.c2.isAssignableFrom(t))
					|| info.c1.isAssignableFrom(s) && (third == null || info.c1.isAssignableFrom(t)) && info.c2.isAssignableFrom(f)) {
				comp = info.c;
				reverseOrder = !(info.c1.isAssignableFrom(f) && info.c2.isAssignableFrom(s) && (third == null || info.c2.isAssignableFrom(t)));
				return true;
			}
		}
		
		final SubLog log = SkriptLogger.startSubLog();
		
		for (final ComparatorInfo<?, ?> info : Skript.getComparators()) {
			for (final int c : zeroOne) {
				if (info.getType(c).isAssignableFrom(f)) {
					final Expression<?> temp1 = second.getConvertedExpression(info.getType(1 - c));
					final Expression<?> temp2 = third == null ? null : third.getConvertedExpression(info.getType(1 - c));
					if (temp1 != null && (third == null || temp2 != null)) {
						reverseOrder = c == 1;
						comp = info.c;
						second = temp1;
						third = temp2;
						
						SkriptLogger.stopSubLog(log);
						log.printLog();
						return true;
					}
					log.clear();
				}
				if (info.getType(c).isAssignableFrom(s) && (third == null || info.getType(c).isAssignableFrom(t))) {
					final Expression<?> temp = first.getConvertedExpression(info.getType(1 - c));
					if (temp != null) {
						reverseOrder = c == 0;
						comp = info.c;
						first = temp;
						
						SkriptLogger.stopSubLog(log);
						log.printLog();
						return true;
					}
					log.clear();
				}
			}
		}
		
		for (final ComparatorInfo<?, ?> info : Skript.getComparators()) {
			for (final int c : zeroOne) {
				final Expression<?> v1 = first.getConvertedExpression(info.getType(c));
				final Expression<?> v2 = second.getConvertedExpression(info.getType(1 - c));
				final Expression<?> v3 = third == null ? null : third.getConvertedExpression(info.getType(1 - c));
				if (v1 != null && v2 != null && (third == null || v3 != null)) {
					first = v1;
					second = v2;
					third = v3;
					reverseOrder = c == 1;
					comp = info.c;
					
					SkriptLogger.stopSubLog(log);
					log.printLog();
					return true;
				}
				log.clear();
			}
		}
		/*
		if (s != Object.class) {
			final Expression<?> v1 = first.getConvertedExpression(s);
			if (v1 != null) {
				comp = Comparator.equalsComparator;
				first = v1;
				return true;
			}
		}
		if (f != Object.class) {
			final Expression<?> v2 = second.getConvertedExpression(f);
			if (v2 != null) {
				comp = Comparator.equalsComparator;
				second = v2;
				return true;
			}
		}
		*/
		
		SkriptLogger.stopSubLog(log);
		return false;
	}
	
	@Override
	public boolean check(final Event e) {
		return first.check(e, new Checker<Object>() {
			@Override
			public boolean check(final Object o1) {
				return second.check(e, new Checker<Object>() {
					@Override
					public boolean check(final Object o2) {
						if (third == null)
							return relation.is(compare(o1, o2));
						return third.check(e, new Checker<Object>() {
							@Override
							public boolean check(final Object o3) {
								return relation == Relation.NOT_EQUAL ^
										(Relation.GREATER_OR_EQUAL.is(compare(o1, o2))
										&& Relation.SMALLER_OR_EQUAL.is(compare(o1, o3)));
							}
						});
					}
				});
			}
		}, this);
	}
	
	@SuppressWarnings("unchecked")
	private <T1, T2> Relation compare(final T1 o1, final T2 o2) {
		return reverseOrder ? ((Comparator<? super T2, ? super T1>) comp).compare(o2, o1).getSwitched() : ((Comparator<? super T1, ? super T2>) comp).compare(o1, o2);
	}
	
	@Override
	public String getDebugMessage(final Event e) {
		if (third == null)
			return first.getDebugMessage(e) + " is " + (isNegated() ? relation.getInverse() : relation) + " " + second.getDebugMessage(e);
		return first.getDebugMessage(e) + " is" + (relation == Relation.EQUAL ? "" : " not") + " between " + second.getDebugMessage(e) + " and " + third.getDebugMessage(e);
	}
	
}
