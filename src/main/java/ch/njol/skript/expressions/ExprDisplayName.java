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

package ch.njol.skript.expressions;

import org.bukkit.entity.Player;
import org.bukkit.event.Event;

import ch.njol.skript.classes.Changer.ChangeMode;
import ch.njol.skript.classes.Converter;
import ch.njol.skript.expressions.base.SimplePropertyExpression;
import ch.njol.skript.util.Utils;

/**
 * @author Peter Güttinger
 * 
 */
public class ExprDisplayName extends SimplePropertyExpression<Player, String> {
	
	static {
		register(ExprDisplayName.class, String.class, "display name", "players");
	}
	
	/**
	 * @param returnType
	 * @param propertyName
	 * @param converter
	 */
	public ExprDisplayName() {
		super(String.class, "display name", new Converter<Player, String>() {
			@Override
			public String convert(final Player p) {
				return p.getDisplayName();
			}
		});
	}
	
	@Override
	public Class<?> acceptChange(final ChangeMode mode) {
		if (mode == ChangeMode.SET)
			return String.class;
		return null;
	}
	
	@Override
	public void change(final Event e, final Object delta, final ChangeMode mode) throws UnsupportedOperationException {
		for (final Player p : getExpr().getArray(e)) {
			p.setDisplayName(Utils.replaceChatStyles((String) delta));
		}
	}
	
}
