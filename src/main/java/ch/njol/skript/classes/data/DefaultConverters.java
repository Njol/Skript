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
import org.bukkit.block.Block;
import org.bukkit.block.BlockState;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Item;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;

import ch.njol.skript.classes.Converter.ConverterOptions;
import ch.njol.skript.classes.SerializableConverter;
import ch.njol.skript.entity.EntityData;
import ch.njol.skript.entity.EntityType;
import ch.njol.skript.registrations.Converters;
import ch.njol.skript.util.ItemType;
import ch.njol.skript.util.Slot;

/**
 * @author Peter Güttinger
 */
@SuppressWarnings("rawtypes")
public class DefaultConverters {
	
	public DefaultConverters() {}
	
	static {
		
		//Numbers
//		Skript.registerConverter(Number.class, Double.class, new SerializableConverter<Number, Double>() {
//			private static final long serialVersionUID = 961604740292695669L;
//			
//			@Override
//			public Double convert(final Number n) {
//				return n.doubleValue();
//			}
//		});
//		Skript.registerConverter(Number.class, Float.class, new SerializableConverter<Number, Float>() {
//			private static final long serialVersionUID = -3957430418305715186L;
//			
//			@Override
//			public Float convert(final Number n) {
//				return n.floatValue();
//			}
//		});
//		Skript.registerConverter(Integer.class, Long.class, new SerializableConverter<Integer, Long>() {
//			private static final long serialVersionUID = -2281206164098922538L;
//			
//			@Override
//			public Long convert(final Integer i) {
//				return i.longValue();
//			}
//		});
//		Skript.registerConverter(Short.class, Long.class, new SerializableConverter<Short, Long>() {
//			private static final long serialVersionUID = -2281206164098922538L;
//			
//			@Override
//			public Long convert(final Short s) {
//				return Long.valueOf(s.longValue());
//			}
//		});
//		Skript.registerConverter(Byte.class, Long.class, new SerializableConverter<Byte, Long>() {
//			private static final long serialVersionUID = -3514663025279567196L;
//			
//			@Override
//			public Long convert(final Byte b) {
//				return Long.valueOf(b.longValue());
//			}
//		});
//		Skript.registerConverter(Byte.class, Short.class, new SerializableConverter<Byte, Short>() {
//			private static final long serialVersionUID = -3715377159539354118L;
//			
//			@Override
//			public Short convert(final Byte b) {
//				return Short.valueOf(b.shortValue());
//			}
//		});
		
		// OfflinePlayer - PlayerInventory
		Converters.registerConverter(OfflinePlayer.class, PlayerInventory.class, new SerializableConverter<OfflinePlayer, PlayerInventory>() {
			private static final long serialVersionUID = 1406259567778884904L;
			
			@Override
			public PlayerInventory convert(final OfflinePlayer p) {
				if (!p.isOnline())
					return null;
				return p.getPlayer().getInventory();
			}
		});
		// OfflinePlayer - Player
		Converters.registerConverter(OfflinePlayer.class, Player.class, new SerializableConverter<OfflinePlayer, Player>() {
			private static final long serialVersionUID = 1784932642233241204L;
			
			@Override
			public Player convert(final OfflinePlayer p) {
				return p.getPlayer();
			}
		});
		
		// CommandSender - Player
		Converters.registerConverter(CommandSender.class, Player.class, new SerializableConverter<CommandSender, Player>() {
			private static final long serialVersionUID = -1461407582063991942L;
			
			@Override
			public Player convert(final CommandSender s) {
				if (s instanceof Player)
					return (Player) s;
				return null;
			}
		});
		// Entity - Player
		// TODO improve handling of interfaces
		Converters.registerConverter(Entity.class, Player.class, new SerializableConverter<Entity, Player>() {
			private static final long serialVersionUID = 6892053559153452238L;
			
			@Override
			public Player convert(final Entity e) {
				if (e instanceof Player)
					return (Player) e;
				return null;
			}
		});
		
		// Block - Inventory
		Converters.registerConverter(Block.class, Inventory.class, new SerializableConverter<Block, Inventory>() {
			private static final long serialVersionUID = -720656618540060571L;
			
			@Override
			public Inventory convert(final Block b) {
				if (b.getState() instanceof InventoryHolder)
					return ((InventoryHolder) b.getState()).getInventory();
				return null;
			}
		});
		
		// Block - ItemStack
		Converters.registerConverter(Block.class, ItemStack.class, new SerializableConverter<Block, ItemStack>() {
			private static final long serialVersionUID = 1919746367202352251L;
			
			@Override
			public ItemStack convert(final Block b) {
				return new ItemStack(b.getTypeId(), 1, b.getData());
			}
		}, ConverterOptions.NO_LEFT_CHAINING);
		
		// Location - Block
		Converters.registerConverter(Location.class, Block.class, new SerializableConverter<Location, Block>() {
			private static final long serialVersionUID = -5292388902665009733L;
			
			@Override
			public Block convert(final Location l) {
				return l.getBlock();
			}
		});
		Converters.registerConverter(Block.class, Location.class, new SerializableConverter<Block, Location>() {
			private static final long serialVersionUID = -8082270387051361765L;
			
			@Override
			public Location convert(final Block b) {
				return b.getLocation().add(0.5, 0.5, 0.5);
			}
		});
		
		// Entity - Location
		Converters.registerConverter(Entity.class, Location.class, new SerializableConverter<Entity, Location>() {
			private static final long serialVersionUID = 4290287600480149382L;
			
			@Override
			public Location convert(final Entity e) {
				if (e == null)
					return null;
				return e.getLocation();
			}
		});
		// Entity - EntityData
		Converters.registerConverter(Entity.class, EntityData.class, new SerializableConverter<Entity, EntityData>() {
			private static final long serialVersionUID = -4840228378205738178L;
			
			@Override
			public EntityData convert(final Entity e) {
				return EntityData.fromEntity(e);
			}
		});
		// EntityData - EntityType
		Converters.registerConverter(EntityData.class, EntityType.class, new SerializableConverter<EntityData, EntityType>() {
			private static final long serialVersionUID = -8509228303455103889L;
			
			@Override
			public EntityType convert(final EntityData data) {
				return new EntityType(data, -1);
			}
		});
		
		// Location - World
//		Skript.registerConverter(Location.class, World.class, new SerializableConverter<Location, World>() {
//			private static final long serialVersionUID = 3270661123492313649L;
//			
//			@Override
//			public World convert(final Location l) {
//				if (l == null)
//					return null;
//				return l.getWorld();
//			}
//		});
		
		// ItemType - ItemStack
//		Skript.addSerializableConverter(ItemType.class, ItemStack.class, new SerializableConverter<ItemType, ItemStack>() {
//			@Override
//			public ItemStack convert(final ItemType i) {
//				if (i == null)
//					return null;
//				return i.getRandom();
//			}
//		});
		Converters.registerConverter(ItemStack.class, ItemType.class, new SerializableConverter<ItemStack, ItemType>() {
			private static final long serialVersionUID = -5693219418938859295L;
			
			@Override
			public ItemType convert(final ItemStack i) {
				if (i == null)
					return null;
				return new ItemType(i);
			}
		});
		
		// Slot - ItemStack
		Converters.registerConverter(Slot.class, ItemStack.class, new SerializableConverter<Slot, ItemStack>() {
			private static final long serialVersionUID = -8985272066421244801L;
			
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
//		Skript.addSerializableConverter(Slot.class, Inventory.class, new SerializableConverter<Slot, Inventory>() {
//			@Override
//			public Inventory convert(final Slot s) {
//				if (s == null)
//					return null;
//				return s.getInventory();
//			}
//		});
		
		// Item - ItemStack
		Converters.registerConverter(Item.class, ItemStack.class, new SerializableConverter<Item, ItemStack>() {
			private static final long serialVersionUID = 7259092571473789525L;
			
			@Override
			public ItemStack convert(final Item i) {
				if (i == null)
					return null;
				return i.getItemStack();
			}
		});
		
		// OfflinePlayer - InventoryHolder
		Converters.registerConverter(OfflinePlayer.class, InventoryHolder.class, new SerializableConverter<OfflinePlayer, InventoryHolder>() {
			private static final long serialVersionUID = 767824297432714799L;
			
			@Override
			public InventoryHolder convert(final OfflinePlayer p) {
				if (p == null || !p.isOnline())
					return null;
				return p.getPlayer();
			}
		});
		
		// Block - InventoryHolder
		Converters.registerConverter(Block.class, InventoryHolder.class, new SerializableConverter<Block, InventoryHolder>() {
			private static final long serialVersionUID = -9025690640371588378L;
			
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
//		Skript.registerConverter(InventoryHolder.class, Block.class, new SerializableConverter<InventoryHolder, Block>() {
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
//		Skript.registerConverter(World.class, Time.class, new SerializableConverter<World, Time>() {
//			@Override
//			public Time convert(final World w) {
//				if (w == null)
//					return null;
//				return new Time((int) w.getTime());
//			}
//		});
		
	}
}
