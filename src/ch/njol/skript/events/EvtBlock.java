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
import org.bukkit.event.Event;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockBurnEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.painting.PaintingBreakEvent;
import org.bukkit.event.painting.PaintingPlaceEvent;

import ch.njol.skript.Skript;
import ch.njol.skript.api.SkriptEvent;
import ch.njol.skript.lang.ExprParser.ParseResult;
import ch.njol.skript.lang.Literal;
import ch.njol.skript.util.ItemType;
import ch.njol.util.Checker;

/**
 * @author Peter Güttinger
 * 
 */
@SuppressWarnings("unchecked")
public class EvtBlock extends SkriptEvent {
	
	static {
		Skript.addEvent(EvtBlock.class, Skript.array(BlockBreakEvent.class, PaintingBreakEvent.class), "break[ing] [[of] %itemtypes%]");
		Skript.addEvent(EvtBlock.class, BlockBurnEvent.class, "burn[ing] [[of] %itemtypes%]");
		Skript.addEvent(EvtBlock.class, Skript.array(BlockPlaceEvent.class, PaintingPlaceEvent.class), "plac(e|ing) [[of] %itemtypes%]");
	}
	
	private Literal<ItemType> types;
	
	@Override
	public void init(final Literal<?>[] args, final int matchedPattern, final ParseResult parser) {
		types = (Literal<ItemType>) args[0];
	}
	
	@Override
	public boolean check(final Event e) {
		if (types == null)
			return true;
		final Block b = Skript.getEventValue(e, Block.class);
		if (b == null)
			return false;
		return types.check(e, new Checker<ItemType>() {
			@Override
			public boolean check(final ItemType t) {
				return t.isOfType(b);
			}
		});
	}
	
	@Override
	public String getDebugMessage(final Event e) {
		return "break/place/burn of " + Skript.toString(types);
	}
	
}
