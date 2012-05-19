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
public abstract class Validate {
	
	private final static class ValidationException extends RuntimeException {
		
		private static final long serialVersionUID = 8696920645087221191L;
		
		public ValidationException(final String message) {
			super(message);
		}
	}
	
	public static void notNull(final Object... objects) {
		for (int i = 0; i < objects.length; i++) {
			if (objects[i] == null)
				throw new ValidationException("the " + StringUtils.fancyOrderNumber(i + 1) + " parameter must not be null");
		}
	}
	
	public static void notNull(final Object object, final String name) {
		if (object == null)
			throw new ValidationException(name + " must not be null");
	}
	
	public static void isTrue(final boolean b, final String error) {
		if (!b)
			throw new ValidationException(error);
	}
	
	public static void isFalse(final boolean b, final String error) {
		if (b)
			throw new ValidationException(error);
	}
	
	public static void notEmpty(final String s, final String name) {
		if (s != null && s.isEmpty())
			throw new ValidationException(name + " must not be empty");
	}
	
	public static void notNullOrEmpty(final String s, final String name) {
		if (s == null || s.isEmpty())
			throw new ValidationException(name + " must neither be null nor empty");
	}
	
	public static void notEmpty(final Object[] array, final String name) {
		if (array.length == 0)
			throw new ValidationException(name + " must not be empty");
	}
	
	public static void notEmpty(final int[] nums, final String name) {
		if (nums.length == 0)
			throw new ValidationException(name + " must not be empty");
	}
	
}
