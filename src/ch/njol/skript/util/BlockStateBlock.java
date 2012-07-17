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

package ch.njol.skript.util;

import java.util.Collection;
import java.util.Collections;
import java.util.List;

import org.bukkit.Chunk;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Biome;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.BlockState;
import org.bukkit.block.PistonMoveReaction;
import org.bukkit.inventory.ItemStack;
import org.bukkit.metadata.MetadataValue;
import org.bukkit.plugin.Plugin;

/**
 * 
 * 
 * @author Peter Güttinger
 * 
 */
public class BlockStateBlock implements Block {
	
	private final BlockState state;
	
	public BlockStateBlock(final BlockState state) {
		this.state = state;
	}
	
	@Override
	public void setMetadata(final String metadataKey, final MetadataValue newMetadataValue) {
		state.setMetadata(metadataKey, newMetadataValue);
	}
	
	@Override
	public List<MetadataValue> getMetadata(final String metadataKey) {
		return state.getMetadata(metadataKey);
	}
	
	@Override
	public boolean hasMetadata(final String metadataKey) {
		return state.hasMetadata(metadataKey);
	}
	
	@Override
	public void removeMetadata(final String metadataKey, final Plugin owningPlugin) {
		state.removeMetadata(metadataKey, owningPlugin);
	}
	
	@Override
	public byte getData() {
		return state.getRawData();
	}
	
	@Override
	public Block getRelative(final int modX, final int modY, final int modZ) {
		return state.getBlock().getRelative(modX, modY, modZ);
	}
	
	@Override
	public Block getRelative(final BlockFace face) {
		return state.getBlock().getRelative(face);
	}
	
	@Override
	public Block getRelative(final BlockFace face, final int distance) {
		return state.getBlock().getRelative(face, distance);
	}
	
	@Override
	public Material getType() {
		return state.getType();
	}
	
	@Override
	public int getTypeId() {
		return state.getTypeId();
	}
	
	@Override
	public byte getLightLevel() {
		return state.getLightLevel();
	}
	
	@Override
	public byte getLightFromSky() {
		return state.getBlock().getLightFromSky();
	}
	
	@Override
	public byte getLightFromBlocks() {
		return state.getBlock().getLightFromBlocks();
	}
	
	@Override
	public World getWorld() {
		return state.getWorld();
	}
	
	@Override
	public int getX() {
		return state.getX();
	}
	
	@Override
	public int getY() {
		return state.getY();
	}
	
	@Override
	public int getZ() {
		return state.getZ();
	}
	
	@Override
	public Location getLocation() {
		return state.getLocation();
	}
	
	@Override
	public Chunk getChunk() {
		return state.getChunk();
	}
	
	@Override
	public void setData(final byte data) {
		throw new UnsupportedOperationException();
	}
	
	@Override
	public void setData(final byte data, final boolean applyPhysics) {
		throw new UnsupportedOperationException();
	}
	
	@Override
	public void setType(final Material type) {
		throw new UnsupportedOperationException();
	}
	
	@Override
	public boolean setTypeId(final int type) {
		throw new UnsupportedOperationException();
	}
	
	@Override
	public boolean setTypeId(final int type, final boolean applyPhysics) {
		throw new UnsupportedOperationException();
	}
	
	@Override
	public boolean setTypeIdAndData(final int type, final byte data, final boolean applyPhysics) {
		throw new UnsupportedOperationException();
	}
	
	@Override
	public BlockFace getFace(final Block block) {
		return state.getBlock().getFace(block);
	}
	
	@Override
	public BlockState getState() {
		return state;
	}
	
	@Override
	public Biome getBiome() {
		return state.getBlock().getBiome();
	}
	
	@Override
	public void setBiome(final Biome bio) {
		state.getBlock().setBiome(bio);
	}
	
	@Override
	public boolean isBlockPowered() {
		return state.getBlock().isBlockPowered();
	}
	
	@Override
	public boolean isBlockIndirectlyPowered() {
		return state.getBlock().isBlockIndirectlyPowered();
	}
	
	@Override
	public boolean isBlockFacePowered(final BlockFace face) {
		return state.getBlock().isBlockFacePowered(face);
	}
	
	@Override
	public boolean isBlockFaceIndirectlyPowered(final BlockFace face) {
		return state.getBlock().isBlockFaceIndirectlyPowered(face);
	}
	
	@Override
	public int getBlockPower(final BlockFace face) {
		return state.getBlock().getBlockPower(face);
	}
	
	@Override
	public int getBlockPower() {
		return state.getBlock().getBlockPower();
	}
	
	@Override
	public boolean isEmpty() {
		return getTypeId() == 0;
	}
	
	@Override
	public boolean isLiquid() {
		return getType() == Material.WATER || getType() == Material.STATIONARY_WATER || getType() == Material.LAVA || getType() == Material.STATIONARY_LAVA;
	}
	
	@Override
	public double getTemperature() {
		return state.getBlock().getTemperature();
	}
	
	@Override
	public double getHumidity() {
		return state.getBlock().getHumidity();
	}
	
	@Override
	public PistonMoveReaction getPistonMoveReaction() {
		throw new UnsupportedOperationException();
	}
	
	@Override
	public boolean breakNaturally() {
		throw new UnsupportedOperationException();
	}
	
	@Override
	public boolean breakNaturally(final ItemStack tool) {
		throw new UnsupportedOperationException();
	}
	
	@Override
	public Collection<ItemStack> getDrops() {
		return Collections.emptySet();
	}
	
	@Override
	public Collection<ItemStack> getDrops(final ItemStack tool) {
		return Collections.emptySet();
	}
	
}
