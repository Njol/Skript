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

package ch.njol.skript.expressions;

import java.util.List;

import org.bukkit.event.Event;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.inventory.ItemStack;

import ch.njol.skript.ScriptLoader;
import ch.njol.skript.Skript;
import ch.njol.skript.Skript.ExpressionType;
import ch.njol.skript.api.Changer.ChangeMode;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.SimpleExpression;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.skript.util.ItemType;
import ch.njol.skript.util.Utils;

/**
 * @author Peter Güttinger
 * 
 */
public class ExprDrops extends SimpleExpression<ItemStack> {
	
	static {
		Skript.registerExpression(ExprDrops.class, ItemStack.class, ExpressionType.SIMPLE, "[the] drops");
	}
	
	@Override
	public boolean init(final Expression<?>[] vars, final int matchedPattern, final ParseResult parser) {
		if (Utils.indexOf(ScriptLoader.currentEvents, EntityDeathEvent.class) == -1) {
			Skript.error("'drops' can only be used in death events");
			return false;
		}
		return true;
	}
	
	@Override
	protected ItemStack[] get(final Event e) {
		if (!(e instanceof EntityDeathEvent))
			return null;
		return ((EntityDeathEvent) e).getDrops().toArray(new ItemStack[0]);
	}
	
	@Override
	public Class<?> acceptChange(final ChangeMode mode) {
		return ItemType[].class;
	}
	
	@SuppressWarnings("unchecked")
	@Override
	public void change(final Event e, final Object delta, final ChangeMode mode) {
		if (!(e instanceof EntityDeathEvent))
			return;
		final List<ItemStack> drops = ((EntityDeathEvent) e).getDrops();
		switch (mode) {
			case SET:
				drops.clear();
				//$FALL-THROUGH$
			case ADD:
				for (final ItemType i : ((ItemType[]) delta)) {
					i.addTo(drops);
				}
			break;
			case REMOVE:
				for (final ItemType i : ((ItemType[]) delta)) {
					i.removeFrom(drops);
				}
			break;
			case CLEAR:
				drops.clear();
			break;
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
		return Skript.getDebugMessage(getAll(e));
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
