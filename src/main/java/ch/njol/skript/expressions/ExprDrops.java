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
import ch.njol.skript.classes.Changer.ChangeMode;
import ch.njol.skript.entity.XpOrbData;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.skript.lang.util.SimpleExpression;
import ch.njol.skript.log.ErrorQuality;
import ch.njol.skript.registrations.Classes;
import ch.njol.skript.util.ItemType;
import ch.njol.skript.util.Utils;

/**
 * @author Peter Güttinger
 */
public class ExprDrops extends SimpleExpression<ItemStack> {
	private static final long serialVersionUID = 3089011835058396051L;
	
	static {
		Skript.registerExpression(ExprDrops.class, ItemStack.class, ExpressionType.SIMPLE, "[the] drops");
	}
	
	private int delay;
	
	@Override
	public boolean init(final Expression<?>[] vars, final int matchedPattern, final int isDelayed, final ParseResult parser) {
		if (!Utils.contains(ScriptLoader.currentEvents, EntityDeathEvent.class)) {
			Skript.error("'drops' can only be used in death events", ErrorQuality.SEMANTIC_ERROR);
			return false;
		}
		delay = isDelayed;
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
		if (delay != -1) {
			Skript.error("Can't change the drops anymore after the event has already passed");
			return null;
		}
		return Skript.array(ItemType[].class, XpOrbData.class);
	}
	
	@SuppressWarnings("unchecked")
	@Override
	public void change(final Event e, final Object delta, final ChangeMode mode) {
		if (!(e instanceof EntityDeathEvent))
			return;
		if (delta instanceof XpOrbData) {
			if (mode == ChangeMode.REMOVE && ((XpOrbData) delta).getInternExperience() == -1) {
				((EntityDeathEvent) e).setDroppedExp(0);
			} else {
				int xp = ((XpOrbData) delta).getExperience();
				if (mode == ChangeMode.ADD)
					xp += ((EntityDeathEvent) e).getDroppedExp();
				else if (mode == ChangeMode.REMOVE)
					xp = ((EntityDeathEvent) e).getDroppedExp() - xp;
				((EntityDeathEvent) e).setDroppedExp(xp < 0 ? 0 : xp);
			}
		} else if (delta != null) {
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
