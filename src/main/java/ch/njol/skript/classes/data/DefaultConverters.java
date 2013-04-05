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

import java.util.HashMap;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.block.Block;
import org.bukkit.block.BlockState;
import org.bukkit.command.CommandSender;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Arrow;
import org.bukkit.entity.Boat;
import org.bukkit.entity.Egg;
import org.bukkit.entity.EnderPearl;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Fish;
import org.bukkit.entity.Item;
import org.bukkit.entity.ItemFrame;
import org.bukkit.entity.Minecart;
import org.bukkit.entity.Painting;
import org.bukkit.entity.Player;
import org.bukkit.entity.PoweredMinecart;
import org.bukkit.entity.Snowball;
import org.bukkit.entity.StorageMinecart;
import org.bukkit.entity.TNTPrimed;
import org.bukkit.entity.ThrownExpBottle;
import org.bukkit.entity.ThrownPotion;
import org.bukkit.entity.WitherSkull;
import org.bukkit.entity.minecart.ExplosiveMinecart;
import org.bukkit.entity.minecart.HopperMinecart;
import org.bukkit.entity.minecart.RideableMinecart;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;

import ch.njol.skript.Skript;
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
import ch.njol.skript.util.PotionEffectUtils;
import ch.njol.skript.util.Slot;

/**
 * @author Peter Güttinger
 */
@SuppressWarnings({"rawtypes", "serial", "deprecation"})
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
		Converters.registerConverter(Location.class, Block.class, new SerializableConverter<Location, Block>() {
			@Override
			public Block convert(final Location l) {
				return l.getBlock();
			}
		});
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
		
		// to fix comparisions of eggs, arrows, etc. (e.g. 'projectile is an arrow'), but also allows to add these entities to inventories as items
		// TODO !Update with every version [entities]
		final HashMap<Class<? extends Entity>, Material> entityMaterials = new HashMap<Class<? extends Entity>, Material>();
		entityMaterials.put(Boat.class, Material.BOAT);
		entityMaterials.put(Minecart.class, Material.MINECART);
		entityMaterials.put(Painting.class, Material.PAINTING);
		entityMaterials.put(Arrow.class, Material.ARROW);
		entityMaterials.put(Egg.class, Material.EGG);
		entityMaterials.put(EnderPearl.class, Material.ENDER_PEARL);
		entityMaterials.put(Snowball.class, Material.SNOW_BALL);
		entityMaterials.put(ThrownExpBottle.class, Material.EXP_BOTTLE);
		entityMaterials.put(Fish.class, Material.RAW_FISH);
		entityMaterials.put(TNTPrimed.class, Material.TNT);
		if (Skript.isRunningMinecraft(1, 4))
			entityMaterials.put(ItemFrame.class, Material.ITEM_FRAME);
		if (Skript.isRunningMinecraft(1, 5)) {
			entityMaterials.put(org.bukkit.entity.minecart.StorageMinecart.class, Material.STORAGE_MINECART);
			entityMaterials.put(org.bukkit.entity.minecart.PoweredMinecart.class, Material.POWERED_MINECART);
			entityMaterials.put(RideableMinecart.class, Material.MINECART);
			entityMaterials.put(HopperMinecart.class, Material.HOPPER_MINECART);
			entityMaterials.put(ExplosiveMinecart.class, Material.EXPLOSIVE_MINECART);
		} else {
			entityMaterials.put(StorageMinecart.class, Material.STORAGE_MINECART);
			entityMaterials.put(PoweredMinecart.class, Material.POWERED_MINECART);
		}
		Converters.registerConverter(Entity.class, ItemStack.class, new SerializableConverter<Entity, ItemStack>() {
			@Override
			public ItemStack convert(final Entity e) {
				for (final Class<?> i : e.getClass().getInterfaces()) {
					final Material m = entityMaterials.get(i);
					if (m != null)
						return new ItemStack(m);
				}
				if (e instanceof Item)
					return ((Item) e).getItemStack();
				if (e instanceof ThrownPotion)
					return new ItemStack(Material.POTION, 1, PotionEffectUtils.guessData((ThrownPotion) e));
				if (Skript.isRunningMinecraft(1, 4) && e instanceof WitherSkull)
					return new ItemStack(Material.SKULL_ITEM, 1, (short) 1);
				return null;
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
		
		// OfflinePlayer - Player
		Converters.registerConverter(OfflinePlayer.class, Player.class, new SerializableConverter<OfflinePlayer, Player>() {
			@Override
			public Player convert(final OfflinePlayer p) {
				if (!p.isOnline())
					return null;
				return p.getPlayer();
			}
		});
		
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
