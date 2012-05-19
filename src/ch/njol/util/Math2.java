/*
 *  This program is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with this program.  If not, see <http://www.gnu.org/licenses/>.
 * 
 * 
 * Copyright 2011, 2012 Peter Güttinger
 * 
 */

package ch.njol.util;

/**
 * @author Peter Güttinger
 * 
 */
public abstract class Math2 {
	
	public final static int max(final int... nums) {
		Validate.notEmpty(nums, "nums");
		int max = nums[0];
		for (int i = 1; i < nums.length; i++) {
			if (nums[i] > max)
				max = nums[i];
		}
		return max;
	}
	
	/**
	 * finds the smallest positive number in the sequence
	 * 
	 * @param nums
	 * @return smallest positive number of the sequence or -1 if all numbers are negative
	 */
	public final static int minPositive(final int... nums) {
		Validate.notEmpty(nums, "nums");
		int max = -1;
		for (int i = 0; i < nums.length; i++) {
			if (nums[i] > 0 && nums[i] < max)
				max = nums[i];
		}
		return max;
	}
	
}
