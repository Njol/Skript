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

import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Item;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Painting;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.entity.Vehicle;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockDamageEvent;
import org.bukkit.event.block.BlockDispenseEvent;
import org.bukkit.event.block.BlockEvent;
import org.bukkit.event.block.BlockIgniteEvent;
import org.bukkit.event.block.BlockPistonEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.block.SignChangeEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityDamageEvent.DamageCause;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.entity.EntityEvent;
import org.bukkit.event.entity.ProjectileHitEvent;
import org.bukkit.event.painting.PaintingEvent;
import org.bukkit.event.painting.PaintingPlaceEvent;
import org.bukkit.event.player.PlayerBedEnterEvent;
import org.bukkit.event.player.PlayerBedLeaveEvent;
import org.bukkit.event.player.PlayerBucketEvent;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerEvent;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerShearEntityEvent;
import org.bukkit.event.server.ServerCommandEvent;
import org.bukkit.event.vehicle.VehicleEvent;
import org.bukkit.event.vehicle.VehicleExitEvent;
import org.bukkit.event.weather.WeatherEvent;
import org.bukkit.event.world.WorldEvent;
import org.bukkit.inventory.ItemStack;

import ch.njol.skript.Skript;
import ch.njol.skript.api.Getter;

/**
 * @author Peter Güttinger
 * 
 */
public final class BukkitEventValues {
	
	public BukkitEventValues() {}
	
	static {
		
		// === WorldEvents ===
		Skript.registerEventValue(WorldEvent.class, World.class, new Getter<World, WorldEvent>() {
			@Override
			public World get(final WorldEvent e) {
				return e.getWorld();
			}
		});
		
		// WeatherEvent
		Skript.registerEventValue(WeatherEvent.class, World.class, new Getter<World, WeatherEvent>() {
			@Override
			public World get(final WeatherEvent e) {
				return e.getWorld();
			}
		});
		
		// ServerCommandEvent
		Skript.registerEventValue(ServerCommandEvent.class, CommandSender.class, new Getter<CommandSender, ServerCommandEvent>() {
			@Override
			public CommandSender get(final ServerCommandEvent e) {
				return e.getSender();
			}
		});
		
		// === BlockEvents ===
		Skript.registerEventValue(BlockEvent.class, Block.class, new Getter<Block, BlockEvent>() {
			@Override
			public Block get(final BlockEvent e) {
				return e.getBlock();
			}
		});
		// BlockPlaceEvent
		Skript.registerEventValue(BlockPlaceEvent.class, Player.class, new Getter<Player, BlockPlaceEvent>() {
			@Override
			public Player get(final BlockPlaceEvent e) {
				return e.getPlayer();
			}
		});
		// BlockDamageEvent
		Skript.registerEventValue(BlockDamageEvent.class, Player.class, new Getter<Player, BlockDamageEvent>() {
			@Override
			public Player get(final BlockDamageEvent e) {
				return e.getPlayer();
			}
		});
		// BlockBreakEvent
		Skript.registerEventValue(BlockBreakEvent.class, Player.class, new Getter<Player, BlockBreakEvent>() {
			@Override
			public Player get(final BlockBreakEvent e) {
				return e.getPlayer();
			}
		});
		// BlockIgniteEvent
		Skript.registerEventValue(BlockIgniteEvent.class, Player.class, new Getter<Player, BlockIgniteEvent>() {
			@Override
			public Player get(final BlockIgniteEvent e) {
				return e.getPlayer();
			}
		});
		// BlockDispenseEvent
		Skript.registerEventValue(BlockDispenseEvent.class, ItemStack.class, new Getter<ItemStack, BlockDispenseEvent>() {
			@Override
			public ItemStack get(final BlockDispenseEvent e) {
				return e.getItem();
			}
		});
		// BlockPistonEvent
		Skript.registerEventValue(BlockPistonEvent.class, BlockFace.class, new Getter<BlockFace, BlockPistonEvent>() {
			@Override
			public BlockFace get(final BlockPistonEvent e) {
				return e.getDirection();
			}
		});
		// SignChangeEvent
		Skript.registerEventValue(SignChangeEvent.class, Player.class, new Getter<Player, SignChangeEvent>() {
			@Override
			public Player get(final SignChangeEvent e) {
				return e.getPlayer();
			}
		});
		
		// === EntityEvents ===
		Skript.registerEventValue(EntityEvent.class, Entity.class, new Getter<Entity, EntityEvent>() {
			@Override
			public Entity get(final EntityEvent e) {
				return e.getEntity();
			}
		});
		// EntityDeathEvent
		Skript.registerEventValue(EntityDeathEvent.class, ItemStack[].class, new Getter<ItemStack[], EntityDeathEvent>() {
			@Override
			public ItemStack[] get(final EntityDeathEvent e) {
				return e.getDrops().toArray(new ItemStack[0]);
			}
		});
		// EntityDamageEvent
		Skript.registerEventValue(EntityDamageEvent.class, DamageCause.class, new Getter<DamageCause, EntityDamageEvent>() {
			@Override
			public DamageCause get(final EntityDamageEvent e) {
				return e.getCause();
			}
		});
		// ProjectileHitEvent
		Skript.registerEventValue(ProjectileHitEvent.class, Projectile.class, new Getter<Projectile, ProjectileHitEvent>() {
			@Override
			public Projectile get(final ProjectileHitEvent e) {
				return e.getEntity();
			}
		});
		Skript.registerEventValue(ProjectileHitEvent.class, LivingEntity.class, new Getter<LivingEntity, ProjectileHitEvent>() {
			@Override
			public LivingEntity get(final ProjectileHitEvent e) {
				return e.getEntity().getShooter();
			}
		});
		
		// --- PlayerEvents ---
		Skript.registerEventValue(PlayerEvent.class, Player.class, new Getter<Player, PlayerEvent>() {
			@Override
			public Player get(final PlayerEvent e) {
				return e.getPlayer();
			}
		});
		// PlayerBedEnterEvent
		Skript.registerEventValue(PlayerBedEnterEvent.class, Block.class, new Getter<Block, PlayerBedEnterEvent>() {
			@Override
			public Block get(final PlayerBedEnterEvent e) {
				return e.getBed();
			}
		});
		// PlayerBedLeaveEvent
		Skript.registerEventValue(PlayerBedLeaveEvent.class, Block.class, new Getter<Block, PlayerBedLeaveEvent>() {
			@Override
			public Block get(final PlayerBedLeaveEvent e) {
				return e.getBed();
			}
		});
		// PlayerBucketEvent
		Skript.registerEventValue(PlayerBucketEvent.class, Block.class, new Getter<Block, PlayerBucketEvent>() {
			@Override
			public Block get(final PlayerBucketEvent e) {
				return e.getBlockClicked().getRelative(e.getBlockFace());
			}
		});
		// PlayerDropItemEvent
		Skript.registerEventValue(PlayerDropItemEvent.class, Item.class, new Getter<Item, PlayerDropItemEvent>() {
			@Override
			public Item get(final PlayerDropItemEvent e) {
				return e.getItemDrop();
			}
		});
		// PlayerInteractEntityEvent
		Skript.registerEventValue(PlayerInteractEntityEvent.class, Entity.class, new Getter<Entity, PlayerInteractEntityEvent>() {
			@Override
			public Entity get(final PlayerInteractEntityEvent e) {
				return e.getRightClicked();
			}
		});
		// PlayerInteractEvent
		Skript.registerEventValue(PlayerInteractEvent.class, Block.class, new Getter<Block, PlayerInteractEvent>() {
			@Override
			public Block get(final PlayerInteractEvent e) {
				return e.getClickedBlock();
			}
		});
		// PlayerShearEntityEvent
		Skript.registerEventValue(PlayerShearEntityEvent.class, Entity.class, new Getter<Entity, PlayerShearEntityEvent>() {
			@Override
			public Entity get(final PlayerShearEntityEvent e) {
				return e.getEntity();
			}
		});
		// PlayerCommandPreprocessEvent
		Skript.registerEventValue(PlayerCommandPreprocessEvent.class, Player.class, new Getter<Player, PlayerCommandPreprocessEvent>() {
			@Override
			public Player get(final PlayerCommandPreprocessEvent e) {
				return e.getPlayer();
			}
		});
		
		// --- PaintingEvents ---
		Skript.registerEventValue(PaintingEvent.class, Painting.class, new Getter<Painting, PaintingEvent>() {
			@Override
			public Painting get(final PaintingEvent e) {
				return e.getPainting();
			}
		});
		// PaintingPlaceEvent
		Skript.registerEventValue(PaintingPlaceEvent.class, Player.class, new Getter<Player, PaintingPlaceEvent>() {
			@Override
			public Player get(final PaintingPlaceEvent e) {
				return e.getPlayer();
			}
		});
		
		// --- VehicleEvents ---
		Skript.registerEventValue(VehicleEvent.class, Vehicle.class, new Getter<Vehicle, VehicleEvent>() {
			@Override
			public Vehicle get(final VehicleEvent e) {
				return e.getVehicle();
			}
		});
		Skript.registerEventValue(VehicleExitEvent.class, LivingEntity.class, new Getter<LivingEntity, VehicleExitEvent>() {
			@Override
			public LivingEntity get(final VehicleExitEvent e) {
				return e.getExited();
			}
		});
		Skript.registerEventValue(VehicleEvent.class, Entity.class, new Getter<Entity, VehicleEvent>() {
			@Override
			public Entity get(final VehicleEvent e) {
				return e.getVehicle().getPassenger();
			}
		});
		
	}
	
}
