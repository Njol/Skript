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

import org.bukkit.OfflinePlayer;
import org.bukkit.event.Event;

import ch.njol.skript.classes.Changer.ChangeMode;
import ch.njol.skript.expressions.base.SimplePropertyExpression;
import ch.njol.skript.hooks.economy.EconomyHook;

/**
 * @author Peter Güttinger
 */
@SuppressWarnings("serial")
public class ExprBalance extends SimplePropertyExpression<OfflinePlayer, Money> {// TODO doc!
	static {
		register(ExprBalance.class, Money.class, "(money|balance|[bank] account)", "players");
	}
	
	@Override
	public Money convert(final OfflinePlayer p) {
		return new Money(EconomyHook.economy.getBalance(p.getName()));
	}
	
	@Override
	public Class<? extends Money> getReturnType() {
		return Money.class;
	}
	
	@Override
	protected String getPropertyName() {
		return "money";
	}
	
	@Override
	public Class<?>[] acceptChange(final ChangeMode mode) {
		return new Class[] {Money.class};
	}
	
	@SuppressWarnings("incomplete-switch")
	@Override
	public void change(final Event e, final Object delta, final ChangeMode mode) throws UnsupportedOperationException {
		if (mode == ChangeMode.DELETE) {
			for (final OfflinePlayer p : getExpr().getAll(e))
				EconomyHook.economy.withdrawPlayer(p.getName(), EconomyHook.economy.getBalance(p.getName()));
			return;
		}
		final double m = ((Money) delta).getAmount();
		for (final OfflinePlayer p : getExpr().getAll(e)) {
			switch (mode) {
				case SET:
					final double b = EconomyHook.economy.getBalance(p.getName());
					if (b < m) {
						EconomyHook.economy.depositPlayer(p.getName(), m - b);
					} else if (b > m) {
						EconomyHook.economy.withdrawPlayer(p.getName(), b - m);
					}
					break;
				case ADD:
					EconomyHook.economy.depositPlayer(p.getName(), m);
					break;
				case REMOVE:
					EconomyHook.economy.withdrawPlayer(p.getName(), m);
					break;
			}
		}
	}
	
}
