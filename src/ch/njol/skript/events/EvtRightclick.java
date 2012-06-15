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

package ch.njol.skript.events;

import org.bukkit.event.Event;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerEvent;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.event.player.PlayerInteractEvent;

import ch.njol.skript.Skript;
import ch.njol.skript.api.SkriptEvent;
import ch.njol.skript.lang.Literal;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.skript.util.EntityType;
import ch.njol.skript.util.ItemType;
import ch.njol.util.Checker;

/**
 * @author Peter Güttinger
 * 
 */
@SuppressWarnings("unchecked")
public class EvtRightclick extends SkriptEvent {
	
	static {
		Skript.registerEvent(EvtRightclick.class, Skript.array(PlayerInteractEvent.class, PlayerInteractEntityEvent.class),
				"rightclick[ing] [on %entitytype%] [(with|using|holding) %itemtype%]",// this must be the first
				"rightclick[ing] [on %itemtype%] [(with|using|holding) %itemtype%]");
	}
	
	private Literal<ItemType> blocks = null;
	private Literal<EntityType> entities = null;
	private Literal<ItemType> tools;
	
	@Override
	public boolean init(final Literal<?>[] args, final int matchedPattern, final ParseResult parser) {
		if (matchedPattern == 0)
			entities = (Literal<EntityType>) args[0];
		else
			blocks = (Literal<ItemType>) args[0];
		tools = (Literal<ItemType>) args[1];
		return true;
	}
	
	@Override
	public boolean check(final Event e) {
		if (!(e instanceof PlayerInteractEntityEvent
				|| ((PlayerInteractEvent) e).getAction() == Action.RIGHT_CLICK_AIR
				|| ((PlayerInteractEvent) e).getAction() == Action.RIGHT_CLICK_BLOCK))
			return false;
		if (tools != null) {
			if (!tools.check(e, new Checker<ItemType>() {
				@Override
				public boolean check(final ItemType t) {
					return t.isOfType(((PlayerEvent) e).getPlayer().getItemInHand());
				}
			})) {
				return false;
			}
		}
		if (blocks == null && entities == null)
			return true;
		if (e instanceof PlayerInteractEntityEvent && entities != null) {
			return entities.check(e, new Checker<EntityType>() {
				@Override
				public boolean check(final EntityType t) {
					return t.isInstance(((PlayerInteractEntityEvent) e).getRightClicked());
				}
			});
		} else if (e instanceof PlayerInteractEvent && blocks != null) {
			return blocks.check(e, new Checker<ItemType>() {
				@Override
				public boolean check(final ItemType t) {
					return t.isOfType(((PlayerInteractEvent) e).getClickedBlock());
				}
			});
		}
		return false;
	}
	
	@Override
	public String getDebugMessage(final Event e) {
		return "rightclick on " + Skript.toString(blocks == null ? entities : blocks) + " holding " + Skript.toString(tools);
	}
	
}
