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

package ch.njol.skript.events;

import org.bukkit.block.Block;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.event.player.PlayerInteractEvent;

import ch.njol.skript.Skript;
import ch.njol.skript.aliases.ItemType;
import ch.njol.skript.entity.EntityData;
import ch.njol.skript.lang.Literal;
import ch.njol.skript.lang.SkriptEvent;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.skript.log.ErrorQuality;
import ch.njol.skript.util.Utils;
import ch.njol.util.Checker;
import ch.njol.util.StringUtils;

/**
 * @author Peter Güttinger
 */
@SuppressWarnings("unchecked")
public class EvtClick extends SkriptEvent {
	
	// Important: a click on an entity fires both an PlayerInteractEntityEvent and a PlayerInteractEvent
	
	private static final long serialVersionUID = -5935656107417409014L;
	
	static {
		Skript.registerEvent("Click", EvtClick.class, Utils.array(PlayerInteractEvent.class, PlayerInteractEntityEvent.class),
				"[(left|right)[ ]][mouse[ ]]click[ing] [on %-entitydata/itemtype%] [(with|using|holding) %itemtype%]",
				"[(left|right)[ ]][mouse[ ]]click[ing] (with|using|holding) %itemtype% on %entitydata/itemtype%")
				.description("Called when a user clicks on a block, an entity or air with or without an item in their hand.",
						"Please note that rightclick events with an empty hand are not sent to the server, so there's no way to detect them.")
				.examples("on click",
						"on rightclick holding a fishing rod",
						"on leftclick on a stone or obsidian",
						"on rightclick on a creeper",
						"on click with a sword")
				.since("1.0");
	}
	
	private Literal<?> types = null;
	private Literal<ItemType> tools;
	
	private final static int RIGHT = 1, LEFT = 2, ANY = 3;
	private int click = ANY;
	
	@Override
	public boolean init(final Literal<?>[] args, final int matchedPattern, final ParseResult parser) {
		if (StringUtils.startsWithIgnoreCase(parser.expr, "right"))
			click = RIGHT;
		else if (StringUtils.startsWithIgnoreCase(parser.expr, "left"))
			click = LEFT;
		types = args[matchedPattern];
		if (types != null && !ItemType.class.isAssignableFrom(types.getReturnType())) {
			if (click == LEFT) {
				Skript.error("A leftclick on an entity is an attack and thus not covered by the 'click' event, but the 'damage' event.", ErrorQuality.SEMANTIC_ERROR);
				return false;
			} else if (click == ANY) {
				Skript.warning("A leftclick on an entity is an attack and thus not covered by the 'click' event, but the 'damage' event. Change this event to a rightclick to disable this warning message.");
			}
		}
		tools = (Literal<ItemType>) args[1 - matchedPattern];
		return true;
	}
	
	@Override
	public boolean check(final Event e) {
		final Player player;
		final Block block;
		final Entity entity;
		
		if (e instanceof PlayerInteractEntityEvent) {
			if (click == LEFT || types == null) // types == null  will be handled by the PlayerInteractEvent that is fired as well
				return false;
			player = ((PlayerInteractEntityEvent) e).getPlayer();
			entity = ((PlayerInteractEntityEvent) e).getRightClicked();
			block = null;
		} else if (e instanceof PlayerInteractEvent) {
			if (click == LEFT && !(((PlayerInteractEvent) e).getAction() == Action.LEFT_CLICK_AIR || ((PlayerInteractEvent) e).getAction() == Action.LEFT_CLICK_BLOCK))
				return false;
			else if (click == RIGHT && !(((PlayerInteractEvent) e).getAction() == Action.RIGHT_CLICK_AIR || ((PlayerInteractEvent) e).getAction() == Action.RIGHT_CLICK_BLOCK))
				return false;
			player = ((PlayerInteractEvent) e).getPlayer();
			block = ((PlayerInteractEvent) e).getClickedBlock();
			entity = null;
		} else {
			assert false;
			return false;
		}
		
		if (tools != null && !tools.check(e, new Checker<ItemType>() {
			@Override
			public boolean check(final ItemType t) {
				return t.isOfType(player.getItemInHand());
			}
		})) {
			return false;
		}
		
		if (types == null)
			return true;
		return types.check(e, new Checker<Object>() {
			@Override
			public boolean check(final Object o) {
				return o instanceof EntityData ? ((EntityData<?>) o).isInstance(entity) : ((ItemType) o).isOfType(block); // both tests are null-safe
			}
		});
	}
	
	@Override
	public String toString(final Event e, final boolean debug) {
		return (click == LEFT ? "left" : click == RIGHT ? "right" : "") + "click" + (types == null ? "" : " on " + types.toString(e, debug)) + (tools == null ? "" : " holding " + tools.toString(e, debug));
	}
	
}
