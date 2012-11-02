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

package ch.njol.skript.hooks;

import ch.njol.skript.Skript;
import ch.njol.skript.classes.ClassInfo;
import ch.njol.skript.classes.Parser;
import ch.njol.skript.lang.ParseContext;
import ch.njol.skript.registrations.Classes;

/**
 * @author Peter Güttinger
 */
public class Money {
	
	static {
		if (Economy.getEconomy() != null) {
			Classes.registerClass(new ClassInfo<Money>(Money.class, "money", "money")
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
		if (Economy.getEconomy() == null) {
			Skript.error("No economy plugin detected");
			return null;
		}
		if (!Economy.plural.isEmpty() && s.toLowerCase().endsWith(Economy.pluralLower)) {
			try {
				final double d = Double.parseDouble(s.substring(0, s.length() - Economy.plural.length()).trim());
				return new Money(d);
			} catch (final NumberFormatException e) {}
		}
		if (!Economy.singular.isEmpty() && s.toLowerCase().endsWith(Economy.singularLower)) {
			try {
				final double d = Double.parseDouble(s.substring(0, s.length() - Economy.singular.length()).trim());
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
		return Economy.getEconomy().format(amount);
	}
	
}
