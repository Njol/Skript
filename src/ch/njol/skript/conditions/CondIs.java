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

import java.util.regex.Matcher;

import org.bukkit.event.Event;

import ch.njol.skript.Skript;
import ch.njol.skript.api.Comparator;
import ch.njol.skript.api.Comparator.ComparatorInfo;
import ch.njol.skript.api.Comparator.Relation;
import ch.njol.skript.api.Condition;
import ch.njol.skript.api.exception.ParseException;
import ch.njol.skript.api.intern.UnparsedLiteral;
import ch.njol.skript.api.intern.Variable;
import ch.njol.skript.util.Patterns;
import ch.njol.util.Checker;

/**
 * @author Peter Güttinger
 * 
 */
public class CondIs extends Condition {
	
	private final static Patterns<Relation> patterns = new Patterns<Relation>(new Object[][] {
			{"%object% ((is|are) (greater|more|higher|bigger) than|>) %object%", Relation.GREATER},
			{"%object% ((is|are) ((greater|more|higher|bigger) or equal to|not (less|smaller) than)|>=) %object", Relation.GREATER_OR_EQUAL},
			{"%object% ((is|are) (less|smaller) than|<) %object%", Relation.SMALLER},
			{"%object% ((is|are) ((less|smaller) or equal to|not (greater|more|higher|bigger) than)|<=) %object%", Relation.SMALLER_OR_EQUAL},
			{"%object% ((is|are) not|isn't|aren't|!=) (equal to )?%object%", Relation.NOT_EQUAL},
			{"%object% (is|are|=) (equal to )?%object%", Relation.EQUAL}
	});
	
	static {
		Skript.addCondition(CondIs.class, patterns.getPatterns());
	}
	
	private Variable<?> first, second;
	private Relation relation;
	private Comparator<?, ?> comp;
	private boolean reverseOrder = false;
	
	@Override
	public void init(final Variable<?>[] vars, final int matchedPattern, final Matcher matcher) throws ParseException {
		first = vars[0];
		second = vars[1];
		relation = patterns.getInfo(matchedPattern);
		final boolean b = init();
		if (!b) {
			if (first instanceof UnparsedLiteral || second instanceof UnparsedLiteral) {
				throw new ParseException("the given values are invalid or cannot be compared");
			} else {
				if (Skript.logExtreme())
					Skript.info("Can't compare "+first.getReturnType().getName()+" and "+second.getReturnType().getName());
				throw new ParseException("the given values cannot be compared");
			}
		}
		if (!comp.supportsRelation(relation)) {
			throw new ParseException("The given objects can't be compared with '" + relation + "'");
		}
		if (first instanceof UnparsedLiteral || second instanceof UnparsedLiteral) {
			Skript.exception("Unexpected UnparsedLiteral(s) in 'is' condition:",
					getDebugMessage(null),
					"comparator: " + comp.getClass().getName());
			throw new ParseException();
		}
	}
	
	private boolean init() {
		final Class<?> f = first.getReturnType(), s = second.getReturnType();
		final int[] zeroOne = {0, 1};
		
		for (final ComparatorInfo<?, ?> info : Skript.getComparators()) {
			if (info.c1.isAssignableFrom(f) && info.c2.isAssignableFrom(s)
					|| info.c1.isAssignableFrom(s) && info.c2.isAssignableFrom(f)) {
				comp = info.c;
				reverseOrder = !(info.c1.isAssignableFrom(f) && info.c2.isAssignableFrom(s));
				return true;
			}
		}
		
		for (final ComparatorInfo<?, ?> info : Skript.getComparators()) {
			for (final int c : zeroOne) {
				for (final int v : zeroOne) {
					if (info.getType(c).isAssignableFrom((v == 0 ? f : s))) {
						final Variable<?> temp = (v == 0 ? second : first).getConvertedVariable(info.getType(1 - c));
						if (temp != null) {
							reverseOrder = (c != v);
							comp = info.c;
							if (v == 0)
								second = temp;
							else
								first = temp;
							return true;
						}
					}
				}
			}
		}
		
		for (final ComparatorInfo<?, ?> info : Skript.getComparators()) {
			for (final int c : zeroOne) {
				final Variable<?> v1 = first.getConvertedVariable(info.getType(c));
				final Variable<?> v2 = second.getConvertedVariable(info.getType(1 - c));
				if (v1 != null && v2 != null) {
					first = v1;
					second = v2;
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
						return relation.is(reverseOrder ? compare(o2, o1) : compare(o1, o2));
					}
				}, true);
			}
		}, this, true);
	}
	
	@SuppressWarnings("unchecked")
	private <T1, T2> Relation compare(final T1 o1, final T2 o2) {
		return ((Comparator<? super T1, ? super T2>) comp).compare(o1, o2);
	}
	
	@Override
	public String getDebugMessage(final Event e) {
		return first.getDebugMessage(e) + " is " + relation + " " + second.getDebugMessage(e);
	}
	
}
