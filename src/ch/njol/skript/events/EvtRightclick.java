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

import java.util.regex.Matcher;

import org.bukkit.event.Event;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerEvent;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.event.player.PlayerInteractEvent;

import ch.njol.skript.Skript;
import ch.njol.skript.api.SkriptEvent;
import ch.njol.skript.util.EntityType;
import ch.njol.skript.util.ItemType;

/**
 * @author Peter Güttinger
 * 
 */
@SuppressWarnings("unchecked")
public class EvtRightclick extends SkriptEvent {
	
	static {
		Skript.addEvent(EvtRightclick.class, Skript.array(PlayerInteractEvent.class, PlayerInteractEntityEvent.class),
				"rightclick(ing)?( on %itemtype%)?( (with|using|holding)( tool)? %itemtype%)?",
				"rightclick(ing)?( on %entitytype%)?( (with|using|holding)( tool)? %itemtype%)?");
	}
	
	private ItemType[] blocks = null;
	private EntityType[] entities = null;
	private ItemType[] tools;
	
	@Override
	public void init(final Object[][] args, final int matchedPattern, final Matcher matcher) {
		if (args[0] instanceof ItemType[])
			blocks = (ItemType[]) args[0];
		else
			entities = (EntityType[]) args[0];
		tools = (ItemType[]) args[1];
	}
	
	@Override
	public boolean check(final Event e) {
		if (!(e instanceof PlayerInteractEntityEvent
				|| ((PlayerInteractEvent) e).getAction() == Action.RIGHT_CLICK_AIR
				|| ((PlayerInteractEvent) e).getAction() == Action.RIGHT_CLICK_BLOCK))
			return false;
		boolean ok = false;
		if (tools != null) {
			for (final ItemType tool : tools) {
				if (tool.isOfType(((PlayerEvent) e).getPlayer().getItemInHand())) {
					ok = true;
					break;
				}
			}
			if (!ok)
				return false;
		}
		if (blocks == null && entities == null)
			return true;
		if (e instanceof PlayerInteractEntityEvent && entities != null) {
			for (final EntityType entity : entities) {
				if (entity.isInstance(((PlayerInteractEntityEvent) e).getRightClicked())) {
					return true;
				}
			}
			return false;
		} else if (e instanceof PlayerInteractEvent && blocks != null) {
			for (final ItemType block : blocks) {
				if (block.isOfType(((PlayerInteractEvent) e).getClickedBlock())) {
					return true;
				}
			}
			return false;
		}
		return false;
	}
	
	@Override
	public String getDebugMessage(final Event e) {
		return "rightclick on " + Skript.toString(blocks == null ? entities : blocks) + " holding " + Skript.toString(tools);
	}
	
}
