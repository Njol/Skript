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

package ch.njol.skript.util;

import ch.njol.skript.Skript;
import ch.njol.skript.api.ClassInfo;
import ch.njol.skript.api.Parser;
import ch.njol.skript.api.intern.SkriptAPIException;

/**
 * @author Peter Güttinger
 * 
 */
public class Money {
	
	static {
		Skript.registerClass(new ClassInfo<Money>("money", "money", Money.class, null, new Parser<Money>() {
			@Override
			public Money parse(final String s) {
				return Money.parse(s);
			}
			
			@Override
			public String toString(final Money m) {
				return m.toString();
			}
		}, "money"));
	}
	
	private static String plural;
	private static String singular;
	static {
		if (Skript.getEconomy() != null) {
			plural = Skript.getEconomy().currencyNamePlural().toLowerCase();
			singular = Skript.getEconomy().currencyNameSingular().toLowerCase();
		}
	}
	
	private final double amount;
	
	public Money(final double amount) {
		if (Skript.getEconomy() == null)
			throw new SkriptAPIException("can't create a new money instance if there's no economy plugin present");
		this.amount = amount;
	}
	
	public double getAmount() {
		return amount;
	}
	
	public static final Money parse(final String s) {
		if (Skript.getEconomy() == null) {
			Skript.error("No economy plugin detected");
			return null;
		}
		if (!plural.isEmpty() && s.toLowerCase().endsWith(plural)) {
			try {
				final double d = Double.parseDouble(s.substring(0, s.length() - plural.length()).trim());
				if (d == 1 && !singular.equals(plural))
					Skript.pluralWarning(s);
				return new Money(d);
			} catch (final NumberFormatException e) {}
		}
		if (!singular.isEmpty() && s.toLowerCase().endsWith(singular)) {
			try {
				final double d = Double.parseDouble(s.substring(0, s.length() - singular.length()).trim());
				if (d != 1)
					Skript.pluralWarning(s);
				return new Money(d);
			} catch (final NumberFormatException e) {}
		}
		try {
			return new Money(Double.parseDouble(s));
		} catch (final NumberFormatException e) {}
		return null;
	}
	
	@Override
	public String toString() {
		return Skript.getEconomy().format(amount);
	}
	
}
