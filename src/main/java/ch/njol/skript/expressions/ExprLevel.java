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

import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.player.PlayerLevelChangeEvent;

import ch.njol.skript.ScriptLoader;
import ch.njol.skript.classes.Changer.ChangeMode;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Examples;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import ch.njol.skript.effects.Delay;
import ch.njol.skript.expressions.base.SimplePropertyExpression;

/**
 * @author Peter Güttinger
 */
@SuppressWarnings("serial")
@Name("Level")
@Description("The level of a player.")
@Examples({"reduce the victim's level by 1",
		"set the player's level to 0"})
@Since("")
public class ExprLevel extends SimplePropertyExpression<Player, Integer> {
	
	static {
		register(ExprLevel.class, Integer.class, "level", "players");
	}
	
	@Override
	protected Integer[] get(final Event e, final Player[] source) {
		if (getExpr().isDefault() && e instanceof PlayerLevelChangeEvent && !Delay.isDelayed(e)) {
			return new Integer[] {getTime() < 0 ? ((PlayerLevelChangeEvent) e).getOldLevel() : ((PlayerLevelChangeEvent) e).getNewLevel()};
		}
		return super.get(e, source);
	}
	
	@Override
	public Integer convert(final Player p) {
		return p.getLevel();
	}
	
	@Override
	public Class<Integer> getReturnType() {
		return Integer.class;
	}
	
	@Override
	public Class<?>[] acceptChange(final ChangeMode mode) {
		if (getTime() == -1)
			return null;
		return new Class[] {Number.class};
	}
	
	@Override
	public void change(final Event e, final Object delta, final ChangeMode mode) {
		final int l = delta == null ? 0 : ((Number) delta).intValue();
		for (final Player p : getExpr().getArray(e)) {
			switch (mode) {
				case SET:
					p.setLevel(l);
					break;
				case ADD:
					p.setLevel(p.getLevel() + l);
					break;
				case REMOVE:
					p.setLevel(p.getLevel() - l);
					break;
				case DELETE:
					p.setLevel(0);
					break;
			}
		}
	}
	
	@Override
	public boolean setTime(final int time) {
		super.setTime(time);
		return getExpr().isDefault() && ScriptLoader.isCurrentEvent(PlayerLevelChangeEvent.class);
	}
	
	@Override
	protected String getPropertyName() {
		return "level";
	}
	
}
