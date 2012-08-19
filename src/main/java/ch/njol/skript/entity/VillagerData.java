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
import org.bukkit.entity.Villager;
import org.bukkit.entity.Villager.Profession;

import ch.njol.skript.lang.Literal;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.skript.util.Utils;

/**
 * @author Peter Güttinger
 * 
 */
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
		plural = parseResult.expr.endsWith("s");
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
			v.setProfession(Utils.getRandom(Profession.values()));
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
	
}
