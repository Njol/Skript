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

package ch.njol.skript.data;

import org.bukkit.Location;
import org.bukkit.OfflinePlayer;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Item;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;

import ch.njol.skript.Skript;
import ch.njol.skript.api.Converter;
import ch.njol.skript.util.EntityType;
import ch.njol.skript.util.ItemType;
import ch.njol.skript.util.Slot;
import ch.njol.skript.util.Time;

/**
 * @author Peter Güttinger
 * 
 */
public class DefaultConverters {
	
	public DefaultConverters() {}
	
	static {
		
		//Numbers
		Skript.registerConverter(Integer.class, Float.class, new Converter<Integer, Float>() {
			@Override
			public Float convert(final Integer i) {
				return i.floatValue();
			}
		});
		Skript.registerConverter(Integer.class, Double.class, new Converter<Integer, Double>() {
			@Override
			public Double convert(final Integer i) {
				return i.doubleValue();
			}
		});
		Skript.registerConverter(Float.class, Double.class, new Converter<Float, Double>() {
			@Override
			public Double convert(final Float f) {
				return f.doubleValue();
			}
		});
		Skript.registerConverter(Double.class, Float.class, new Converter<Double, Float>() {
			@Override
			public Float convert(final Double d) {
				return d.floatValue();
			}
		});
		
		// OfflinePlayer - PlayerInventory
		Skript.registerConverter(OfflinePlayer.class, PlayerInventory.class, new Converter<OfflinePlayer, PlayerInventory>() {
			@Override
			public PlayerInventory convert(final OfflinePlayer p) {
				if (p == null || !p.isOnline())
					return null;
				return p.getPlayer().getInventory();
			}
		});
//		Skript.registerConverter(PlayerInventory.class, OfflinePlayer.class, new Converter<PlayerInventory, OfflinePlayer>() {
//			@Override
//			public OfflinePlayer convert(final PlayerInventory i) {
//				if (i == null)
//					return null;
//				return Bukkit.getOfflinePlayer(i.getName());
//			}
//		});
		
		// CommandSender - Player
		Skript.registerConverter(CommandSender.class, Player.class, new Converter<CommandSender, Player>() {
			@Override
			public Player convert(final CommandSender s) {
				if (s instanceof Player)
					return (Player) s;
				return null;
			}
		});
//		// Entity - Player
//		Skript.registerConverter(Entity.class, Player.class, new Converter<Entity, Player>() {
//			@Override
//			public Player convert(final Entity s) {
//				if (s instanceof Player)
//					return (Player) s;
//				return null;
//			}
//		});
		
		// Block - Inventory
		Skript.registerConverter(Block.class, Inventory.class, new Converter<Block, Inventory>() {
			@Override
			public Inventory convert(final Block b) {
				if (b == null)
					return null;
				if (b.getState() instanceof InventoryHolder)
					return ((InventoryHolder) b.getState()).getInventory();
				return null;
			}
		});
		
		// Location - Block
		Skript.registerConverter(Location.class, Block.class, new Converter<Location, Block>() {
			@Override
			public Block convert(final Location l) {
				if (l == null)
					return null;
				return l.getBlock();
			}
		});
		Skript.registerConverter(Block.class, Location.class, new Converter<Block, Location>() {
			@Override
			public Location convert(final Block b) {
				if (b == null)
					return null;
				return b.getLocation().add(0.5, 0.5, 0.5);
			}
		});
		
		// Entity - Location
		Skript.registerConverter(Entity.class, Location.class, new Converter<Entity, Location>() {
			@Override
			public Location convert(final Entity e) {
				if (e == null)
					return null;
				return e.getLocation();
			}
		});
		// Entity - EntityType
		Skript.registerConverter(Entity.class, EntityType.class, new Converter<Entity, EntityType>() {
			@Override
			public EntityType convert(final Entity e) {
				return new EntityType(e.getClass(), 1);
			}
		});
		
		// Location - World
		Skript.registerConverter(Location.class, World.class, new Converter<Location, World>() {
			@Override
			public World convert(final Location l) {
				if (l == null)
					return null;
				return l.getWorld();
			}
		});
		
		// ItemType - ItemStack
//		Skript.addConverter(ItemType.class, ItemStack.class, new Converter<ItemType, ItemStack>() {
//			@Override
//			public ItemStack convert(final ItemType i) {
//				if (i == null)
//					return null;
//				return i.getRandom();
//			}
//		});
		Skript.registerConverter(ItemStack.class, ItemType.class, new Converter<ItemStack, ItemType>() {
			@Override
			public ItemType convert(final ItemStack i) {
				if (i == null)
					return null;
				return new ItemType(i);
			}
		});
		
		// Slot - ItemStack
		Skript.registerConverter(Slot.class, ItemStack.class, new Converter<Slot, ItemStack>() {
			@Override
			public ItemStack convert(final Slot s) {
				if (s == null)
					return null;
				final ItemStack i = s.getItem();
				if (i == null)
					return new ItemStack(0, 1);
				return i;
			}
		});
//		// Slot - Inventory
//		Skript.addConverter(Slot.class, Inventory.class, new Converter<Slot, Inventory>() {
//			@Override
//			public Inventory convert(final Slot s) {
//				if (s == null)
//					return null;
//				return s.getInventory();
//			}
//		});
		
		// Item - ItemStack
		Skript.registerConverter(Item.class, ItemStack.class, new Converter<Item, ItemStack>() {
			@Override
			public ItemStack convert(final Item i) {
				if (i == null)
					return null;
				return i.getItemStack();
			}
		});
		
		// OfflinePlayer - InventoryHolder
		Skript.registerConverter(OfflinePlayer.class, InventoryHolder.class, new Converter<OfflinePlayer, InventoryHolder>() {
			@Override
			public InventoryHolder convert(final OfflinePlayer p) {
				if (p == null || !p.isOnline())
					return null;
				return p.getPlayer();
			}
		});
		
		// Block - InventoryHolder
//		Skript.registerConverter(Block.class, InventoryHolder.class, new Converter<Block, InventoryHolder>() {
//			@Override
//			public InventoryHolder convert(final Block b) {
//				if (b == null || b.getState() == null)
//					return null;
//				final BlockState s = b.getState();
//				if (s instanceof InventoryHolder)
//					return (InventoryHolder) s;
//				return null;
//			}
//		});
//		Skript.registerConverter(InventoryHolder.class, Block.class, new Converter<InventoryHolder, Block>() {
//			@Override
//			public Block convert(final InventoryHolder h) {
//				if (h == null)
//					return null;
//				if (h instanceof BlockState)
//					return ((BlockState) h).getBlock();
//				return null;
//			}
//		});
		
		// World - Time
		Skript.registerConverter(World.class, Time.class, new Converter<World, Time>() {
			@Override
			public Time convert(final World w) {
				if (w == null)
					return null;
				return new Time((int) w.getTime());
			}
		});
		
	}
}
