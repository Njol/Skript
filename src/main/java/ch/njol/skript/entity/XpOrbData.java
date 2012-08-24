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

import org.bukkit.Location;
import org.bukkit.entity.ExperienceOrb;

import ch.njol.skript.Skript;
import ch.njol.skript.lang.Literal;
import ch.njol.skript.lang.SkriptParser.ParseResult;

/**
 * @author Peter Güttinger
 * 
 */
public class XpOrbData extends EntityData<ExperienceOrb> {
	
	static {
		register(XpOrbData.class, "xporb", ExperienceOrb.class, "([e]xp|experience)( |-)orb[s]", "<\\d+> ([e]xp|experience)");
	}
	
	private int xp = -1;
	
	private boolean plural;
	
	@Override
	protected boolean init(final Literal<?>[] exprs, final int matchedPattern, final ParseResult parseResult) {
		if (matchedPattern == 1)
			xp = Skript.parseInt(parseResult.regexes.get(0).group());
		plural = parseResult.expr.endsWith("s");
		return true;
	}
	
	@Override
	public void set(final ExperienceOrb entity) {
		if (xp != -1)
			entity.setExperience(xp);
	}
	
	@Override
	protected boolean match(final ExperienceOrb entity) {
		return xp == -1 || entity.getExperience() == xp;
	}
	
	@Override
	public Class<? extends ExperienceOrb> getType() {
		return ExperienceOrb.class;
	}
	
	@Override
	public String toString() {
		return "experience orb";
	}
	
	@Override
	public ExperienceOrb spawn(final Location loc) {
		final ExperienceOrb orb = super.spawn(loc);
		if (xp == -1)
			orb.setExperience(1);
		return orb;
	}
	
	@Override
	public boolean isPlural() {
		return plural;
	}
}
