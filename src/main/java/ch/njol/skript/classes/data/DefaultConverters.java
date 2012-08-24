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

package ch.njol.skript.classes.data;

import org.bukkit.Location;
import org.bukkit.OfflinePlayer;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.BlockState;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Item;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;

import ch.njol.skript.Skript;
import ch.njol.skript.classes.Converter;
import ch.njol.skript.classes.Converter.ConverterOptions;
import ch.njol.skript.entity.EntityData;
import ch.njol.skript.entity.EntityType;
import ch.njol.skript.util.ItemType;
import ch.njol.skript.util.Offset;
import ch.njol.skript.util.Slot;

/**
 * @author Peter Güttinger
 * 
 */
@SuppressWarnings("rawtypes")
public class DefaultConverters {
	
	public DefaultConverters() {}
	
	static {
		
		//Numbers
		Skript.registerConverter(Number.class, Double.class, new Converter<Number, Double>() {
			@Override
			public Double convert(final Number n) {
				return n.doubleValue();
			}
		});
		Skript.registerConverter(Number.class, Float.class, new Converter<Number, Float>() {
			@Override
			public Float convert(final Number n) {
				return n.floatValue();
			}
		});
		Skript.registerConverter(Short.class, Integer.class, new Converter<Short, Integer>() {
			@Override
			public Integer convert(final Short s) {
				return s.intValue();
			}
		});
		Skript.registerConverter(Byte.class, Integer.class, new Converter<Byte, Integer>() {
			@Override
			public Integer convert(final Byte b) {
				return b.intValue();
			}
		});
		
		// OfflinePlayer - PlayerInventory
		Skript.registerConverter(OfflinePlayer.class, PlayerInventory.class, new Converter<OfflinePlayer, PlayerInventory>() {
			@Override
			public PlayerInventory convert(final OfflinePlayer p) {
				if (!p.isOnline())
					return null;
				return p.getPlayer().getInventory();
			}
		});
		// OfflinePlayer - Player
		Skript.registerConverter(OfflinePlayer.class, Player.class, new Converter<OfflinePlayer, Player>() {
			@Override
			public Player convert(final OfflinePlayer p) {
				return p.getPlayer();
			}
		});
		
		// CommandSender - Player
		Skript.registerConverter(CommandSender.class, Player.class, new Converter<CommandSender, Player>() {
			@Override
			public Player convert(final CommandSender s) {
				if (s instanceof Player)
					return (Player) s;
				return null;
			}
		});
		// Entity - Player
		// TODO improve handling of interfaces
		Skript.registerConverter(Entity.class, Player.class, new Converter<Entity, Player>() {
			@Override
			public Player convert(final Entity e) {
				if (e instanceof Player)
					return (Player) e;
				return null;
			}
		});
		
		// Block - Inventory
		Skript.registerConverter(Block.class, Inventory.class, new Converter<Block, Inventory>() {
			@Override
			public Inventory convert(final Block b) {
				if (b.getState() instanceof InventoryHolder)
					return ((InventoryHolder) b.getState()).getInventory();
				return null;
			}
		});
		
		// Block - ItemStack
		Skript.registerConverter(Block.class, ItemStack.class, new Converter<Block, ItemStack>() {
			@Override
			public ItemStack convert(final Block b) {
				return new ItemStack(b.getTypeId(), 1, b.getData());
			}
		}, ConverterOptions.NO_LEFT_CHAINING);
		
		// Location - Block
		Skript.registerConverter(Location.class, Block.class, new Converter<Location, Block>() {
			@Override
			public Block convert(final Location l) {
				return l.getBlock();
			}
		});
		Skript.registerConverter(Block.class, Location.class, new Converter<Block, Location>() {
			@Override
			public Location convert(final Block b) {
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
		// Entity - EntityData
		Skript.registerConverter(Entity.class, EntityData.class, new Converter<Entity, EntityData>() {
			@Override
			public EntityData convert(final Entity e) {
				return EntityData.fromEntity(e);
			}
		});
		// EntityData - EntityType
		Skript.registerConverter(EntityData.class, EntityType.class, new Converter<EntityData, EntityType>() {
			@Override
			public EntityType convert(final EntityData data) {
				return new EntityType(data, -1);
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
		Skript.registerConverter(Block.class, InventoryHolder.class, new Converter<Block, InventoryHolder>() {
			@Override
			public InventoryHolder convert(final Block b) {
				if (b == null || b.getState() == null)
					return null;
				final BlockState s = b.getState();
				if (s instanceof InventoryHolder)
					return (InventoryHolder) s;
				return null;
			}
		});
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
		
//		// World - Time
//		Skript.registerConverter(World.class, Time.class, new Converter<World, Time>() {
//			@Override
//			public Time convert(final World w) {
//				if (w == null)
//					return null;
//				return new Time((int) w.getTime());
//			}
//		});
		
		// Blockface - Offset
		Skript.registerConverter(BlockFace.class, Offset.class, new Converter<BlockFace, Offset>() {
			@Override
			public Offset convert(final BlockFace f) {
				return new Offset(f.getModX(), f.getModY(), f.getModZ());
			}
		});
		
	}
}
