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

import org.bukkit.event.block.BlockCanBuildEvent;
import org.bukkit.event.block.BlockDamageEvent;
import org.bukkit.event.block.BlockFromToEvent;
import org.bukkit.event.block.BlockIgniteEvent;
import org.bukkit.event.block.BlockPhysicsEvent;
import org.bukkit.event.block.BlockPistonExtendEvent;
import org.bukkit.event.block.BlockPistonRetractEvent;
import org.bukkit.event.block.BlockRedstoneEvent;
import org.bukkit.event.block.BlockSpreadEvent;
import org.bukkit.event.block.LeavesDecayEvent;
import org.bukkit.event.block.SignChangeEvent;
import org.bukkit.event.entity.CreeperPowerEvent;
import org.bukkit.event.entity.EntityBreakDoorEvent;
import org.bukkit.event.entity.EntityCombustEvent;
import org.bukkit.event.entity.EntityExplodeEvent;
import org.bukkit.event.entity.EntityPortalEnterEvent;
import org.bukkit.event.entity.EntityRegainHealthEvent;
import org.bukkit.event.entity.EntityTameEvent;
import org.bukkit.event.entity.EntityTargetEvent;
import org.bukkit.event.entity.ExplosionPrimeEvent;
import org.bukkit.event.entity.FoodLevelChangeEvent;
import org.bukkit.event.entity.PigZapEvent;
import org.bukkit.event.entity.ProjectileHitEvent;
import org.bukkit.event.entity.ProjectileLaunchEvent;
import org.bukkit.event.inventory.FurnaceBurnEvent;
import org.bukkit.event.inventory.FurnaceSmeltEvent;
import org.bukkit.event.player.PlayerBedEnterEvent;
import org.bukkit.event.player.PlayerBedLeaveEvent;
import org.bukkit.event.player.PlayerBucketEmptyEvent;
import org.bukkit.event.player.PlayerBucketFillEvent;
import org.bukkit.event.player.PlayerEggThrowEvent;
import org.bukkit.event.player.PlayerFishEvent;
import org.bukkit.event.player.PlayerItemHeldEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerKickEvent;
import org.bukkit.event.player.PlayerLevelChangeEvent;
import org.bukkit.event.player.PlayerPortalEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.player.PlayerRespawnEvent;
import org.bukkit.event.player.PlayerTeleportEvent;
import org.bukkit.event.player.PlayerToggleSneakEvent;
import org.bukkit.event.player.PlayerToggleSprintEvent;
import org.bukkit.event.vehicle.VehicleCreateEvent;
import org.bukkit.event.vehicle.VehicleDamageEvent;
import org.bukkit.event.vehicle.VehicleDestroyEvent;
import org.bukkit.event.vehicle.VehicleEnterEvent;
import org.bukkit.event.vehicle.VehicleExitEvent;
import org.bukkit.event.weather.LightningStrikeEvent;
import org.bukkit.event.world.ChunkLoadEvent;
import org.bukkit.event.world.ChunkPopulateEvent;
import org.bukkit.event.world.ChunkUnloadEvent;
import org.bukkit.event.world.PortalCreateEvent;
import org.bukkit.event.world.SpawnChangeEvent;
import org.bukkit.event.world.WorldInitEvent;
import org.bukkit.event.world.WorldLoadEvent;
import org.bukkit.event.world.WorldSaveEvent;
import org.bukkit.event.world.WorldUnloadEvent;

import ch.njol.skript.Skript;
import ch.njol.skript.lang.util.SimpleEvent;

/**
 * @author Peter Güttinger
 */
@SuppressWarnings("unchecked")
public class SimpleEvents {
	
	static {
		
		Skript.registerEvent(SimpleEvent.class, BlockCanBuildEvent.class, "can build check");
		Skript.registerEvent(SimpleEvent.class, BlockDamageEvent.class, "block damage");
		Skript.registerEvent(SimpleEvent.class, BlockFromToEvent.class, "flow[ing]");
		Skript.registerEvent(SimpleEvent.class, BlockIgniteEvent.class, "ignit(e|ion)");
		Skript.registerEvent(SimpleEvent.class, BlockPhysicsEvent.class, "[block] physics");
		Skript.registerEvent(SimpleEvent.class, BlockPistonExtendEvent.class, "piston extend");
		Skript.registerEvent(SimpleEvent.class, BlockPistonRetractEvent.class, "piston retract");
		Skript.registerEvent(SimpleEvent.class, BlockRedstoneEvent.class, "redstone");
		Skript.registerEvent(SimpleEvent.class, BlockSpreadEvent.class, "spread[ing]");
		Skript.registerEvent(SimpleEvent.class, ChunkLoadEvent.class, "chunk load");
		Skript.registerEvent(SimpleEvent.class, ChunkPopulateEvent.class, "chunk populate");
		Skript.registerEvent(SimpleEvent.class, ChunkUnloadEvent.class, "chunk unload");
		Skript.registerEvent(SimpleEvent.class, CreeperPowerEvent.class, "creeper power");
		Skript.registerEvent(SimpleEvent.class, EntityBreakDoorEvent.class, "zombie break[ing] [a] [wood[en]] door");
		Skript.registerEvent(SimpleEvent.class, EntityCombustEvent.class, "combust[ing]");
		Skript.registerEvent(SimpleEvent.class, EntityExplodeEvent.class, "explo(d(e|ing)|sion)");
//		Skript.registerEvent(SimpleEvent.class, EntityInteractEvent.class, "interact");// = entity interacts with block, e.g. endermen?; player -> PlayerInteractEvent // likely tripwires, pressure plates, etc.
		Skript.registerEvent(SimpleEvent.class, EntityPortalEnterEvent.class, "portal enter", "entering [a] portal");
		Skript.registerEvent(SimpleEvent.class, EntityRegainHealthEvent.class, "heal[ing]");
		Skript.registerEvent(SimpleEvent.class, EntityTameEvent.class, "tame");
		Skript.registerEvent(SimpleEvent.class, EntityTargetEvent.class, "[entity] target");
		Skript.registerEvent(SimpleEvent.class, ExplosionPrimeEvent.class, "explosion prime");
		Skript.registerEvent(SimpleEvent.class, FoodLevelChangeEvent.class, "food (level|meter|bar) change");
		Skript.registerEvent(SimpleEvent.class, FurnaceBurnEvent.class, "fuel burn");
		Skript.registerEvent(SimpleEvent.class, FurnaceSmeltEvent.class, "[ore] smelt[ing]", "smelt[ing] of ore");//"smelt[ing] of %itemtype%"
		Skript.registerEvent(SimpleEvent.class, LeavesDecayEvent.class, "leaves decay");
		Skript.registerEvent(SimpleEvent.class, LightningStrikeEvent.class, "lightning strike");
		Skript.registerEvent(SimpleEvent.class, PigZapEvent.class, "pig[ ]zap");
		Skript.registerEvent(SimpleEvent.class, PlayerBedEnterEvent.class, "bed enter[ing]", "entering bed");
		Skript.registerEvent(SimpleEvent.class, PlayerBedLeaveEvent.class, "bed leave", "leaving bed");
		Skript.registerEvent(SimpleEvent.class, PlayerBucketEmptyEvent.class, "bucket empty");//, "emptying bucket [of %itemtype%]", "emptying %itemtype% bucket"); -> place of water/lava
		Skript.registerEvent(SimpleEvent.class, PlayerBucketFillEvent.class, "bucket fill");//, "filling bucket [(with|of) %itemtype%]", "filling %itemtype% bucket");
		Skript.registerEvent(SimpleEvent.class, PlayerEggThrowEvent.class, "throw[ing] of [an] egg");
		Skript.registerEvent(SimpleEvent.class, PlayerFishEvent.class, "fish[ing]");
		Skript.registerEvent(SimpleEvent.class, PlayerItemHeldEvent.class, "item held change");
		Skript.registerEvent(SimpleEvent.class, PlayerJoinEvent.class, "(login|logging in|join[ing]|connect[ing])");
		Skript.registerEvent(SimpleEvent.class, PlayerKickEvent.class, "(kick|being kicked)");
		Skript.registerEvent(SimpleEvent.class, PlayerLevelChangeEvent.class, "level [up]");
		Skript.registerEvent(SimpleEvent.class, PlayerPortalEvent.class, "portal");
		Skript.registerEvent(SimpleEvent.class, PlayerQuitEvent.class, "(quit[ting]|disconnect[ing]|log[ ]out|logging out)");
		Skript.registerEvent(SimpleEvent.class, PlayerRespawnEvent.class, "respawn[ing]");
		Skript.registerEvent(SimpleEvent.class, Skript.array(PlayerPortalEvent.class, PlayerTeleportEvent.class), "teleport[ing]");
		Skript.registerEvent(SimpleEvent.class, PlayerToggleSneakEvent.class, "toggl(e|ing) sneak", "sneak toggle");
		Skript.registerEvent(SimpleEvent.class, PlayerToggleSprintEvent.class, "sprint toggle", "toggl(e|ing) sprint");
		Skript.registerEvent(SimpleEvent.class, PortalCreateEvent.class, "portal create");
		Skript.registerEvent(SimpleEvent.class, ProjectileHitEvent.class, "projectile hit");
		Skript.registerEvent(SimpleEvent.class, ProjectileLaunchEvent.class, "shoot");
		Skript.registerEvent(SimpleEvent.class, SignChangeEvent.class, "sign change");
		Skript.registerEvent(SimpleEvent.class, SpawnChangeEvent.class, "spawn change");
		Skript.registerEvent(SimpleEvent.class, VehicleCreateEvent.class, "vehicle create", "creat(e|ing) [a] vehicle");
		Skript.registerEvent(SimpleEvent.class, VehicleDamageEvent.class, "vehicle damage", "damag(e|ing) [a] vehicle");
		Skript.registerEvent(SimpleEvent.class, VehicleDestroyEvent.class, "vehicle destroy", "destr(oy[ing]|uction of) [a] vehicle");
		Skript.registerEvent(SimpleEvent.class, VehicleEnterEvent.class, "vehicle enter", "enter[ing] [a] vehicle");
		Skript.registerEvent(SimpleEvent.class, VehicleExitEvent.class, "vehicle exit", "exit[ing] [a] vehicle");
		Skript.registerEvent(SimpleEvent.class, WorldInitEvent.class, "world init");
		Skript.registerEvent(SimpleEvent.class, WorldLoadEvent.class, "world load[ing]");
		Skript.registerEvent(SimpleEvent.class, WorldSaveEvent.class, "world sav(e|ing)");
		Skript.registerEvent(SimpleEvent.class, WorldUnloadEvent.class, "world unload[ing]");
		
	}
	
}
