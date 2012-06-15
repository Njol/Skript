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
import org.bukkit.event.player.PlayerEggThrowEvent;
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
import ch.njol.skript.effects.EffBroadcast;
import ch.njol.skript.effects.EffCancelEvent;
import ch.njol.skript.effects.EffChange;
import ch.njol.skript.effects.EffCommand;
import ch.njol.skript.effects.EffDrop;
import ch.njol.skript.effects.EffEquip;
import ch.njol.skript.effects.EffExit;
import ch.njol.skript.effects.EffExplosion;
import ch.njol.skript.effects.EffFertilize;
import ch.njol.skript.effects.EffHealth;
import ch.njol.skript.effects.EffKill;
import ch.njol.skript.effects.EffMessage;
import ch.njol.skript.effects.EffSpawn;
import ch.njol.skript.effects.EffTeleport;
import ch.njol.skript.effects.EffTree;
import ch.njol.skript.events.EvtBlock;
import ch.njol.skript.events.EvtDamage;
import ch.njol.skript.events.EvtEntity;
import ch.njol.skript.events.EvtEntityBlockChange;
import ch.njol.skript.events.EvtGameMode;
import ch.njol.skript.events.EvtItem;
import ch.njol.skript.events.EvtPeriodical;
import ch.njol.skript.events.EvtRightclick;
import ch.njol.skript.events.EvtWeatherChange;
import ch.njol.skript.expressions.ExprArgument;
import ch.njol.skript.expressions.ExprArmorSlot;
import ch.njol.skript.expressions.ExprAttacked;
import ch.njol.skript.expressions.ExprAttacker;
import ch.njol.skript.expressions.ExprBlock;
import ch.njol.skript.expressions.ExprCreature;
import ch.njol.skript.expressions.ExprDistance;
import ch.njol.skript.expressions.ExprDrops;
import ch.njol.skript.expressions.ExprEntity;
import ch.njol.skript.expressions.ExprEventCancelled;
import ch.njol.skript.expressions.ExprEventExpression;
import ch.njol.skript.expressions.ExprFoodLevel;
import ch.njol.skript.expressions.ExprFurnaceSlot;
import ch.njol.skript.expressions.ExprGameMode;
import ch.njol.skript.expressions.ExprHealth;
import ch.njol.skript.expressions.ExprIdOf;
import ch.njol.skript.expressions.ExprInventory;
import ch.njol.skript.expressions.ExprLocation;
import ch.njol.skript.expressions.ExprLoopValue;
import ch.njol.skript.expressions.ExprPlayer;
import ch.njol.skript.expressions.ExprProjectile;
import ch.njol.skript.expressions.ExprRandom;
import ch.njol.skript.expressions.ExprTarget;
import ch.njol.skript.expressions.ExprTargetedBlock;
import ch.njol.skript.expressions.ExprTime;
import ch.njol.skript.expressions.ExprTimeState;
import ch.njol.skript.expressions.ExprTool;
import ch.njol.skript.expressions.ExprWeather;
import ch.njol.skript.expressions.ExprWorld;
import ch.njol.skript.loops.LoopVarArguments;
import ch.njol.skript.loops.LoopVarBlockLine;
import ch.njol.skript.loops.LoopVarBlockSphere;
import ch.njol.skript.loops.LoopVarIdsOf;
import ch.njol.skript.loops.LoopVarItem;
import ch.njol.skript.loops.LoopVarPlayer;
import ch.njol.skript.loops.LoopVarWorld;

/**
 * @author Peter Güttinger
 * 
 */
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
				
				EffChange.class,
				EffBroadcast.class,
				EffCancelEvent.class,
				EffCommand.class,
				EffDrop.class,
				EffEquip.class,
				// EffExec.class,
				EffExit.class,
				EffExplosion.class,
				EffFertilize.class,
				EffHealth.class,
				EffKill.class,
				EffMessage.class,
				EffSpawn.class,
				EffTeleport.class,
				EffTree.class,
				
				ExprArgument.class,
				ExprArmorSlot.class,
				ExprAttacked.class,
				ExprAttacker.class,
				ExprBlock.class,
				ExprCreature.class,
				ExprDistance.class,
				ExprDrops.class,
				ExprEntity.class,
				ExprEventCancelled.class,
				ExprEventExpression.class,
				ExprFoodLevel.class,
				ExprFurnaceSlot.class,
				ExprGameMode.class,
				ExprHealth.class,
				ExprIdOf.class,
				ExprInventory.class,
				ExprLocation.class,
				ExprLoopValue.class,
				ExprPlayer.class,
				ExprProjectile.class,
				ExprRandom.class,
				ExprTargetedBlock.class, // targeted block
				ExprTarget.class,        // targeted *
				ExprTime.class,
				ExprTimeState.class,
				ExprTool.class,
				ExprWeather.class,
				ExprWorld.class,
				
				LoopVarArguments.class,
				LoopVarBlockLine.class,
				LoopVarBlockSphere.class,
				LoopVarIdsOf.class,
				LoopVarItem.class,
				LoopVarPlayer.class,
				LoopVarWorld.class,
				
				EvtBlock.class,
				EvtDamage.class,
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
		
		Skript.registerEvent(SimpleEvent.class, BlockCanBuildEvent.class, "can build check");
		Skript.registerEvent(SimpleEvent.class, BlockDamageEvent.class, "block damage");
		Skript.registerEvent(SimpleEvent.class, BlockFadeEvent.class, "fad(e|ing)");
		Skript.registerEvent(SimpleEvent.class, BlockFormEvent.class, "form[ing]");
		Skript.registerEvent(SimpleEvent.class, BlockFromToEvent.class, "flow[ing]");
		Skript.registerEvent(SimpleEvent.class, BlockIgniteEvent.class, "(ignite|ignition)");
		Skript.registerEvent(SimpleEvent.class, BlockPhysicsEvent.class, "physics");
		Skript.registerEvent(SimpleEvent.class, BlockPistonExtendEvent.class, "piston extend");
		Skript.registerEvent(SimpleEvent.class, BlockPistonRetractEvent.class, "piston retract");
		Skript.registerEvent(SimpleEvent.class, BlockSpreadEvent.class, "spread(ing)?");
		Skript.registerEvent(SimpleEvent.class, ChunkLoadEvent.class, "chunk load");
		Skript.registerEvent(SimpleEvent.class, ChunkPopulateEvent.class, "chunk populate");
		Skript.registerEvent(SimpleEvent.class, ChunkUnloadEvent.class, "chunk unload");
		Skript.registerEvent(SimpleEvent.class, CreeperPowerEvent.class, "creeper power");
		Skript.registerEvent(SimpleEvent.class, EntityCombustEvent.class, "combust[ing]");
		Skript.registerEvent(SimpleEvent.class, EntityExplodeEvent.class, "(explode|explosion)");
		Skript.registerEvent(SimpleEvent.class, EntityInteractEvent.class, "interact");// = entity interacts with block, e.g. endermen?; player -> PlayerInteractEvent
		Skript.registerEvent(SimpleEvent.class, EntityPortalEnterEvent.class, "portal enter", "entering [a] portal");
		Skript.registerEvent(SimpleEvent.class, EntityRegainHealthEvent.class, "heal[ing]");
		Skript.registerEvent(SimpleEvent.class, EntityTameEvent.class, "tame");
		Skript.registerEvent(SimpleEvent.class, EntityTargetEvent.class, "target");
		Skript.registerEvent(SimpleEvent.class, ExplosionPrimeEvent.class, "explosion prime");
		Skript.registerEvent(SimpleEvent.class, FurnaceBurnEvent.class, "fuel burn", "burn[ing] of fuel");
		Skript.registerEvent(SimpleEvent.class, FurnaceSmeltEvent.class, "[ore] smelt[ing]", "smelt[ing] of ore");//"smelt[ing] of %itemtype%"
		Skript.registerEvent(SimpleEvent.class, LeavesDecayEvent.class, "leaves decay");
		Skript.registerEvent(SimpleEvent.class, LightningStrikeEvent.class, "lightning strike");
		Skript.registerEvent(SimpleEvent.class, PigZapEvent.class, "pig[ ]zap");
		Skript.registerEvent(SimpleEvent.class, PlayerBedEnterEvent.class, "bed enter[ing]", "entering bed");
		Skript.registerEvent(SimpleEvent.class, PlayerBedLeaveEvent.class, "bed leave", "leaving bed");
		Skript.registerEvent(SimpleEvent.class, PlayerBucketEmptyEvent.class, "bucket empty");//, "emptying bucket [of %itemtype%]", "emptying %itemtype% bucket");
		Skript.registerEvent(SimpleEvent.class, PlayerBucketFillEvent.class, "bucket fill");//, "filling bucket [(with|of) %itemtype%]", "filling %itemtype% bucket");
		Skript.registerEvent(SimpleEvent.class, PlayerChatEvent.class, "chat[ting]");
		Skript.registerEvent(SimpleEvent.class, PlayerEggThrowEvent.class, "throw[ing] [of [an] egg]");
		Skript.registerEvent(SimpleEvent.class, PlayerFishEvent.class, "fish[ing]");
		final class EvtLeftclick extends EvtItem {
			@Override
			public boolean check(final Event e) {
				return (((PlayerInteractEvent) e).getAction() == Action.LEFT_CLICK_AIR || ((PlayerInteractEvent) e).getAction() == Action.LEFT_CLICK_BLOCK) && super.check(e);
			}
		}
		Skript.registerEvent(EvtLeftclick.class, PlayerInteractEvent.class, "leftclick[ing] [on %itemtype%]");
		final class EvtPressurePlate extends SimpleEvent {
			@Override
			public boolean check(final Event e) {
				return ((PlayerInteractEvent) e).getAction() == Action.PHYSICAL;
			}
		}
		Skript.registerEvent(EvtPressurePlate.class, PlayerInteractEvent.class, "[pressure] plate");
		Skript.registerEvent(SimpleEvent.class, PlayerItemHeldEvent.class, "item held change");
		Skript.registerEvent(SimpleEvent.class, PlayerJoinEvent.class, "join[ing]");
		Skript.registerEvent(SimpleEvent.class, PlayerKickEvent.class, "kick|being kicked");
		Skript.registerEvent(SimpleEvent.class, PlayerLoginEvent.class, "login|logging in");
		Skript.registerEvent(SimpleEvent.class, PlayerPickupItemEvent.class, "pickup|picking up");
		Skript.registerEvent(SimpleEvent.class, PlayerPortalEvent.class, "portal");
		Skript.registerEvent(SimpleEvent.class, PlayerPreLoginEvent.class, "prelogin");
		Skript.registerEvent(SimpleEvent.class, PlayerQuitEvent.class, "quit[ting]");
		Skript.registerEvent(SimpleEvent.class, PlayerRespawnEvent.class, "respawn[ing]");
		Skript.registerEvent(SimpleEvent.class, PlayerTeleportEvent.class, "teleport[ing]");
		Skript.registerEvent(SimpleEvent.class, PlayerToggleSneakEvent.class, "toggl(e|ing) sneak", "skeak toggle");
		Skript.registerEvent(SimpleEvent.class, PlayerVelocityEvent.class, "player velocity");
		Skript.registerEvent(SimpleEvent.class, PortalCreateEvent.class, "portal create");
		Skript.registerEvent(SimpleEvent.class, ProjectileHitEvent.class, "projectile hit");
		Skript.registerEvent(SimpleEvent.class, BlockRedstoneEvent.class, "redstone");
		Skript.registerEvent(SimpleEvent.class, SignChangeEvent.class, "sign change");
		Skript.registerEvent(SimpleEvent.class, SpawnChangeEvent.class, "spawn change");
		Skript.registerEvent(SimpleEvent.class, VehicleCreateEvent.class, "vehicle create");
		Skript.registerEvent(SimpleEvent.class, VehicleDamageEvent.class, "vehicle damage");
		Skript.registerEvent(SimpleEvent.class, VehicleDestroyEvent.class, "vehicle destroy");
		Skript.registerEvent(SimpleEvent.class, VehicleEnterEvent.class, "vehicle enter");
		Skript.registerEvent(SimpleEvent.class, VehicleExitEvent.class, "vehicle exit");
		Skript.registerEvent(SimpleEvent.class, WorldInitEvent.class, "world init");
		Skript.registerEvent(SimpleEvent.class, WorldLoadEvent.class, "world load[ing]");
		Skript.registerEvent(SimpleEvent.class, WorldSaveEvent.class, "world sav(e|ing)");
		Skript.registerEvent(SimpleEvent.class, WorldUnloadEvent.class, "world unload[ing]");
		Skript.registerEvent(SimpleEvent.class, PlayerToggleSprintEvent.class, "sprint toggle", "toggl(e|ing) sprint");
		
	}
	
}
