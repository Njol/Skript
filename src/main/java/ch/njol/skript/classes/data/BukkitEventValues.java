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
import org.bukkit.event.entity.ProjectileLaunchEvent;
import org.bukkit.event.inventory.CraftItemEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
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
import org.bukkit.event.world.StructureGrowEvent;
import org.bukkit.event.world.WorldEvent;
import org.bukkit.inventory.ItemStack;

import ch.njol.skript.classes.SerializableGetter;
import ch.njol.skript.command.CommandEvent;
import ch.njol.skript.registrations.EventValues;
import ch.njol.skript.util.BlockStateBlock;
import ch.njol.skript.util.DelayedChangeBlock;

/**
 * @author Peter Güttinger
 */
@SuppressWarnings("unchecked")
public final class BukkitEventValues {
	
	public BukkitEventValues() {}
	
	static {
		
		// === WorldEvents ===
		EventValues.registerEventValue(WorldEvent.class, World.class, new SerializableGetter<World, WorldEvent>() {
			private static final long serialVersionUID = -7443815450956171816L;
			
			@Override
			public World get(final WorldEvent e) {
				return e.getWorld();
			}
		}, 0);
		// StructureGrowEvent - a WorldEvent
		EventValues.registerEventValue(StructureGrowEvent.class, Block.class, new SerializableGetter<Block, StructureGrowEvent>() {
			private static final long serialVersionUID = -6042531897738571425L;
			
			@Override
			public Block get(final StructureGrowEvent e) {
				return e.getLocation().getBlock();
			}
		}, 0);
		EventValues.registerEventValue(StructureGrowEvent.class, Block.class, new SerializableGetter<Block, StructureGrowEvent>() {
			private static final long serialVersionUID = -7527424366055881049L;
			
			@Override
			public Block get(final StructureGrowEvent e) {
				for (final BlockState bs : e.getBlocks()) {
					if (bs.getLocation().equals(e.getLocation()))
						return new BlockStateBlock(bs);
				}
				return e.getLocation().getBlock();
			}
		}, 1);
		// WeatherEvent - not a WorldEvent (wtf)
		EventValues.registerEventValue(WeatherEvent.class, World.class, new SerializableGetter<World, WeatherEvent>() {
			private static final long serialVersionUID = 6160858209830485192L;
			
			@Override
			public World get(final WeatherEvent e) {
				return e.getWorld();
			}
		}, 0);
		
		// === BlockEvents ===
		EventValues.registerEventValue(BlockEvent.class, Block.class, new SerializableGetter<Block, BlockEvent>() {
			private static final long serialVersionUID = -7962674241469204454L;
			
			@Override
			public Block get(final BlockEvent e) {
				return e.getBlock();
			}
		}, 0);
		EventValues.registerEventValue(BlockEvent.class, World.class, new SerializableGetter<World, BlockEvent>() {
			private static final long serialVersionUID = -2449296245781224280L;
			
			@Override
			public World get(final BlockEvent e) {
				return e.getBlock().getWorld();
			}
		}, 0);
		// TODO workaround of the event's location being at the entity in block events that have an entity event value
		EventValues.registerEventValue(BlockEvent.class, Location.class, new SerializableGetter<Location, BlockEvent>() {
			private static final long serialVersionUID = -2449296245781224280L;
			
			@Override
			public Location get(final BlockEvent e) {
				return e.getBlock().getLocation().add(0.5, 0.5, 0.5);
			}
		}, 0);
		// BlockPlaceEvent
		EventValues.registerEventValue(BlockPlaceEvent.class, Player.class, new SerializableGetter<Player, BlockPlaceEvent>() {
			private static final long serialVersionUID = 8080152284527093291L;
			
			@Override
			public Player get(final BlockPlaceEvent e) {
				return e.getPlayer();
			}
		}, 0);
		EventValues.registerEventValue(BlockPlaceEvent.class, Block.class, new SerializableGetter<Block, BlockPlaceEvent>() {
			private static final long serialVersionUID = 1959266322618092289L;
			
			@Override
			public Block get(final BlockPlaceEvent e) {
				return new BlockStateBlock(e.getBlockReplacedState());
			}
		}, -1);
		// BlockFadeEvent
		EventValues.registerEventValue(BlockFadeEvent.class, Block.class, new SerializableGetter<Block, BlockFadeEvent>() {
			private static final long serialVersionUID = -4762355074663203911L;
			
			@Override
			public Block get(final BlockFadeEvent e) {
				return e.getBlock();
			}
		}, -1);
		EventValues.registerEventValue(BlockFadeEvent.class, Block.class, new SerializableGetter<Block, BlockFadeEvent>() {
			private static final long serialVersionUID = -6242353616756917178L;
			
			@Override
			public Block get(final BlockFadeEvent e) {
				return new DelayedChangeBlock(e.getBlock(), e.getNewState());
			}
		}, 0);
		EventValues.registerEventValue(BlockFadeEvent.class, Block.class, new SerializableGetter<Block, BlockFadeEvent>() {
			private static final long serialVersionUID = 6044293746987262821L;
			
			@Override
			public Block get(final BlockFadeEvent e) {
				return new BlockStateBlock(e.getNewState());
			}
		}, 1);
		// BlockFormEvent
		EventValues.registerEventValue(BlockFormEvent.class, Block.class, new SerializableGetter<Block, BlockFormEvent>() {
			private static final long serialVersionUID = 589343441385520964L;
			
			@Override
			public Block get(final BlockFormEvent e) {
				if (e instanceof BlockSpreadEvent)
					return e.getBlock();
				return new BlockStateBlock(e.getNewState());
			}
		}, 0);
		EventValues.registerEventValue(BlockFormEvent.class, Block.class, new SerializableGetter<Block, BlockFormEvent>() {
			private static final long serialVersionUID = 8523480782337243671L;
			
			@Override
			public Block get(final BlockFormEvent e) {
				return e.getBlock();
			}
		}, -1);
		// BlockDamageEvent
		EventValues.registerEventValue(BlockDamageEvent.class, Player.class, new SerializableGetter<Player, BlockDamageEvent>() {
			private static final long serialVersionUID = 3901071726237090025L;
			
			@Override
			public Player get(final BlockDamageEvent e) {
				return e.getPlayer();
			}
		}, 0);
		// BlockBreakEvent
		EventValues.registerEventValue(BlockBreakEvent.class, Player.class, new SerializableGetter<Player, BlockBreakEvent>() {
			private static final long serialVersionUID = -6570978836128673081L;
			
			@Override
			public Player get(final BlockBreakEvent e) {
				return e.getPlayer();
			}
		}, 0);
		EventValues.registerEventValue(BlockBreakEvent.class, Block.class, new SerializableGetter<Block, BlockBreakEvent>() {
			private static final long serialVersionUID = 6069643162531707845L;
			
			@Override
			public Block get(final BlockBreakEvent e) {
				return e.getBlock();
			}
		}, -1);
		EventValues.registerEventValue(BlockBreakEvent.class, Block.class, new SerializableGetter<Block, BlockBreakEvent>() {
			private static final long serialVersionUID = 5644830980508061927L;
			
			@Override
			public Block get(final BlockBreakEvent e) {
				return new DelayedChangeBlock(e.getBlock());
			}
		}, 0);
		EventValues.registerEventValue(BlockBreakEvent.class, Block.class, new SerializableGetter<Block, BlockBreakEvent>() {
			private static final long serialVersionUID = -1043031758894282038L;
			
			@Override
			public Block get(final BlockBreakEvent e) {
				final BlockState s = e.getBlock().getState();
				s.setType(s.getType() == Material.ICE ? Material.STATIONARY_WATER : Material.AIR);
				return new BlockStateBlock(s, true);
			}
		}, 1);
		// BlockIgniteEvent
		EventValues.registerEventValue(BlockIgniteEvent.class, Player.class, new SerializableGetter<Player, BlockIgniteEvent>() {
			private static final long serialVersionUID = 5545058251893116240L;
			
			@Override
			public Player get(final BlockIgniteEvent e) {
				return e.getPlayer();
			}
		}, 0);
		// BlockDispenseEvent
		EventValues.registerEventValue(BlockDispenseEvent.class, ItemStack.class, new SerializableGetter<ItemStack, BlockDispenseEvent>() {
			private static final long serialVersionUID = -4955344643647561369L;
			
			@Override
			public ItemStack get(final BlockDispenseEvent e) {
				return e.getItem();
			}
		}, 0);
		// BlockPistonEvent
		EventValues.registerEventValue(BlockPistonEvent.class, BlockFace.class, new SerializableGetter<BlockFace, BlockPistonEvent>() {
			private static final long serialVersionUID = 7011074064679528577L;
			
			@Override
			public BlockFace get(final BlockPistonEvent e) {
				return e.getDirection();
			}
		}, 0);
		// SignChangeEvent
		EventValues.registerEventValue(SignChangeEvent.class, Player.class, new SerializableGetter<Player, SignChangeEvent>() {
			private static final long serialVersionUID = -125995721827677072L;
			
			@Override
			public Player get(final SignChangeEvent e) {
				return e.getPlayer();
			}
		}, 0);
		
		// === EntityEvents ===
		EventValues.registerEventValue(EntityEvent.class, Entity.class, new SerializableGetter<Entity, EntityEvent>() {
			private static final long serialVersionUID = -961753091605382468L;
			
			@Override
			public Entity get(final EntityEvent e) {
				return e.getEntity();
			}
		}, 0, "Use 'attacker' and/or 'victim' in damage events", EntityDamageEvent.class);
		EventValues.registerEventValue(EntityEvent.class, World.class, new SerializableGetter<World, EntityEvent>() {
			private static final long serialVersionUID = -961753091605382468L;
			
			@Override
			public World get(final EntityEvent e) {
				return e.getEntity().getWorld();
			}
		}, 0);
		// EntityDamageEvent
		EventValues.registerEventValue(EntityDamageEvent.class, DamageCause.class, new SerializableGetter<DamageCause, EntityDamageEvent>() {
			private static final long serialVersionUID = 1767831289780790690L;
			
			@Override
			public DamageCause get(final EntityDamageEvent e) {
				return e.getCause();
			}
		}, 0);
		EventValues.registerEventValue(EntityDeathEvent.class, DamageCause.class, new SerializableGetter<DamageCause, EntityDeathEvent>() {
			private static final long serialVersionUID = 4556489367122319653L;
			
			@Override
			public DamageCause get(final EntityDeathEvent e) {
				return e.getEntity().getLastDamageCause().getCause();
			}
		}, 0);
		EventValues.registerEventValue(EntityDamageByEntityEvent.class, Projectile.class, new SerializableGetter<Projectile, EntityDamageByEntityEvent>() {
			private static final long serialVersionUID = 6140553353433330650L;
			
			@Override
			public Projectile get(final EntityDamageByEntityEvent e) {
				if (e.getDamager() instanceof Projectile)
					return (Projectile) e.getDamager();
				return null;
			}
		}, 0);
		// EntityDeathEvent
		EventValues.registerEventValue(EntityDeathEvent.class, Projectile.class, new SerializableGetter<Projectile, EntityDeathEvent>() {
			private static final long serialVersionUID = -6305699664910388201L;
			
			@Override
			public Projectile get(final EntityDeathEvent e) {
				if (e.getEntity().getLastDamageCause() instanceof EntityDamageByEntityEvent && ((EntityDamageByEntityEvent) e.getEntity().getLastDamageCause()).getDamager() instanceof Projectile)
					return (Projectile) ((EntityDamageByEntityEvent) e.getEntity().getLastDamageCause()).getDamager();
				return null;
			}
		}, 0);
		EventValues.registerEventValue(EntityDeathEvent.class, DamageCause.class, new SerializableGetter<DamageCause, EntityDeathEvent>() {
			private static final long serialVersionUID = 880569954515534953L;
			
			@Override
			public DamageCause get(final EntityDeathEvent e) {
				return e.getEntity().getLastDamageCause().getCause();
			}
		}, 0);
		// ProjectileHitEvent
		EventValues.registerEventValue(ProjectileHitEvent.class, Entity.class, new SerializableGetter<Entity, ProjectileHitEvent>() {
			private static final long serialVersionUID = 6031190016320160872L;
			
			@Override
			public Entity get(final ProjectileHitEvent e) {
				assert false;
				return e.getEntity().getShooter();
			}
		}, 0, "Use 'projectile' and/or 'shooter' in projectile hit events", ProjectileHitEvent.class);
		EventValues.registerEventValue(ProjectileHitEvent.class, Projectile.class, new SerializableGetter<Projectile, ProjectileHitEvent>() {
			private static final long serialVersionUID = 8468025566956055232L;
			
			@Override
			public Projectile get(final ProjectileHitEvent e) {
				return e.getEntity();
			}
		}, 0);
		// ProjectileLaunchEvent
		EventValues.registerEventValue(ProjectileLaunchEvent.class, Entity.class, new SerializableGetter<Entity, ProjectileLaunchEvent>() {
			private static final long serialVersionUID = -3055355091280502763L;
			
			@Override
			public Entity get(final ProjectileLaunchEvent e) {
				assert false;
				return e.getEntity().getShooter();
			}
		}, 0, "Use 'projectile' and/or 'shooter' in shoot events", ProjectileLaunchEvent.class);
		EventValues.registerEventValue(ProjectileLaunchEvent.class, Projectile.class, new SerializableGetter<Projectile, ProjectileLaunchEvent>() {
			private static final long serialVersionUID = -309517424914583751L;
			
			@Override
			public Projectile get(final ProjectileLaunchEvent e) {
				return e.getEntity();
			}
		}, 0);
		
		// --- PlayerEvents ---
		EventValues.registerEventValue(PlayerEvent.class, Player.class, new SerializableGetter<Player, PlayerEvent>() {
			private static final long serialVersionUID = -1510591548534443089L;
			
			@Override
			public Player get(final PlayerEvent e) {
				return e.getPlayer();
			}
		}, 0);
		EventValues.registerEventValue(PlayerEvent.class, World.class, new SerializableGetter<World, PlayerEvent>() {
			private static final long serialVersionUID = -1510591548534443089L;
			
			@Override
			public World get(final PlayerEvent e) {
				return e.getPlayer().getWorld();
			}
		}, 0);
		// PlayerQuitEvent
		EventValues.registerEventValue(PlayerQuitEvent.class, OfflinePlayer.class, new SerializableGetter<OfflinePlayer, PlayerQuitEvent>() {
			private static final long serialVersionUID = -2592710984786565651L;
			
			@Override
			public OfflinePlayer get(final PlayerQuitEvent e) {
				return Bukkit.getOfflinePlayer(e.getPlayer().getName());
			}
		}, 0);
		// PlayerBedEnterEvent
		EventValues.registerEventValue(PlayerBedEnterEvent.class, Block.class, new SerializableGetter<Block, PlayerBedEnterEvent>() {
			private static final long serialVersionUID = 7467908347255715708L;
			
			@Override
			public Block get(final PlayerBedEnterEvent e) {
				return e.getBed();
			}
		}, 0);
		// PlayerBedLeaveEvent
		EventValues.registerEventValue(PlayerBedLeaveEvent.class, Block.class, new SerializableGetter<Block, PlayerBedLeaveEvent>() {
			private static final long serialVersionUID = -2628198873080950227L;
			
			@Override
			public Block get(final PlayerBedLeaveEvent e) {
				return e.getBed();
			}
		}, 0);
		// PlayerBucketEvents
		EventValues.registerEventValue(PlayerBucketFillEvent.class, Block.class, new SerializableGetter<Block, PlayerBucketFillEvent>() {
			private static final long serialVersionUID = 7865528508725390436L;
			
			@Override
			public Block get(final PlayerBucketFillEvent e) {
				return e.getBlockClicked().getRelative(e.getBlockFace());
			}
		}, 0);
		EventValues.registerEventValue(PlayerBucketFillEvent.class, Block.class, new SerializableGetter<Block, PlayerBucketFillEvent>() {
			private static final long serialVersionUID = -3237118253069313854L;
			
			@Override
			public Block get(final PlayerBucketFillEvent e) {
				final BlockState s = e.getBlockClicked().getRelative(e.getBlockFace()).getState();
				s.setTypeId(0);
				s.setRawData((byte) 0);
				return new BlockStateBlock(s, true);
			}
		}, 1);
		EventValues.registerEventValue(PlayerBucketEmptyEvent.class, Block.class, new SerializableGetter<Block, PlayerBucketEmptyEvent>() {
			private static final long serialVersionUID = 5317370132880416601L;
			
			@Override
			public Block get(final PlayerBucketEmptyEvent e) {
				return e.getBlockClicked().getRelative(e.getBlockFace());
			}
		}, -1);
		EventValues.registerEventValue(PlayerBucketEmptyEvent.class, Block.class, new SerializableGetter<Block, PlayerBucketEmptyEvent>() {
			private static final long serialVersionUID = -694384347594778736L;
			
			@Override
			public Block get(final PlayerBucketEmptyEvent e) {
				final BlockState s = e.getBlockClicked().getRelative(e.getBlockFace()).getState();
				s.setType(e.getBucket() == Material.WATER_BUCKET ? Material.STATIONARY_WATER : Material.STATIONARY_LAVA);
				s.setRawData((byte) 0);
				return new BlockStateBlock(s, true);
			}
		}, 0);
		// PlayerDropItemEvent
		EventValues.registerEventValue(PlayerDropItemEvent.class, Item.class, new SerializableGetter<Item, PlayerDropItemEvent>() {
			private static final long serialVersionUID = 2133249280752038995L;
			
			@Override
			public Item get(final PlayerDropItemEvent e) {
				return e.getItemDrop();
			}
		}, 0);
		EventValues.registerEventValue(PlayerDropItemEvent.class, ItemStack.class, new SerializableGetter<ItemStack, PlayerDropItemEvent>() {
			private static final long serialVersionUID = 2133249280752038995L;
			
			@Override
			public ItemStack get(final PlayerDropItemEvent e) {
				return e.getItemDrop().getItemStack();
			}
		}, 0);
		// PlayerInteractEntityEvent
		EventValues.registerEventValue(PlayerInteractEntityEvent.class, Entity.class, new SerializableGetter<Entity, PlayerInteractEntityEvent>() {
			private static final long serialVersionUID = -3010489949520696456L;
			
			@Override
			public Entity get(final PlayerInteractEntityEvent e) {
				return e.getRightClicked();
			}
		}, 0);
		// PlayerInteractEvent
		EventValues.registerEventValue(PlayerInteractEvent.class, Block.class, new SerializableGetter<Block, PlayerInteractEvent>() {
			private static final long serialVersionUID = -1469349022423674678L;
			
			@Override
			public Block get(final PlayerInteractEvent e) {
				return e.getClickedBlock();
			}
		}, 0);
		// PlayerShearEntityEvent
		EventValues.registerEventValue(PlayerShearEntityEvent.class, Entity.class, new SerializableGetter<Entity, PlayerShearEntityEvent>() {
			private static final long serialVersionUID = 3045125371768647021L;
			
			@Override
			public Entity get(final PlayerShearEntityEvent e) {
				return e.getEntity();
			}
		}, 0);
		
		// --- PaintingEvents ---
		EventValues.registerEventValue(PaintingEvent.class, Painting.class, new SerializableGetter<Painting, PaintingEvent>() {
			private static final long serialVersionUID = -5565730270163060739L;
			
			@Override
			public Painting get(final PaintingEvent e) {
				return e.getPainting();
			}
		}, 0);
		EventValues.registerEventValue(PaintingEvent.class, World.class, new SerializableGetter<World, PaintingEvent>() {
			private static final long serialVersionUID = -5565730270163060739L;
			
			@Override
			public World get(final PaintingEvent e) {
				return e.getPainting().getWorld();
			}
		}, 0);
		// PaintingPlaceEvent
		EventValues.registerEventValue(PaintingPlaceEvent.class, Player.class, new SerializableGetter<Player, PaintingPlaceEvent>() {
			private static final long serialVersionUID = 3797356301163722964L;
			
			@Override
			public Player get(final PaintingPlaceEvent e) {
				return e.getPlayer();
			}
		}, 0);
		
		// --- VehicleEvents ---
		EventValues.registerEventValue(VehicleEvent.class, Vehicle.class, new SerializableGetter<Vehicle, VehicleEvent>() {
			private static final long serialVersionUID = 4293012179463300253L;
			
			@Override
			public Vehicle get(final VehicleEvent e) {
				return e.getVehicle();
			}
		}, 0);
		EventValues.registerEventValue(VehicleEvent.class, World.class, new SerializableGetter<World, VehicleEvent>() {
			private static final long serialVersionUID = 4293012179463300253L;
			
			@Override
			public World get(final VehicleEvent e) {
				return e.getVehicle().getWorld();
			}
		}, 0);
		EventValues.registerEventValue(VehicleExitEvent.class, LivingEntity.class, new SerializableGetter<LivingEntity, VehicleExitEvent>() {
			private static final long serialVersionUID = -381266108283565468L;
			
			@Override
			public LivingEntity get(final VehicleExitEvent e) {
				return e.getExited();
			}
		}, 0);
		EventValues.registerEventValue(VehicleEvent.class, Entity.class, new SerializableGetter<Entity, VehicleEvent>() {
			private static final long serialVersionUID = 631172477985022319L;
			
			@Override
			public Entity get(final VehicleEvent e) {
				return e.getVehicle().getPassenger();
			}
		}, 0);
		
		// === CommandEvents ===
		EventValues.registerEventValue(CommandEvent.class, CommandSender.class, new SerializableGetter<CommandSender, CommandEvent>() {
			private static final long serialVersionUID = 2365437364677999399L;
			
			@Override
			public CommandSender get(final CommandEvent e) {
				return e.getSender();
			}
		}, 0);
		EventValues.registerEventValue(CommandEvent.class, World.class, new SerializableGetter<World, CommandEvent>() {
			private static final long serialVersionUID = 2365437364677999399L;
			
			@Override
			public World get(final CommandEvent e) {
				return e.getSender() instanceof Player ? ((Player) e.getSender()).getWorld() : null;
			}
		}, 0);
		
		// === InventoryEvents ===
		EventValues.registerEventValue(InventoryClickEvent.class, Player.class, new SerializableGetter<Player, InventoryClickEvent>() {
			private static final long serialVersionUID = 2365437364677999399L;
			
			@Override
			public Player get(final InventoryClickEvent e) {
				return e.getWhoClicked() instanceof Player ? (Player) e.getWhoClicked() : null;
			}
		}, 0);
		EventValues.registerEventValue(CraftItemEvent.class, ItemStack.class, new SerializableGetter<ItemStack, CraftItemEvent>() {
			private static final long serialVersionUID = 2365437364677999399L;
			
			@Override
			public ItemStack get(final CraftItemEvent e) {
				return e.getRecipe().getResult();
			}
		}, 0);
		
	}
	
}
