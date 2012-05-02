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

import org.bukkit.event.Event;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockCanBuildEvent;
import org.bukkit.event.block.BlockDamageEvent;
import org.bukkit.event.block.BlockFadeEvent;
import org.bukkit.event.block.BlockFormEvent;
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
import org.bukkit.event.entity.EntityCombustEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityExplodeEvent;
import org.bukkit.event.entity.EntityInteractEvent;
import org.bukkit.event.entity.EntityPortalEnterEvent;
import org.bukkit.event.entity.EntityRegainHealthEvent;
import org.bukkit.event.entity.EntityTameEvent;
import org.bukkit.event.entity.EntityTargetEvent;
import org.bukkit.event.entity.ExplosionPrimeEvent;
import org.bukkit.event.entity.PigZapEvent;
import org.bukkit.event.entity.ProjectileHitEvent;
import org.bukkit.event.inventory.FurnaceBurnEvent;
import org.bukkit.event.inventory.FurnaceSmeltEvent;
import org.bukkit.event.player.PlayerBedEnterEvent;
import org.bukkit.event.player.PlayerBedLeaveEvent;
import org.bukkit.event.player.PlayerBucketEmptyEvent;
import org.bukkit.event.player.PlayerBucketFillEvent;
import org.bukkit.event.player.PlayerChatEvent;
import org.bukkit.event.player.PlayerFishEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerItemHeldEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerKickEvent;
import org.bukkit.event.player.PlayerLoginEvent;
import org.bukkit.event.player.PlayerPickupItemEvent;
import org.bukkit.event.player.PlayerPortalEvent;
import org.bukkit.event.player.PlayerPreLoginEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.player.PlayerRespawnEvent;
import org.bukkit.event.player.PlayerTeleportEvent;
import org.bukkit.event.player.PlayerToggleSneakEvent;
import org.bukkit.event.player.PlayerToggleSprintEvent;
import org.bukkit.event.player.PlayerVelocityEvent;
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
import ch.njol.skript.api.SimpleEvent;
import ch.njol.skript.api.intern.SkriptAPIException;
import ch.njol.skript.conditions.CondCanHold;
import ch.njol.skript.conditions.CondChance;
import ch.njol.skript.conditions.CondInventoryContains;
import ch.njol.skript.conditions.CondIs;
import ch.njol.skript.conditions.CondItemInHand;
import ch.njol.skript.conditions.CondPermission;
import ch.njol.skript.conditions.CondWeather;
import ch.njol.skript.effects.EffAdd;
import ch.njol.skript.effects.EffBroadcast;
import ch.njol.skript.effects.EffCancelEvent;
import ch.njol.skript.effects.EffClear;
import ch.njol.skript.effects.EffCommand;
import ch.njol.skript.effects.EffDrop;
import ch.njol.skript.effects.EffEquip;
import ch.njol.skript.effects.EffExec;
import ch.njol.skript.effects.EffExit;
import ch.njol.skript.effects.EffExplosion;
import ch.njol.skript.effects.EffHealth;
import ch.njol.skript.effects.EffKill;
import ch.njol.skript.effects.EffMessage;
import ch.njol.skript.effects.EffRemove;
import ch.njol.skript.effects.EffSet;
import ch.njol.skript.effects.EffSpawn;
import ch.njol.skript.effects.EffTeleport;
import ch.njol.skript.effects.EffTree;
import ch.njol.skript.events.EvtBlock;
import ch.njol.skript.events.EvtEntity;
import ch.njol.skript.events.EvtEntityBlockChange;
import ch.njol.skript.events.EvtGameMode;
import ch.njol.skript.events.EvtItem;
import ch.njol.skript.events.EvtPeriodical;
import ch.njol.skript.events.EvtRightclick;
import ch.njol.skript.events.EvtWeatherChange;
import ch.njol.skript.loops.LoopVarArguments;
import ch.njol.skript.loops.LoopVarBlockLine;
import ch.njol.skript.loops.LoopVarBlockSphere;
import ch.njol.skript.loops.LoopVarIdsOf;
import ch.njol.skript.loops.LoopVarItem;
import ch.njol.skript.loops.LoopVarPlayer;
import ch.njol.skript.loops.LoopVarWorld;
import ch.njol.skript.variables.VarArgument;
import ch.njol.skript.variables.VarArmorSlot;
import ch.njol.skript.variables.VarAttacked;
import ch.njol.skript.variables.VarAttacker;
import ch.njol.skript.variables.VarBlock;
import ch.njol.skript.variables.VarCreature;
import ch.njol.skript.variables.VarDrops;
import ch.njol.skript.variables.VarEntity;
import ch.njol.skript.variables.VarEventCancelled;
import ch.njol.skript.variables.VarEventVariable;
import ch.njol.skript.variables.VarFoodLevel;
import ch.njol.skript.variables.VarFurnaceSlot;
import ch.njol.skript.variables.VarGameMode;
import ch.njol.skript.variables.VarHealth;
import ch.njol.skript.variables.VarIdOf;
import ch.njol.skript.variables.VarInventory;
import ch.njol.skript.variables.VarLocation;
import ch.njol.skript.variables.VarLoopValue;
import ch.njol.skript.variables.VarPlayer;
import ch.njol.skript.variables.VarProjectile;
import ch.njol.skript.variables.VarRandom;
import ch.njol.skript.variables.VarTarget;
import ch.njol.skript.variables.VarTargetedBlock;
import ch.njol.skript.variables.VarTime;
import ch.njol.skript.variables.VarTool;
import ch.njol.skript.variables.VarWeather;
import ch.njol.skript.variables.VarWorld;

/**
 * @author Peter Güttinger
 * 
 */
@SuppressWarnings("unchecked")
public class SkriptTriggerItems {
	
	static {
		
		final Class<?>[] items = {
				
				CondCanHold.class,
				CondChance.class,
				CondPermission.class,        // has permission
				CondInventoryContains.class, // has
				CondItemInHand.class, // * is holding
				CondIs.class,         // * is *
				CondWeather.class,    // is *
				
				EffAdd.class,
				EffBroadcast.class,
				EffCancelEvent.class,
				EffClear.class,
				EffCommand.class,
				EffDrop.class,
				EffEquip.class,
				EffExec.class,
				EffExit.class,
				EffExplosion.class,
				// EffFertilize.class,
				EffHealth.class,
				EffKill.class,
				EffMessage.class,
				EffRemove.class,
				EffSet.class,
				EffSpawn.class,
				EffTeleport.class,
				EffTree.class,
				
				VarArgument.class,
				VarArmorSlot.class,
				VarAttacked.class,
				VarAttacker.class,
				// VarBetween.class,
				VarBlock.class,
				VarCreature.class,
				VarDrops.class,
				VarEntity.class,
				VarEventCancelled.class,
				VarEventVariable.class,
				VarFoodLevel.class,
				VarFurnaceSlot.class,
				VarGameMode.class,
				VarHealth.class,
				VarIdOf.class,
				VarInventory.class,
				VarLoopValue.class,
				VarLocation.class,
				VarPlayer.class,
				VarProjectile.class,
				VarRandom.class,
				VarTargetedBlock.class, // targeted block
				VarTarget.class,        // targeted *
				VarTime.class,
				VarTool.class,
				VarWeather.class,
				VarWorld.class,
				
				LoopVarArguments.class,
				LoopVarBlockLine.class,
				LoopVarBlockSphere.class,
				LoopVarIdsOf.class,
				LoopVarItem.class,
				LoopVarPlayer.class,
				LoopVarWorld.class,
				
				EvtBlock.class,
				EvtEntity.class,
				EvtEntityBlockChange.class,
				EvtGameMode.class,
				EvtItem.class,
				EvtPeriodical.class,
				EvtRightclick.class,
				EvtWeatherChange.class,
		
		};
		for (final Class<?> c : items) {
			try {
				c.newInstance();
			} catch (final InstantiationException e) {
				SkriptAPIException.instantiationException(c, e);
			} catch (final IllegalAccessException e) {
				SkriptAPIException.inaccessibleConstructor(c, e);
			}
		}
		
		// Skript.addVariable(VarBetween.class, Interval.class, false, "between %object% and %object%");// TODO think about this
		
		Skript.addEvent(SimpleEvent.class, BlockCanBuildEvent.class, "can build check");
		Skript.addEvent(SimpleEvent.class, BlockDamageEvent.class, "block damage");
		Skript.addEvent(SimpleEvent.class, BlockFadeEvent.class, "fad(e|ing)");
		Skript.addEvent(SimpleEvent.class, BlockFormEvent.class, "form(ing)?");
		Skript.addEvent(SimpleEvent.class, BlockFromToEvent.class, "flow(ing)?");
		Skript.addEvent(SimpleEvent.class, BlockIgniteEvent.class, "(ignite|ignition)");
		Skript.addEvent(SimpleEvent.class, BlockPhysicsEvent.class, "physics");
		Skript.addEvent(SimpleEvent.class, BlockPistonExtendEvent.class, "piston extend");
		Skript.addEvent(SimpleEvent.class, BlockPistonRetractEvent.class, "piston retract");
		Skript.addEvent(SimpleEvent.class, BlockSpreadEvent.class, "spread(ing)?");
		Skript.addEvent(SimpleEvent.class, ChunkLoadEvent.class, "chunk load");
		Skript.addEvent(SimpleEvent.class, ChunkPopulateEvent.class, "chunk populate");
		Skript.addEvent(SimpleEvent.class, ChunkUnloadEvent.class, "chunk unload");
		Skript.addEvent(SimpleEvent.class, CreeperPowerEvent.class, "creeper power");
		Skript.addEvent(SimpleEvent.class, EntityCombustEvent.class, "combust(ing)?");
		Skript.addEvent(SimpleEvent.class, Skript.array(EntityDamageEvent.class, VehicleDamageEvent.class), "damag(e|ing)");
		Skript.addEvent(SimpleEvent.class, EntityExplodeEvent.class, "(explode|explosion)");
		Skript.addEvent(SimpleEvent.class, EntityInteractEvent.class, "interact");// = entity interacts with block, e.g. endermen?; player -> PlayerInteractEvent
		Skript.addEvent(SimpleEvent.class, EntityPortalEnterEvent.class, "portal enter", "entering( a)? portal");
		Skript.addEvent(SimpleEvent.class, EntityRegainHealthEvent.class, "heal(ing)?");
		Skript.addEvent(SimpleEvent.class, EntityTameEvent.class, "tame");
		Skript.addEvent(SimpleEvent.class, EntityTargetEvent.class, "target");
		Skript.addEvent(SimpleEvent.class, ExplosionPrimeEvent.class, "explosion prime");
		Skript.addEvent(SimpleEvent.class, FurnaceBurnEvent.class, "furnace burn");
		Skript.addEvent(SimpleEvent.class, FurnaceSmeltEvent.class, "furnace smelt");
		Skript.addEvent(SimpleEvent.class, LeavesDecayEvent.class, "leaves decay");
		Skript.addEvent(SimpleEvent.class, LightningStrikeEvent.class, "lightning strike");
		Skript.addEvent(SimpleEvent.class, PigZapEvent.class, "pig ?zap");
		Skript.addEvent(SimpleEvent.class, PlayerBedEnterEvent.class, "bed enter(ing)?", "entering bed");
		Skript.addEvent(SimpleEvent.class, PlayerBedLeaveEvent.class, "bed leave", "leaving bed");
		Skript.addEvent(SimpleEvent.class, PlayerBucketEmptyEvent.class, "bucket empty", "emptying bucket( of %itemtype%)?", "emptying %itemtype% bucket");
		Skript.addEvent(SimpleEvent.class, PlayerBucketFillEvent.class, "bucket fill", "filling bucket( with %itemtype%)?");
		Skript.addEvent(SimpleEvent.class, PlayerChatEvent.class, "chat(ting)?");
		Skript.addEvent(SimpleEvent.class, PlayerFishEvent.class, "fish(ing)?");
		final class EvtLeftclick extends EvtItem {
			@Override
			public boolean check(final Event e) {
				return (((PlayerInteractEvent) e).getAction() == Action.LEFT_CLICK_AIR || ((PlayerInteractEvent) e).getAction() == Action.LEFT_CLICK_BLOCK) && super.check(e);
			}
		}
		Skript.addEvent(EvtLeftclick.class, PlayerInteractEvent.class, "leftclick(ing)?( on %itemtype%)?");
		final class EvtPressurePlate extends SimpleEvent {
			@Override
			public boolean check(final Event e) {
				return ((PlayerInteractEvent) e).getAction() == Action.PHYSICAL;
			}
		}
		Skript.addEvent(EvtPressurePlate.class, PlayerInteractEvent.class, "(pressure )?plate");
		Skript.addEvent(SimpleEvent.class, PlayerItemHeldEvent.class, "item held change");
		Skript.addEvent(SimpleEvent.class, PlayerJoinEvent.class, "join(ing)?");
		Skript.addEvent(SimpleEvent.class, PlayerKickEvent.class, "kick|being kicked");
		Skript.addEvent(SimpleEvent.class, PlayerLoginEvent.class, "login|logging in");
		Skript.addEvent(SimpleEvent.class, PlayerPickupItemEvent.class, "pickup|picking up");
		Skript.addEvent(SimpleEvent.class, PlayerPortalEvent.class, "portal");
		Skript.addEvent(SimpleEvent.class, PlayerPreLoginEvent.class, "prelogin");
		Skript.addEvent(SimpleEvent.class, PlayerQuitEvent.class, "quit(ting)?");
		Skript.addEvent(SimpleEvent.class, PlayerRespawnEvent.class, "respawn(ing)?");
		Skript.addEvent(SimpleEvent.class, PlayerTeleportEvent.class, "teleport(ing)?");
		Skript.addEvent(SimpleEvent.class, PlayerToggleSneakEvent.class, "toggl(e|ing) sneak");
		Skript.addEvent(SimpleEvent.class, PlayerVelocityEvent.class, "player velocity");
		Skript.addEvent(SimpleEvent.class, PortalCreateEvent.class, "portal create");
		Skript.addEvent(SimpleEvent.class, ProjectileHitEvent.class, "projectile hit");
		Skript.addEvent(SimpleEvent.class, BlockRedstoneEvent.class, "redstone");
		Skript.addEvent(SimpleEvent.class, SignChangeEvent.class, "sign change");
		Skript.addEvent(SimpleEvent.class, SpawnChangeEvent.class, "spawn change");
		Skript.addEvent(SimpleEvent.class, VehicleCreateEvent.class, "vehicle create");
		Skript.addEvent(SimpleEvent.class, VehicleDestroyEvent.class, "vehicle destroy");
		Skript.addEvent(SimpleEvent.class, VehicleEnterEvent.class, "vehicle enter");
		Skript.addEvent(SimpleEvent.class, VehicleExitEvent.class, "vehicle exit");
		Skript.addEvent(SimpleEvent.class, WorldInitEvent.class, "world init");
		Skript.addEvent(SimpleEvent.class, WorldLoadEvent.class, "world load(ing)?");
		Skript.addEvent(SimpleEvent.class, WorldSaveEvent.class, "world sav(e|ing)");
		Skript.addEvent(SimpleEvent.class, WorldUnloadEvent.class, "world unload(ing)?");
		Skript.addEvent(SimpleEvent.class, PlayerToggleSprintEvent.class, "sprint toggle", "toggl(e|ing) sprint");
		
	}
	
}
