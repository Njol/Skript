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

package ch.njol.skript.classes.data;

import org.bukkit.Location;
import org.bukkit.OfflinePlayer;
import org.bukkit.block.Block;
import org.bukkit.block.BlockState;
import org.bukkit.command.CommandSender;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;

import ch.njol.skript.aliases.ItemType;
import ch.njol.skript.classes.Converter.ConverterOptions;
import ch.njol.skript.classes.SerializableConverter;
import ch.njol.skript.entity.EntityData;
import ch.njol.skript.entity.EntityType;
import ch.njol.skript.entity.XpOrbData;
import ch.njol.skript.registrations.Converters;
import ch.njol.skript.util.BlockUtils;
import ch.njol.skript.util.EnchantmentType;
import ch.njol.skript.util.Experience;
import ch.njol.skript.util.Slot;

/**
 * @author Peter Güttinger
 */
@SuppressWarnings({"rawtypes", "serial"})
public class DefaultConverters {
	
	public DefaultConverters() {}
	
	static {
		
		// OfflinePlayer - PlayerInventory
		Converters.registerConverter(OfflinePlayer.class, PlayerInventory.class, new SerializableConverter<OfflinePlayer, PlayerInventory>() {
			@Override
			public PlayerInventory convert(final OfflinePlayer p) {
				if (!p.isOnline())
					return null;
				return p.getPlayer().getInventory();
			}
		});
		// OfflinePlayer - Player
		Converters.registerConverter(OfflinePlayer.class, Player.class, new SerializableConverter<OfflinePlayer, Player>() {
			@Override
			public Player convert(final OfflinePlayer p) {
				return p.getPlayer();
			}
		});
		
		// TODO improve handling of interfaces
		// CommandSender - Player
		Converters.registerConverter(CommandSender.class, Player.class, new SerializableConverter<CommandSender, Player>() {
			@Override
			public Player convert(final CommandSender s) {
				if (s instanceof Player)
					return (Player) s;
				return null;
			}
		});
		// Entity - Player
		Converters.registerConverter(Entity.class, Player.class, new SerializableConverter<Entity, Player>() {
			@Override
			public Player convert(final Entity e) {
				if (e instanceof Player)
					return (Player) e;
				return null;
			}
		});
		// Entity - LivingEntity // Entity->Player is used if this doesn't exist
		Converters.registerConverter(Entity.class, LivingEntity.class, new SerializableConverter<Entity, LivingEntity>() {
			@Override
			public LivingEntity convert(final Entity e) {
				if (e instanceof LivingEntity)
					return (LivingEntity) e;
				return null;
			}
		});
		
		// Block - Inventory
		Converters.registerConverter(Block.class, Inventory.class, new SerializableConverter<Block, Inventory>() {
			@Override
			public Inventory convert(final Block b) {
				if (b.getState() instanceof InventoryHolder)
					return ((InventoryHolder) b.getState()).getInventory();
				return null;
			}
		});
		
		// Block - ItemStack
		Converters.registerConverter(Block.class, ItemStack.class, new SerializableConverter<Block, ItemStack>() {
			@Override
			public ItemStack convert(final Block b) {
				return new ItemStack(b.getTypeId(), 1, b.getData());
			}
		}, ConverterOptions.NO_LEFT_CHAINING);
		
		// Location - Block
//		Converters.registerConverter(Location.class, Block.class, new SerializableConverter<Location, Block>() {
//			@Override
//			public Block convert(final Location l) {
//				return l.getBlock();
//			}
//		});
		Converters.registerConverter(Block.class, Location.class, new SerializableConverter<Block, Location>() {
			@Override
			public Location convert(final Block b) {
				return BlockUtils.getLocation(b);
			}
		});
		
		// Entity - Location
		Converters.registerConverter(Entity.class, Location.class, new SerializableConverter<Entity, Location>() {
			@Override
			public Location convert(final Entity e) {
				return e.getLocation();
			}
		});
		// Entity - EntityData
		Converters.registerConverter(Entity.class, EntityData.class, new SerializableConverter<Entity, EntityData>() {
			@Override
			public EntityData convert(final Entity e) {
				return EntityData.fromEntity(e);
			}
		});
		// EntityData - EntityType
		Converters.registerConverter(EntityData.class, EntityType.class, new SerializableConverter<EntityData, EntityType>() {
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
		Converters.registerConverter(ItemType.class, ItemStack.class, new SerializableConverter<ItemType, ItemStack>() {
			@Override
			public ItemStack convert(final ItemType i) {
				return i.getRandom();
			}
		});
		Converters.registerConverter(ItemStack.class, ItemType.class, new SerializableConverter<ItemStack, ItemType>() {
			@Override
			public ItemType convert(final ItemStack i) {
				return new ItemType(i);
			}
		});
		
		// Experience - XpOrbData
		Converters.registerConverter(Experience.class, XpOrbData.class, new SerializableConverter<Experience, XpOrbData>() {
			@Override
			public XpOrbData convert(final Experience e) {
				return new XpOrbData(e.getXP());
			}
		});
		Converters.registerConverter(XpOrbData.class, Experience.class, new SerializableConverter<XpOrbData, Experience>() {
			@Override
			public Experience convert(final XpOrbData e) {
				return new Experience(e.getExperience());
			}
		});
		
//		// Item - ItemStack
//		Converters.registerConverter(Item.class, ItemStack.class, new SerializableConverter<Item, ItemStack>() {
//			@Override
//			public ItemStack convert(final Item i) {
//				return i.getItemStack();
//			}
//		});
		
		// Slot - ItemStack
		Converters.registerConverter(Slot.class, ItemStack.class, new SerializableConverter<Slot, ItemStack>() {
			@Override
			public ItemStack convert(final Slot s) {
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
		
		// Block - InventoryHolder
		Converters.registerConverter(Block.class, InventoryHolder.class, new SerializableConverter<Block, InventoryHolder>() {
			@Override
			public InventoryHolder convert(final Block b) {
				if (b.getState() == null)
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
		
		// Enchantment - EnchantmentType
		Converters.registerConverter(Enchantment.class, EnchantmentType.class, new SerializableConverter<Enchantment, EnchantmentType>() {
			@Override
			public EnchantmentType convert(final Enchantment e) {
				return new EnchantmentType(e, -1);
			}
		});
		
	}
}
