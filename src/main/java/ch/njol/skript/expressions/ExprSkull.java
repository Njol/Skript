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

package ch.njol.skript.expressions;

import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.SkullType;
import org.bukkit.entity.Creeper;
import org.bukkit.entity.Skeleton;
import org.bukkit.entity.Skeleton.SkeletonType;
import org.bukkit.entity.Zombie;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.SkullMeta;

import ch.njol.skript.Skript;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Examples;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import ch.njol.skript.entity.CreeperData;
import ch.njol.skript.entity.EntityData;
import ch.njol.skript.entity.PlayerData;
import ch.njol.skript.entity.SkeletonData;
import ch.njol.skript.expressions.base.SimplePropertyExpression;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.util.Kleenean;

/**
 * @author Peter Güttinger
 */
@SuppressWarnings("serial")
@Name("Skull")
@Description("Gets a skull item representing a player or an entity.")
@Examples({"give the victim's skull to the attacker",
		"set the block at the entity to the entity's skull"})
@Since("2.0")
public class ExprSkull extends SimplePropertyExpression<Object, ItemStack> {
	static {
		register(ExprSkull.class, ItemStack.class, "skull", "offlineplayers/entities/entitydatas");
	}
	
	@Override
	public boolean init(final Expression<?>[] exprs, final int matchedPattern, final Kleenean isDelayed, final ParseResult parseResult) {
		if (!Skript.isRunningMinecraft(1, 4, 5)) {
			Skript.error("Skulls are only available in Bukkit 1.4.5+");
			return false;
		}
		return super.init(exprs, matchedPattern, isDelayed, parseResult);
	}
	
	@Override
	public ItemStack convert(final Object o) {
		final SkullType type;
		if (o instanceof Skeleton || o instanceof SkeletonData) {
			if (o instanceof SkeletonData ? ((SkeletonData) o).isWither() : ((Skeleton) o).getSkeletonType() == SkeletonType.WITHER) {
				type = SkullType.WITHER;
			} else {
				type = SkullType.SKELETON;
			}
		} else if (o instanceof Zombie || o instanceof EntityData && Zombie.class.isAssignableFrom(((EntityData<?>) o).getType())) {
			type = SkullType.ZOMBIE;
		} else if (o instanceof OfflinePlayer || o instanceof PlayerData) {
			type = SkullType.PLAYER;
		} else if (o instanceof Creeper || o instanceof CreeperData) {
			type = SkullType.CREEPER;
		} else {
			return null;
		}
		final ItemStack is = new ItemStack(Material.SKULL_ITEM, 1, (short) type.ordinal());
		if (o instanceof OfflinePlayer) {
			final SkullMeta s = (SkullMeta) is.getItemMeta();
			s.setOwner(((OfflinePlayer) o).getName());
			is.setItemMeta(s);
		}
		return is;
	}
	
	@Override
	public Class<? extends ItemStack> getReturnType() {
		return ItemStack.class;
	}
	
	@Override
	protected String getPropertyName() {
		return "skull";
	}
	
}
