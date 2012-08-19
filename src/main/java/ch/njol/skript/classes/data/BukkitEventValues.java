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

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.BlockState;
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
import org.bukkit.event.block.BlockFadeEvent;
import org.bukkit.event.block.BlockFormEvent;
import org.bukkit.event.block.BlockIgniteEvent;
import org.bukkit.event.block.BlockPistonEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.block.BlockSpreadEvent;
import org.bukkit.event.block.SignChangeEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityDamageEvent.DamageCause;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.entity.EntityEvent;
import org.bukkit.event.entity.ProjectileHitEvent;
import org.bukkit.event.painting.PaintingEvent;
import org.bukkit.event.painting.PaintingPlaceEvent;
import org.bukkit.event.player.PlayerBedEnterEvent;
import org.bukkit.event.player.PlayerBedLeaveEvent;
import org.bukkit.event.player.PlayerBucketEmptyEvent;
import org.bukkit.event.player.PlayerBucketFillEvent;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerEvent;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.player.PlayerShearEntityEvent;
import org.bukkit.event.vehicle.VehicleEvent;
import org.bukkit.event.vehicle.VehicleExitEvent;
import org.bukkit.event.weather.WeatherEvent;
import org.bukkit.event.world.WorldEvent;
import org.bukkit.inventory.ItemStack;

import ch.njol.skript.Skript;
import ch.njol.skript.command.CommandEvent;
import ch.njol.skript.util.BlockStateBlock;
import ch.njol.skript.util.DelayedChangeBlock;
import ch.njol.skript.util.Getter;

/**
 * @author Peter Güttinger
 * 
 */
@SuppressWarnings("unchecked")
public final class BukkitEventValues {
	
	public BukkitEventValues() {}
	
	static {
		
		// === WorldEvents ===
		Skript.registerEventValue(WorldEvent.class, World.class, new Getter<World, WorldEvent>() {
			@Override
			public World get(final WorldEvent e) {
				return e.getWorld();
			}
		}, 0);
		
		// WeatherEvent
		Skript.registerEventValue(WeatherEvent.class, World.class, new Getter<World, WeatherEvent>() {
			@Override
			public World get(final WeatherEvent e) {
				return e.getWorld();
			}
		}, 0);
		
		// === BlockEvents ===
		Skript.registerEventValue(BlockEvent.class, Block.class, new Getter<Block, BlockEvent>() {
			@Override
			public Block get(final BlockEvent e) {
				return e.getBlock();
			}
		}, 0);
		// TODO workaround of the event's location being at the entity in block events that have an entity event value
		Skript.registerEventValue(BlockEvent.class, Location.class, new Getter<Location, BlockEvent>() {
			@Override
			public Location get(final BlockEvent e) {
				return e.getBlock().getLocation().add(0.5, 0.5, 0.5);
			}
		}, 0);
		// BlockPlaceEvent
		Skript.registerEventValue(BlockPlaceEvent.class, Player.class, new Getter<Player, BlockPlaceEvent>() {
			@Override
			public Player get(final BlockPlaceEvent e) {
				return e.getPlayer();
			}
		}, 0);
		Skript.registerEventValue(BlockPlaceEvent.class, Block.class, new Getter<Block, BlockPlaceEvent>() {
			@Override
			public Block get(final BlockPlaceEvent e) {
				return new BlockStateBlock(e.getBlockReplacedState());
			}
		}, -1);
		// BlockFadeEvent
		Skript.registerEventValue(BlockFadeEvent.class, Block.class, new Getter<Block, BlockFadeEvent>() {
			@Override
			public Block get(final BlockFadeEvent e) {
				return e.getBlock();
			}
		}, -1);
		Skript.registerEventValue(BlockFadeEvent.class, Block.class, new Getter<Block, BlockFadeEvent>() {
			@Override
			public Block get(final BlockFadeEvent e) {
				return new DelayedChangeBlock(e.getBlock(), e.getNewState());
			}
		}, 0);
		Skript.registerEventValue(BlockFadeEvent.class, Block.class, new Getter<Block, BlockFadeEvent>() {
			@Override
			public Block get(final BlockFadeEvent e) {
				return new BlockStateBlock(e.getNewState());
			}
		}, 1);
		// BlockFormEvent
		Skript.registerEventValue(BlockFormEvent.class, Block.class, new Getter<Block, BlockFormEvent>() {
			@Override
			public Block get(final BlockFormEvent e) {
				if (e instanceof BlockSpreadEvent)
					return e.getBlock();
				return new BlockStateBlock(e.getNewState());
			}
		}, 0);
		Skript.registerEventValue(BlockFormEvent.class, Block.class, new Getter<Block, BlockFormEvent>() {
			@Override
			public Block get(final BlockFormEvent e) {
				return e.getBlock();
			}
		}, -1);
		// BlockDamageEvent
		Skript.registerEventValue(BlockDamageEvent.class, Player.class, new Getter<Player, BlockDamageEvent>() {
			@Override
			public Player get(final BlockDamageEvent e) {
				return e.getPlayer();
			}
		}, 0);
		// BlockBreakEvent
		Skript.registerEventValue(BlockBreakEvent.class, Player.class, new Getter<Player, BlockBreakEvent>() {
			@Override
			public Player get(final BlockBreakEvent e) {
				return e.getPlayer();
			}
		}, 0);
		Skript.registerEventValue(BlockBreakEvent.class, Block.class, new Getter<Block, BlockBreakEvent>() {
			@Override
			public Block get(final BlockBreakEvent e) {
				return e.getBlock();
			}
		}, -1);
		Skript.registerEventValue(BlockBreakEvent.class, Block.class, new Getter<Block, BlockBreakEvent>() {
			@Override
			public Block get(final BlockBreakEvent e) {
				return new DelayedChangeBlock(e.getBlock());
			}
		}, 0);
		Skript.registerEventValue(BlockBreakEvent.class, Block.class, new Getter<Block, BlockBreakEvent>() {
			@Override
			public Block get(final BlockBreakEvent e) {
				final BlockState s = e.getBlock().getState();
				s.setType(s.getType() == Material.ICE ? Material.STATIONARY_WATER : Material.AIR);
				return new BlockStateBlock(s, true);
			}
		}, 1);
		// BlockIgniteEvent
		Skript.registerEventValue(BlockIgniteEvent.class, Player.class, new Getter<Player, BlockIgniteEvent>() {
			@Override
			public Player get(final BlockIgniteEvent e) {
				return e.getPlayer();
			}
		}, 0);
		// BlockDispenseEvent
		Skript.registerEventValue(BlockDispenseEvent.class, ItemStack.class, new Getter<ItemStack, BlockDispenseEvent>() {
			@Override
			public ItemStack get(final BlockDispenseEvent e) {
				return e.getItem();
			}
		}, 0);
		// BlockPistonEvent
		Skript.registerEventValue(BlockPistonEvent.class, BlockFace.class, new Getter<BlockFace, BlockPistonEvent>() {
			@Override
			public BlockFace get(final BlockPistonEvent e) {
				return e.getDirection();
			}
		}, 0);
		// SignChangeEvent
		Skript.registerEventValue(SignChangeEvent.class, Player.class, new Getter<Player, SignChangeEvent>() {
			@Override
			public Player get(final SignChangeEvent e) {
				return e.getPlayer();
			}
		}, 0);
		
		// === EntityEvents ===
		Skript.registerEventValue(EntityEvent.class, Entity.class, new Getter<Entity, EntityEvent>() {
			@Override
			public Entity get(final EntityEvent e) {
				return e.getEntity();
			}
		}, 0, "Use 'attacker' and/or 'victim' in damage events", EntityDamageEvent.class);
		// EntityDamageEvent
		Skript.registerEventValue(EntityDamageEvent.class, DamageCause.class, new Getter<DamageCause, EntityDamageEvent>() {
			@Override
			public DamageCause get(final EntityDamageEvent e) {
				return e.getCause();
			}
		}, 0);
		Skript.registerEventValue(EntityDeathEvent.class, DamageCause.class, new Getter<DamageCause, EntityDeathEvent>() {
			@Override
			public DamageCause get(final EntityDeathEvent e) {
				return e.getEntity().getLastDamageCause().getCause();
			}
		}, 0);
		Skript.registerEventValue(EntityDamageByEntityEvent.class, Projectile.class, new Getter<Projectile, EntityDamageByEntityEvent>() {
			@Override
			public Projectile get(final EntityDamageByEntityEvent e) {
				if (e.getDamager() instanceof Projectile)
					return (Projectile) e.getDamager();
				return null;
			}
		}, 0);
		// EntityDeathEvent
		Skript.registerEventValue(EntityDeathEvent.class, Projectile.class, new Getter<Projectile, EntityDeathEvent>() {
			@Override
			public Projectile get(final EntityDeathEvent e) {
				if (e.getEntity().getLastDamageCause() instanceof EntityDamageByEntityEvent && ((EntityDamageByEntityEvent) e.getEntity().getLastDamageCause()).getDamager() instanceof Projectile)
					return (Projectile) ((EntityDamageByEntityEvent) e.getEntity().getLastDamageCause()).getDamager();
				return null;
			}
		}, 0);
		Skript.registerEventValue(EntityDeathEvent.class, DamageCause.class, new Getter<DamageCause, EntityDeathEvent>() {
			@Override
			public DamageCause get(final EntityDeathEvent e) {
				return e.getEntity().getLastDamageCause().getCause();
			}
		}, 0);
		// ProjectileHitEvent
		Skript.registerEventValue(ProjectileHitEvent.class, Entity.class, new Getter<Entity, ProjectileHitEvent>() {
			@Override
			public Entity get(final ProjectileHitEvent e) {
				return e.getEntity().getShooter();
			}
		}, 0, "Use 'projectile' and/or 'shooter' in projectile hit events", ProjectileHitEvent.class);
		Skript.registerEventValue(ProjectileHitEvent.class, Projectile.class, new Getter<Projectile, ProjectileHitEvent>() {
			@Override
			public Projectile get(final ProjectileHitEvent e) {
				return e.getEntity();
			}
		}, 0);
		
		// --- PlayerEvents ---
		Skript.registerEventValue(PlayerEvent.class, Player.class, new Getter<Player, PlayerEvent>() {
			@Override
			public Player get(final PlayerEvent e) {
				return e.getPlayer();
			}
		}, 0);//, "When a player quits he already left the server", PlayerQuitEvent.class);
		// PlayerQuitEvent
		Skript.registerEventValue(PlayerQuitEvent.class, OfflinePlayer.class, new Getter<OfflinePlayer, PlayerQuitEvent>() {
			@Override
			public OfflinePlayer get(final PlayerQuitEvent e) {
				return Bukkit.getOfflinePlayer(e.getPlayer().getName());
			}
		}, 0);
		// PlayerBedEnterEvent
		Skript.registerEventValue(PlayerBedEnterEvent.class, Block.class, new Getter<Block, PlayerBedEnterEvent>() {
			@Override
			public Block get(final PlayerBedEnterEvent e) {
				return e.getBed();
			}
		}, 0);
		// PlayerBedLeaveEvent
		Skript.registerEventValue(PlayerBedLeaveEvent.class, Block.class, new Getter<Block, PlayerBedLeaveEvent>() {
			@Override
			public Block get(final PlayerBedLeaveEvent e) {
				return e.getBed();
			}
		}, 0);
		// PlayerBucketEvents
		Skript.registerEventValue(PlayerBucketFillEvent.class, Block.class, new Getter<Block, PlayerBucketFillEvent>() {
			@Override
			public Block get(final PlayerBucketFillEvent e) {
				return e.getBlockClicked().getRelative(e.getBlockFace());
			}
		}, 0);
		Skript.registerEventValue(PlayerBucketFillEvent.class, Block.class, new Getter<Block, PlayerBucketFillEvent>() {
			@Override
			public Block get(final PlayerBucketFillEvent e) {
				final BlockState s = e.getBlockClicked().getRelative(e.getBlockFace()).getState();
				s.setTypeId(0);
				s.setRawData((byte) 0);
				return new BlockStateBlock(s, true);
			}
		}, 1);
		Skript.registerEventValue(PlayerBucketEmptyEvent.class, Block.class, new Getter<Block, PlayerBucketEmptyEvent>() {
			@Override
			public Block get(final PlayerBucketEmptyEvent e) {
				return e.getBlockClicked().getRelative(e.getBlockFace());
			}
		}, -1);
		Skript.registerEventValue(PlayerBucketEmptyEvent.class, Block.class, new Getter<Block, PlayerBucketEmptyEvent>() {
			@Override
			public Block get(final PlayerBucketEmptyEvent e) {
				final BlockState s = e.getBlockClicked().getRelative(e.getBlockFace()).getState();
				s.setType(e.getBucket() == Material.WATER_BUCKET ? Material.STATIONARY_WATER : Material.STATIONARY_LAVA);
				s.setRawData((byte) 0);
				return new BlockStateBlock(s, true);
			}
		}, 0);
		// PlayerDropItemEvent
		Skript.registerEventValue(PlayerDropItemEvent.class, Item.class, new Getter<Item, PlayerDropItemEvent>() {
			@Override
			public Item get(final PlayerDropItemEvent e) {
				return e.getItemDrop();
			}
		}, 0);
		// PlayerInteractEntityEvent
		Skript.registerEventValue(PlayerInteractEntityEvent.class, Entity.class, new Getter<Entity, PlayerInteractEntityEvent>() {
			@Override
			public Entity get(final PlayerInteractEntityEvent e) {
				return e.getRightClicked();
			}
		}, 0);
		// PlayerInteractEvent
		Skript.registerEventValue(PlayerInteractEvent.class, Block.class, new Getter<Block, PlayerInteractEvent>() {
			@Override
			public Block get(final PlayerInteractEvent e) {
				return e.getClickedBlock();
			}
		}, 0);
		// PlayerShearEntityEvent
		Skript.registerEventValue(PlayerShearEntityEvent.class, Entity.class, new Getter<Entity, PlayerShearEntityEvent>() {
			@Override
			public Entity get(final PlayerShearEntityEvent e) {
				return e.getEntity();
			}
		}, 0);
		
		// --- PaintingEvents ---
		Skript.registerEventValue(PaintingEvent.class, Painting.class, new Getter<Painting, PaintingEvent>() {
			@Override
			public Painting get(final PaintingEvent e) {
				return e.getPainting();
			}
		}, 0);
		// PaintingPlaceEvent
		Skript.registerEventValue(PaintingPlaceEvent.class, Player.class, new Getter<Player, PaintingPlaceEvent>() {
			@Override
			public Player get(final PaintingPlaceEvent e) {
				return e.getPlayer();
			}
		}, 0);
		
		// --- VehicleEvents ---
		Skript.registerEventValue(VehicleEvent.class, Vehicle.class, new Getter<Vehicle, VehicleEvent>() {
			@Override
			public Vehicle get(final VehicleEvent e) {
				return e.getVehicle();
			}
		}, 0);
		Skript.registerEventValue(VehicleExitEvent.class, LivingEntity.class, new Getter<LivingEntity, VehicleExitEvent>() {
			@Override
			public LivingEntity get(final VehicleExitEvent e) {
				return e.getExited();
			}
		}, 0);
		Skript.registerEventValue(VehicleEvent.class, Entity.class, new Getter<Entity, VehicleEvent>() {
			@Override
			public Entity get(final VehicleEvent e) {
				return e.getVehicle().getPassenger();
			}
		}, 0);
		
		// === CommandEvent ===
		Skript.registerEventValue(CommandEvent.class, CommandSender.class, new Getter<CommandSender, CommandEvent>() {
			@Override
			public CommandSender get(final CommandEvent e) {
				return e.getSender();
			}
		}, 0);
		
	}
	
}
