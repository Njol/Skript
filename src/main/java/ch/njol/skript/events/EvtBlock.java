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

import org.bukkit.Material;
import org.bukkit.event.Event;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockBurnEvent;
import org.bukkit.event.block.BlockEvent;
import org.bukkit.event.block.BlockFadeEvent;
import org.bukkit.event.block.BlockFormEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.painting.PaintingBreakEvent;
import org.bukkit.event.painting.PaintingEvent;
import org.bukkit.event.painting.PaintingPlaceEvent;
import org.bukkit.event.player.PlayerBucketEmptyEvent;
import org.bukkit.event.player.PlayerBucketEvent;
import org.bukkit.event.player.PlayerBucketFillEvent;

import ch.njol.skript.Skript;
import ch.njol.skript.lang.Literal;
import ch.njol.skript.lang.SkriptEvent;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.skript.util.ItemType;
import ch.njol.util.Checker;

/**
 * @author Peter Güttinger
 * 
 */
@SuppressWarnings("unchecked")
public class EvtBlock extends SkriptEvent {
	
	static {
		Skript.registerEvent(EvtBlock.class, Skript.array(BlockBreakEvent.class, PaintingBreakEvent.class, PlayerBucketFillEvent.class), "(break[ing]|min(e|ing)) [[of] %itemtypes%]");
		Skript.registerEvent(EvtBlock.class, BlockBurnEvent.class, "burn[ing] [[of] %itemtypes%]");
		Skript.registerEvent(EvtBlock.class, Skript.array(BlockPlaceEvent.class, PaintingPlaceEvent.class, PlayerBucketEmptyEvent.class), "plac(e|ing) [[of] %itemtypes%]");
		Skript.registerEvent(EvtBlock.class, BlockFadeEvent.class, "fad(e|ing) [[of] %itemtypes%]");
		Skript.registerEvent(EvtBlock.class, BlockFormEvent.class, "form[ing] [[of] %itemtypes%]");
	}
	
	private Literal<ItemType> types;
	
	private boolean mine = false;
	
	@Override
	public boolean init(final Literal<?>[] args, final int matchedPattern, final ParseResult parser) {
		types = (Literal<ItemType>) args[0];
		mine = parser.expr.toLowerCase().startsWith("min");
		return true;
	}
	
	@Override
	public boolean check(final Event e) {
		if (mine && e instanceof BlockBreakEvent) {
			if (((BlockBreakEvent) e).getBlock().getDrops(((BlockBreakEvent) e).getPlayer().getItemInHand()).isEmpty())
				return false;
		}
		if (types == null)
			return true;
		final int id;
		final short durability;
		if (e instanceof BlockEvent) {
			id = ((BlockEvent) e).getBlock().getTypeId();
			durability = ((BlockEvent) e).getBlock().getData();
		} else if (e instanceof PlayerBucketFillEvent) {
			id = ((PlayerBucketEvent) e).getBlockClicked().getRelative(((PlayerBucketEvent) e).getBlockFace()).getTypeId();
			durability = ((PlayerBucketEvent) e).getBlockClicked().getRelative(((PlayerBucketEvent) e).getBlockFace()).getData();
		} else if (e instanceof PlayerBucketEmptyEvent) {
			id = ((PlayerBucketEmptyEvent) e).getBucket() == Material.WATER_BUCKET ? Material.STATIONARY_WATER.getId() : Material.STATIONARY_LAVA.getId();
			durability = 0;
		} else if (e instanceof PaintingEvent) {
			id = Material.PAINTING.getId();
			durability = 0;
			//((PaintingEvent) e).getPainting().getArt().getId();
		} else {
			throw new IllegalStateException();
		}
		return types.check(e, new Checker<ItemType>() {
			@Override
			public boolean check(final ItemType t) {
				return t.isOfType(id, durability);
			}
		});
	}
	
	@Override
	public String toString(final Event e, final boolean debug) {
		return "break/place/burn/fade/form of " + Skript.toString(types);
	}
	
}
