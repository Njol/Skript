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

package ch.njol.skript.hooks.economy.expressions;

import ch.njol.skript.Skript;
import ch.njol.skript.classes.ClassInfo;
import ch.njol.skript.classes.Comparator;
import ch.njol.skript.classes.Parser;
import ch.njol.skript.hooks.economy.EconomyHook;
import ch.njol.skript.lang.ParseContext;
import ch.njol.skript.registrations.Classes;
import ch.njol.skript.registrations.Comparators;
import ch.njol.util.StringUtils;

/**
 * @author Peter Güttinger
 */
@SuppressWarnings("serial")
public class Money {
	
	static {
		if (EconomyHook.economy != null) {
			Classes.registerClass(new ClassInfo<Money>(Money.class, "money")
					.user("money")
					.parser(new Parser<Money>() {
						@Override
						public Money parse(final String s, final ParseContext context) {
							return Money.parse(s);
						}
						
						@Override
						public String toString(final Money m) {
							return m.toString();
						}
						
						@Override
						public String toVariableNameString(final Money o) {
							return "money:" + o.amount;
						}
						
						@Override
						public String getVariableNamePattern() {
							return "money:-?\\d+(\\.\\d+)?";
						}
					}));
			
			Comparators.registerComparator(Money.class, Money.class, new Comparator<Money, Money>() {
				@Override
				public Relation compare(final Money m1, final Money m2) {
					return Relation.get(m1.amount - m2.amount);
				}
				
				@Override
				public boolean supportsOrdering() {
					return true;
				}
			});
		}
	}
	
	private final double amount;
	
	public Money(final double amount) {
		this.amount = amount;
	}
	
	public double getAmount() {
		return amount;
	}
	
	public static final Money parse(final String s) {
		if (EconomyHook.economy == null) {
			Skript.error("No economy plugin detected");
			return null;
		}
		if (!EconomyHook.plural.isEmpty()) {
			if (StringUtils.endsWithIgnoreCase(s, EconomyHook.plural)) {
				try {
					final double d = Double.parseDouble(s.substring(0, s.length() - EconomyHook.plural.length()).trim());
					return new Money(d);
				} catch (final NumberFormatException e) {}
			} else if (StringUtils.startsWithIgnoreCase(s, EconomyHook.plural)) {
				try {
					final double d = Double.parseDouble(s.substring(EconomyHook.plural.length()).trim());
					return new Money(d);
				} catch (final NumberFormatException e) {}
			}
		}
		if (!EconomyHook.singular.isEmpty()) {
			if (StringUtils.endsWithIgnoreCase(s, EconomyHook.singular)) {
				try {
					final double d = Double.parseDouble(s.substring(0, s.length() - EconomyHook.singular.length()).trim());
					return new Money(d);
				} catch (final NumberFormatException e) {}
			} else if (StringUtils.startsWithIgnoreCase(s, EconomyHook.singular)) {
				try {
					final double d = Double.parseDouble(s.substring(EconomyHook.singular.length()).trim());
					return new Money(d);
				} catch (final NumberFormatException e) {}
			}
		}
//		try {
//			return new Money(Double.parseDouble(s));
//		} catch (final NumberFormatException e) {}
		return null;
	}
	
	@Override
	public String toString() {
		return EconomyHook.economy.format(amount);
	}
	
}
