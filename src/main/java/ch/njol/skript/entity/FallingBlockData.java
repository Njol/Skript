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

import org.bukkit.Location;
import org.bukkit.entity.FallingBlock;
import org.bukkit.inventory.ItemStack;

import ch.njol.skript.Skript;
import ch.njol.skript.aliases.ItemType;
import ch.njol.skript.classes.Converter;
import ch.njol.skript.lang.Literal;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.skript.localization.Adjective;
import ch.njol.skript.localization.Language;
import ch.njol.skript.localization.Message;
import ch.njol.skript.localization.Noun;
import ch.njol.skript.registrations.Classes;
import ch.njol.skript.registrations.Converters;
import ch.njol.util.coll.CollectionUtils;

/**
 * @author Peter Güttinger
 */
@SuppressWarnings("serial")
public class FallingBlockData extends EntityData<FallingBlock> {
	static {
		register(FallingBlockData.class, "falling block", FallingBlock.class, "falling block");
	}
	
	private final static Message m_not_a_block_error = new Message("entities.falling block.not a block error");
	private final static Adjective m_adjective = new Adjective("entities.falling block.adjective");
	
	private ItemType[] types = null;
	
	@SuppressWarnings("unchecked")
	@Override
	protected boolean init(final Literal<?>[] exprs, final int matchedPattern, final ParseResult parseResult) {
		if (exprs.length > 0 && exprs[0] != null) {
			types = Converters.convert(((Literal<ItemType>) exprs[0]).getAll(), ItemType.class, new Converter<ItemType, ItemType>() {
				@Override
				public ItemType convert(ItemType t) {
					t = t.getBlock();
					if (!t.hasBlock())
						return null;
					t = t.clone();
					t.setAmount(-1);
					t.setAll(false);
					t.clearEnchantments();
					return t;
				}
			});
			if (types.length == 0) {
				Skript.error(m_not_a_block_error.toString());
				return false;
			}
		}
		return true;
	}
	
	@SuppressWarnings("deprecation")
	@Override
	protected boolean init(final Class<? extends FallingBlock> c, final FallingBlock e) {
		if (e != null)
			types = new ItemType[] {new ItemType(e.getBlockId(), e.getBlockData())};
		return true;
	}
	
	@SuppressWarnings("deprecation")
	@Override
	protected boolean match(final FallingBlock entity) {
		if (types == null)
			return true;
		for (final ItemType t : types) {
			if (t.isOfType(entity.getBlockId(), entity.getBlockData()))
				return true;
		}
		return false;
	}
	
	@Override
	public FallingBlock spawn(final Location loc) {
		final ItemStack t = CollectionUtils.getRandom(types).getRandom();
		return loc.getWorld().spawnFallingBlock(loc, t.getType(), (byte) t.getDurability());
	}
	
	@Override
	public void set(final FallingBlock entity) {
		assert false;
	}
	
	@Override
	public Class<? extends FallingBlock> getType() {
		return FallingBlock.class;
	}
	
	@Override
	public boolean isSupertypeOf(final EntityData<?> e) {
		if (!(e instanceof FallingBlockData))
			return false;
		if (types == null)
			return true;
		final FallingBlockData d = (FallingBlockData) e;
		if (d.types == null)
			return false;
		return ItemType.isSubset(types, d.types);
	}
	
	@Override
	public EntityData getSuperType() {
		return new FallingBlockData();
	}
	
	@Override
	public String toString(final int flags) {// FIXME test
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
		if (!(obj instanceof FallingBlockData))
			return false;
		return Arrays.equals(types, ((FallingBlockData) obj).types);
	}
	
	@Override
	protected int hashCode_i() {
		return Arrays.hashCode(types);
	}
	
}
