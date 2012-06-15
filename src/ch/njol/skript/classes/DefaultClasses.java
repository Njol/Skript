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

package ch.njol.skript.classes;

import ch.njol.skript.Skript;
import ch.njol.skript.api.Parser;
import ch.njol.skript.lang.SimpleLiteral;
import ch.njol.skript.util.Utils;
import ch.njol.util.StringUtils;

/**
 * @author Peter Güttinger
 * 
 */
public class DefaultClasses {
	
	public DefaultClasses() {}
	
	static {
		Skript.registerClass(new ClassInfo<Object>(Object.class, "object"));
		
		Skript.registerClass(new ClassInfo<Float>(Float.class, "float").defaultExpression(new SimpleLiteral<Float>(1f, true)).parser(new Parser<Float>() {
			@Override
			public Float parse(final String s) {
				try {
					if (s.endsWith("%")) {
						return Float.valueOf(Float.parseFloat(s.substring(0, s.length() - 1)) / 100);
					}
					return Float.valueOf(s);
				} catch (final NumberFormatException e) {
					return null;
				}
			}
			
			@Override
			public String toString(final Float f) {
				return StringUtils.toString(f, Skript.NUMBERACCURACY);
			}
		}));
		
		Skript.registerClass(new ClassInfo<Double>(Double.class, "double").user("number", "number").defaultExpression(new SimpleLiteral<Double>(1., true)).parser(new Parser<Double>() {
			@Override
			public Double parse(final String s) {
				try {
					if (s.endsWith("%")) {
						return Double.valueOf(Double.parseDouble(s.substring(0, s.length() - 1)) / 100);
					}
					return Double.valueOf(s);
				} catch (final NumberFormatException e) {
					return null;
				}
			}
			
			@Override
			public String toString(final Double d) {
				return StringUtils.toString(d, Skript.NUMBERACCURACY);
			}
		}));
		
		Skript.registerClass(new ClassInfo<Boolean>(Boolean.class, "boolean").parser(new Parser<Boolean>() {
			@Override
			public Boolean parse(final String s) {
				final byte i = Utils.parseBooleanNoError(s);
				if (i == 1)
					return Boolean.TRUE;
				if (i == 0)
					return Boolean.FALSE;
				return null;
			}
			
			@Override
			public String toString(final Boolean o) {
				return o.toString();
			}
		}));
		
		Skript.registerClass(new ClassInfo<Byte>(Byte.class, "byte").defaultExpression(new SimpleLiteral<Byte>((byte) 1, true)).parser(new Parser<Byte>() {
			@Override
			public Byte parse(final String s) {
				try {
					return Byte.valueOf(s);
				} catch (final NumberFormatException e) {
					return null;
				}
			}
			
			@Override
			public String toString(final Byte o) {
				return o.toString();
			}
		}));
		
		Skript.registerClass(new ClassInfo<Short>(Short.class, "short").defaultExpression(new SimpleLiteral<Short>((short) 1, true)).parser(new Parser<Short>() {
			@Override
			public Short parse(final String s) {
				try {
					return Short.valueOf(s);
				} catch (final NumberFormatException e) {
					return null;
				}
			}
			
			@Override
			public String toString(final Short o) {
				return o.toString();
			}
		}));
		
		Skript.registerClass(new ClassInfo<Integer>(Integer.class, "integer").user("integer", "integers?").defaultExpression(new SimpleLiteral<Integer>(1, true)).parser(new Parser<Integer>() {
			@Override
			public Integer parse(final String s) {
				try {
					return Integer.valueOf(s);
				} catch (final NumberFormatException e) {
					return null;
				}
			}
			
			@Override
			public String toString(final Integer o) {
				return o.toString();
			}
		}));
		
		Skript.registerClass(new ClassInfo<Long>(Long.class, "long").defaultExpression(new SimpleLiteral<Long>((long) 1, true)).parser(new Parser<Long>() {
			@Override
			public Long parse(final String s) {
				try {
					return Long.valueOf(s);
				} catch (final NumberFormatException e) {
					return null;
				}
			}
			
			@Override
			public String toString(final Long o) {
				return o.toString();
			}
		}));
		
		Skript.registerClass(new ClassInfo<String>(String.class, "string").parser(new Parser<String>() {
			@Override
			public String parse(final String s) {
				return null;
			}
			
			@Override
			public String toString(final String s) {
				return s;
			}
			
			@Override
			public String getDebugMessage(final String s) {
				return '"' + s + '"';
			}
		}));
	}
}
