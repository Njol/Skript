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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map.Entry;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.event.Event;
import org.bukkit.event.EventException;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.plugin.EventExecutor;

import ch.njol.skript.Skript;
import ch.njol.skript.SkriptEventHandler;
import ch.njol.skript.lang.Literal;
import ch.njol.skript.lang.SelfRegisteringSkriptEvent;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.skript.lang.Trigger;
import ch.njol.skript.registrations.Classes;
import ch.njol.skript.util.ItemData;
import ch.njol.skript.util.ItemType;

/**
 * @author Peter Güttinger
 */
public class EvtMoveOn extends SelfRegisteringSkriptEvent {
	private static final long serialVersionUID = -6868864134840413002L;
	
//	private final static class BlockLocation {
//		final World world;
//		final int x, y, z;
//		
//		BlockLocation(final World world, final int x, final int y, final int z) {
//			this.world = world;
//			this.x = x;
//			this.y = y;
//			this.z = z;
//		}
//		
//		@Override
//		public boolean equals(final Object obj) {
//			if (obj == this)
//				return true;
//			if (!(obj instanceof BlockLocation))
//				return false;
//			final BlockLocation other = (BlockLocation) obj;
//			return world == other.world && x == other.x && y == other.y && z == other.z;
//		}
//		
//		@Override
//		public int hashCode() {
//			return world.hashCode() + 29 * (x + 17 * (y + 31 * z));
//		}
//	}
	
	static {
//		Skript.registerEvent(EvtMoveOn.class, PlayerMoveEvent.class, "(step|walk) on <.+>");
		Skript.registerEvent(EvtMoveOn.class, PlayerMoveEvent.class, "(step|walk) on %itemtypes%");
	}
	
//	private final static HashMap<BlockLocation, List<Trigger>> blockTriggers = new HashMap<BlockLocation, List<Trigger>>();
	private final static HashMap<Integer, List<Trigger>> itemTypeTriggers = new HashMap<Integer, List<Trigger>>();
	private ItemType[] types = null;
//	private World world;
//	private int x, y, z;
	
	private static boolean registeredExecutor = false;
	private final static EventExecutor executor = new EventExecutor() {
		@Override
		public void execute(final Listener l, final Event event) throws EventException {
			final PlayerMoveEvent e = (PlayerMoveEvent) event;
			final Location from = e.getFrom(), to = e.getTo();
			if (from.getWorld() == to.getWorld() && from.getBlockX() == to.getBlockX() && from.getBlockY() == to.getBlockY() && from.getBlockZ() == to.getBlockZ())
				return;
			SkriptEventHandler.logEventStart(e);
//			if (!blockTriggers.isEmpty()) {
//				final List<Trigger> ts = blockTriggers.get(new BlockLocation(to.getWorld(), to.getBlockX(), to.getBlockY(), to.getBlockZ()));
//				if (ts != null) {
//					for (final Trigger t : ts) {
//						SkriptEventHandler.logTriggerStart(t);
//						t.start(e);
//						SkriptEventHandler.logTriggerEnd(t);
//					}
//				}
//			}
			if (!itemTypeTriggers.isEmpty()) {
				final int id = to.getWorld().getBlockTypeIdAt(to.getBlockX(), to.getBlockY(), to.getBlockZ());
				final List<Trigger> ts = itemTypeTriggers.get(id);
				if (ts != null) {
					final byte data = to.getBlock().getData();
					triggersLoop: for (final Trigger t : ts) {
						final EvtMoveOn se = (EvtMoveOn) t.getEvent();
						for (final ItemType i : se.types) {
							if (i.isOfType(id, data)) {
								SkriptEventHandler.logTriggerStart(t);
								t.start(e);
								SkriptEventHandler.logTriggerEnd(t);
								continue triggersLoop;
							}
						}
					}
				}
			}
			SkriptEventHandler.logEventEnd();
		}
	};
	
	@Override
	public boolean init(final Literal<?>[] args, final int matchedPattern, final ParseResult parser) {
		if (parser.regexes.get(0).group().equalsIgnoreCase("<block>")/* && isValidatingInput*/)
			return true;
//		final Matcher m = Pattern.compile("<block:(.+)>").matcher(parser.regexes.get(0).group());
//		if (m.matches()) {
//			final Block b = (Block) Skript.deserialize("block", m.group(1));
//			if (b == null)
//				return false;
//			world = b.getWorld();
//			x = b.getX();
//			y = b.getY();
//			z = b.getZ();
//		} else {
		@SuppressWarnings("unchecked")
		final Literal<? extends ItemType> l = (Literal<? extends ItemType>) args[0];//SkriptParser.parseLiteral(parser.regexes.get(0).group(), ItemType.class, ParseContext.EVENT);
		if (l == null)
			return false;
		types = l.getAll();
		for (final ItemType t : types) {
			boolean hasBlock = false;
			for (final ItemData d : t) {
				if (d.getId() == -1) {
					Skript.error("Can't use an 'on walk' event with an alias that matches all blocks");
					return false;
				}
				if (d.getId() <= Skript.MAXBLOCKID)
					hasBlock = true;
			}
			if (!hasBlock) {
				Skript.error(t + " is not a block and can thus not be walked on");
				return false;
			}
		}
//		}
		return true;
	}
	
	@Override
	public String toString(final Event e, final boolean debug) {
		return "walk on " + Classes.toString(types, false);
//		return "walk on " + (types != null ? Skript.toString(types, false) : "<block:" + world.getName() + ":" + x + "," + y + "," + z + ">");
	}
	
	@Override
	public void register(final Trigger trigger) {
//		if (types == null) {
//			final BlockLocation l = new BlockLocation(world, x, y, z);
//			List<Trigger> ts = blockTriggers.get(l);
//			if (ts == null)
//				blockTriggers.put(l, ts = new ArrayList<Trigger>());
//			ts.add(trigger);
//		} else {
		for (final ItemType t : types) {
			for (final ItemData d : t) {
				if (d.getId() > Skript.MAXBLOCKID)
					continue;
				List<Trigger> ts = itemTypeTriggers.get(d.getId());
				if (ts == null)
					itemTypeTriggers.put(d.getId(), ts = new ArrayList<Trigger>());
				ts.add(trigger);
			}
		}
//		}
		if (!registeredExecutor) {
			Bukkit.getPluginManager().registerEvent(PlayerMoveEvent.class, new Listener() {}, Skript.getDefaultEventPriority(), executor, Skript.getInstance(), true);
			registeredExecutor = true;
		}
	}
	
	@Override
	public void unregister(final Trigger t) {
//		final Iterator<Entry<BlockLocation, List<Trigger>>> i = blockTriggers.entrySet().iterator();
//		while (i.hasNext()) {
//			final List<Trigger> ts = i.next().getValue();
//			ts.remove(t);
//			if (ts.isEmpty())
//				i.remove();
//		}
		final Iterator<Entry<Integer, List<Trigger>>> i2 = itemTypeTriggers.entrySet().iterator();
		while (i2.hasNext()) {
			final List<Trigger> ts = i2.next().getValue();
			ts.remove(t);
			if (ts.isEmpty())
				i2.remove();
		}
	}
	
	@Override
	public void unregisterAll() {
//		blockTriggers.clear();
		itemTypeTriggers.clear();
	}
}
