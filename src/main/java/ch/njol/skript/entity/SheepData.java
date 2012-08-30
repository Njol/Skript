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

package ch.njol.skript.entity;

import org.bukkit.entity.Sheep;

import ch.njol.skript.lang.Literal;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.skript.util.Color;
import ch.njol.util.Checker;

/**
 * @author Peter Güttinger
 */
public class SheepData extends EntityData<Sheep> {
	
	static {
		EntityData.register(SheepData.class, "sheep", Sheep.class, "(un|non[-])sheared [%-colors%] sheep[s]", "[%-colors%] sheep[s]", "sheared [%-colors%] sheep[s]");
	}
	
	private Literal<Color> colors = null;
	private int sheared = 0;
	
	private boolean plural;
	
	@SuppressWarnings("unchecked")
	@Override
	protected boolean init(final Literal<?>[] exprs, final int matchedPattern, final ParseResult parseResult) {
		sheared = matchedPattern - 1;
		colors = (Literal<Color>) exprs[0];
		plural = parseResult.expr.endsWith("s");
		return true;
	}
	
	@Override
	public void set(final Sheep entity) {
		if (colors != null)
			entity.setColor(colors.getSingle().getWoolColor());
	}
	
	@Override
	public boolean match(final Sheep entity) {
		return (sheared == 0 || entity.isSheared() == (sheared == 1))
				&& (colors == null || colors.check(null, new Checker<Color>() {
					@Override
					public boolean check(final Color c) {
						return entity.getColor() == c.getWoolColor();
					}
				}));
	}
	
	@Override
	public Class<Sheep> getType() {
		return Sheep.class;
	}
	
	@Override
	public String toString() {
		if (colors == null)
			return "sheep";
		return colors.toString() + " sheep";
	}
	
	@Override
	public boolean isPlural() {
		return plural;
	}
}
