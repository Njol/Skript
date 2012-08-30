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

import ch.njol.skript.classes.Converter;
import ch.njol.skript.expressions.base.SimplePropertyExpression;

/**
 * @author Peter Güttinger
 */
public class ExprName extends SimplePropertyExpression<Player, String> {
	
	static {
		register(ExprName.class, String.class, "name", "players");
	}
	
	/**
	 * @param returnType
	 * @param propertyName
	 * @param converter
	 */
	public ExprName() {
		super(String.class, "name", new Converter<Player, String>() {
			@Override
			public String convert(final Player p) {
				return p.getName();
			}
		});
	}
	
}
