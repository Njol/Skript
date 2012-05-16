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
import ch.njol.skript.api.Comparator;
import ch.njol.skript.api.Comparator.ComparatorInfo;
import ch.njol.skript.api.Comparator.Relation;
import ch.njol.skript.api.Condition;
import ch.njol.skript.api.exception.InitException;
import ch.njol.skript.api.exception.ParseException;
import ch.njol.skript.lang.ExprParser.ParseResult;
import ch.njol.skript.lang.UnparsedLiteral;
import ch.njol.skript.lang.Variable;
import ch.njol.skript.util.Patterns;
import ch.njol.util.Checker;

/**
 * @author Peter Güttinger
 * 
 */
public class CondIs extends Condition {
	
	private final static Patterns<Object> patterns = new Patterns<Object>(new Object[][] {
			{"%objects% ((is|are) (greater|more|higher|bigger) than|\\>) %objects%", Relation.GREATER},
			{"%objects% ((is|are) ((greater|more|higher|bigger) or equal to|not (less|smaller) than)|\\>=) %objects%", Relation.GREATER_OR_EQUAL},
			{"%objects% ((is|are) (less|smaller) than|\\<) %objects%", Relation.SMALLER},
			{"%objects% ((is|are) ((less|smaller) or equal to|not (greater|more|higher|bigger) than)|\\<=) %objects%", Relation.SMALLER_OR_EQUAL},
			{"%objects% ((is|are) not|isn't|aren't|!=) [equal to] %objects%", Relation.NOT_EQUAL},
			{"%objects% (is|are|=) [equal to] %objects%", Relation.EQUAL},
			{"%objects% (is|are) between %objects% and %objects%", true},
			{"%objects% ((is|are) not|isn't|aren't) between %objects% and %objects%", false}
	});
	
	static {
		Skript.addCondition(CondIs.class, patterns.getPatterns());
	}
	
	private Variable<?> first, second, third;
	private Relation relation;
	private Comparator<?, ?> comp;
	private boolean reverseOrder = false;
	
	@Override
	public void init(final Variable<?>[] vars, final int matchedPattern, final ParseResult parser) throws ParseException, InitException {
		first = vars[0];
		second = vars[1];
		relation = null;
		final Object r = patterns.getInfo(matchedPattern);
		if (r instanceof Relation) {
			relation = (Relation) r;
		} else {
			third = vars[2];
			relation = Relation.get((Boolean) r);
		}
		final boolean b = init();
		if (!b) {
			if (first instanceof UnparsedLiteral || second instanceof UnparsedLiteral || third instanceof UnparsedLiteral) {
				throw new InitException();
			} else {
				if (Skript.debug())
					Skript.info("Can't compare " + first.getReturnType().getName() + " with " + second.getReturnType().getName() + (third == null ? "" : " and " + third.getReturnType().getName()));
				throw new ParseException("the given values cannot be compared");
			}
		}
		if (third == null) {
			if (!comp.supportsRelation(relation)) {
				throw new ParseException("The given objects can't be compared with '" + relation + "'");
			}
		} else {
			if (!((comp.supportsRelation(Relation.GREATER) || comp.supportsRelation(Relation.GREATER_OR_EQUAL))
			&& (comp.supportsRelation(Relation.SMALLER) || comp.supportsRelation(Relation.SMALLER_OR_EQUAL)))) {
				throw new ParseException("The given objects can't be checked for being 'in between'");
			}
		}
	}
	
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
		
		for (final ComparatorInfo<?, ?> info : Skript.getComparators()) {
			for (final int c : zeroOne) {
				if (info.getType(c).isAssignableFrom(f)) {
					final Variable<?> temp1 = second.getConvertedVariable(info.getType(1 - c));
					final Variable<?> temp2 = third == null ? null : third.getConvertedVariable(info.getType(1 - c));
					if (temp1 != null && (third == null || temp2 != null)) {
						reverseOrder = c == 1;
						comp = info.c;
						second = temp1;
						third = temp2;
						return true;
					}
				}
				if (info.getType(c).isAssignableFrom(s) && (third == null || info.getType(c).isAssignableFrom(t))) {
					final Variable<?> temp = first.getConvertedVariable(info.getType(1 - c));
					if (temp != null) {
						reverseOrder = c == 0;
						comp = info.c;
						first = temp;
						return true;
					}
				}
			}
		}
		
		for (final ComparatorInfo<?, ?> info : Skript.getComparators()) {
			for (final int c : zeroOne) {
				final Variable<?> v1 = first.getConvertedVariable(info.getType(c));
				final Variable<?> v2 = second.getConvertedVariable(info.getType(1 - c));
				final Variable<?> v3 = third == null ? null : third.getConvertedVariable(info.getType(1 - c));
				if (v1 != null && v2 != null && (third == null || v3 != null)) {
					first = v1;
					second = v2;
					third = v3;
					reverseOrder = c == 1;
					comp = info.c;
					return true;
				}
			}
		}
		/*
		if (s != Object.class) {
			final Variable<?> v1 = first.getConvertedVariable(s);
			if (v1 != null) {
				comp = Comparator.equalsComparator;
				first = v1;
				return true;
			}
		}
		if (f != Object.class) {
			final Variable<?> v2 = second.getConvertedVariable(f);
			if (v2 != null) {
				comp = Comparator.equalsComparator;
				second = v2;
				return true;
			}
		}
		*/
		return false;
	}
	
	@Override
	public boolean run(final Event e) {
		return first.check(e, new Checker<Object>() {
			@Override
			public boolean check(final Object o1) {
				return second.check(e, new Checker<Object>() {
					@Override
					public boolean check(final Object o2) {
						if (third == null)
							return relation.is(reverseOrder ? compare(o2, o1) : compare(o1, o2));
						return third.check(e, new Checker<Object>() {
							@Override
							public boolean check(final Object o3) {
								return relation == Relation.NOT_EQUAL ^
										(Relation.GREATER_OR_EQUAL.is(reverseOrder ? compare(o2, o1) : compare(o1, o2))
										&& Relation.SMALLER_OR_EQUAL.is(reverseOrder ? compare(o3, o1) : compare(o1, o3)));
							}
						});
					}
				});
			}
		}, this);
	}
	
	@SuppressWarnings("unchecked")
	private <T1, T2> Relation compare(final T1 o1, final T2 o2) {
		return ((Comparator<? super T1, ? super T2>) comp).compare(o1, o2);
	}
	
	@Override
	public String getDebugMessage(final Event e) {
		if (third == null)
			return first.getDebugMessage(e) + " is " + relation + " " + second.getDebugMessage(e);
		return first.getDebugMessage(e) + " is" + (relation == Relation.EQUAL ? "" : " not") + " between " + second.getDebugMessage(e) + " and " + third.getDebugMessage(e);
	}
	
}
