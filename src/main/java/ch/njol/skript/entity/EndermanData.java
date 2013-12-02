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

import java.util.Arrays;

import org.bukkit.entity.Enderman;

import ch.njol.skript.aliases.ItemType;
import ch.njol.skript.lang.Literal;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.skript.lang.util.SimpleExpression;
import ch.njol.skript.localization.ArgsMessage;
import ch.njol.skript.registrations.Classes;
import ch.njol.util.Checker;
import ch.njol.util.coll.CollectionUtils;

/**
 * @author Peter Güttinger
 */
@SuppressWarnings("serial")
public class EndermanData extends EntityData<Enderman> {
	static {
		EntityData.register(EndermanData.class, "enderman", Enderman.class, "enderman");
	}
	
	private ItemType[] hand = null;
	
	@SuppressWarnings("unchecked")
	@Override
	protected boolean init(final Literal<?>[] exprs, final int matchedPattern, final ParseResult parseResult) {
		if (exprs[0] != null)
			hand = ((Literal<ItemType>) exprs[0]).getAll();
		return true;
	}
	
	@Override
	protected boolean init(final Class<? extends Enderman> c, final Enderman e) {
		hand = e == null || e.getCarriedMaterial() == null ? null : new ItemType[] {new ItemType(e.getCarriedMaterial().toItemStack(1))};
		return true;
	}
	
	@Override
	public void set(final Enderman entity) {
		if (hand != null)
			entity.setCarriedMaterial(CollectionUtils.getRandom(hand).getBlock().getRandom().getData());
	}
	
	@Override
	public boolean match(final Enderman entity) {
		return hand == null || SimpleExpression.check(hand, new Checker<ItemType>() {
			@SuppressWarnings("deprecation")
			@Override
			public boolean check(final ItemType t) {
				return t.isOfType(entity.getCarriedMaterial().getItemTypeId(), entity.getCarriedMaterial().getData());
			}
		}, false, false);
	}
	
	@Override
	public Class<Enderman> getType() {
		return Enderman.class;
	}
	
	private final static ArgsMessage format = new ArgsMessage("entities.enderman.format");
	
	@Override
	public String toString(final int flags) {
		if (hand == null)
			return super.toString(flags);
		return format.toString(super.toString(flags), Classes.toString(hand, false));
	}
	
	@Override
	protected int hashCode_i() {
		return Arrays.hashCode(hand);
	}
	
	@Override
	protected boolean equals_i(final EntityData<?> obj) {
		if (!(obj instanceof EndermanData))
			return false;
		final EndermanData other = (EndermanData) obj;
		return Arrays.equals(hand, other.hand);
	}
	
//		if (hand == null)
//			return "";
//		final StringBuilder b = new StringBuilder();
//		for (final ItemType h : hand) {
//			final Pair<String, String> s = Classes.serialize(h);
//			if (s == null)
//				return null;
//			if (b.length() != 0)
//				b.append(",");
//			b.append(s.first);
//			b.append(":");
//			b.append(s.second.replace(",", ",,").replace(":", "::"));
//		}
//		return b.toString();
	@Override
	@Deprecated
	protected boolean deserialize(final String s) {
		if (s.isEmpty())
			return true;
		final String[] split = s.split("(?<!,),(?!,)");
		hand = new ItemType[split.length];
		for (int i = 0; i < hand.length; i++) {
			final String[] t = split[i].split("(?<!:):(?::)");
			if (t.length != 2)
				return false;
			final Object o = Classes.deserialize(t[0], t[1].replace(",,", ",").replace("::", ":"));
			if (o == null || !(o instanceof ItemType))
				return false;
			hand[i] = (ItemType) o;
		}
		return false;
	}
	
	private boolean isSubhand(final ItemType[] sub) {
		if (hand == null)
			return true;
		if (sub == null)
			return false;
		return ItemType.isSubset(hand, sub);
	}
	
	@Override
	public boolean isSupertypeOf(final EntityData<?> e) {
		if (e instanceof EndermanData)
			return isSubhand(((EndermanData) e).hand);
		return false;
	}
	
	@Override
	public EntityData getSuperType() {
		return new EndermanData();
	}
	
}
