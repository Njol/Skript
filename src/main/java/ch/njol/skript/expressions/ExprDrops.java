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

import java.util.List;

import org.bukkit.event.Event;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import ch.njol.skript.ScriptLoader;
import ch.njol.skript.Skript;
import ch.njol.skript.aliases.ItemType;
import ch.njol.skript.classes.Changer.ChangeMode;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Examples;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.ExpressionType;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.skript.lang.util.SimpleExpression;
import ch.njol.skript.log.ErrorQuality;
import ch.njol.skript.registrations.Classes;
import ch.njol.skript.util.Experience;
import ch.njol.util.CollectionUtils;
import ch.njol.util.Kleenean;
import ch.njol.util.iterator.IteratorIterable;

/**
 * @author Peter Güttinger
 */
@SuppressWarnings("serial")
@Name("Drops")
@Description("Only works in death events. Holds the drops of the dieing creature. Drops can be prevented by removing them with \"remove ... from drops\", e.g. \"remove all pickaxes from the drops\", or \"clear drops\" if you don't want any drops at all.")
@Examples({"clear drops",
		"remove 4 planks from the drops"})
@Since("1.0")
public class ExprDrops extends SimpleExpression<ItemStack> {
	
	static {
		Skript.registerExpression(ExprDrops.class, ItemStack.class, ExpressionType.SIMPLE, "[the] drops");
	}
	
	private Kleenean delayed;
	
	@Override
	public boolean init(final Expression<?>[] vars, final int matchedPattern, final Kleenean isDelayed, final ParseResult parser) {
		if (!ScriptLoader.isCurrentEvent(EntityDeathEvent.class)) {
			Skript.error("The expression 'drops' can only be used in death events", ErrorQuality.SEMANTIC_ERROR);
			return false;
		}
		delayed = isDelayed;
		return true;
	}
	
	@Override
	protected ItemStack[] get(final Event e) {
		if (!(e instanceof EntityDeathEvent))
			return null;
		return ((EntityDeathEvent) e).getDrops().toArray(new ItemStack[0]);
	}
	
	@SuppressWarnings("unchecked")
	@Override
	public Class<?>[] acceptChange(final ChangeMode mode) {
		if (delayed.isTrue()) {
			Skript.error("Can't change the drops anymore after the event has already passed");
			return null;
		}
		return CollectionUtils.array(ItemType[].class, Inventory.class, Experience.class);
	}
	
	@SuppressWarnings("unchecked")
	@Override
	public void change(final Event e, final Object delta, final ChangeMode mode) {
		if (!(e instanceof EntityDeathEvent))
			return;
		if (delta instanceof Experience) {
			if (mode == ChangeMode.REMOVE && ((Experience) delta).getInternalXP() == -1) {
				((EntityDeathEvent) e).setDroppedExp(0);
			} else {
				int xp = ((Experience) delta).getXP();
				if (mode == ChangeMode.ADD)
					xp += ((EntityDeathEvent) e).getDroppedExp();
				else if (mode == ChangeMode.REMOVE)
					xp = ((EntityDeathEvent) e).getDroppedExp() - xp;
				((EntityDeathEvent) e).setDroppedExp(xp < 0 ? 0 : xp);
			}
		} else {
			final List<ItemStack> drops = ((EntityDeathEvent) e).getDrops();
			switch (mode) {
				case SET:
					drops.clear();
					//$FALL-THROUGH$
				case ADD:
					if (delta instanceof Inventory) {
						for (final ItemStack is : new IteratorIterable<ItemStack>(((Inventory) delta).iterator())) {
							if (is != null)
								drops.add(is);
						}
					} else {
						for (final ItemType type : (ItemType[]) delta) {
							type.addTo(drops);
						}
					}
					break;
				case REMOVE:
					if (delta instanceof Inventory) {
						for (final ItemStack is : new IteratorIterable<ItemStack>(((Inventory) delta).iterator())) {
							if (is == null)
								continue;
							new ItemType(is).removeFrom(drops);
						}
					} else {
						for (final ItemType type : (ItemType[]) delta) {
							type.removeFrom(drops);
						}
					}
					break;
				case DELETE:
					drops.clear();
					break;
			}
		}
	}
	
	@Override
	public Class<? extends ItemStack> getReturnType() {
		return ItemStack.class;
	}
	
	@Override
	public String toString(final Event e, final boolean debug) {
		if (e == null)
			return "the drops";
		return Classes.getDebugMessage(getAll(e));
	}
	
	@Override
	public boolean isSingle() {
		return false;
	}
	
	@Override
	public boolean getAnd() {
		return true;
	}
	
}
