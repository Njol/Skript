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
import ch.njol.skript.classes.ClassInfo;
import ch.njol.skript.classes.Parser;
import ch.njol.skript.lang.Literal;
import ch.njol.skript.lang.ParseContext;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.skript.registrations.Classes;
import ch.njol.util.StringUtils;

/**
 * @author Peter Güttinger
 */
public class XpOrbData extends EntityData<ExperienceOrb> {
	private static final long serialVersionUID = -8836499239061252576L;
	
	static {
		register(XpOrbData.class, "xporb", ExperienceOrb.class, "<\\d+> ([e]xp|experience)", "([e]xp|experience)[( |-)orb[s]]");
		Classes.registerClass(new ClassInfo<XpOrbData>(XpOrbData.class, "experience", "experience")
				.parser(new Parser<XpOrbData>() {
					@Override
					public XpOrbData parse(String s, final ParseContext context) {
						int xp = -1;
						if (s.matches("\\d+ .+")) {
							xp = Skript.parseInt(s.substring(0, s.indexOf(' ')));
							s = s.substring(s.indexOf(' ') + 1);
						} else if (StringUtils.startsWithIgnoreCase(s, "a ") || StringUtils.startsWithIgnoreCase(s, "an ")) {
							xp = 1;
							s = s.substring(s.indexOf(' ') + 1);
						}
						if (s.matches("(e?xp|experience)([ -]orbs?)?"))
							return new XpOrbData(xp);
						return null;
					}
					
					@Override
					public String toString(final XpOrbData xp) {
						return xp.toString();
					}
					
					@Override
					public String toVariableNameString(final XpOrbData xp) {
						return xp.toString();
					}
					
					@Override
					public String getVariableNamePattern() {
						return ".+";
					}
				})
				.serializer(EntityData.serializer));
	}
	
	private int xp = -1;
	
	private boolean plural;
	
	public XpOrbData() {}
	
	public XpOrbData(final int xp) {
		this.xp = xp;
	}
	
	@Override
	protected boolean init(final Literal<?>[] exprs, final int matchedPattern, final ParseResult parseResult) {
		if (matchedPattern == 0)
			xp = Skript.parseInt(parseResult.regexes.get(0).group());
		plural = StringUtils.endsWithIgnoreCase(parseResult.expr, "s");
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
		orb.setExperience(xp == -1 ? 1 : xp);
		return orb;
	}
	
	@Override
	public boolean isPlural() {
		return plural;
	}
	
	public int getExperience() {
		return xp == -1 ? 1 : xp;
	}
	
	public int getInternExperience() {
		return xp;
	}
	
	@Override
	public int hashCode() {
		return xp;
	}
	
	@Override
	public boolean equals(final Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (!(obj instanceof XpOrbData))
			return false;
		final XpOrbData other = (XpOrbData) obj;
		return xp == other.xp;
	}
	
	@Override
	public String serialize() {
		return "" + xp;
	}
	
	@Override
	protected boolean deserialize(final String s) {
		try {
			xp = Integer.parseInt(s);
			return true;
		} catch (final NumberFormatException e) {
			return false;
		}
	}
}
