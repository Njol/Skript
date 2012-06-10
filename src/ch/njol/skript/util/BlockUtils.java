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

import java.util.Arrays;

import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.material.Attachable;
import org.bukkit.material.Bed;
import org.bukkit.material.Button;
import org.bukkit.material.Door;
import org.bukkit.material.Ladder;
import org.bukkit.material.Lever;
import org.bukkit.material.MaterialData;
import org.bukkit.material.Torch;
import org.bukkit.material.TrapDoor;
import org.bukkit.material.Vine;

/**
 * TODO: maybe use NMS instead of coding everything myself?
 * 
 * @author Peter Güttinger
 * 
 */
public abstract class BlockUtils {
	
	/**
	 * 
	 * @param b
	 * @param mat
	 * @param data The data, can be -1
	 * @param applyPhysics
	 * @return Whether the block could be set successfully
	 */
	public static boolean set(final Block b, final Material mat, final byte data, final boolean applyPhysics) {
		if (!mat.isBlock())
			return false;
		Block other;
		Attachable a = null;
		switch (mat) {
			case BED_BLOCK:
				if (data == -1) {
					for (final BlockFace f : getFaces()) {
						other = b.getRelative(f);
						if (other.getTypeId() == 0) {
							final Bed bed = new Bed(mat);
							bed.setFacingDirection(f);
							b.setTypeIdAndData(mat.getId(), bed.getData(), applyPhysics);
							bed.setHeadOfBed(!bed.isHeadOfBed());
							other.setTypeIdAndData(mat.getId(), bed.getData(), applyPhysics);
							return true;
						}
					}
					return false;
				}
				final Bed bed = new Bed(mat, data);
				other = b.getRelative(bed.isHeadOfBed() ? bed.getFacing().getOppositeFace() : bed.getFacing());
				b.setTypeIdAndData(mat.getId(), data, applyPhysics);
				bed.setHeadOfBed(!bed.isHeadOfBed());
				other.setTypeIdAndData(mat.getId(), bed.getData(), applyPhysics);
			break;
			case WOODEN_DOOR:
			case IRON_DOOR_BLOCK:
				if (data == -1) {
					final Door door = new Door(mat);
					door.setTopHalf(false);
					other = b.getRelative(BlockFace.UP);
					if (other.getTypeId() != 0) {
						other = b.getRelative(BlockFace.DOWN);
						if (other.getTypeId() != 0)
							return false;
						door.setTopHalf(true);
					}
					if (!isSolid(b.getRelative(BlockFace.DOWN, door.isTopHalf() ? 2 : 1).getType()))
						return false;
					b.setTypeIdAndData(mat.getId(), door.getData(), applyPhysics);
					door.setTopHalf(!door.isTopHalf());
					other.setTypeIdAndData(mat.getId(), door.getData(), applyPhysics);
					return true;
				}
				final Door door = new Door(mat, data);
				if (!isSolid(b.getRelative(BlockFace.DOWN, door.isTopHalf() ? 2 : 1).getType()))
					return false;
				other = b.getRelative(door.isTopHalf() ? BlockFace.DOWN : BlockFace.UP);
				b.setTypeIdAndData(mat.getId(), data, applyPhysics);
				door.setTopHalf(!door.isTopHalf());
				other.setTypeIdAndData(mat.getId(), door.getData(), applyPhysics);
			break;
			case TORCH:
			case REDSTONE_LAMP_ON:
			case REDSTONE_LAMP_OFF:
				a = new Torch(mat, data == -1 ? 0 : data);
				//$FALL-THROUGH$
			case LADDER:
				if (a == null)
					a = new Ladder(mat, data == -1 ? 0 : data);
				//$FALL-THROUGH$
			case STONE_BUTTON:
				if (a == null)
					a = new Button(mat, data == -1 ? 0 : data);
				//$FALL-THROUGH$
			case LEVER:
				if (a == null)
					a = new Lever(mat, data == -1 ? 0 : data);
				//$FALL-THROUGH$
			case TRAP_DOOR:
				if (a == null)
					a = new TrapDoor(mat, data == -1 ? 0 : data);
				if (data == -1) {
					for (final BlockFace f : getFaces()) {
						other = b.getRelative(f);
						if (isSolid(other.getType())) {
							a.setFacingDirection(f.getOppositeFace());
							b.setTypeIdAndData(mat.getId(), ((MaterialData) a).getData(), applyPhysics);
							return true;
						}
					}
					return false;
				}
				other = b.getRelative(a.getAttachedFace());
				if (!isSolid(other.getType()))
					return false;
				b.setTypeIdAndData(mat.getId(), data, applyPhysics);
			break;
			case VINE:
				if (data == -1) {
					final Vine vine = new Vine();
					for (final BlockFace f : getFaces()) {
						if (isSolid(b.getRelative(f).getType())) {
							b.setTypeIdAndData(mat.getId(), (byte) 15, applyPhysics);
							return true;
						}
					}
					if (isSolid(b.getRelative(BlockFace.UP).getType())) {
						b.setTypeIdAndData(mat.getId(), (byte) 0, applyPhysics);
						return true;
					}
					if (b.getRelative(BlockFace.UP).getType() == Material.VINE) {
						data &= b.getRelative(BlockFace.UP).getData();
						if (data == 0)
							return false;
						b.setTypeIdAndData(mat.getId(), data, applyPhysics);
					}
					return false;
				}
				final Vine vine = new Vine(data);
			// TODO has multiple faces
			break;
			case WALL_SIGN:
			case SIGN_POST:
				
			case RAILS:
				
			default:
				b.setTypeIdAndData(mat.getId(), data, applyPhysics);
		}
		return true;
	}
	
	public static final boolean isSolid(final Material mat) {
		return true;
	}
	
	public static Iterable<Block> getBlocksAround(final Block b) {
		return Arrays.asList(b.getRelative(BlockFace.NORTH), b.getRelative(BlockFace.EAST), b.getRelative(BlockFace.SOUTH), b.getRelative(BlockFace.WEST));
	}
	
	public static Iterable<BlockFace> getFaces() {
		return Arrays.asList(BlockFace.NORTH, BlockFace.EAST, BlockFace.SOUTH, BlockFace.WEST);
	}
	
}
