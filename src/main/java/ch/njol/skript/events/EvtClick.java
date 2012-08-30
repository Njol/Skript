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

import org.bukkit.block.Block;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.event.player.PlayerInteractEvent;

import ch.njol.skript.Skript;
import ch.njol.skript.entity.EntityData;
import ch.njol.skript.lang.Literal;
import ch.njol.skript.lang.SkriptEvent;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.skript.log.SkriptLogger;
import ch.njol.skript.log.SubLog;
import ch.njol.skript.util.ItemType;
import ch.njol.util.Checker;
import ch.njol.util.StringUtils;

/**
 * @author Peter Güttinger
 */
@SuppressWarnings("unchecked")
public class EvtClick extends SkriptEvent {
	
	// Important: a click on an entity fires both an PlayerInteractEntityEvent and a PlayerInteractEvent
	
	static {
		Skript.registerEvent(EvtClick.class, Skript.array(PlayerInteractEvent.class, PlayerInteractEntityEvent.class),
				"[(left|right)[ ]][mouse[ ]]click[ing] [on %object%] [(with|using|holding) %itemtype%]",
				"[(left|right)[ ]][mouse[ ]]click[ing] (with|using|holding) %itemtype% on %object%");
	}
	
	private Literal<? extends ItemType> blocks = null;
	private Literal<? extends EntityData<?>> entities = null;
	private Literal<ItemType> tools;
	
	private final static int RIGHT = 1, LEFT = 2, ANY = 3;
	private int click = ANY;
	
	@Override
	public boolean init(final Literal<?>[] args, final int matchedPattern, final ParseResult parser) {
		if (StringUtils.startsWithIgnoreCase(parser.expr, "right"))
			click = RIGHT;
		else if (StringUtils.startsWithIgnoreCase(parser.expr, "left"))
			click = LEFT;
		if (args[matchedPattern] != null) {
			final SubLog log = SkriptLogger.startSubLog();
			entities = (Literal<? extends EntityData<?>>) args[matchedPattern].getConvertedExpression(EntityData.class);
			if (entities == null) {
				blocks = args[matchedPattern].getConvertedExpression(ItemType.class);
				log.clear();
				if (blocks == null) {
					SkriptLogger.stopSubLog(log);
					Skript.error("'" + args[matchedPattern] + "' is neither an entity type nor an item type");
					return false;
				} else if (!blocks.isSingle()) {
					SkriptLogger.stopSubLog(log);
					Skript.error("It's impossible to click on multiple blocks at the same time");
					return false;
				}
			} else {
				if (!entities.isSingle()) {
					SkriptLogger.stopSubLog(log);
					Skript.error("It's impossible to click on multiple entites at the same time");
					return false;
				}
				if (click == LEFT) {
					SkriptLogger.stopSubLog(log);
					Skript.error("A leftclick on an entity is an attack and thus not covered by the 'click' event, but the 'damage' event.");
					return false;
				} else if (click == ANY) {
					Skript.warning("A leftclick on an entity is an attack and thus not covered by the 'click' event, but the 'damage' event. Change this event to a rightclick to disable this warning message.");
				}
			}
			SkriptLogger.stopSubLog(log);
			log.printLog();
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
			if (click == LEFT || entities == null && blocks == null)
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
		
		if (tools != null) {
			if (!tools.check(e, new Checker<ItemType>() {
				@Override
				public boolean check(final ItemType t) {
					return t.isOfType(player.getItemInHand());
				}
			})) {
				return false;
			}
		}
		if (blocks == null && entities == null)
			return true;
		if (entities != null && block == null) {
			return entities.check(e, new Checker<EntityData<?>>() {
				@Override
				public boolean check(final EntityData<?> t) {
					return t.isInstance(entity);
				}
			});
		} else if (blocks != null && entity == null) {
			if (block != null) {
				return blocks.check(e, new Checker<ItemType>() {
					@Override
					public boolean check(final ItemType t) {
						return t.isOfType(block);
					}
				});
			} else {
				return blocks.check(e, new Checker<ItemType>() {
					@Override
					public boolean check(final ItemType t) {
						return t.isOfType(0, (short) 0);
					}
				});
			}
		}
		return false;
	}
	
	@Override
	public String toString(final Event e, final boolean debug) {
		return (click == LEFT ? "left" : click == RIGHT ? "right" : "") + "click" + (blocks == null && entities == null ? "" : " on " + (blocks == null ? entities.toString(e, debug) : blocks.toString(e, debug))) + (tools == null ? "" : " holding " + tools.toString(e, debug));
	}
	
}
