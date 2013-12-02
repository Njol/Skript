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

import java.util.Arrays;

import org.bukkit.Material;
import org.bukkit.entity.ThrownPotion;

import ch.njol.skript.aliases.ItemData;
import ch.njol.skript.aliases.ItemType;
import ch.njol.skript.classes.Converter;
import ch.njol.skript.lang.Literal;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.skript.localization.Adjective;
import ch.njol.skript.localization.Language;
import ch.njol.skript.localization.Noun;
import ch.njol.skript.registrations.Classes;
import ch.njol.skript.registrations.Converters;
import ch.njol.util.coll.CollectionUtils;

/**
 * @author Peter Güttinger
 */
@SuppressWarnings("serial")
public class ThrownPotionData extends EntityData<ThrownPotion> {
	static {
		register(ThrownPotionData.class, "thrown potion", ThrownPotion.class, "thrown potion");
	}
	
	private final static Adjective m_adjective = new Adjective("entities.thrown potion.adjective");
	
	private ItemType[] types;
	
	@Override
	protected boolean init(final Literal<?>[] exprs, final int matchedPattern, final ParseResult parseResult) {
		if (exprs.length > 0 && exprs[0] != null) {
			types = Converters.convert((ItemType[]) exprs[0].getAll(), ItemType.class, new Converter<ItemType, ItemType>() {
				@SuppressWarnings("deprecation")
				@Override
				public ItemType convert(final ItemType t) {
					ItemType r = null;
					for (final ItemData d : t.getTypes()) {
						if (d.getId() == Material.POTION.getId()) {
							if (r == null)
								r = new ItemType(d);
							else
								r.add(d);
						}
					}
					return r;
				}
			});
			if (types.length == 0)
				return false; // no error message - other things can be thrown as well
		}
		return false;
	}
	
	@Override
	protected boolean init(final Class<? extends ThrownPotion> c, final ThrownPotion e) {
		if (e != null)
			types = new ItemType[] {new ItemType(e.getItem())};
		return true;
	}
	
	@Override
	protected boolean match(final ThrownPotion entity) {
		if (types == null)
			return true;
		for (final ItemType t : types) {
			if (t.isOfType(entity.getItem()))
				return true;
		}
		return false;
	}
	
	@Override
	public void set(final ThrownPotion entity) {
		if (types != null)
			entity.setItem(CollectionUtils.getRandom(types).getRandom());
	}
	
	@Override
	public Class<? extends ThrownPotion> getType() {
		return ThrownPotion.class;
	}
	
	@Override
	public EntityData getSuperType() {
		return new ThrownPotionData();
	}
	
	@Override
	public boolean isSupertypeOf(final EntityData<?> e) {
		if (!(e instanceof ThrownPotionData))
			return false;
		if (types == null)
			return true;
		final ThrownPotionData d = (ThrownPotionData) e;
		return d.types != null && ItemType.isSubset(types, d.types);
	}
	
	@Override
	public String toString(final int flags) {
		final StringBuilder b = new StringBuilder();
		b.append(Noun.getArticleWithSpace(types[0].getTypes().get(0).getGender(), flags));
		b.append(m_adjective.toString(types[0].getTypes().get(0).getGender(), flags));
		b.append(" ");
		b.append(Classes.toString(types, flags & ~(Language.F_DEFINITE_ARTICLE | Language.F_INDEFINITE_ARTICLE), false));
		return b.toString();
	}
	
//		return ItemType.serialize(types);
	@Override
	@Deprecated
	protected boolean deserialize(final String s) {
		if (s.isEmpty())
			return true;
		types = ItemType.deserialize(s);
		return types != null;
	}
	
	@Override
	protected boolean equals_i(final EntityData<?> obj) {
		if (!(obj instanceof ThrownPotionData))
			return false;
		return Arrays.equals(types, ((ThrownPotionData) obj).types);
	}
	
	@Override
	protected int hashCode_i() {
		return Arrays.hashCode(types);
	}
	
}
