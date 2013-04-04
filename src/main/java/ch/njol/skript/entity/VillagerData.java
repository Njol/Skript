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

package ch.njol.skript.entity;

import org.bukkit.Location;
import org.bukkit.entity.Villager;
import org.bukkit.entity.Villager.Profession;

import ch.njol.skript.lang.Literal;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.skript.util.Utils;
import ch.njol.util.StringUtils;

/**
 * @author Peter Güttinger
 */
@SuppressWarnings("serial")
public class VillagerData extends EntityData<Villager> {
	
	static {
		// professions in order!
		// FARMER(0), LIBRARIAN(1), PRIEST(2), BLACKSMITH(3), BUTCHER(4);
		register(VillagerData.class, "villager", Villager.class,
				"villager[s]", "farmer[s]", "librarian[s]", "priest[s]", "[black]smith[s]", "butcher[s]");
	}
	
	private Profession profession = null;
	
	private boolean plural;
	
	@Override
	protected boolean init(final Literal<?>[] exprs, final int matchedPattern, final ParseResult parseResult) {
		if (matchedPattern > 0)
			profession = Profession.getProfession(matchedPattern - 1);
		plural = StringUtils.endsWithIgnoreCase(parseResult.expr, "s");
		return true;
	}
	
	@Override
	public void set(final Villager entity) {
		if (profession != null)
			entity.setProfession(profession);
	}
	
	@Override
	public Villager spawn(final Location loc) {
		final Villager v = super.spawn(loc);
		if (profession == null)
			v.setProfession(Utils.random(Profession.values()));
		return v;
	}
	
	@Override
	protected boolean match(final Villager entity) {
		return profession == null || entity.getProfession() == profession;
	}
	
	@Override
	public Class<? extends Villager> getType() {
		return Villager.class;
	}
	
	@Override
	public String toString() {
		return profession == null ? "villager" : profession.toString().toLowerCase();
	}
	
	@Override
	public boolean isPlural() {
		return plural;
	}
	
	@Override
	public int hashCode() {
		return profession == null ? 0 : profession.hashCode();
	}
	
	@Override
	public boolean equals(final Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (!(obj instanceof VillagerData))
			return false;
		final VillagerData other = (VillagerData) obj;
		return profession == other.profession;
	}
	
	@Override
	public String serialize() {
		return profession == null ? "" : profession.name();
	}
	
	@Override
	protected boolean deserialize(final String s) {
		if (s.isEmpty())
			return true;
		try {
			profession = Profession.valueOf(s);
			return true;
		} catch (final IllegalArgumentException e) {
			return false;
		}
	}
	
	@Override
	protected boolean isSupertypeOf_i(final EntityData<? extends Villager> e) {
		if (e instanceof VillagerData)
			return profession == null || ((VillagerData) e).profession == profession;
		return false;
	}
	
}
